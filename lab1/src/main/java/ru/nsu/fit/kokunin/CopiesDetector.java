package ru.nsu.fit.kokunin;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class CopiesDetector {
    
    private static final int PORT = /*5555*/12345,
            DELAY = 0, TIMEOUT = 5000, BUF_SIZE = 0;
    
//    private static final long
    
    private static final byte[] buffer = new byte[BUF_SIZE];
    
    private static Timer sendTimer = new Timer(true);
    
    static void detectCopies(InetAddress multicastAddress) {
        try (MulticastSocket /* derived of Datagram socket. Required for receiving from multicast */
                     socket = new MulticastSocket(new InetSocketAddress(PORT))) {
            //есть всякие конструкторы, важно забиндить порт
            socket.joinGroup(multicastAddress);
            socket.setSoTimeout(TIMEOUT);//если не будет ничего принято после 5с -- SocketTimeoutException
            //broadcast -- пересылка сообщения по всей доступной сети
            //multicast -- пересылка сообщения по какой-либо группе адресов
    
            DatagramPacket recvPack = new DatagramPacket(buffer, BUF_SIZE);
    
            Hashtable<InetAddress, Date> activeCopies = new Hashtable<>();
//            activeCopies.put(multicastAddress, true);
            setTimer(socket, multicastAddress);
            //multicast adress -- один ip из опр-го диапозона -- передаётся в аргс[0]
            while (true) {
                boolean isActiveCopiesChanged = false;
                try {
                    socket.receive(recvPack);
                    InetAddress recvAddress = recvPack.getAddress();
                    if (activeCopies.put(recvAddress, new Date()) == null) {
                        System.out.println("\nNew copy join with address: " + recvAddress.getHostAddress());
                        isActiveCopiesChanged = true;
                    }
                } catch (SocketTimeoutException exc) {
                    break;
                }
                
                Iterator<Map.Entry<InetAddress, Date>> iterator = activeCopies.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<InetAddress, Date> element = iterator.next();
                    if (System.currentTimeMillis() - element.getValue().getTime() > TIMEOUT) {
                        System.out.println("\nCopy disconnected with address:" + element.getKey().getHostAddress());
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
    
    private static void printActiveCopies(Hashtable<InetAddress, Date> activeCopies) {
        System.out.println("\n" + Integer.toString(activeCopies.size()) + " copies were detected:");
        for (InetAddress address : activeCopies.keySet()) {
            System.out.println("Copy with address: " + address.getHostAddress());
        }
    }
    
    private static void setTimer(MulticastSocket socket, InetAddress multicastAddress) {
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
