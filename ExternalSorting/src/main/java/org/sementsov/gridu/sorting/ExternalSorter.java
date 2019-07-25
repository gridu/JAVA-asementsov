package org.sementsov.gridu.sorting;

import java.io.File;

public interface ExternalSorter {

    void sort();

    static String getDefaultSortedFileName(String fileLocation) {
        File file = new File(fileLocation);

        return file.getParent() + File.separator + "sorted_" + file.getName();
    }
}
