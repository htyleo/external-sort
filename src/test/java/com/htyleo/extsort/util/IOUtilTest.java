package com.htyleo.extsort.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IOUtil test
 *
 * @author htyleo
 */
public class IOUtilTest {

    private File file = new File("./test.txt");

    @Before
    public void before() {
        FileUtils.deleteQuietly(file);
    }

    @After
    public void after() {
        FileUtils.deleteQuietly(file);
    }

    @Test
    public void testSkipNextLine() {
        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipNextLine(raf);
            Assert.assertEquals(4, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(1);
            IOUtil.skipNextLine(raf);
            Assert.assertEquals(4, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\rHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(1);
            IOUtil.skipNextLine(raf);
            Assert.assertEquals(4, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(1);
            IOUtil.skipNextLine(raf);
            Assert.assertEquals(5, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(4);
            IOUtil.skipNextLine(raf);
            Assert.assertEquals(raf.length(), raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSkipNextBlankLines() {
        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipNextBlankLines(raf);
            Assert.assertEquals(4, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipNextBlankLines(raf);
            Assert.assertEquals(6, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\n");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipNextBlankLines(raf);
            Assert.assertEquals(6, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(1);
            IOUtil.skipNextBlankLines(raf);
            Assert.assertEquals(1, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSkipPrevLine() {
        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipPrevLine(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(5);
            IOUtil.skipPrevLine(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\rHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(5);
            IOUtil.skipPrevLine(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(5);
            IOUtil.skipPrevLine(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(2);
            IOUtil.skipPrevLine(raf);
            Assert.assertEquals(0, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSkipPrevBlankLines() {
        try {
            FileUtils.write(file, "ABC\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(5);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\n");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(6);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(2, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "ABC\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(6);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(6, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(2);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(0, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            FileUtils.write(file, "\n\r\nHIJ");
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(3);
            IOUtil.skipPrevBlankLines(raf);
            Assert.assertEquals(3, raf.getFilePointer());

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
