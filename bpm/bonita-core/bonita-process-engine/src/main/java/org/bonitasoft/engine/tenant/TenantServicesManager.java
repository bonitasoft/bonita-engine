/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.tenant;

import static org.bonitasoft.engine.tenant.TenantServicesManager.ServiceAction.*;

import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.service.RunnableWithException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Handles the lifecycle of tenant services: start, stop, (pause, resume -> will be removed)
 * Does not handle state of the tenant in database
 */
@Component
public class TenantServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantServicesManager.class);

    public enum ServiceAction {
        START, STOP, PAUSE, RESUME
    }

    public enum TenantServiceState {
        STOPPED, STARTING, STARTED, STOPPING
    }

    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final TransactionService transactionService;
    private final ClassLoaderService classLoaderService;
    private final TenantConfiguration tenantConfiguration;
    private final Long tenantId;
    private final TenantElementsRestarter tenantElementsRestarter;

    private TenantServiceState tenantServiceState = TenantServiceState.STOPPED;

    public TenantServicesManager(SessionAccessor sessionAccessor, SessionService sessionService,
            TransactionService transactionService, ClassLoaderService classLoaderService,
            TenantConfiguration tenantConfiguration, @Value("${tenantId}") Long tenantId,
            TenantElementsRestarter tenantElementsRestarter) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        this.classLoaderService = classLoaderService;
        this.tenantConfiguration = tenantConfiguration;
        this.tenantId = tenantId;
        this.tenantElementsRestarter = tenantElementsRestarter;
    }

    public boolean isStarted() {
        return tenantServiceState == TenantServiceState.STARTED;
    }

    public void start() throws Exception {
        doStart(START);
    }

    public void resume() throws Exception {
        doStart(RESUME);
    }

    public void stop() throws Exception {
        // stop the tenant services:
        doStop(STOP);
    }

    public void pause() throws Exception {
        doStop(PAUSE);
    }

    private void doStart(ServiceAction serviceAction) throws Exception {
        LOGGER.debug("Starting services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STOPPED) {
            LOGGER.debug("Tenant services cannot be started, they are {}", tenantServiceState);
            return;
        }
        tenantServiceState = TenantServiceState.STARTING;
        inTenantSession(() -> {
            tenantElementsRestarter.prepareRestartOfElements();
            transactionService.executeInTransaction(() -> doChangeServiceState(serviceAction));
            tenantElementsRestarter.restartElements();
        });
        //FIXME handle state on exception
        tenantServiceState = TenantServiceState.STARTED;
        LOGGER.debug("Services of tenant {} are started.", tenantId);
    }

    private void doStop(ServiceAction serviceAction) throws Exception {
        LOGGER.debug("Stopping services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STARTED) {
            LOGGER.debug("Tenant services cannot be stopped, they are {}", tenantServiceState);
            return;
        }
        tenantServiceState = TenantServiceState.STOPPING;
        transactionService.executeInTransaction(() -> doChangeServiceState(serviceAction));
        tenantServiceState = TenantServiceState.STOPPED;
        LOGGER.debug("Services of tenant {} are stopped.", tenantId);
    }

    private Void doChangeServiceState(ServiceAction action) throws SClassLoaderException, UpdateException {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {

            // Set the right classloader only on start and resume because we destroy it on stop and pause anyway
            if (action == START || action == RESUME) {
                final ClassLoader serverClassLoader = classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(),
                        tenantId);
                Thread.currentThread().setContextClassLoader(serverClassLoader);
            }

            for (final TenantLifecycleService tenantService : tenantConfiguration.getLifecycleServices()) {
                LOGGER.info("{} tenant-level service {} on tenant with ID {}", action,
                        tenantService.getClass().getName(), tenantId);

                try {
                    switch (action) {
                        case START:
                            tenantService.start();
                            break;
                        case STOP:
                            tenantService.stop();
                            break;
                        case PAUSE:
                            tenantService.pause();
                            break;
                        case RESUME:
                            tenantService.resume();
                            break;
                    }
                } catch (final SBonitaException sbe) {
                    throw new UpdateException("Unable to " + action + " service: " + tenantService.getClass().getName(),
                            sbe);
                }
            }
            return null;
        } finally {
            // reset previous class loader:
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    private void inTenantSession(RunnableWithException runnable) throws Exception {
        if (sessionAccessor.isTenantSession()) {
            runnable.run();
        } else { // is a platform session: create a tenant session to run that
            long currentSessionId = sessionAccessor.getSessionId();
            try {
                final long sessionId = createSession(tenantId, sessionService);
                sessionAccessor.deleteSessionId();
                sessionAccessor.setSessionInfo(sessionId, tenantId);
                runnable.run();
                sessionService.deleteSession(sessionId);
            } finally {
                sessionAccessor.setSessionInfo(currentSessionId, -1);
            }
        }
    }
}
