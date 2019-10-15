package ru.nsu.kokunin.net.task2.server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimeStatGenerator {
    public final static int MINIMAL_TIME_INTERVAL = 1000;

    private ByteCounter bytesCounter;
    private Timer timer;

    private int interval; //milliseconds
    private double time; //seconds

    TimeStatGenerator(ByteCounter bytesCounter, int interval) {
        this.bytesCounter = bytesCounter;

        //or maybe the ternary operator will look better here?
        this.interval = Math.max(MINIMAL_TIME_INTERVAL, interval);
        this.time = 0;

        this.timer = new Timer(this.interval, (ActionEvent event) -> {
            this.time += (double)this.interval / 1000;


            double speed = (double)(bytesCounter.startNewLap() * 1000) / interval;
            String tailMessage = (speed < 513) ? " Bytes/sec.\n" : " KB/sec.\n";
            System.out.printf("\tCurrent speed: %.2f" + tailMessage, (speed < 513 ? speed : speed / 1024));

            System.out.printf("\tTotal transmitted: %.2f KB.%n", (double)bytesCounter.getTotalBytesNumber() / 1024);

            speed = (double)bytesCounter.getTotalBytesNumber() / time;
            tailMessage = (speed < 513) ? " Bytes/sec.\n\n" : " KB/sec.\n\n";
            System.out.printf("\tTotal speed: %.2f" + tailMessage, (speed < 513 ? speed : speed / 1024));
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        if (time <= interval) {
            ActionListener listener = timer.getActionListeners()[0];
            listener.actionPerformed(null);
        }
        timer.stop();
    }
}
