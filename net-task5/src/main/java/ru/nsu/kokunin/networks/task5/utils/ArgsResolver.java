package ru.nsu.kokunin.networks.task5.utils;

public class ArgsResolver {
    private final int portIndex;
    private final int argsCount;

    public ArgsResolver(int portIndex, int argsCount) {
        this.portIndex = portIndex;
        this.argsCount = argsCount;
    }

    public int getPort(String[] args) throws IllegalArgumentException{
        if (args.length != argsCount) {
            throw new IllegalArgumentException();
        }

        return Integer.parseInt(args[portIndex]);
    }
}
