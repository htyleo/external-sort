package com.htyleo.extsort.common;

/**
 * line filter
 *
 * @author htyleo
 */
public interface LineFilter {

    /**
     * The logic to filter lines
     *
     * @param line a line in a file
     * @return true if this line should not be filtered out (i.e. included in the output files), false otherwise
     */
    boolean isConcerned(String line);

}
