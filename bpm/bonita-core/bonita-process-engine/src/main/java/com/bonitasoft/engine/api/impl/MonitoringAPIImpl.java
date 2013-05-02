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
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.MonitoringException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.TenantMonitoringService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.BusinessTransaction;
import org.bonitasoft.engine.transaction.TransactionService;

import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.manager.Features;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class MonitoringAPIImpl implements MonitoringAPI {

    @Override
    public long getNumberOfActiveTransactions() throws MonitoringException, InvalidSessionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.SERVICE_MONITORING);

        final TenantMonitoringService tenantMonitoringService = getTenantMonitoringService();
        return tenantMonitoringService.getNumberOfActiveTransactions();
    }

    private TenantMonitoringService getTenantMonitoringService() throws InvalidSessionException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        return tenantServiceAccessor.getTenantMonitoringService();
    }

    private TenantServiceAccessor getTenantServiceAccessor() throws InvalidSessionException {
        long tenantId = 0;
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final TenantIdNotSetException e) {
            throw new InvalidSessionException(e);
        }
        return TenantServiceSingleton.getInstance(tenantId);
    }

    @Override
    public long getNumberOfExecutingProcesses() throws MonitoringException, InvalidSessionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.BPM_MONITORING);

        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getNumberOfUsers() throws MonitoringException, InvalidSessionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.BPM_MONITORING);

        final TenantMonitoringService tenantMonitoringService = getTenantMonitoringService();
        final TransactionService transactionService = getTransactionService();
        final TechnicalLoggerService logger = getTechnicalLogger();

        BusinessTransaction btx = null;
        long numberOfUsers;
        try {
            btx = transactionService.createTransaction();
            btx.begin();
            numberOfUsers = tenantMonitoringService.getNumberOfUsers();
        } catch (final SBonitaException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            throw new MonitoringException(e.getMessage());
        } finally {
            if (btx != null) {
                try {
                    btx.complete();
                } catch (final SBonitaException e) {
                    logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
                    throw new MonitoringException(e.getMessage());
                }
            }
        }
        return numberOfUsers;
    }

    private TransactionService getTransactionService() throws InvalidSessionException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        return tenantServiceAccessor.getTransactionService();
    }

    private TechnicalLoggerService getTechnicalLogger() throws InvalidSessionException {
        final TenantServiceAccessor tenantServiceAccessor = getTenantServiceAccessor();
        return tenantServiceAccessor.getTechnicalLoggerService();
    }

}
