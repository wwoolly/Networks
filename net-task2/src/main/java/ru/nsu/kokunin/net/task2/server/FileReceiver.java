package ru.nsu.kokunin.net.task2.server;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileReceiver implements Runnable {
    private final static int TIME_MEASURING_INTERVAL = 3000; //milliseconds
    private final static int BUFFER_SIZE = 512;//0.5 KB for bytes array

    private Socket socket;
    private TransmittedByteCounter transmittedByteCounter;
    private String reseivedFilename;
    
    FileReceiver(Socket socket) {
        this.socket = socket;
        this.transmittedByteCounter = new TransmittedByteCounter();
        reseivedFilename = null;
    }

    private ByteBuffer readDigitFromBytebuffer(InputStream input, int bytesNumber) throws IOException {
        byte[] buffer = new byte[bytesNumber];
        //TODO fix - надо читать 2 байта в цикле (while читается)//а надо ли?
        if (input.read(buffer) < 1) {
            throw new IOException();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesNumber);
        byteBuffer.put(buffer);
        byteBuffer.clear();
        return byteBuffer;
    }

    private short readInputFilenameLength(InputStream input) throws IOException {
        ByteBuffer byteBuffer = readDigitFromBytebuffer(input, Short.BYTES);
        return byteBuffer.getShort();
    }

    private long readInputFileLength(InputStream input) throws IOException {
        ByteBuffer byteBuffer = readDigitFromBytebuffer(input, Long.BYTES);
        return byteBuffer.getLong();
    }

    private String readInputFilename(InputStream input, short filenameLength) throws IOException {
        byte[] buffer = new byte[filenameLength];
        //TODO fix - надо читать 2 байта в цикле (while читается)//а надо ли?
        if (input.read(buffer, 0, filenameLength) < 1) {
            throw new IOException();
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private FileOutputStream createFileOutputStream(String filename) throws IOException {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd_kk.mm.ss.SSS]_");
        File file = new File(filename);
        Path path = Paths.get("uploads" + File.separator + format.format(currentDate) + file.getName());
        for(int i = 1; Files.exists(path) || i >= Integer.MAX_VALUE; ++i) {
            System.out.println("ebuchyy circle iteration: " + i);
            path = Paths.get("uploads" + File.separator + format.format(currentDate) + i + file.getName());
        }

        try {
            FileOutputStream output = new FileOutputStream(path.toFile());
            reseivedFilename = path.getFileName().getFileName().toString();
            return output;
        } catch (FileNotFoundException exc) {
            System.err.println("Couldn't create a file" + path.toString());
            exc.printStackTrace();
            throw new IOException();
        }
    }

    private long receiveFile(InputStream input, FileOutputStream output) throws IOException {
        int count = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        TimeStatGenerator statisticGenerator = new TimeStatGenerator(transmittedByteCounter, TIME_MEASURING_INTERVAL);
        statisticGenerator.start();
        while ((count = input.read(buffer)) > -1) {
            transmittedByteCounter.registerBytes(count);
            output.write(buffer, 0, count);
        }
        statisticGenerator.stop();

        output.flush();
        return output.getChannel().size();
    }

    private void printFileInfo(String filename, long fileSize) {
        System.out.printf("Received a request to download file from host: %s.\n", socket.getInetAddress());
        System.out.printf("File \"%s\" will saved as \"%s\".\n", filename, reseivedFilename);
        String tailMessage = (fileSize < 1024) ? "%d bytes.\n" : "%.2f KB.\n";
        System.out.printf("File size is " + tailMessage, (fileSize < 1024 ? fileSize : (double)fileSize /1024));
        /*System.out.println("Received a request to download file from host:\n"
                    + socket.getInetAddress()
                    + ".\nFile \"" + filename + "\" with size "
                    + ((fileSize < 1024) ? (fileSize + " bytes.") : ((double)fileSize / 1024 + " KB.")));*/
        System.out.println("Start receiving...");
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();) {
            short filenameLength = readInputFilenameLength(input);
            String filename = readInputFilename(input, filenameLength);
            long sendFileSize = readInputFileLength(input);
            long receivedFileSize = 0;


            try (FileOutputStream output = createFileOutputStream(filename)) {
                printFileInfo(filename, sendFileSize);
                receivedFileSize = receiveFile(input, output);
                System.out.println("Done.");

                if (sendFileSize != receivedFileSize || receivedFileSize != transmittedByteCounter.getTotalBytesNumber()) {
                    System.err.println("FileLengths of send and received files don't match!");
                    throw new Exception();
                }

            } catch (IOException exc) {
                System.err.println("Error while file receiving :(");
                exc.getStackTrace();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
