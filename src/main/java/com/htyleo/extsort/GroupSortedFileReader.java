package com.htyleo.extsort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.htyleo.extsort.util.IOUtil;

/**
 * A wrapper of BufferedReader for reading a group of sorted files
 *
 * @author htyleo
 */
public class GroupSortedFileReader {

    /** default encoding */
    private static final String        DEFAULT_ENCODING    = "UTF-8";

    /** default buffer size 8 KB */
    private static final int           DEFAULT_BUFFER_SIZE = 8 * 1024;

    /** file encoding used for reading files */
    private String                     encoding;

    /** buffer size used for reading each file  */
    private int                        bufferSize;

    /** file list */
    private List<File>                 files;

    /** readers */
    private BufferedReader[]           readers;

    /** always pop the first line */
    private PriorityQueue<LineWrapper> minHeap;

    /**
     * Create a GroupSortedFileReader, using default encoding and default buffer size
     *
     * @param files input files
     * @param lineComparator line comparator
     */
    public GroupSortedFileReader(List<File> files, final Comparator<String> lineComparator) {
        this(files, lineComparator, DEFAULT_ENCODING, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a GroupSortedFileReader, using default buffer size
     *
     * @param files input files
     * @param lineComparator line comparator
     * @param encoding File encoding used for reading files
     */
    public GroupSortedFileReader(List<File> files, final Comparator<String> lineComparator,
                                 String encoding) {
        this(files, lineComparator, encoding, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a GroupSortedFileReader
     *
     * @param files Input files
     * @param lineComparator Line comparator
     * @param encoding File encoding used for reading files
     * @param bufferSize Buffer size used for reading each file 
     */
    public GroupSortedFileReader(List<File> files, final Comparator<String> lineComparator,
                                 String encoding, int bufferSize) {
        this.files = files;
        this.encoding = encoding;
        this.bufferSize = bufferSize;
        this.readers = new BufferedReader[files.size()];
        this.minHeap = new PriorityQueue<LineWrapper>(files.size(), new Comparator<LineWrapper>() {
            @Override
            public int compare(LineWrapper w1, LineWrapper w2) {
                return lineComparator.compare(w1.line, w2.line);
            }
        });
    }

    /**
     * Read next line
     *
     * @return Next line
     * @throws IOException If an I/O error occurs
     */
    public String readLine() throws IOException {
        if (minHeap.isEmpty()) {
            for (int i = 0; i < readers.length; i++) {
                String line = readLine(i);
                addToHeap(line, i);
            }
        }

        if (minHeap.isEmpty()) {
            return null;
        }

        LineWrapper lineWrapper = minHeap.remove();
        int fileIndex = lineWrapper.fileIndex;
        String nextLine = readLine(fileIndex);
        addToHeap(nextLine, fileIndex);

        return lineWrapper.line;
    }

    /**
     * Let readers[fileIndex] read next line
     *
     * @param fileIndex Index of file
     * @return A line read by readers[fileIndex]
     * @throws IOException If an I/O error occurs
     */
    private String readLine(int fileIndex) throws IOException {
        ensureReaderReady(fileIndex);
        return readers[fileIndex].readLine();
    }

    /**
     * Add a line to heap
     *
     * @param line A line of file
     * @param fileIndex Index of the file that the line belongs to
     */
    private void addToHeap(String line, int fileIndex) {
        if (line == null) {
            return;
        }

        minHeap.add(new LineWrapper(line, fileIndex));
    }

    /**
     * Close reader
     */
    public void close() {
        if (readers != null) {
            for (BufferedReader reader : readers) {
                IOUtil.closeQuietly(reader);
            }
        }
    }

    /**
     * line wrapper, only used by heap
     */
    private static class LineWrapper {
        /** A line of file */
        public final String line;

        /** Index of the file that the line belongs to */
        public final int    fileIndex;

        /**
         * create a LineWrapper
         *
         * @param line A line of file
         * @param fileIndex Index of the file that the line belongs to
         */
        public LineWrapper(String line, int fileIndex) {
            this.line = line;
            this.fileIndex = fileIndex;
        }
    }

    /**
     * Ensure the file stream is open
     *
     * @param fileIndex Index of file
     * @throws IOException If an I/O error occurs
     */
    private void ensureReaderReady(int fileIndex) throws IOException {
        if (readers[fileIndex] == null) {
            readers[fileIndex] = new BufferedReader(new InputStreamReader(new FileInputStream(
                files.get(fileIndex)), encoding), bufferSize);
        }
    }

}
