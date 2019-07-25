package org.sementsov.gridu;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class FileGenerator {

    static final double DEFAULT_PRECISION = 0.01;

    private static final Logger LOG = Logger.getLogger(FileGenerator.class);

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("You must provide a commandline argument specifying the file to generate " +
                    "and size of the file to generate");
            System.exit(-1);
        }
        String fileLocation = args[0];
        Long sizeInBytes = Long.getLong(args[1]);
        generate(fileLocation, sizeInBytes);
    }

    public static void generate(String fileLocation, long expectedSizeInBytes) {
        generate(fileLocation, expectedSizeInBytes, DEFAULT_PRECISION, 0);
    }

    public static void generate(String fileLocation, long expectedSizeInBytes, double precision, int pace) {

        LOG.debug("expectedSizeInBytes = " + expectedSizeInBytes);
        LOG.debug("precision = " + precision);

        if (expectedSizeInBytes < 0) {
            throw new IllegalArgumentException("Size of file cannot be less than zero bytes");
        }

        long heapMaxSize = Runtime.getRuntime().maxMemory();
        final int defaultBufferSize = (int) (heapMaxSize * 0.01);

        if (pace == 0) {
            pace = (int) max(
                    min(
                            defaultBufferSize,
                            expectedSizeInBytes * precision / 2

                    ),
                    1);
        }

        LOG.debug("pace = " + pace);

        long sizeToFlush = 1L * 1000 * 1000;
        int flushFrequency = (int) (sizeToFlush / pace);

        File file = createEmptyFile(fileLocation);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            if (expectedSizeInBytes > 0) {
                StringBuffer sb = new StringBuffer(pace);

                long lengthInBytes = 0;
                long restLength = expectedSizeInBytes;

                for (; restLength > 0; restLength -= pace) {

//                    LOG.debug("lengthInBytes = " + lengthInBytes);

                    sb.append(RandomStringUtils.randomAlphanumeric(pace - 1)).append('\n');

                    if (restLength < pace) {
                        sb = new StringBuffer(sb.toString().substring(
                                0, ((int) restLength)));
                    }

                    writer.write(sb.toString());

                    if (lengthInBytes % pace == flushFrequency) {
                        writer.flush();
                    }
                    sb.delete(0, sb.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createEmptyFile(String fileLocation) {
        // create directory
        File file = new File(fileLocation).getParentFile();
        if (!file.exists())
            if (!file.mkdirs())
                LOG.warn("Can't create directory path");

        // create file
        file = new File(fileLocation);
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    LOG.warn("Can't delete file");
                } else {
                    if (!file.createNewFile()) {
                        LOG.warn("Can't create new file");
                    }
                }
            } else {
                if (!file.createNewFile()) {
                    LOG.warn("Can't create new file");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warn("Can't create new file");
        }
        return file;
    }
}
