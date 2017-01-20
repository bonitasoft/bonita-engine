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
package org.bonitasoft.engine.api.impl;

import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Thread start when the engine is ready.
 * Its purpose is to start elements to be recovered from the previous run of the engine.
 *
 * @author Baptiste Mesta
 */
public class StarterThread extends Thread {

    private final PlatformServiceAccessor platformServiceAccessor;
    private final TenantServiceAccessor tenantServiceAccessor;
    private final List<TenantRestartHandler> tenantRestartHandlers;

    public StarterThread(PlatformServiceAccessor platformServiceAccessor, TenantServiceAccessor tenantServiceAccessor,
            List<TenantRestartHandler> tenantRestartHandlers) {
        super("Tenant " + tenantServiceAccessor.getTenantId() + " starter Thread");
        this.platformServiceAccessor = platformServiceAccessor;
        this.tenantServiceAccessor = tenantServiceAccessor;
        this.tenantRestartHandlers = tenantRestartHandlers;
    }

    @Override
    public void run() {
        final TechnicalLoggerService technicalLoggerService = tenantServiceAccessor.getTechnicalLoggerService();
        try {
            final long tenantId = tenantServiceAccessor.getTenantId();
            SessionAccessor sessionAccessor = tenantServiceAccessor.getSessionAccessor();
            STenant tenant = getTenant(tenantId);
            technicalLoggerService.log(getClass(), TechnicalLogSeverity.INFO,
                    "Restarting elements of tenant " + tenant.getId() + " that were not finished at the last shutdown");
            if (tenant.isPaused() || !tenant.isActivated()) {
                technicalLoggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Unable to restart elements of tenant " + tenant.getStatus());
                return;
            }
            executeHandlers(tenantId, sessionAccessor);

        } catch (Exception e) {
            technicalLoggerService.log(StarterThread.class, TechnicalLogSeverity.ERROR, "Error while restarting elements", e);
        }
    }

    private void executeHandlers(long tenantId, SessionAccessor sessionAccessor) throws SBonitaException, RestartException {
        final SessionService sessionService = platformServiceAccessor.getTenantServiceAccessor(tenantId).getSessionService();
        long sessionId = createSessionAndMakeItActive(platformServiceAccessor, sessionAccessor, tenantId);
        try {
            final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);

            for (final TenantRestartHandler restartHandler : tenantRestartHandlers) {
                restartHandler.afterServicesStart(platformServiceAccessor, tenantServiceAccessor);

            }
        } finally {
            sessionService.deleteSession(sessionId);
        }
    }

    STenant getTenant(final long tenantId) throws Exception {
        return platformServiceAccessor.getTransactionService().executeInTransaction(new Callable<STenant>() {

            @Override
            public STenant call() throws Exception {
                return platformServiceAccessor.getPlatformService().getTenant(tenantId);
            }
        });
    }

    private long createSessionAndMakeItActive(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor, final long tenantId)
            throws SBonitaException {
        final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();

        final long sessionId = sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
        sessionAccessor.setSessionInfo(sessionId, tenantId);
        return sessionId;
    }
}
