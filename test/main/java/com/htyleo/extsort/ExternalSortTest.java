package com.htyleo.extsort;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ExternalSort test
 *
 * @author htyleo
 */
public class ExternalSortTest {

    private File sourceFile = new File("./test.txt");

    private File dstDir     = new File("./dst");

    @Before
    public void before() {
        FileUtils.deleteQuietly(sourceFile);
        FileUtils.deleteQuietly(dstDir);
    }

    @After
    public void after() {
        FileUtils.deleteQuietly(sourceFile);
        FileUtils.deleteQuietly(dstDir);
    }

    @Test
    public void testExternalSort() {
        Random rand = new Random();

        int headerSize = 10;
        int bodySize = 1000;
        int tailSize = 5;
        List<String> headerLines = new ArrayList<String>(headerSize);
        List<String> bodyLines = new ArrayList<String>(bodySize);
        List<String> tailLines = new ArrayList<String>(tailSize);
        try {
            for (int i = 0; i < headerSize; i++) {
                headerLines.add(String.valueOf(rand.nextInt(100000)));
            }
            for (int i = 0; i < bodySize; i++) {
                bodyLines.add(String.valueOf(rand.nextInt(100000)));
            }
            for (int i = 0; i < tailSize; i++) {
                tailLines.add(String.valueOf(rand.nextInt(100000)));
            }

            List<String> lines = new ArrayList<String>(headerSize + bodySize + tailSize);
            lines.addAll(headerLines);
            lines.addAll(bodyLines);
            lines.addAll(tailLines);
            FileUtils.writeLines(sourceFile, "UTF-8", lines);

            ExternalSortConfig config = new ExternalSortConfig();
            config.setHeaderLines(10);
            config.setTailLines(5);
            config.setSliceSize(512);
            ExternalSortResult result = ExternalSort.sort(sourceFile, dstDir, config);

            File headerFile = result.getHeader();
            List<String> headerLines2 = FileUtils.readLines(headerFile);
            Assert.assertEquals(headerLines, headerLines2);

            File tailFile = result.getTail();
            List<String> tailLines2 = FileUtils.readLines(tailFile);
            Assert.assertEquals(tailLines, tailLines2);

            List<File> bodyFiles = result.getBodies();
            GroupSortedFileReader groupReader = new GroupSortedFileReader(bodyFiles,
                config.getLineComparator(), "UTF-8");
            List<String> bodyLines2 = new ArrayList<String>();
            for (String line; (line = groupReader.readLine()) != null;) {
                bodyLines2.add(line);
            }
            Collections.sort(bodyLines, config.getLineComparator());
            Assert.assertEquals(bodyLines, bodyLines2);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }
}
