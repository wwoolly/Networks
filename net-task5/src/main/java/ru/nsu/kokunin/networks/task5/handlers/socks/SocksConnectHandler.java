package ru.nsu.kokunin.networks.task5.handlers.socks;

import ru.nsu.kokunin.networks.task5.socks.SocksConnectRequest;
import ru.nsu.kokunin.networks.task5.socks.SocksConnectResponse;
import ru.nsu.kokunin.networks.task5.utils.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import static ru.nsu.kokunin.networks.task5.socks.SocksParser.parseConnect;

public class SocksConnectHandler extends SocksHandler {
    private static final byte NO_AUTHENTICATION = 0x00;
    private static final int SOCKS_VERSION = 0x05;
    private static final byte NO_COMPARABLE_METHOD = (byte) 0xFF;

    public SocksConnectHandler(Connection connection) {
        super(connection);
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        Connection connection = getConnection();
        ByteBuffer outputBuffer = connection.getOutputBuffer();
        read(selectionKey);

        SocksConnectRequest connectRequest = parseConnect(outputBuffer);
        if(connectRequest == null) {
            return;
        }

        SocksConnectResponse connectResponse = new SocksConnectResponse();
        if(!checkRequest(connectRequest)) {
            connectResponse.setMethod(NO_COMPARABLE_METHOD);
        }

        ByteBuffer inputBuffer = connection.getInputBuffer();
        inputBuffer.put(connectResponse.toByteArr());

        selectionKey.interestOpsOr(SelectionKey.OP_WRITE);
        selectionKey.attach(new SocksRequestHandler(connection));
        connection.getOutputBuffer().clear();
    }

    private boolean checkRequest(SocksConnectRequest connectRequest){
        return (connectRequest.getVersion() == SOCKS_VERSION) && checkMethods(connectRequest.getMethods());
    }

    private static boolean checkMethods(byte[] methods){
        for(byte method : methods) {
            if(method == NO_AUTHENTICATION) {
                return true;
            }
        }

        return false;
    }
}

