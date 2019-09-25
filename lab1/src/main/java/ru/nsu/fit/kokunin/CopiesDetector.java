package ru.nsu.fit.kokunin;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;


public class CopiesDetector {
    
    private static final int PORT = 8888,
            DELAY = 0, TIMEOUT = 5000, BUF_SIZE = 0;
    
    
    private static final byte[] buffer = new byte[BUF_SIZE];
    
    private static Timer sendTimer = new Timer(true);
    
    static void detectCopies(InetAddress multicastAddress) {
        try (MulticastSocket recvSocket = new MulticastSocket(new InetSocketAddress(PORT));
             DatagramSocket sendSocket = new DatagramSocket()) {
            
            recvSocket.joinGroup(multicastAddress);
            recvSocket.setSoTimeout(TIMEOUT);
    
            DatagramPacket recvPack = new DatagramPacket(buffer, BUF_SIZE);
    
            Hashtable<SocketAddress, Long> activeCopies = new Hashtable<>();
            setTimer(sendSocket, multicastAddress);
            while (true) {
                boolean isActiveCopiesChanged = false;
                try {
                    recvSocket.receive(recvPack);
                    SocketAddress recvAddress = recvPack.getSocketAddress();
                    if (activeCopies.put(recvAddress, System.currentTimeMillis()) == null) {
                        System.out.println("\nNew copy join with address: " + recvAddress);
                        
                        isActiveCopiesChanged = true;
                    }
                } catch (SocketTimeoutException exc) {
                    break;
                }
                
                Iterator<Entry<SocketAddress, Long>> iterator = activeCopies.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<SocketAddress, Long> element = iterator.next();
                    if (System.currentTimeMillis() - element.getValue() > TIMEOUT) {
                        System.out.println("\nCopy disconnected with address:" + element.getKey());
                        iterator.remove();
                        isActiveCopiesChanged = true;
                    }
                }
                
                if (isActiveCopiesChanged) {
                    printActiveCopies(activeCopies);
                }
                
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
    
    private static void printActiveCopies(Hashtable<SocketAddress, Long> activeCopies) {
        System.out.println("\n" + Integer.toString(activeCopies.size()) + " copies were detected:");
        for (SocketAddress address : activeCopies.keySet()) {
            System.out.println("Copy with address: " + address);
        }
    }
    
    private static void setTimer(DatagramSocket socket, InetAddress multicastAddress) {
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    socket.send(new DatagramPacket(buffer, BUF_SIZE, multicastAddress, PORT));
                } catch(IOException exc) {
                    exc.printStackTrace();
                }
            }
        }, DELAY, TIMEOUT);
    }
}
