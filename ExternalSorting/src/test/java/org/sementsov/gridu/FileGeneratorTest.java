package org.sementsov.gridu;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.sementsov.gridu.TestHelper.*;

public class FileGeneratorTest {

    private static final String DIRECTORY_FOR_TESTING = "testing/generating/";

    @Test
    public void shouldCreateFile() {
        //given
        final String fileLocation = DIRECTORY_FOR_TESTING + "just_a_file.txt";

        //when
        FileGenerator.generate(fileLocation, 0L);

        //then
        File file = new File(fileLocation);
        assertTrue("File doesn't exist", file.exists());
    }

    @Test
    public void shouldCreateFileWithSpecifiedSize() {
        //given
        final String fileLocation = DIRECTORY_FOR_TESTING + "file_with_specified_size.txt";
        final long expectedSize = 10L * 1000 * 1000;

        //when
        FileGenerator.generate(fileLocation, expectedSize);

        //then
        File file = new File(fileLocation);
        final long length = file.length();
        assertTrue("Actual size = " + length + " bytes of generated file " +
                        "is not fit to expected size = " + expectedSize + " bytes",
                checkSizeOfFile(length, expectedSize, DEFAULT_PRECISION));
    }

    @Test
    public void shouldCreateHugeFileWithSpecifiedSize() {
        //given
        final String fileLocation = DIRECTORY_FOR_TESTING + "huge_file_with_specified_size.txt";
        final long expectedSize = 256L * 1000 * 1000;

        //when
        FileGenerator.generate(fileLocation, expectedSize);

        //then
        File file = new File(fileLocation);
        final long length = file.length();
        assertTrue("Actual size = " + length + " bytes of generated file " +
                        "is not fit to expected size = " + expectedSize + " bytes",
                checkSizeOfFile(length, expectedSize, DEFAULT_PRECISION));
    }

//    @BeforeClass
//    public static void clearTestingFolder() {
//        purgeDirectory(DIRECTORY_FOR_TESTING, true, false);
//    }
}
