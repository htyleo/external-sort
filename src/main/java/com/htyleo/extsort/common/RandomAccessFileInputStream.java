package com.htyleo.extsort.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * An InputStream wrapper of RandomAccessFile. The stream corresponds to only part of the file. 
 * 
 * @author htyleo
 */
public class RandomAccessFileInputStream extends InputStream {

    /** RandomAccessFile */
    private final RandomAccessFile randomAccessFile;

    /** number of remaining bytes */
    private long                   remaining;

    /**
     * Create a RandomAccessFileInputStream
     * 
     * @param file source file
     * @param begin the beginning position of the file, inclusive
     * @param end the end position of the file, exclusive
     * @throws IOException If an I/O error occurs
     */
    public RandomAccessFileInputStream(File file, long begin, long end) throws IOException {
        this.remaining = end - begin;
        this.randomAccessFile = new RandomAccessFile(file, "r");
        this.randomAccessFile.seek(begin);
    }

    @Override
    public int read() throws IOException {
        if (this.remaining <= 0) {
            return -1;
        }

        this.remaining--;
        return this.randomAccessFile.readByte();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.remaining <= 0) {
            return -1;
        }

        if (len <= 0) {
            return 0;
        }

        if (len > this.remaining) {
            len = (int) this.remaining;
        }
        int ret = this.randomAccessFile.read(b, off, len);

        if (ret > 0) {
            this.remaining -= ret;
        }
        return ret;
    }

    @Override
    public void close() throws IOException {
        this.randomAccessFile.close();
    }

    @Override
    public int available() throws IOException {
        return (int) this.remaining;
    }
}