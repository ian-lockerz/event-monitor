package com.lockerz.event;

import java.util.Set;

public interface MethodEventMonitorMBean {
    public Set<String> getMonitoredMethodNames();
    public boolean addMonitoredMethodName(String name);
    public boolean removeMonitoredMethodName(String name);
    public void setMonitorAllMethodCalls(boolean monitorAll);
    public boolean getMonitorAllMethodCalls();
}
