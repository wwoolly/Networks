package ru.nsu.kokunin.net.task2.server;

import java.util.concurrent.atomic.AtomicLong;

public class TransmittedByteCounter implements ByteCounter {
    private final AtomicLong totalRegisteredBytes;
    private final AtomicLong currentLapRegisteredBytes;

    TransmittedByteCounter() {
        this.totalRegisteredBytes = new AtomicLong(0);
        this.currentLapRegisteredBytes = new AtomicLong(0);
    }

    @Override
    public void registerBytes(long sendBytesCount) {
        totalRegisteredBytes.addAndGet(sendBytesCount);
        currentLapRegisteredBytes.addAndGet(sendBytesCount);
    }

    @Override
    public long startNewLap() {
        long registeredOnLap = currentLapRegisteredBytes.get();
        currentLapRegisteredBytes.set(0);
        return registeredOnLap;
    }

    @Override
    public long getTotalBytesNumber() {
        return  totalRegisteredBytes.get();
    }
}
