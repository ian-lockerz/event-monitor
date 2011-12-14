package com.lockerz.kestrel.nio;

public interface InternalHandler {
    public boolean receiveData(byte[] data);
    public void onError(String type, String message);
}
