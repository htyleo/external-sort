package com.htyleo.extsort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import com.htyleo.extsort.common.FileSlice;
import com.htyleo.extsort.common.LineFilter;
import com.htyleo.extsort.common.RandomAccessFileInputStream;
import com.htyleo.extsort.common.SliceType;
import com.htyleo.extsort.util.IOUtil;

/**
 * <p>
 * An implementation of external sorting.
 * The sorting is composed of two phases:
 * <ul>
 *     <li>Partition: partition the input file into a bunch of smaller sorted files.</li>
 *     <li>Merge: merge the sorted files into a single one, which is the output.</li>
 * </ul>
 *
 * The basic idea of this implementation is that we logically partition the file into smaller slices, 
 * each of which is sorted in memory and then written to a separate file.
 * Because sorting slices in memory is performed concurrently (i.e. each slice is sorted in a separate thread),
 * the maximum memory used = max size of thread pool * (slice size + buffer size).
 * </p>
 *
 * <p>
 * This implementation has the following features:
 * <ul>
 *     <li>Every line will be included in some slice in its entirely. In other words, a line will not be sliced in between.</li>
 *     <li>The first and last several lines of the source file are treated as header and tail. They will be stored in two separate files.</li>
 *     <li>There is an option to ignore the leading and trailing blank lines in the source file.</li>
 *     <li>The user-defined line filter and line comparator is supported.</li>
 * </ul>
 * </p>
 *
 * @author htyleo
 */
public class ExternalSort {

    /**
     * Perform external sorting
     * 
     * @param sourceFile input file
     * @param dstDir output directory
     * @param config sorting configuration
     * @return output file
     * @throws Exception If an exception error occurs
     */
    public static File sort(File sourceFile, File dstDir, ExternalSortConfig config)
                                                                                    throws Exception {
        PartitionResult partitionResult = partition(sourceFile, dstDir, config);
        File dstFile = new File(dstDir, sourceFile.getName());
        merge(dstFile, partitionResult, config);

        return dstFile;
    }

