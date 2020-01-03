package ru.nsu.kokunin.networks.task5.handlers;

import ru.nsu.kokunin.networks.task5.utils.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class Handler {
    private static final int BUFF_LENGTH = 65536;
    private static final int NO_REMAINING = 0;

    private Connection connection;

    abstract public void handle(SelectionKey selectionKey) throws IOException;

    public Handler(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
    public int read(SelectionKey selectionKey) throws IOException {}
    public int write(SelectionKey selectionKey) throws IOException {}
    public static int getBuffLength() {
        return BUFF_LENGTH;
    }
    private boolean isReadyToRead(ByteBuffer buffer, Connection connection) {}
    private void checkAssociate(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {}
    private void checkConnectionClose(SocketChannel socketChannel) throws IOException {}

}
