package com.htyleo.extsort;

import java.io.File;
import java.util.List;

/**
 * Result of the partition phase of external sorting
 * 
 * @author htyleo
 */
public class PartitionResult {

    /** header file */
    private File       header;

    /** body files */
    private List<File> bodies;

    /** tail file */
    private File       tail;

    /**
     * Create an PartitionResult
     *
     * @param header file that contains the header of the original file
     * @param bodies files that contain the body of the original file
     * @param tail file that contains the tail of the original file
     */
    public PartitionResult(File header, List<File> bodies, File tail) {
        this.header = header;
        this.bodies = bodies;
        this.tail = tail;
    }

    /**
     * Getter method for property <tt>header</tt>.
     *
     * @return property value of header
     */
    public File getHeader() {
        return header;
    }

    /**
     * Getter method for property <tt>bodies</tt>.
     *
     * @return property value of bodies
     */
    public List<File> getBodies() {
        return bodies;
    }

    /**
     * Getter method for property <tt>tail</tt>.
     *
     * @return property value of tail
     */
    public File getTail() {
        return tail;
    }

}
