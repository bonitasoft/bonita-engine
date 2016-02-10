/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.mbean.impl;

import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.mbean.MBeanStartException;
import com.bonitasoft.engine.monitoring.mbean.MBeanStopException;
import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;

/**
 * @author Christophe Havard
 */
public class SJvmMXBeanImpl implements SJvmMXBean {

    private final MBeanServer mbserver;

    private final ObjectName name;

    private final String strName;

    private final MemoryMXBean memoryMB;

    private final OperatingSystemMXBean osMB;

    private final RuntimeMXBean runtimeMB;

    private final ThreadMXBean threadMB;

    private long uptime = 0;

    private double systemLoad = 0;

    private long usage = 0;

    private long startTime = 0;

    private int threadCount = 0;

    /**
     * Default constructor.
     * 
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     */
    public SJvmMXBeanImpl() throws MalformedObjectNameException, NullPointerException {
        this.mbserver = MBeanUtil.getMBeanServer();
        this.strName = PlatformMonitoringService.JVM_MBEAN_NAME;
        this.name = new ObjectName(strName);
        this.memoryMB = MBeanUtil.getMemoryMXBean();
        this.osMB = MBeanUtil.getOSMXBean();
        this.runtimeMB = MBeanUtil.getRuntimeMXBean();
        this.threadMB = MBeanUtil.getThreadMXBean();
    }

    @Override
    public void start() throws MBeanStartException {
        // TODO Complete the definition of the start
        try {
            // register the MXBean
            if (!mbserver.isRegistered(name)) {
                mbserver.registerMBean(this, name);
            }
        } catch (final Exception e) {
            throw new MBeanStartException(e);
        }
    }

    @Override
    public void stop() throws MBeanStopException {
        // TODO Complete the stop definition
        try {
            // Unregister the MXBean
            if (mbserver.isRegistered(name)) {
                mbserver.unregisterMBean(name);
            }
        } catch (final Exception e) {
            throw new MBeanStopException(e);
        }
    }

    @Override
    public long getMemoryUsage() {
        usage = (this.memoryMB.getHeapMemoryUsage().getUsed() + this.memoryMB.getNonHeapMemoryUsage().getUsed());
        return usage;
    }

    @Override
    public double getSystemLoadAverage() {
        systemLoad = this.osMB.getSystemLoadAverage();
        return systemLoad;
    }

    @Override
    public long getUpTime() {
        uptime = this.runtimeMB.getUptime();
        return uptime;
    }

    @Override
    public long getStartTime() {
        startTime = this.runtimeMB.getStartTime();
        return startTime;
    }

    @Override
    public long getTotalThreadsCpuTime() {
        long cpuTimeSum = -1;
        // fetch the threadCpuTime only if it's available
        if (this.threadMB.isThreadCpuTimeSupported() && this.threadMB.isThreadCpuTimeEnabled()) {
            // take the total number of thread and sum the cpu time for each.
            final long[] threadIds = this.threadMB.getAllThreadIds();
            cpuTimeSum = 0;
            for (final long id : threadIds) {
                cpuTimeSum += this.threadMB.getThreadCpuTime(id);
            }
        }
        return cpuTimeSum;
    }

    @Override
    public int getThreadCount() {
        threadCount = this.threadMB.getThreadCount();
        return threadCount;
    }

    @Override
    public float getMemoryUsagePercentage() {
        final float currentUsage = (this.memoryMB.getHeapMemoryUsage().getUsed() + this.memoryMB.getNonHeapMemoryUsage().getUsed());
        final float maxMemory = (this.memoryMB.getHeapMemoryUsage().getMax() + this.memoryMB.getNonHeapMemoryUsage().getMax());
        final float percentage = currentUsage / maxMemory;
        return (percentage * 100);
    }

    @Override
    public String getOSArch() {
        return this.osMB.getArch();
    }

    @Override
    public int getAvailableProcessors() {
        return this.osMB.getAvailableProcessors();
    }

    @Override
    public String getOSName() {
        return (this.osMB.getName());
    }

    @Override
    public String getOSVersion() {
        return (this.osMB.getVersion());
    }

    @Override
    public String getJvmName() {
        return (this.runtimeMB.getVmName());
    }

    @Override
    public String getJvmVendor() {
        return (this.runtimeMB.getVmVendor());
    }

    @Override
    public String getJvmVersion() {
        return (this.runtimeMB.getVmVersion());
    }

    @Override
    public String getName() {
        return this.strName;
    }

    @Override
    public Map<String, String> getJvmSystemProperties() {
        return (this.runtimeMB.getSystemProperties());
    }

}
