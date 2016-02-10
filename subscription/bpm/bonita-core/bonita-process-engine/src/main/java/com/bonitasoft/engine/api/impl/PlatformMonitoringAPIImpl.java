/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;

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

    protected PlatformMonitoringService getPlatformMonitoringService() throws MonitoringException {
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
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getCurrentMemoryUsage();
    }

    @Override
    @CustomTransactions
    public float getMemoryUsagePercentage() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getMemoryUsagePercentage();
    }

    @Override
    @CustomTransactions
    public double getSystemLoadAverage() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getSystemLoadAverage();
    }

    @Override
    @CustomTransactions
    public long getUpTime() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getUpTime();
    }

    @Override
    @CustomTransactions
    public long getStartTime() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getStartTime();
    }

    @Override
    @CustomTransactions
    public long getTotalThreadsCpuTime() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getTotalThreadsCpuTime();
    }

    @Override
    @CustomTransactions
    public int getThreadCount() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getThreadCount();
    }

    @Override
    @CustomTransactions
    public int getAvailableProcessors() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getAvailableProcessors();
    }

    @Override
    @CustomTransactions
    public String getOSArch() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getOSArch();
    }

    @Override
    @CustomTransactions
    public String getOSName() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getOSName();
    }

    @Override
    @CustomTransactions
    public String getOSVersion() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getOSVersion();
    }

    @Override
    @CustomTransactions
    public String getJvmName() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getJvmName();
    }

    @Override
    @CustomTransactions
    public String getJvmVendor() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getJvmVendor();
    }

    @Override
    @CustomTransactions
    public String getJvmVersion() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getJvmVersion();
    }

    @Override
    @CustomTransactions
    public Map<String, String> getJvmSystemProperties() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getJvmSystemProperties();
    }

    @Override
    @CustomTransactions
    public boolean isSchedulerStarted() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        try {
            return platformMonitoringService.isSchedulerStarted();
        } catch (SBonitaException e) {
            throw new MonitoringException("Cannot determine if the scheduler is started", e);
        }
    }

    @Override
    @CustomTransactions
    public long getNumberOfActiveTransactions() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.SERVICE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.getNumberOfActiveTransactions();
    }

    protected LicenseChecker getLicenseChecker() {
        return LicenseChecker.getInstance();
    }

    @Override
    @CustomTransactions
    public long getProcessCpuTime() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Process Cpu Time.");
        }
        return platformMonitoringService.getProcessCpuTime();
    }

    @Override
    @CustomTransactions
    public long getCommittedVirtualMemorySize() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Committed Virtual Memory Size.");
        }
        return platformMonitoringService.getCommittedVirtualMemorySize();
    }

    @Override
    @CustomTransactions
    public long getTotalSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Swap Space Size.");
        }
        return platformMonitoringService.getTotalSwapSpaceSize();
    }

    @Override
    @CustomTransactions
    public long getFreeSwapSpaceSize() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Swap Space Size.");
        }
        return platformMonitoringService.getFreeSwapSpaceSize();
    }

    @Override
    @CustomTransactions
    public long getFreePhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Free Physical Memory Size.");
        }
        return platformMonitoringService.getFreePhysicalMemorySize();
    }

    @Override
    @CustomTransactions
    public long getTotalPhysicalMemorySize() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get Total Physical Memory Size.");
        }
        return platformMonitoringService.getTotalPhysicalMemorySize();
    }

    @Override
    @CustomTransactions
    public boolean isOptionalMonitoringInformationAvailable() throws MonitoringException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        return platformMonitoringService.isOptionalMonitoringInformationAvailable();
    }

    @Override
    @CustomTransactions
    public Map<String, GcInfo> getLastGcInfo() throws MonitoringException, UnavailableInformationException {
        getLicenseChecker().checkLicenseAndFeature(Features.RESOURCE_MONITORING);

        final PlatformMonitoringService platformMonitoringService = getPlatformMonitoringService();
        if (!platformMonitoringService.isOptionalMonitoringInformationAvailable()) {
            throw new UnavailableInformationException("Impossible to get the last GC info.");
        }
        return SPModelConvertor.toGcInfos(platformMonitoringService.getLastGcInfo());
    }

}
