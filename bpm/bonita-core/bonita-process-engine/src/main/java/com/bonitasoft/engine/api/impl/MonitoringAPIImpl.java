/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.monitoring.MonitoringException;
import com.bonitasoft.engine.monitoring.SMonitoringException;
import com.bonitasoft.engine.monitoring.TenantMonitoringService;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class MonitoringAPIImpl implements MonitoringAPI {

    protected void checkLicenceAndFeature(final String feature) {
        LicenseChecker.getInstance().checkLicenceAndFeature(feature);
    }

    protected TenantServiceAccessor getTenantServiceAccessor() {
        long tenantId = 0;
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException e) {
            throw new BonitaRuntimeException(e);
        }
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @CustomTransactions
    @Override
    public long getNumberOfActiveTransactions() {
        checkLicenceAndFeature(Features.SERVICE_MONITORING);
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        final TenantMonitoringService monitoringService = tenantServiceAccessor.getTenantMonitoringService();
        return monitoringService.getNumberOfActiveTransactions();
    }

    @Override
    public long getNumberOfExecutingProcesses() {
        checkLicenceAndFeature(Features.BPM_MONITORING);
        // FIXME
        return 0;
    }

    @Override
    public long getNumberOfUsers() throws MonitoringException {
        checkLicenceAndFeature(Features.BPM_MONITORING);
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        try {
            final TenantMonitoringService monitoringService = tenantServiceAccessor.getTenantMonitoringService();
            return monitoringService.getNumberOfUsers();
        } catch (final SMonitoringException e) {
            final TechnicalLoggerService logger = tenantServiceAccessor.getTechnicalLoggerService();
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
            }
            throw new MonitoringException(e.getMessage());
        }
    }

}
