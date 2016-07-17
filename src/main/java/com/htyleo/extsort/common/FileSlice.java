package com.htyleo.extsort.common;

/**
 * A logical file slice
 *
 * @author htyleo
 */
public class FileSlice {

    /** slice type */
    public final SliceType type;

    /** the beginning position, inclusive */
    public final long      begin;

    /** the end position, exclusive */
    public final long      end;

    /**
     * Create a FileSlice, which is the the logical part of the original file from begin (inclusive) to end (exclusive)
     *
     * @param type slice type
     * @param begin the beginning position
     * @param end the end position
     */
    public FileSlice(SliceType type, long begin, long end) {
        this.type = type;
        this.begin = begin;
        this.end = end;
    }

}
