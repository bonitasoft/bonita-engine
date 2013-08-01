/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.monitoring.GcInfo;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.PlatformMonitoringService;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.manager.Features;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class PlatformMonitoringAPIImpl implements PlatformMonitoringAPI {

    private PlatformMonitoringService getPlatformMonitoring() throws MonitoringException {
        PlatformServiceAccessor platformServiceAccessor = null;
        try {
            platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new MonitoringException("Impossible to get platform service accessor", e);
        }

        return platformServiceAccessor.getPlatformMonitoringService();
    }

    @Override
    @CustomTransactions
    public long getCurrentMemoryUsage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getCurrentMemoryUsage();
    }

    @Override
    @CustomTransactions
    public float getMemoryUsagePercentage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getMemoryUsagePercentage();
    }

    @Override
    @CustomTransactions
    public double getSystemLoadAverage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getSystemLoadAverage();
    }

    @Override
    @CustomTransactions
    public long getUpTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getUpTime();
    }

    @Override
    @CustomTransactions
    public long getStartTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getStartTime();
    }

    @Override
    @CustomTransactions
    public long getTotalThreadsCpuTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getTotalThreadsCpuTime();
    }

    @Override
    @CustomTransactions
    public int getThreadCount() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getThreadCount();
    }

    @Override
    @CustomTransactions
    public int getAvailableProcessors() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getAvailableProcessors();
    }

    @Override
    @CustomTransactions
    public String getOSArch() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSArch();
    }

    @Override
    @CustomTransactions
    public String getOSName() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSName();
    }

    @Override
    @CustomTransactions
    public String getOSVersion() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSVersion();
    }

    @Override
    @CustomTransactions
    public String getJvmName() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmName();
    }

    @Override
    @CustomTransactions
    public String getJvmVendor() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmVendor();
    }

    @Override
    @CustomTransactions
    public String getJvmVersion() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmVersion();
    }

    @Override
    @CustomTransactions
    public Map<String, String> getJvmSystemProperties() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmSystemProperties();
    }

    @Override
    @CustomTransactions
    public boolean isSchedulerStarted() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.isSchedulerStarted();
    }

    @Override
    @CustomTransactions
    public long getNumberOfActiveTransactions() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.SERVICE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getNumberOfActiveTransactions();
    }

    @Override
    @CustomTransactions
    public long getProcessCpuTime() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Process Cpu Time.");
        }
        return platformMonitoringService.getProcessCpuTime();
    }

    @Override
    @CustomTransactions
    public long getCommittedVirtualMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Committed Virtual Memory Size.");
        }
        return platformMonitoringService.getCommittedVirtualMemorySize();
    }

    @Override
    @CustomTransactions
    public long getTotalSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Swap Space Size.");
        }
        return platformMonitoringService.getTotalSwapSpaceSize();
    }

    @Override
    @CustomTransactions
    public long getFreeSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Swap Space Size.");
        }
        return platformMonitoringService.getFreeSwapSpaceSize();
    }

    @Override
    @CustomTransactions
    public long getFreePhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Physical Memory Size.");
        }
        return platformMonitoringService.getFreePhysicalMemorySize();
    }

    @Override
    @CustomTransactions
    public long getTotalPhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Physical Memory Size.");
        }
        return platformMonitoringService.getTotalPhysicalMemorySize();
    }

    @Override
    @CustomTransactions
    public boolean isOptionalMonitoringInformationAvailable() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.isOptionalMonitoringInformationAvailable();
    }

    @Override
    @CustomTransactions
    public Map<String, GcInfo> getLastGcInfo() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get the last GC info.");
        }
        return SPModelConvertor.toGcInfos(platformMonitoringService.getLastGcInfo());
    }

}
