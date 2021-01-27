/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import static java.text.MessageFormat.format;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.tenant.TenantServicesManager.ServiceAction.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SLifecycleException;
import org.bonitasoft.engine.dependency.model.ScopeType;
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
        STOPPED, STARTING, STARTED, STOPPING, ABORTING_START
    }

    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;
    private final TransactionService transactionService;
    private final ClassLoaderService classLoaderService;
    private List<TenantLifecycleService> services;
    private final Long tenantId;
    private final TenantElementsRestarter tenantElementsRestarter;
    private TenantServiceState tenantServiceState = TenantServiceState.STOPPED;

    public TenantServicesManager(SessionAccessor sessionAccessor, SessionService sessionService,
            TransactionService transactionService, ClassLoaderService classLoaderService,
            List<TenantLifecycleService> services, @Value("${tenantId}") Long tenantId,
            TenantElementsRestarter tenantElementsRestarter) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        this.classLoaderService = classLoaderService;
        this.services = services;
        this.tenantId = tenantId;
        this.tenantElementsRestarter = tenantElementsRestarter;
    }

    public boolean isStarted() {
        return tenantServiceState == TenantServiceState.STARTED;
    }

    private void updateState(TenantServiceState tenantServiceState) {
        LOGGER.debug("Tenant services state updated to {}", tenantServiceState);
        this.tenantServiceState = tenantServiceState;
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

    private void doStart(ServiceAction startAction) throws Exception {
        LOGGER.debug("Starting services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STOPPED) {
            LOGGER.debug("Tenant services cannot be started, they are {}", tenantServiceState);
            return;
        }
        updateState(TenantServiceState.STARTING);
        try {
            inTenantSession(() -> {
                tenantElementsRestarter.prepareRestartOfElements();
                transactionService.executeInTransaction((Callable<Void>) () -> {
                    executeInClassloader(() -> startServices(startAction));
                    return null;
                });
            });
        } catch (Exception e) {
            abortStart(startAction, e);
            throw new SLifecycleException(
                    "Unable to " + startAction + " a service. All services are STOPPED again. Error: " + e.getMessage(),
                    e);
        }
        updateState(TenantServiceState.STARTED);
        inTenantSession(tenantElementsRestarter::restartElements);
        LOGGER.debug("Services of tenant {} are started.", tenantId);
    }

    private void startServices(ServiceAction startAction) throws SLifecycleException {

        for (TenantLifecycleService tenantService : services) {
            try {
                LOGGER.info("{} tenant-level service {} on tenant with ID {}", startAction,
                        tenantService.getClass().getName(), tenantId);
                if (startAction == RESUME) {
                    tenantService.resume();
                } else {
                    tenantService.start();
                }
            } catch (Exception e) {
                LOGGER.error("Error while executing the {} of the service {}", startAction,
                        tenantService.getClass().getName());
                throw new SLifecycleException(
                        format("Error while executing the {0} of the service {1}: {2}", startAction,
                                transactionService.getClass().getName(), e.getMessage()),
                        e);
            }
        }
    }

    private void abortStart(ServiceAction startAction, Exception e) {
        updateState(TenantServiceState.ABORTING_START);
        ServiceAction stopAction = startAction == START ? STOP : PAUSE;
        try {
            LOGGER.info("Stopping tenant services after a failed {}...", startAction);
            doStop(stopAction);
        } catch (Exception exceptionOnStop) {
            LOGGER.warn("Unable to {} tenant services to recover from exception when executing {} because {}: {}",
                    stopAction, startAction, e.getClass().getName(), e.getMessage());
            LOGGER.debug("Caused by: ", exceptionOnStop);
        }
    }

    private void doStop(ServiceAction stopAction) throws Exception {
        LOGGER.debug("Stopping services of tenant {}", tenantId);
        if (tenantServiceState != TenantServiceState.STARTED
                && tenantServiceState != TenantServiceState.ABORTING_START) {
            LOGGER.debug("Tenant services cannot be stopped, they are {}", tenantServiceState);
            return;
        }
        updateState(TenantServiceState.STOPPING);
        List<TenantLifecycleService> list = new ArrayList<>(services);
        Collections.reverse(list);
        Optional<Exception> firstIssue = transactionService
                .executeInTransaction(
                        () -> list.stream()
                                .map(tenantService -> {
                                    LOGGER.info("{} tenant-level service {} on tenant with ID {}", stopAction,
                                            tenantService.getClass().getName(), tenantId);
                                    try {
                                        if (stopAction == PAUSE) {
                                            tenantService.pause();
                                        } else {
                                            tenantService.stop();
                                        }
                                    } catch (final Exception e) {
                                        LOGGER.error("Error executing the {} of the service {} because: {} {}",
                                                stopAction, tenantService.getClass().getName(), e.getClass().getName(),
                                                e.getMessage());
                                        LOGGER.debug("Cause", e);
                                        return e;
                                    }
                                    return null;
                                }).filter(Objects::nonNull).findFirst());
        updateState(TenantServiceState.STOPPED);
        LOGGER.debug("Services of tenant {} are stopped.", tenantId);
        if (firstIssue.isPresent()) {
            throw new SLifecycleException("Unable to stop some services", firstIssue.get());
        }
    }

    private void executeInClassloader(RunnableWithException runnable) throws Exception {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set the right classloader only on start and resume because we destroy it on stop and pause anyway
            final ClassLoader serverClassLoader = classLoaderService.getLocalClassLoader(
                    identifier(ScopeType.TENANT, tenantId));
            Thread.currentThread().setContextClassLoader(serverClassLoader);
            runnable.run();
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
