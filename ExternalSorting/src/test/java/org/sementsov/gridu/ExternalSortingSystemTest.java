package org.sementsov.gridu;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.sementsov.gridu.TestHelper.*;
import static org.sementsov.gridu.sorting.ExternalSorter.getDefaultSortedFileName;

public class ExternalSortingSystemTest {

    private static final String DIRECTORY_FOR_TESTING = "testing/system/";

    @Test
    public void shouldGenerateAndSortSmallFile() {
        //given
        final String fileLocation = DIRECTORY_FOR_TESTING + "system_test_small_file.txt";
        final String sortedFileName = getDefaultSortedFileName(fileLocation);
        final long sizeInBytes = 10L * 1000 * 1000;

        //when
        FileGenerator.generate(fileLocation, sizeInBytes, DEFAULT_PRECISION, 0);
        ExternalSortingRunner.run(fileLocation, sortedFileName);

        //then
        assertTrue("Sorted file is not actually sorted!", checkOrderInSortedFile(sortedFileName));
    }

    @Test
    public void shouldGenerateAndSortHugeFile() {
        //given
        final String fileLocation = DIRECTORY_FOR_TESTING + "system_test_huge_file.txt";
        final String sortedFileName = getDefaultSortedFileName(fileLocation);
        final long sizeInBytes = 1L * 1000 * 1000 * 1000;

        //when
        FileGenerator.generate(fileLocation, sizeInBytes, DEFAULT_PRECISION, 0);
        ExternalSortingRunner.run(fileLocation, sortedFileName);

        //then
        assertTrue("Sorted file is not actually sorted!", checkOrderInSortedFile(sortedFileName));
    }

    @BeforeClass
    public static void clearTestingFolder() {
        purgeDirectory(DIRECTORY_FOR_TESTING, true, true);
    }

}