    /**
     * External sorting phase One: Partition
     * The input is the source file and the output is a bunch of sorted files.
     * Note that the file header and tail will each be stored in a separate file.
     *
     * @param sourceFile input file
     * @param dstDir output directory
     * @param config sorting configuration
     * @return sorting result
     * @throws Exception If an exception error occurs
     */
    public static PartitionResult partition(final File sourceFile, final File dstDir,
                                            final ExternalSortConfig config) throws Exception {
        List<FileSlice> slices = slice(sourceFile, config.getHeaderLines(),
            config.getIgnoreHeaderBlankLines(), config.getTailLines(),
            config.getIgnoreTailBlankLines(), config.getSliceSize());

        ThreadPoolExecutor executor = config.getExecutor();
        List<Future<File>> futures = new ArrayList<Future<File>>(slices.size());
        for (final FileSlice slice : slices) {
            futures.add(executor.submit(new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return writeSlice(sourceFile, dstDir, config, slice);
                }
            }));
        }

        File header = null;
        List<File> bodies = new ArrayList<File>(slices.size());
        File tail = null;
        for (int i = 0; i < futures.size(); i++) {
            File result = futures.get(i).get();
            switch (slices.get(i).type) {
                case HEADER:
                    header = result;
                    break;
                case BODY:
                    bodies.add(result);
                    break;
                case TAIL:
                    tail = result;
                    break;
            }
        }

        return new PartitionResult(header, bodies, tail);
    }

    /**
     * External sorting phase One: Merge
     *
     * @param dstFile output file
     * @param result partition result
     * @param config sorting configuration
     * @throws IOException If an I/O error occurs
     */
    public static void merge(File dstFile, PartitionResult result, ExternalSortConfig config)
                                                                                             throws IOException {
        String encoding = config.getEncoding();
        int bufferSize = config.getBufferSize();
        Comparator<String> lineComparator = config.getLineComparator();

        BufferedWriter writer = null;
        BufferedReader reader = null;
        GroupSortedFileReader groupReader = null;
        try {
            String line;
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dstFile),
                encoding), bufferSize);

            // write header
            File header = result.getHeader();
            if (header != null && header.isFile()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(header),
                    encoding), bufferSize);
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            // merge bodies
            groupReader = new GroupSortedFileReader(result.getBodies(), lineComparator, encoding,
                bufferSize);
            while ((line = groupReader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            // write tail
            File tail = result.getTail();
            if (tail != null && tail.isFile()) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(tail),
                    encoding), bufferSize);
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

        } finally {
            IOUtil.closeQuietly(writer);
            IOUtil.closeQuietly(reader);
            IOUtil.closeQuietly(groupReader);
        }

    }

    /**
     * Logically partition the file into header, tail and several slices 
     * 
     * @param file file
     * @param headerLines the first headerLines lines are regarded as the header
     * @param ignoreHeaderBlankLines whether we ignore (i.e. do not count them in headerLines) the leading blank lines in the header
     * @param tailLines the last tailLines lines are regarded as the tail
     * @param ignoreTailBlankLines whether we ignore (i.e. do not count them in tailLines) the trailing blank lines in the tail
     * @param sliceSize size of each slice (the actual size may be larger since a line will not be partitioned in between)
     * @return file slices
     * @throws IOException If an I/O error occurs
     */
    private static List<FileSlice> slice(File file, int headerLines,
                                         boolean ignoreHeaderBlankLines, int tailLines,
                                         boolean ignoreTailBlankLines, int sliceSize)
                                                                                     throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");

            List<FileSlice> slices = new ArrayList<FileSlice>();
            FileSlice header = getHeader(raf, headerLines, ignoreHeaderBlankLines);
            slices.add(header);
            FileSlice tail = getTail(raf, tailLines, ignoreTailBlankLines);
            slices.add(tail);

            long bodyBegin = header.end;
            long bodyEnd = tail.begin;
            if (bodyBegin == bodyEnd) {
                slices.add(new FileSlice(SliceType.BODY, bodyBegin, bodyEnd));
                return slices;
            }

            for (raf.seek(bodyBegin); bodyBegin < bodyEnd; bodyBegin = raf.getFilePointer()) {
                raf.skipBytes(sliceSize);
                IOUtil.skipNextLine(raf);
                slices.add(new FileSlice(SliceType.BODY, bodyBegin, Math.min(raf.getFilePointer(),
                    bodyEnd)));
            }

            return slices;

        } finally {
            IOUtil.closeQuietly(raf);
        }
    }

    /**
     * Get the header slice
     *
     * @param raf RandomAccessFile
     * @param headerLines the first headerLines lines are regarded as the header
     * @param ignoreHeaderBlankLines whether we ignore (i.e. do not count them in headerLines) the leading blank lines in the header
     * @return header slice
     * @throws IOException If an I/O error occurs
     */
    private static FileSlice getHeader(RandomAccessFile raf, int headerLines,
                                       boolean ignoreHeaderBlankLines) throws IOException {
        if (ignoreHeaderBlankLines) {
            IOUtil.skipNextBlankLines(raf);
        }

        for (int i = 0; i < headerLines; i++) {
            IOUtil.skipNextLine(raf);
        }

        return new FileSlice(SliceType.HEADER, 0, raf.getFilePointer());
    }

    /**
     * Get the tail slice
     * 
     * @param raf RandomAccessFile
     * @param tailLines the last tailLines lines are regarded as the tail
     * @param ignoreTailBlankLines whether we ignore (i.e. do not count them in tailLines) the trailing blank lines in the tail
     * @return tail slice
     * @throws IOException If an I/O error occurs
     */
    private static FileSlice getTail(RandomAccessFile raf, int tailLines,
                                     boolean ignoreTailBlankLines) throws IOException {
        raf.seek(raf.length());

        if (ignoreTailBlankLines) {
            IOUtil.skipPrevBlankLines(raf);
        }

        boolean meetFileHead = false;
        for (int i = 0; i < tailLines; i++) {
            if (!IOUtil.skipPrevLine(raf)) {
                meetFileHead = true;
                break;
            }
        }

        if (!meetFileHead) {
            IOUtil.skipNextLine(raf);
        }

        return new FileSlice(SliceType.TAIL, Math.max(0, raf.getFilePointer()), raf.length());
    }

    /**
     * Write a slice to file
     * 
     * @param sourceFile original file
     * @param dstDir the directory where the file will be written
     * @param config config
     * @param slice file slice
     * @return the written file
     * @throws IOException If an I/O error occurs
     */
    private static File writeSlice(File sourceFile, File dstDir, ExternalSortConfig config,
                                   FileSlice slice) throws IOException {

        String encoding = config.getEncoding();
        int bufferSize = config.getBufferSize();
        LineFilter lineFilter = config.getLineFilter();
        Comparator<String> comparator = config.getLineComparator();
        SliceType type = slice.type;

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            // read, filter and sort
            reader = new BufferedReader(new InputStreamReader(new RandomAccessFileInputStream(
                sourceFile, slice.begin, slice.end), encoding), bufferSize);

            List<String> lines = new ArrayList<String>();
            for (String line; (line = reader.readLine()) != null;) {
                if (type != SliceType.BODY || lineFilter == null || lineFilter.isConcerned(line)) {
                    lines.add(line);
                }
            }

            if (type == SliceType.BODY && comparator != null) {
                Collections.sort(lines, comparator);
            }

            // write
            dstDir.mkdirs();
            File dstFile = new File(dstDir, String.format("%s-%s-%s.txt", sourceFile.getName(),
                slice.type, slice.begin));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dstFile),
                encoding), bufferSize);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();

            return dstFile;

        } finally {
            IOUtil.closeQuietly(reader);
            IOUtil.closeQuietly(writer);
        }

    }

}