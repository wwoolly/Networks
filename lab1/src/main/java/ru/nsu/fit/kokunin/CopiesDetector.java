package ru.nsu.fit.kokunin;

import java.io.IOException;
import java.net.*;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class CopiesDetector {
    
    private static final int PORT = 5555,
            DELAY = 0, TIMEOUT = 5000, BUF_SIZE = 0;
    
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
    
            Hashtable<InetAddress, Boolean> activeCopies = new Hashtable<>();
//            activeCopies.put(multicastAddress, true);
            setTimer(socket, multicastAddress);
            //multicast adress -- один ip из опр-го диапозона -- передаётся в аргс[0]
    
    
            while (true) {
                //send?
                activeCopies.forEach((address, flag)->{ activeCopies.put(address, false); }); // activeCopies.put(address, false);
                try {
                    while (true) {
                        socket.receive(recvPack);
                        InetAddress recvAddress = recvPack.getAddress();
                        if (!activeCopies.containsKey(recvAddress)) {
                            activeCopies.put(recvAddress, true);
                            System.out.println("New copy join with address: " + recvAddress.getHostAddress());
                            printActiveCopies(activeCopies);
                        } else {
                            activeCopies.put(recvAddress, true);
                        }
                        //parse <...>
                        //если принятый адрес -- новый, то добавить в мапу, печатать список активных пождключений
                        //иначе -- ничего
                    }
                } catch (SocketTimeoutException exc) {
                    //
                }
    
                //чекаем ливнул ли кто...
//                boolean activeCopiesChanged = false;
                for (InetAddress address : activeCopies.keySet()) {
                    if (!activeCopies.get(address)) {
                        activeCopies.remove(address);
                        System.out.println("Copy disconnected with address:" + address.getHostAddress());
//                        activeCopiesChanged = true;
                    }
                }
                
//                if (activeCopiesChanged) {
                    printActiveCopies(activeCopies);
//                }
        
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
    
    private static void printActiveCopies(Hashtable<InetAddress, Boolean> activeCopies) {
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
