package services;

import java.net.InetAddress;
import java.util.Timer;

public class ServiceMessageSender {
    private static final int PORT = 8888,
            DELAY = 0, TIMEOUT = 5000, BUF_SIZE = 0;

    private static ServiceMessageSender instance;

    private Timer sendTimer;
    private InetAddress multicastAddress = null;

    private ServiceMessageSender() {
        sendTimer = new Timer(true);
    }

    public ServiceMessageSender getServiceMessageReceiver() {
        if (instance == null) {
            instance = new ServiceMessageSender();
        }
        return instance;
    }
}
