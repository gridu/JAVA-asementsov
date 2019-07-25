package org.sementsov.gridu;

import org.sementsov.gridu.sorting.ExternalSorter;
import org.sementsov.gridu.sorting.MyMergeExternalSorter;

public class ExternalSortingRunner {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("You must provide a commandline argument specifying the file to sort");
            System.exit(-1);
        }
        run(args[0]);
    }

    public static void run(String fileLocation, String sortedFileLocation) {
        ExternalSorter sorter = new MyMergeExternalSorter(fileLocation, sortedFileLocation);

        sorter.sort();
    }

    public static void run(String fileLocation) {
        run(fileLocation, ExternalSorter.getDefaultSortedFileName(fileLocation));

    }
}
