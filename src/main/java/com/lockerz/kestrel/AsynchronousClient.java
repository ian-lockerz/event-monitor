package com.lockerz.kestrel;

import com.lockerz.kestrel.async.GetResponseHandler;
import com.lockerz.kestrel.async.SetResponseHandler;

import java.io.IOException;

public interface AsynchronousClient extends Client {
    public void setAndForget(String queueName, long expiration, byte[] data);

    public void set(String queueName, long expiration, byte[] data, SetResponseHandler handler)
        throws IOException;

    public void get(String queueName, long timeoutMs, boolean reliable, GetResponseHandler handler)
        throws IOException;

    public void peek(String queueName, long timeoutMs, GetResponseHandler handler)
        throws IOException;
}
