package org.sementsov.gridu;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.sementsov.gridu.sorting.ExternalSorter;
import org.sementsov.gridu.sorting.MyMergeExternalSorter;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.sementsov.gridu.TestHelper.*;
import static org.sementsov.gridu.sorting.ExternalSorter.getDefaultSortedFileName;

public class ExternalSorterTest {

    private static final Logger LOG = Logger.getLogger(ExternalSorterTest.class);
    private static final String DIRECTORY_FOR_TESTING = "testing/sorting/";

    @Test
    public void shouldSortSmallFile() {
        //given
        final String fileLocationForSorting = DIRECTORY_FOR_TESTING + "small_file.txt";
        final File file = new File(fileLocationForSorting);
        final String sortedFileName = file.getParent() + File.separator + "sorted_" + file.getName();
        final long expectedSize = 10L * 1000 * 1000;
        generateFileIfNeeded(fileLocationForSorting, expectedSize);

        //when
        ExternalSorter sorter = new MyMergeExternalSorter(fileLocationForSorting, sortedFileName);
        sorter.sort();

        //then
        assertTrue("File is not sorted", checkOrderInSortedFile(sortedFileName));
    }

    @Test
    public void shouldSortHugeFile() {

        long ts = System.currentTimeMillis();

        //given
        final String fileLocationForSorting = DIRECTORY_FOR_TESTING + "huge_file.txt";
        final String sortedFileName = getDefaultSortedFileName(fileLocationForSorting);
        final long expectedSize = 1L * 1000 * 1000 * 1000;
        long stepTs = System.currentTimeMillis();
        generateFileIfNeeded(fileLocationForSorting, expectedSize, DEFAULT_PRECISION, 0);
        LOG.info("generation of huge file took " + (System.currentTimeMillis() - stepTs) + " milliseconds");

        //when
        ExternalSorter sorter = new MyMergeExternalSorter(fileLocationForSorting, sortedFileName);
        stepTs = System.currentTimeMillis();
        sorter.sort();
        LOG.info("sorting of huge file took " + (System.currentTimeMillis() - stepTs) + " milliseconds");

        //then
        assertTrue("File is not sorted", checkOrderInSortedFile(sortedFileName));

        LOG.info("full duration of huge file sorting took " + (System.currentTimeMillis() - ts) + " milliseconds");

    }
}
