package ru.nsu.kokunin.networks.task5.dns;

import java.nio.channels.SelectionKey;

public class DnsEntry {
    private SelectionKey selectionKey;
    private short targetPort;

    public DnsEntry(SelectionKey selectionKey, short targetPort) {
        this.selectionKey = selectionKey;
        this.targetPort = targetPort;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public short getTargetPort() {
        return targetPort;
    }
}