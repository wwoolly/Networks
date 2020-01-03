package ru.nsu.kokunin.networks.task5;

import ru.nsu.kokunin.networks.task5.utils.ArgsResolver;

public class SocksProxy {
    private final static int PORT_INDEX = 0;
    private final static int ARGS_COUNT = 1;

    private final int proxyPort;

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

    public SocksProxy(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void start() {

    }
}
