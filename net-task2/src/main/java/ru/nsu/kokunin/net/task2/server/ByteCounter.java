package ru.nsu.kokunin.net.task2.server;

public interface ByteCounter {
    long startNewLap();
    void registerBytes(long numberBytes);
    long getTotalBytesNumber();
}
