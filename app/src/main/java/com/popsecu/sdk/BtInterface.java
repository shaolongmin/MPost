package com.popsecu.sdk;

/**
 * Created by xumin on 2015/9/17.
 */
public abstract class BtInterface {
    public abstract boolean conncet();
    public abstract void close();
    public abstract int send(byte[] data, int offset, int len);
    public abstract int recv(byte[] buffer, int offset, int size);
    public abstract int recv(byte[] buffer, int offset, int size, int miliseconds);
    public abstract boolean isConnected();
}

