package com.htyleo.extsort.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;

import com.htyleo.extsort.GroupSortedFileReader;

/**
 * IO util
 *
 * @author htyleo
 */
public class IOUtil {

    /**
     * Repeatedly read forward and skip the current line
     * Note that the line separator could be "\n", "\r" or "\r\n"
     *
     * Example: (The parenthesis indicates the position of RandomAccessFile's pointer)
     * <ul>
     *     <li>Before: "ABC(\n)HIJ" after: "ABC\n(H)IJ"</li>
     *     <li>Before: "A(B)C\nHIJ" after: "ABC\n(H)IJ"</li>
     *     <li>Before: "A(B)C\rHIJ" after: "ABC\r(H)IJ"</li>
     *     <li>Before: "A(B)C\r\nHIJ" after: "ABC\r\n(H)IJ"</li>
     *     <li>Before: "A(B)C[EOF]" after: "ABC([EOF])"</li>
     * <ul/>
     *
     * @param raf After this operation, raf's pointer will be on the right of the next line separator or at EOF
     * @return true if we encounter a line separator, false otherwise (i.e. we meet EOF)
     * @throws IOException If an I/O error occurs
     */
    public static boolean skipNextLine(RandomAccessFile raf) throws IOException {
        boolean eol = false;
        boolean eof = false;
        while (!eol && !eof) {
            switch (raf.read()) {
                case -1:
                    eof = true;
                    break;
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long curr = raf.getFilePointer();
                    if ((raf.read()) != '\n') {
                        raf.seek(curr);
                    }
                    break;
            }
        }

        return eol;
    }

    /**
     * If RandomAccessFile's pointer points to a line separator, repeatedly read forward and skip consecutive blank lines.
     * Note that the line separator could be "\n", "\r" or "\r\n"
     *
     * Example: (The parenthesis indicates the position of RandomAccessFile's pointer)
     * <ul>
     *     <li>before: "ABC(\n)HIJ" after: "ABC\n(H)IJ"</li>
     *     <li>before: "ABC(\n)\n\nHIJ" after: "ABC\n\n\n(H)IJ"</li>
     *     <li>before: "AB(C)\nHIJ" after: "AB(C)\nHIJ"</li>
     * <ul/>
     *
     * @param raf If raf does not currently point to a line separator, its pointer is unchanged. Otherwise, after this operation, raf's pointer will be on the right of the last consecutive line separator or at EOF
     * @return true if we encounter blank lines, false otherwise (i.e. stay still, or we reached EOF)
     * @throws IOException If an I/O error occurs
     */
    public static boolean skipNextBlankLines(RandomAccessFile raf) throws IOException {
        boolean eol = false;
        while (true) {
            switch (raf.read()) {
                case '\n':
                case '\r':
                    eol = true;
                    break;
                case -1:
                    return eol;
                default:
                    raf.seek(raf.getFilePointer() - 1);
                    return eol;
            }
        }
    }

    /**
     * Repeatedly read backward and skip the current line
     * Note that the line separator could be "\n", "\r" or "\r\n"
     *
     * Example: (the parenthesis indicates the position of the pointer)
     * <ul>
     *     <li>before: "ABC(\n)HIJ" after: "AB(C)\nHIJ"</li>
     *     <li>before: "ABC\nH(I)J" after: "AB(C)\nHIJ"</li>
     *     <li>before: "ABC\rH(I)J" after: "AB(C)\rHIJ"</li>
     *     <li>before: "ABC\r\nH(I)J" after: "AB(C)\r\nHIJ"</li>
     *     <li>before: "AB(C)\n" after: 0</li>
     * <ul/>
     *
     * @param raf After this operation, raf's pointer will be on the left of the previous line separator or at 0
     * @return true if we meet a line separator, false otherwise (i.e. we reached the beginning of the file)
     * @throws IOException If an I/O error occurs
     */
    public static boolean skipPrevLine(RandomAccessFile raf) throws IOException {
        while (true) {
            switch (raf.read()) {
                case '\n':
                    raf.seek(Math.max(0, raf.getFilePointer() - 2));
                    long curr = raf.getFilePointer();
                    if (raf.read() == '\r') {
                        raf.seek(Math.max(0, raf.getFilePointer() - 2));
                    } else {
                        raf.seek(curr);
                    }
                    return true;
                case '\r':
                    raf.seek(Math.max(0, raf.getFilePointer() - 2));
                    return true;
                default:
                    if (raf.getFilePointer() == 1) {
                        raf.seek(Math.max(0, raf.getFilePointer() - 2));
                        return false;
                    }
                    raf.seek(Math.max(0, raf.getFilePointer() - 2));
            }
        }
    }

    /**
     * If RandomAccessFile's pointer points to a line separator, repeatedly read backward and skip consecutive blank lines.
     * Note that the line separator could be "\n", "\r" or "\r\n"
     *
     * Example: (the parenthesis indicate the position of the pointer)
     * <ul>
     *     <li>before: "ABC(\n)HIJ" after: "AB(C)\nHIJ"</li>
     *     <li>before: "ABC\n\n(\n)HIJ" after: "AB(C)\n\n\nHIJ"</li>
     *     <li>before: "ABC\n(H)IJ" after: "ABC\n(H)IJ"</li>
     * <ul/>
     *
     * @param raf If raf does not currently point to a line separator, its pointer is unchanged. Otherwise, after this operation, raf's pointer will be on the left of the last consecutive line separator or at 0
     * @return true if we meet blank lines, false otherwise (i.e. stay still, or we reached the beginning of the file)
     * @throws IOException If an I/O error occurs
     */
    public static boolean skipPrevBlankLines(RandomAccessFile raf) throws IOException {
        boolean eol = false;
        while (true) {
            switch (raf.read()) {
                case '\n':
                case '\r':
                    eol = true;
                    raf.seek(Math.max(0, raf.getFilePointer() - 2));
                    if (raf.getFilePointer() == 0) {
                        return true;
                    }
                    break;
                case -1:
                    raf.seek(raf.getFilePointer() - 1);
                    break;
                default:
                    raf.seek(raf.getFilePointer() - 1);
                    return eol;
            }
        }
    }

    /**
     * Unconditionally close a <code>Reader</code>.
     * <p>
     * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param reader the Reader to close, may be null or already closed
     */
    public static void closeQuietly(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close a <code>Writer</code>.
     * <p>
     * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param writer  the Writer to close, may be null or already closed
     */
    public static void closeQuietly(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close a <code>RandomAccessFile</code>.
     * <p>
     * Equivalent to {@link RandomAccessFile#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param raf  the RandomAccessFile to close, may be null or already closed
     */
    public static void closeQuietly(RandomAccessFile raf) {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Unconditionally close a <code>GroupSortedFileReader</code>.
     * <p>
     * Equivalent to {@link GroupSortedFileReader#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param groupReader  the GroupSortedFileReader to close, may be null or already closed
     */
    public static void closeQuietly(GroupSortedFileReader groupReader) {
        if (groupReader != null) {
            groupReader.close();
        }
    }

}
