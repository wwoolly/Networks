package ru.nsu.kokunin.networks.task5.handlers;

import ru.nsu.kokunin.networks.task5.handlers.socks.SocksConnectHandler;
import ru.nsu.kokunin.networks.task5.utils.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler extends Handler {
    private ServerSocketChannel serverSocketChannel;

    public AcceptHandler(ServerSocketChannel serverSocketChannel) {
        super(null);
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        Connection connection = new Connection(getBuffLength());
        SocksConnectHandler connectHandler = new SocksConnectHandler(connection);

        SelectionKey key = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, connectHandler);
        connection.registerBufferListener(() -> key.interestOpsOr(SelectionKey.OP_WRITE));

        System.out.println("New connection: " + socketChannel.getRemoteAddress());
    }
}
