package com.lockerz.event;

import com.lockerz.kestrel.AsynchronousClient;

import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventMonitorImpl implements EventMonitor {
    private static final Logger log = LoggerFactory.getLogger(EventMonitorImpl.class);

    private AsynchronousClient kestrel;

    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());

    public void logEvent(MethodCall call) {
        try {
            this.kestrel.setAndForget("method_calls", 0, this.serializer.serialize(call));
        } catch (Throwable t) {
            log.error("Caught Throwable putting method call on queue [{}]", t.getMessage(), t);
        }
    }

    // Setters

    public void setKestrelClient(AsynchronousClient client) {
        this.kestrel = client;
    }
}
