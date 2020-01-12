package ru.nsu.kokunin.networks.task5.handlers;

import ru.nsu.kokunin.networks.task5.utils.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public int read(SelectionKey selectionKey) throws IOException {
        Handler handler = (Handler) selectionKey.attachment();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Connection connection = handler.getConnection();
        ByteBuffer outputBuffer = connection.getOutputBuffer();

        if(!isReadyToRead(outputBuffer, connection)) {
            return 0;
        }

        int readCount = socketChannel.read(outputBuffer);

        if(readCount <= 0) {
            connection.shutdown();
            selectionKey.interestOps(0);
            checkConnectionClose(socketChannel);
        }

        return readCount;
    }

    public int write(SelectionKey selectionKey) throws IOException {
        ByteBuffer inputBuffer = connection.getInputBuffer();
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        connection.prepareToWrite();
        socketChannel.write(inputBuffer);

        int remaining = inputBuffer.remaining();

        if (remaining == NO_REMAINING) {
            selectionKey.interestOps(SelectionKey.OP_READ);
            checkAssociate(socketChannel, inputBuffer);
        } else {
            connection.setWriteStartPosition();
        }

        return remaining;
    }

    public static int getBuffLength() {
        return BUFF_LENGTH;
    }

    private boolean isReadyToRead(ByteBuffer buffer, Connection connection){
        return (buffer.position() < BUFF_LENGTH / 2) || connection.isAssociateShutDown();
    }

    private void checkConnectionClose(SocketChannel socketChannel) throws IOException {
        if(connection.isReadyToClose()){
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("(kk.mm.ss) ");
            System.out.println(format.format(currentDate) + "Socket closed: " + socketChannel.getRemoteAddress());
            socketChannel.close();
            connection.closeAssociate();
        }
    }

    private void checkAssociate(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        if(connection.isAssociateShutDown()){
            socketChannel.shutdownOutput();
            return;
        }
        buffer.clear();
        connection.resetWriteStartPosition();
    }
}
