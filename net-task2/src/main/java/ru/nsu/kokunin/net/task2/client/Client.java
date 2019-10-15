package ru.nsu.kokunin.net.task2.client;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        /*IP-address & port OR DNS*/
        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException exc) {
            System.err.println("Invalid address passed in arguments to program :(");
            System.exit(1);
        }

        Socket socket = new Socket();
        try {
            socket.setSoTimeout(20000);
            socket.connect(new InetSocketAddress(serverAddress, Integer.parseInt(args[2])));
        } catch (IOException exc) {
            System.err.println("Couldn't connect to server.");
            System.exit(1);
        }

        FileSender fileSender = null;
        try {
            fileSender = new FileSender(socket, args[0]);
            fileSender.sentFile();
        } catch (FileNotFoundException exc) {
            System.err.println("File in address " + args[0] + " is not found!");
            System.exit(1);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}


    /*public static void main(String[] args) {
        *//*IP-address & port OR DNS*//*
        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException exc) {
            System.err.println("Invalid address passed in arguments to program :(");
            System.exit(1);
        }

        try (Socket socket = new Socket()) {
            socket.setSoTimeout(10000);
            socket.connect(new InetSocketAddress(serverAddress, Integer.parseInt(args[2])));
            System.out.println(serverAddress.getHostAddress());
//            InputStream input = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            String filename = args[0];

            ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
            //помещаем в буфер длину имени файла (в байтах)
            buffer.putShort((short)filename.getBytes(StandardCharsets.UTF_8).length);
            //отправляем
            outputStream.write(buffer.array());
            buffer.clear();

            outputStream.write(filename.getBytes(StandardCharsets.UTF_8));

            sendFile(filename, outputStream);
            outputStream.close();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private static void sendFile(String filename, OutputStream output) throws FileNotFoundException {
        File file = new File(filename);
        FileInputStream input;
        if (file.exists()) {
            input = new FileInputStream(file);
        } else {
            throw new FileNotFoundException(filename);
        }

        byte[] buffer = new byte[1024];
        int count = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(file.length());
//        byteBuffer.clear();
        try {
            output.write(byteBuffer.array());
            while ((count = input.read(buffer)) > -1) {
                output.write(buffer,0, count);
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        try {
            input.close();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }*/