package com.lockerz.kestrel.async;

public interface SetResponseHandler {
    public void onSuccess();
    public void onError(String type, String message);
}
