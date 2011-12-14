package com.lockerz.event;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class MethodEventMonitor implements MethodInterceptor,  MethodEventMonitorMBean {
    private static final Logger log = LoggerFactory.getLogger(MethodEventMonitor.class);

    private EventMonitor eventMonitor;
    private Set<String> monitoredMethodNames = Collections.synchronizedSet(new HashSet<String>());
    private String name;
    private boolean monitorAll = false;

    public void init() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objName = new ObjectName(
                    String.format("%s:type=%s,name=%s",
                    this.getClass().getPackage().getName(),
                    this.getClass().getSimpleName(),
                    this.name));
            mbs.registerMBean(this, objName);
        } catch (Exception e) {
            log.error("Caught exception registering MBean [{}]", e.getMessage(), e);
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (!monitorAll && !this.monitoredMethodNames.contains(method.getName())) {
            return invocation.proceed();
        }

        log.trace("Intercepting method [{}] [{}]",
                method.getDeclaringClass().getName(), method.getName());

        MethodCall call = new MethodCall(method.getDeclaringClass().getName(), method.getName());
        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            call.setRunTimeMs(System.currentTimeMillis() - startTime);
            this.eventMonitor.logEvent(call);
        }
    }

    // MBean impl

    public Set<String> getMonitoredMethodNames() {
        return Collections.unmodifiableSet(this.monitoredMethodNames);
    }

    public boolean removeMonitoredMethodName(String name) {
        return this.monitoredMethodNames.remove(name);
    }

    public boolean addMonitoredMethodName(String name) {
        return this.monitoredMethodNames.add(name);
    }

    public boolean getMonitorAllMethodCalls() {
        return this.monitorAll;
    }

    public void setMonitorAllMethodCalls(boolean monitorAll) {
        this.monitorAll = monitorAll;
    }

    // Setters

    public void setEventMonitor(EventMonitor eventMonitor) {
        this.eventMonitor = eventMonitor;
    }

    public void setName(String name) {
        this.name = name;
    }
}
