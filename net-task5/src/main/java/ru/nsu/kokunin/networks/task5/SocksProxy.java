package ru.nsu.kokunin.networks.task5;

import ru.nsu.kokunin.networks.task5.dns.DnsService;
import ru.nsu.kokunin.networks.task5.handlers.AcceptHandler;
import ru.nsu.kokunin.networks.task5.utils.ArgsResolver;
import ru.nsu.kokunin.networks.task5.utils.Connection;
import ru.nsu.kokunin.networks.task5.handlers.Handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SocksProxy {
    private final static int PORT_INDEX = 0;
    private final static int ARGS_COUNT = 1;

    private final int port;

    public static void main(String[] args) {
        ArgsResolver resolver = new ArgsResolver(PORT_INDEX, ARGS_COUNT);
        int port = 0;
        try {
            port = resolver.getPort(args);
        } catch (IllegalArgumentException exc) {
            System.out.println("Wrong arguments!");
            System.out.println("Use \"" + usage() + "\"");
            System.exit(1);
        }
        SocksProxy proxy = new SocksProxy(port);
        proxy.start();
    }

    public static String usage() {
        return "java -jar proxy.jar <port>";
    }

    public SocksProxy(int port) {
        this.port = port;
    }

    public void start() {
        try (
                Selector selector = Selector.open(); //d
                ServerSocketChannel socketChannel = ServerSocketChannel.open();
        ) {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);

            DnsService dnsService = DnsService.getInstance();
            dnsService.setChannel(datagramChannel);
            dnsService.registerSelector(selector);

            socketChannel.configureBlocking(false);
            socketChannel.bind(new InetSocketAddress(port));
            socketChannel.register(selector, SelectionKey.OP_ACCEPT, new AcceptHandler(socketChannel));

            select(selector);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

    private void select(Selector selector) throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey readyKey = iterator.next();
                try {
                    iterator.remove();
                    if(readyKey.isValid()) {
                        treatSelectionKey(readyKey);
                    }
                } catch (IOException exc) {
                    closeConnection(readyKey);
                } catch (CancelledKeyException exc){
                }
            }
        }
    }

    private void closeConnection(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler)selectionKey.attachment();
        Connection connection = handler.getConnection();
        SocketChannel firstSocket = (SocketChannel) selectionKey.channel();

        try {
            System.out.println("Socket closed: " + firstSocket.getRemoteAddress());
            firstSocket.close();
            connection.closeAssociate();
        } catch (ClosedChannelException exc){
            System.out.println("Closed channel exception!");
            System.out.println(exc.getLocalizedMessage());
        }
    }

    private void treatSelectionKey(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler)selectionKey.attachment();

        if (selectionKey.isWritable()) {
            handler.write(selectionKey);
        }

        if (selectionKey.isValid() && selectionKey.readyOps() != SelectionKey.OP_WRITE) {
            handler.handle(selectionKey);
        }
    }
}
