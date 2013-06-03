/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.monitoring.impl;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.monitoring.SGcInfo;
import org.bonitasoft.engine.monitoring.mbean.SJvmMXBean;
import org.bonitasoft.engine.monitoring.mbean.SPlatformServiceMXBean;
import org.bonitasoft.engine.monitoring.mbean.impl.SPlatformServiceMXBeanImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class PlatformMonitoringServiceImpl extends MonitoringServiceImpl implements PlatformMonitoringService {

    private final SJvmMXBean jvmMBean;

    private final EventService eventService;

    private final STransactionHandlerImpl transactionHandler;

    private final SSchedulerHandlerImpl schedulerHandler;

    public PlatformMonitoringServiceImpl(final boolean allowMbeansRegistration, final SJvmMXBean jvmMBean, final EventService eventService,
            final STransactionHandlerImpl transactionHandler, final SSchedulerHandlerImpl schedulerHandler, final TechnicalLoggerService technicalLog)
            throws HandlerRegistrationException, MalformedObjectNameException {
        super(allowMbeansRegistration, technicalLog);
        this.jvmMBean = jvmMBean;
        this.eventService = eventService;
        this.schedulerHandler = schedulerHandler;
        this.transactionHandler = transactionHandler;

        addMBeans();
        addHandlers();
    }

    private void addMBeans() throws MalformedObjectNameException {
        addMBean(jvmMBean);

        final SPlatformServiceMXBean platformSeviceBean = new SPlatformServiceMXBeanImpl(this);
        addMBean(platformSeviceBean);
    }

    private void addHandlers() throws HandlerRegistrationException {
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ACTIVE_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_COMMITED_EVT, transactionHandler);
        eventService.addHandler(STransactionHandlerImpl.TRANSACTION_ROLLEDBACK_EVT, transactionHandler);

        eventService.addHandler(SSchedulerHandlerImpl.SCHEDULER_STARTED, schedulerHandler);
        eventService.addHandler(SSchedulerHandlerImpl.SCHEDULER_STOPPED, schedulerHandler);
    }

    @Override
    public boolean isSchedulerStarted() {
        return schedulerHandler.isSchedulerStarted();
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
        return transactionHandler.getNumberOfActiveTransactions();
    }

    @Override
    @SuppressWarnings("restriction")
    public long getProcessCpuTime() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getProcessCpuTime();
    }

    @Override
    @SuppressWarnings("restriction")
    public long getCommittedVirtualMemorySize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getCommittedVirtualMemorySize();
    }

    @Override
    @SuppressWarnings("restriction")
    public long getTotalSwapSpaceSize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getTotalSwapSpaceSize();
    }

    @Override
    @SuppressWarnings("restriction")
    public long getFreeSwapSpaceSize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getFreeSwapSpaceSize();
    }

    @Override
    @SuppressWarnings("restriction")
    public long getFreePhysicalMemorySize() {
        final com.sun.management.OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        return operatingSystemMXBean.getFreePhysicalMemorySize();
    }

    @Override
    @SuppressWarnings("restriction")
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
