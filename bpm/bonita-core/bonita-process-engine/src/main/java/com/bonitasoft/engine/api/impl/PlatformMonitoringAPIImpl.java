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

import org.bonitasoft.engine.monitoring.PlatformMonitoringService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.monitoring.GcInfo;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.UnavailableInformationException;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
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
    public long getCurrentMemoryUsage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getCurrentMemoryUsage();
    }

    @Override
    public float getMemoryUsagePercentage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getMemoryUsagePercentage();
    }

    @Override
    public double getSystemLoadAverage() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getSystemLoadAverage();
    }

    @Override
    public long getUpTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getUpTime();
    }

    @Override
    public long getStartTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getStartTime();
    }

    @Override
    public long getTotalThreadsCpuTime() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getTotalThreadsCpuTime();
    }

    @Override
    public int getThreadCount() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getThreadCount();
    }

    @Override
    public int getAvailableProcessors() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getAvailableProcessors();
    }

    @Override
    public String getOSArch() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSArch();
    }

    @Override
    public String getOSName() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSName();
    }

    @Override
    public String getOSVersion() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getOSVersion();
    }

    @Override
    public String getJvmName() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmName();
    }

    @Override
    public String getJvmVendor() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmVendor();
    }

    @Override
    public String getJvmVersion() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmVersion();
    }

    @Override
    public Map<String, String> getJvmSystemProperties() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getJvmSystemProperties();
    }

    @Override
    public boolean isSchedulerStarted() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.isSchedulerStarted();
    }

    @Override
    public long getNumberOfActiveTransactions() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.SERVICE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.getNumberOfActiveTransactions();
    }

    @Override
    public long getProcessCpuTime() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Process Cpu Time.");
        }
        return platformMonitoringService.getProcessCpuTime();
    }

    @Override
    public long getCommittedVirtualMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Committed Virtual Memory Size.");
        }
        return platformMonitoringService.getCommittedVirtualMemorySize();
    }

    @Override
    public long getTotalSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Swap Space Size.");
        }
        return platformMonitoringService.getTotalSwapSpaceSize();
    }

    @Override
    public long getFreeSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Swap Space Size.");
        }
        return platformMonitoringService.getFreeSwapSpaceSize();
    }

    @Override
    public long getFreePhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Physical Memory Size.");
        }
        return platformMonitoringService.getFreePhysicalMemorySize();
    }

    @Override
    public long getTotalPhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Physical Memory Size.");
        }
        return platformMonitoringService.getTotalPhysicalMemorySize();
    }

    @Override
    public boolean isOptionalMonitoringInformationAvailable() throws MonitoringException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        return platformMonitoringService.isOptionalMonitoringInformationAvailable();
    }

    @Override
    public Map<String, GcInfo> getLastGcInfo() throws MonitoringException, UnavailableInformationException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoring();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get the last GC info.");
        }
        return SPModelConvertor.toGcInfos(platformMonitoringService.getLastGcInfo());
    }

}
