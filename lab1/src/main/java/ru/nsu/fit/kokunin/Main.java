package ru.nsu.fit.kokunin;


import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        try {
            InetAddress address = InetAddress.getByName(args[0]);//IOException
            CopiesDetector.detectCopies(address);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}
