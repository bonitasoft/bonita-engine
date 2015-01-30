/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.monitoring.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.SGcInfo;
import com.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import com.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import com.bonitasoft.engine.monitoring.mbean.impl.SPlatformServiceMXBeanImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Laurent Vaills
 */
public class PlatformMonitoringServiceImpl extends MonitoringServiceImpl implements PlatformMonitoringService {

    private final SJvmMXBean jvmMBean;

    private final SchedulerService schedulerService;

    private final TransactionService transactionService;

    public PlatformMonitoringServiceImpl(final boolean allowMbeansRegistration, final SJvmMXBean jvmMBean,
            final TransactionService transactionService, final SchedulerService schedulerService, final TechnicalLoggerService technicalLog)
            throws MalformedObjectNameException {
        super(allowMbeansRegistration, technicalLog);
        this.jvmMBean = jvmMBean;
        this.transactionService = transactionService;
        this.schedulerService = schedulerService;

        addMBeans();
    }

    private void addMBeans() throws MalformedObjectNameException {
        addMBean(jvmMBean);

        final SPlatformServiceMXBean platformSeviceBean = new SPlatformServiceMXBeanImpl(this);
        addMBean(platformSeviceBean);
    }

    @Override
    public boolean isSchedulerStarted() throws SBonitaException {
        return schedulerService.isStarted();
    }

    @Override
    public long getCurrentMemoryUsage() {
        return jvmMBean.getMemoryUsage();
    }

    @Override
    public float getMemoryUsagePercentage() {
        return jvmMBean.getMemoryUsagePercentage();
    }

    @Override
    public double getSystemLoadAverage() {
        return jvmMBean.getSystemLoadAverage();
    }

    @Override
    public long getUpTime() {
        return jvmMBean.getUpTime();
    }

    @Override
    public long getStartTime() {
        return jvmMBean.getStartTime();
    }

    @Override
    public long getTotalThreadsCpuTime() {
        return jvmMBean.getTotalThreadsCpuTime();
    }

    @Override
    public int getThreadCount() {
        return jvmMBean.getThreadCount();
    }

    @Override
    public int getAvailableProcessors() {
        return jvmMBean.getAvailableProcessors();
    }

    @Override
    public String getOSArch() {
        return jvmMBean.getOSArch();
    }

    @Override
    public String getOSName() {
        return jvmMBean.getOSName();
    }

    @Override
    public String getOSVersion() {
        return jvmMBean.getOSVersion();
    }

    @Override
    public String getJvmName() {
        return jvmMBean.getJvmName();
    }

    @Override
    public String getJvmVendor() {
        return jvmMBean.getJvmVendor();
    }

    @Override
    public String getJvmVersion() {
        return jvmMBean.getJvmVersion();
    }

    @Override
    public Map<String, String> getJvmSystemProperties() {
        return jvmMBean.getJvmSystemProperties();
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return transactionService.getNumberOfActiveTransactions();
    }

    @Override
    public long getProcessCpuTime() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getProcessCpuTime();
    }

    @Override
    public long getCommittedVirtualMemorySize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getCommittedVirtualMemorySize();
    }

    @Override
    public long getTotalSwapSpaceSize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getTotalSwapSpaceSize();
    }

    @Override
    public long getFreeSwapSpaceSize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getFreeSwapSpaceSize();
    }

    @Override
    public long getFreePhysicalMemorySize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getFreePhysicalMemorySize();
    }

    @Override
    public long getTotalPhysicalMemorySize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getTotalPhysicalMemorySize();
    }

    @Override
    public boolean isOptionalMonitoringInformationAvailable() {
        return jvmMBean.getJvmVendor().indexOf("Sun") >= 0;
    }

    @Override
    public Map<String, SGcInfo> getLastGcInfo() {
        final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        final Map<String, SGcInfo> lastGcInfos = new HashMap<String, SGcInfo>();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            final com.sun.management.GcInfo gcInfo = ((com.sun.management.GarbageCollectorMXBean) garbageCollectorMXBean).getLastGcInfo();
            if (gcInfo != null) {
                final String gcName = ((com.sun.management.GarbageCollectorMXBean) garbageCollectorMXBean).getName();
                lastGcInfos.put(gcName, new SGcInfoImpl(gcInfo));
            }
        }
        return lastGcInfos;
    }

}
