/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Celine Souchet
 */
public class TenantQuartzJobListener extends AbstractQuartzJobListener {

    private final SessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final TechnicalLoggerService logger;

    private final List<AbstractBonitaTenantJobListener> bonitaJobListeners;

    private final String groupName;

    public TenantQuartzJobListener(final List<AbstractBonitaTenantJobListener> bonitaJobListeners, final String groupName,
            final SessionAccessor sessionAccessor, final TransactionService transactionService, final TechnicalLoggerService logger) {
        this.logger = logger;
        this.bonitaJobListeners = bonitaJobListeners;
        this.groupName = groupName;
        this.sessionAccessor = sessionAccessor;
        this.transactionService = transactionService;
    }

    @Override
    public String getName() {
        return "TenantQuartzJobListener_" + groupName;
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        final Long tenantId = Long.valueOf(groupName);
        try {
            // Set the tenant id, because the jobService is a tenant service and need a session to use the tenant persistence service. But, a job listener runs not in a session.
            sessionAccessor.setTenantId(tenantId);
            for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobToBeExecuted(mapContext);
            }
        } finally {
            cleanSession();
        }
    }

    @Override
    public void jobExecutionVetoed(final JobExecutionContext context) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        final Long tenantId = Long.valueOf(groupName);
        try {
            // Set the tenant id, because the jobService is a tenant service and need a session to use the tenant persistence service. But, a job listener runs not in a session.
            sessionAccessor.setTenantId(tenantId);
            for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobExecutionVetoed(mapContext);
            }
        } finally {
            cleanSession();
        }
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Map<String, Serializable> mapContext = buildMapContext(context);

        final Long tenantId = Long.valueOf(groupName);
        try {
            // Set the tenant id, because the jobService is a tenant service and need a session to use the tenant persistence service. But, a job listener runs not in a session.
            sessionAccessor.setTenantId(tenantId);
            for (final AbstractBonitaTenantJobListener abstractBonitaTenantJobListener : bonitaJobListeners) {
                abstractBonitaTenantJobListener.jobWasExecuted(mapContext, jobException);
            }
        } finally {
            cleanSession();
        }
    }

    private void cleanSession() {
        try {
            transactionService.registerBonitaSynchronization(new BonitaTransactionSynchronizationImpl(sessionAccessor));
        } catch (final STransactionNotFoundException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e);
            }
        }
    }

}
