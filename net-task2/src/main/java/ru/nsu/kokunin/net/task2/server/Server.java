package ru.nsu.kokunin.net.task2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Too few arguments!");
            System.exit(1);
        }

        Path uploadsDir = Paths.get("uploads");
        if (!Files.isDirectory(uploadsDir)) {
            try {
                System.out.println("Creating \"uploads\" directory...");
                Files.createDirectory(uploadsDir);
                System.out.println("Done.");
            } catch (IOException exc) {
                exc.printStackTrace();
                System.exit(1);
            }
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while(true) {
                try {
                    Socket socket = serverSocket.accept();//читать
                    Runnable fileReceiver = new FileReceiver(socket);
                    new Thread(fileReceiver).start();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        

    }
}
