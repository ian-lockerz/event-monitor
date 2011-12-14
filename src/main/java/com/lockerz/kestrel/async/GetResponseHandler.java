package com.lockerz.kestrel.async;

public interface GetResponseHandler {
    public void onSuccess(byte[] data);
    public void onError(String type, String message);
}
