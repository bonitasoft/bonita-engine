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
 * @author Baptiste Mesta
 *
 */
public class StarterThread extends Thread {

    private final PlatformServiceAccessor platformAccessor;
    private final NodeConfiguration platformConfiguration;
    private final List<STenant> tenants;
    private final SessionAccessor sessionAccessor;
    private final TechnicalLoggerService technicalLoggerService;

    /**
     * @param platformAccessor
     * @param platformConfiguration
     * @param tenants
     * @param sessionAccessor
     * @param technicalLoggerService
     * @param platformAPIImpl TODO
     */
    public StarterThread(final PlatformServiceAccessor platformAccessor,
            final NodeConfiguration platformConfiguration,
            final List<STenant> tenants, final SessionAccessor sessionAccessor, final TechnicalLoggerService technicalLoggerService) {
        super("Starter Thread");
        this.platformAccessor = platformAccessor;
        this.platformConfiguration = platformConfiguration;
        this.tenants = tenants;
        this.sessionAccessor = sessionAccessor;
        this.technicalLoggerService = technicalLoggerService;
        technicalLoggerService.log(getClass(), TechnicalLogSeverity.INFO,
                "Restarting elements in the Thread " + this.getId());
    }

    @Override
    public void run() {
        try {
            for (final STenant tenant : tenants) {
                technicalLoggerService.log(getClass(), TechnicalLogSeverity.INFO, "Restarting elements for tenant " + tenant.getId());
                if (!tenant.isPaused()) {
                    final long tenantId = tenant.getId();
                    long sessionId = -1;
                    final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();
                    try {
                        sessionId = createSessionAndMakeItActive(platformAccessor, sessionAccessor, tenantId);
                        final TenantServiceAccessor tenantServiceAccessor = platformAccessor.getTenantServiceAccessor(tenantId);

                        for (final TenantRestartHandler restartHandler : platformConfiguration.getTenantRestartHandlers()) {
                            restartHandler.afterServicesStart(platformAccessor, tenantServiceAccessor);

                        }
                    } finally {
                        sessionService.deleteSession(sessionId);
                    }
                }
            }
        } catch (RestartException e) {
            technicalLoggerService.log(StarterThread.class, TechnicalLogSeverity.ERROR, "Error while restarting elements", e);
        } catch (SBonitaException e) {
            technicalLoggerService.log(StarterThread.class, TechnicalLogSeverity.ERROR, "Error while restarting elements", e);
        }
    }

    private long createSessionAndMakeItActive(final PlatformServiceAccessor platformAccessor, final SessionAccessor sessionAccessor, final long tenantId)
            throws SBonitaException {
        final SessionService sessionService = platformAccessor.getTenantServiceAccessor(tenantId).getSessionService();

        final long sessionId = sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
        sessionAccessor.setSessionInfo(sessionId, tenantId);
        return sessionId;
    }
}