package org.sementsov.gridu.sorting;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.min;
import static org.sementsov.gridu.FileGenerator.createEmptyFile;

public class MyMergeExternalSorter implements ExternalSorter {

    private static final String TMP_DIRECTORY_FOR_SORT = "tmp/";
    private static final Logger LOG = Logger.getLogger(MyMergeExternalSorter.class);

    private String fileLocation;
    private String filePath;
    private String fileName;
    private String sortedFileLocation;
//    final Map<Integer, Integer> chuckToLinesCount = new HashMap<>();

    public MyMergeExternalSorter(String fileLocation) {
        File file = new File(fileLocation);
        this.fileLocation = fileLocation;
        this.filePath = file.getParent();
        this.fileName = file.getName();
        this.sortedFileLocation = file.getParent() + "sorted_" + file.getName();
    }

    public MyMergeExternalSorter(String fileLocation, String sortedFileLocation) {
        this(fileLocation);
        this.sortedFileLocation = sortedFileLocation;
    }

    public void sort() {
        try {
            int filesCount = splitFile();

            mergeFiles(filesCount);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int splitFile() throws IOException {
        File file = new File(this.fileLocation);
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);

        long currentNumberOfBytes = 0;
        int fileIndex = 0;

        final long heapMaxSize = Runtime.getRuntime().maxMemory();
        LOG.info("heapMaxSize = " + heapMaxSize);
        final int defaultBytesPerChunk = (int) (heapMaxSize * 0.2);
        final long bytesPerChunk = min(defaultBytesPerChunk, file.length() / 4);

        LOG.debug("bytesPerChunk = " + bytesPerChunk);

        // StringBuilder length = line length
        StringBuilder sb = new StringBuilder();
        // list size ~ bytesPerChunk
        List<String> list = new ArrayList<>();

        while (true) {
            sb.append(br.readLine());
            if (sb.toString().equals("null")) {
                if (!list.isEmpty()) {
                    saveDataInTempFile(fileIndex++, list);
                }
                break;
            } else {
                currentNumberOfBytes += sb.length();
                list.add(sb.toString());
                sb.delete(0, sb.length());

                if (currentNumberOfBytes > bytesPerChunk) {
                    saveDataInTempFile(fileIndex++, list);

                    currentNumberOfBytes = 0;
                }
            }
        }

        //Closing the main input file
        br.close();
        fileReader.close();

        LOG.debug("number of chunks = " + fileIndex);

        return fileIndex;
    }

    private void saveDataInTempFile(int fileIndex, List<String> list) throws IOException {

        final File file = generateFile(fileIndex);

        LOG.debug("chunk file name = " + file.getPath());

        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        list.sort(String::compareTo);

        for (String str : list) {
            try {
                bw.append(str).append('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bw.flush();
        bw.close();
        list.clear();

        LOG.debug("data was saved in file " + file.getName());
    }

    private void mergeFiles(int numOfFiles) {
        try {
            List<FileReader> listOfFileReader = new ArrayList<>();
            List<BufferedReader> listOfBufferedReader = new ArrayList<>();
            for (int index = 0; index < numOfFiles; index++) {
                String fileName = getChunkFileName(index);
                listOfFileReader.add(new FileReader(fileName));
                listOfBufferedReader.add(new BufferedReader(listOfFileReader
                        .get(index)));
            }

            sortFilesAndWriteOutput(listOfBufferedReader);

            for (int index = 0; index < numOfFiles; index++) {
                final BufferedReader bufferedReader = listOfBufferedReader.get(index);
                final FileReader reader = listOfFileReader.get(index);
                bufferedReader.close();
                reader.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sortFilesAndWriteOutput(List<BufferedReader> listOfBufferedReader) {
        try {
            List<StringWithIndex> listOfLinesFromAllFiles = new LinkedList<>();

            // Read first line from each file
            for (int index = 0; index < listOfBufferedReader.size(); index++) {
                String line = listOfBufferedReader.get(index).readLine();
                if (line != null) {
                    listOfLinesFromAllFiles.add(new StringWithIndex(line, index));
                }
            }

            FileWriter fw = new FileWriter(this.sortedFileLocation);
            BufferedWriter bw = new BufferedWriter(fw);
            while (true) {
                if (listOfLinesFromAllFiles.size() == 0) {
                    break;
                } else {
                    listOfLinesFromAllFiles.sort(StringWithIndex::compareTo);

                    StringWithIndex line = listOfLinesFromAllFiles.get(0);
                    bw.append(line.getLine()).append('\n');
                    int indexForFileName = line.getIndex();

                    // Remove read line
                    listOfLinesFromAllFiles.remove(0);
                    StringWithIndex newSWI = getNewSWI(listOfBufferedReader, indexForFileName);

                    if (newSWI != null) {
                        listOfLinesFromAllFiles.add(newSWI);
                    }
                }
            }
            bw.flush();
            bw.close();
            fw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private StringWithIndex getNewSWI(List<BufferedReader> listOfBufferedReader, int indexForFileName) throws IOException {

        BufferedReader bufferedReader = listOfBufferedReader.get(indexForFileName);
        String line = bufferedReader.readLine();

        if (line != null) {
            return new StringWithIndex(line, indexForFileName);
        } else {
            for (int index = 0; index < listOfBufferedReader.size(); index++) {
                bufferedReader = listOfBufferedReader.get(index);

                line = bufferedReader.readLine();
                if (line != null) {
                    return new StringWithIndex(line, index);
                }
            }
        }

        return null;
    }


    private String getChunkFileName(int index) {
        return filePath + File.separator + TMP_DIRECTORY_FOR_SORT + this.fileName + "_chunk_" + index;
    }

    private File generateFile(int index) {
        File file = createEmptyFile(getChunkFileName(index));

        //file.deleteOnExit();
        return file;
    }

    private class StringWithIndex implements Comparable {
        private final String line;
        private final int index;

        StringWithIndex(String line, int index) {
            this.line = line;
            this.index = index;
        }

        public String getLine() {
            return line;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int compareTo(Object o) {
            StringWithIndex s = (StringWithIndex) o;
            return line.compareTo(s.getLine());
        }
    }
}
