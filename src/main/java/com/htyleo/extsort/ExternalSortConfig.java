package com.htyleo.extsort;

import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.htyleo.extsort.common.LineFilter;

/**
 * External sorting config
 *
 * @author htyleo
 */
public class ExternalSortConfig {

    /** File encoding, used for reading and writing files */
    private String             encoding               = "UTF-8";

    /** Buffer size, default is 8 KB */
    private int                bufferSize             = 8 * 1024;

    /** Slice size, default is 8 MB */
    private int                sliceSize              = 8 * 1024 * 1024;

    /** The first headerLines lines are regarded as the header, which will be extracted and written to a separate file */
    private int                headerLines            = 0;

    /** Whether we ignore (i.e. do not count them in headerLines) the leading blank lines in the file */
    private boolean            ignoreHeaderBlankLines = false;

    /** The last tailLines lines are regarded as the tail, which will be extracted and written to a separate file  */
    private int                tailLines              = 0;

    /** Whether we ignore (i.e. do not count them in tailLines) the trailing blank lines in the file */
    private boolean            ignoreTailBlankLines   = false;

    /** Line filter. By default we do not filter out any line */
    private LineFilter         lineFilter             = new LineFilter() {
                                                          @Override
                                                          public boolean isConcerned(String line) {
                                                              return true;
                                                          }
                                                      };

    /** Che comparator used for sorting. By default, lines are sorted in alphabetical order. */
    private Comparator<String> lineComparator         = new Comparator<String>() {
                                                          @Override
                                                          public int compare(String s1, String s2) {
                                                              return s1.compareTo(s2);
                                                          }
                                                      };

    /**
     * ThreadPoolExecutor used to run external sorting in parallel
     * Default parameters:
     * <ul>
     *     <li>coreSize = 5</li>
     *     <li>maxSize = 8</li>
     *     <li>keepAliveTime = 300s</li>
     * <ul/>
     */
    private ThreadPoolExecutor executor               = new ThreadPoolExecutor(5, 8, 300,
                                                          TimeUnit.SECONDS,
                                                          new LinkedBlockingQueue<Runnable>());

    /**
     * Getter method for property <tt>encoding</tt>.
     *
     * @return property value of encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Setter method for property <tt>encoding</tt>.
     *
     * @param encoding value to be assigned to property encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Getter method for property <tt>bufferSize</tt>.
     *
     * @return property value of bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Setter method for property <tt>bufferSize</tt>.
     *
     * @param bufferSize value to be assigned to property bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Getter method for property <tt>sliceSize</tt>.
     *
     * @return property value of sliceSize
     */
    public int getSliceSize() {
        return sliceSize;
    }

    /**
     * Setter method for property <tt>sliceSize</tt>.
     *
     * @param sliceSize value to be assigned to property sliceSize
     */
    public void setSliceSize(int sliceSize) {
        this.sliceSize = sliceSize;
    }

    /**
     * Getter method for property <tt>headerLines</tt>.
     *
     * @return property value of headerLines
     */
    public int getHeaderLines() {
        return headerLines;
    }

    /**
     * Setter method for property <tt>headerLines</tt>.
     *
     * @param headerLines value to be assigned to property headerLines
     */
    public void setHeaderLines(int headerLines) {
        this.headerLines = headerLines;
    }

    /**
     * Getter method for property <tt>ignoreHeaderBlankLines</tt>.
     *
     * @return property value of ignoreHeaderBlankLines
     */
    public boolean getIgnoreHeaderBlankLines() {
        return ignoreHeaderBlankLines;
    }

    /**
     * Setter method for property <tt>ignoreHeaderBlankLines</tt>.
     *
     * @param ignoreHeaderBlankLines value to be assigned to property ignoreHeaderBlankLines
     */
    public void setIgnoreHeaderBlankLines(boolean ignoreHeaderBlankLines) {
        this.ignoreHeaderBlankLines = ignoreHeaderBlankLines;
    }

    /**
     * Getter method for property <tt>tailLines</tt>.
     *
     * @return property value of tailLines
     */
    public int getTailLines() {
        return tailLines;
    }

    /**
     * Setter method for property <tt>tailLines</tt>.
     *
     * @param tailLines value to be assigned to property tailLines
     */
    public void setTailLines(int tailLines) {
        this.tailLines = tailLines;
    }

    /**
     * Getter method for property <tt>ignoreTailBlankLines</tt>.
     *
     * @return property value of ignoreTailBlankLines
     */
    public boolean getIgnoreTailBlankLines() {
        return ignoreTailBlankLines;
    }

    /**
     * Setter method for property <tt>ignoreTailBlankLines</tt>.
     *
     * @param ignoreTailBlankLines value to be assigned to property ignoreTailBlankLines
     */
    public void setIgnoreTailBlankLines(boolean ignoreTailBlankLines) {
        this.ignoreTailBlankLines = ignoreTailBlankLines;
    }

    /**
     * Getter method for property <tt>lineFilter</tt>.
     *
     * @return property value of lineFilter
     */
    public LineFilter getLineFilter() {
        return lineFilter;
    }

    /**
     * Setter method for property <tt>lineFilter</tt>.
     *
     * @param lineFilter value to be assigned to property lineFilter
     */
    public void setLineFilter(LineFilter lineFilter) {
        this.lineFilter = lineFilter;
    }

    /**
     * Getter method for property <tt>lineComparator</tt>.
     *
     * @return property value of lineComparator
     */
    public Comparator<String> getLineComparator() {
        return lineComparator;
    }

    /**
     * Setter method for property <tt>lineComparator</tt>.
     *
     * @param lineComparator value to be assigned to property lineComparator
     */
    public void setLineComparator(Comparator<String> lineComparator) {
        this.lineComparator = lineComparator;
    }

    /**
     * Getter method for property <tt>executor</tt>.
     *
     * @return property value of executor
     */
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    /**
     * Setter method for property <tt>executor</tt>.
     *
     * @param executor value to be assigned to property executor
     */
    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }
}
