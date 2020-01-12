package ru.nsu.kokunin.networks.task5.socks;

public class SocksConnectRequest {
    private byte version;
    private short methodsNumber; //254 max
    private byte[] methods;
    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public void setMethodsNumber(short methodsNumber) {
        this.methodsNumber = methodsNumber;
        this.methods = new byte[methodsNumber];
    }

    public byte[] getMethods() {
        return methods;
    }
}
