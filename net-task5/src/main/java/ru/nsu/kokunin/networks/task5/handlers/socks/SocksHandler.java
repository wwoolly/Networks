package ru.nsu.kokunin.networks.task5.handlers.socks;

import ru.nsu.kokunin.networks.task5.handlers.Handler;
import ru.nsu.kokunin.networks.task5.utils.Connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public abstract class SocksHandler extends Handler {
    public SocksHandler(Connection connection) {
        super(connection);
    }

    @Override
    public int read(SelectionKey selectionKey) throws IOException {
        int readNumber = super.read(selectionKey);
        if(readNumber < 0) {
            throw new IOException("Socket closed during SOCKS5 handshake");
        }
        return readNumber;
    }
}

