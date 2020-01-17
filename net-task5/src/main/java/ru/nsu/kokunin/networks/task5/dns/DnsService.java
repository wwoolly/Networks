package ru.nsu.kokunin.networks.task5.dns;

import org.xbill.DNS.*;

import ru.nsu.kokunin.networks.task5.handlers.ConnectHandler;
import ru.nsu.kokunin.networks.task5.handlers.Handler;
import ru.nsu.kokunin.networks.task5.socks.SocksRequest;
import ru.nsu.kokunin.networks.task5.utils.TreeMapLimited;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

import static ru.nsu.kokunin.networks.task5.handlers.socks.SocksRequestHandler.onError;

public class DnsService {
    private static final int DNS_SERVER_PORT = 53;
    private static final byte HOST_UNREACHABLE_ERROR = 0x04;
    private static final int BUF_SIZE = 1024;
    private static final int CACHE_SIZE = 256;

    private int messageId = 0;
    private DatagramChannel channel;
    private InetSocketAddress dnsServerAddress;
    private Handler dnsResponseHandler;
    private Map<Integer, DnsEntry> unresolvedNames = new HashMap<>(); // key - dns message id
    private TreeMapLimited<String, String> dnsCache = new TreeMapLimited<>(CACHE_SIZE); // key - hostname, value - ip

    private static class SingletonHelper{
        private static final DnsService dnsService = new DnsService();
    }
    public static DnsService getInstance() {
        return SingletonHelper.dnsService;
    }


    private DnsService() {
        String[] dnsServers = ResolverConfig.getCurrentConfig().servers();
        this.dnsServerAddress = new InetSocketAddress(dnsServers[0], DNS_SERVER_PORT);
    }

    public void setChannel(DatagramChannel channel) {
        this.channel = channel;
        initResponseHandler();
    }

    public void registerSelector(Selector selector) throws ClosedChannelException {
        channel.register(selector, SelectionKey.OP_READ, dnsResponseHandler);
    }

    public void resolveName(SocksRequest request, SelectionKey selectionKey) throws IOException {
        try {
            String name = request.getDomainName();
            String cachedAddress = dnsCache.get(name + ".");
            if (cachedAddress != null){
                connectToTarget(cachedAddress, selectionKey, request.getTargetPort());
                return;
            }

            System.out.println("New domain name to resolve: " + request.getDomainName());
            DnsEntry mapValue = new DnsEntry(selectionKey, request.getTargetPort());
            Message query = getQuery(name);
            byte[] queryBytes = query.toWire();

            unresolvedNames.put(query.getHeader().getID(), mapValue);
            channel.send(ByteBuffer.wrap(queryBytes), dnsServerAddress);
        } catch (TextParseException exc){
            onError(selectionKey, HOST_UNREACHABLE_ERROR);
            exc.printStackTrace();
        }
    }

    private void initResponseHandler() {
        dnsResponseHandler = new Handler(null) {
            @Override
            public void handle(SelectionKey selectionKey) throws IOException {
                ByteBuffer byteBuffer = ByteBuffer.allocate(BUF_SIZE);
                if(channel.receive(byteBuffer) == null){
                    return;
                }

                Message response = new Message(byteBuffer.flip());
                Record[] answers = response.getSectionArray(Section.ANSWER);

                int responseId = response.getHeader().getID();
                DnsEntry unresolvedName = unresolvedNames.get(responseId);
                if(answers.length == 0){
                    onError(unresolvedName.getSelectionKey(), HOST_UNREACHABLE_ERROR);
                    return;
                }

                String hostname = response.getQuestion().getName().toString();
                System.out.println(hostname + " resolved");

                String address = answers[0].rdataToString();
                dnsCache.put(hostname, address);
                connectToTarget(address, unresolvedName.getSelectionKey(), unresolvedName.getTargetPort());
                unresolvedNames.remove(responseId);
            }
        };
    }

    private void connectToTarget(String address, SelectionKey selectionKey, int port) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        ConnectHandler.connectToTarget(selectionKey, socketAddress);
    }

    private Message getQuery(String domainName) throws TextParseException {
        Header header = new Header(messageId++);
        header.setFlag(Flags.RD);
        header.setOpcode(0);

        Message message = new Message();
        message.setHeader(header);

        Record record = Record.newRecord(new Name(domainName + "."), Type.A, DClass.IN);
        message.addRecord(record, Section.QUESTION);

        return message;
    }
}