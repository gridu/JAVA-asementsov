package org.sementsov.gridu;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.Math.abs;

class TestHelper {

    private static final Logger LOG = Logger.getLogger(TestHelper.class);

    static final double DEFAULT_PRECISION = FileGenerator.DEFAULT_PRECISION;

    static boolean checkOrderInSortedFile(String fileLocation) {

        int linesCount = 0;
        try (FileReader reader = new FileReader(fileLocation);
             BufferedReader br = new BufferedReader(reader)) {

            String prevLine = null;
            String line;
            while ((line = br.readLine()) != null) {
                ++linesCount;
                if (prevLine != null) {
                    if (line.compareTo(prevLine) < 0) {
                        return false;
                    }
                }

                prevLine = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        LOG.info("sorted file has " + linesCount + " lines");

        return true;
    }

    static boolean checkSizeOfFile(long expectedSize, long actualSize) {
        return checkSizeOfFile(expectedSize, actualSize, DEFAULT_PRECISION);
    }
    
    static boolean checkSizeOfFile(long expectedSize, long actualSize, double precision) {
        return abs(expectedSize - actualSize) / 1.0 / expectedSize <= precision;
    }

    static boolean purgeDirectory(String directoryLocation, boolean recursively, boolean deleteRootDirectory) {
        File directory = new File(directoryLocation);

        if (!directory.exists()) return true;

        return purgeDirectory(directory, recursively, deleteRootDirectory);
    }

    private static boolean purgeDirectory(File directory, boolean recursively, boolean deleteRootDirectory) {

        if (!directory.isDirectory()) throw new IllegalArgumentException("Specified path is not a directory");

        File[] files = directory.listFiles();

        boolean result = true;

        for (File file : files) {
            if (file.isDirectory()) {
                if (recursively) {
                    result &= purgeDirectory(file, true, true);
                } else
                    System.out.println("Directory '" + file.getAbsolutePath() + "' is not going to be deleted");
            } else {
                if (!file.delete()) {
                    System.out.println("Can't delete file " + file.getAbsolutePath());
                    result = false;
                }
            }
        }

        return result & (deleteRootDirectory ? directory.delete() : true);
    }

    static void deleteFileBeforeTest() {

    }

    static void generateFileIfNeeded(String fileLocation, long size) {
        generateFileIfNeeded(fileLocation, size, DEFAULT_PRECISION, 0);
    }

    static void generateFileIfNeeded(String fileLocation, long size, double defaultPrecision, int pace) {
        File file = new File(fileLocation);
        if (!file.exists() || !checkSizeOfFile(size, file.length())) {
            FileGenerator.generate(fileLocation, size, defaultPrecision, pace);
        }
    }
}
