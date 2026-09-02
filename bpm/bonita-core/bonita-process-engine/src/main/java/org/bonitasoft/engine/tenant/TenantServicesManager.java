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
import static org.bonitasoft.engine.tenant.TenantServicesManager.ServiceAction.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.classloader.ClassLoaderIdentifier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SLifecycleException;
import org.bonitasoft.engine.service.RunnableWithException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final List<TenantLifecycleService> services;
    private final TenantElementsRestarter tenantElementsRestarter;
    private TenantServiceState tenantServiceState = TenantServiceState.STOPPED;

    public TenantServicesManager(SessionAccessor sessionAccessor, SessionService sessionService,
            TransactionService transactionService, ClassLoaderService classLoaderService,
            List<TenantLifecycleService> services, TenantElementsRestarter tenantElementsRestarter) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.transactionService = transactionService;
        this.classLoaderService = classLoaderService;
        this.services = services;
        this.tenantElementsRestarter = tenantElementsRestarter;
    }

    public boolean isStarted() {
        return tenantServiceState == TenantServiceState.STARTED;
    }

    private void updateState(TenantServiceState tenantServiceState) {
        LOGGER.debug("Services state updated to {}", tenantServiceState);
        this.tenantServiceState = tenantServiceState;
    }

    public void start() throws Exception {
        doStart(START);
    }

    public void resume() throws Exception {
        doStart(RESUME);
    }

    public void stop() throws Exception {
        // stop the services:
        doStop(STOP);
    }

    public void pause() throws Exception {
        doStop(PAUSE);
    }

    public void initServices() throws Exception {
        inSessionTransaction(() -> {
            for (TenantLifecycleService tenantService : services) {
                LOGGER.info("Initializing service {}", tenantService.getClass().getName());
                tenantService.init();
            }
            return null;
        });
    }

    private void doStart(ServiceAction startAction) throws Exception {
        LOGGER.debug("Starting services");
        if (tenantServiceState != TenantServiceState.STOPPED) {
            LOGGER.debug("Services cannot be started, they are {}", tenantServiceState);
            return;
        }
        updateState(TenantServiceState.STARTING);
        try {
            inSession(() -> {
                tenantElementsRestarter.prepareRestartOfElements();
                transactionService.executeInTransaction((Callable<Void>) () -> {
                    executeInClassloader(() -> startServices(startAction));
                    return null;
                });
            });
        } catch (Exception e) {
            abortStart(startAction, e);
            throw new SLifecycleException(
                    "Unable to " + startAction + " a service. All services are kept STOPPED. Error: " + e.getMessage(),
                    e);
        }
        updateState(TenantServiceState.STARTED);
        inSession(tenantElementsRestarter::restartElements);
        LOGGER.debug("Services are started.");
    }

    private void startServices(ServiceAction startAction) throws SLifecycleException {

        for (TenantLifecycleService tenantService : services) {
            try {
                LOGGER.info("{} service {}", startAction, tenantService.getClass().getName());
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
            LOGGER.info("Stopping services after a failed {}...", startAction);
            doStop(stopAction);
        } catch (Exception exceptionOnStop) {
            LOGGER.warn("Unable to {} services to recover from exception when executing {} because {}: {}",
                    stopAction, startAction, e.getClass().getName(), e.getMessage());
            LOGGER.debug("Caused by: ", exceptionOnStop);
        }
    }

    private void doStop(ServiceAction stopAction) throws Exception {
        LOGGER.debug("Stopping services");
        if (tenantServiceState != TenantServiceState.STARTED
                && tenantServiceState != TenantServiceState.ABORTING_START) {
            LOGGER.debug("Services cannot be stopped, they are {}", tenantServiceState);
            return;
        }
        updateState(TenantServiceState.STOPPING);
        List<TenantLifecycleService> list = new ArrayList<>(services);
        Collections.reverse(list);
        Optional<Exception> firstIssue = transactionService
                .executeInTransaction(
                        () -> list.stream()
                                .map(tenantService -> {
                                    LOGGER.info("{} service {}", stopAction, tenantService.getClass().getName());
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
        LOGGER.debug("Services are stopped.");
        if (firstIssue.isPresent()) {
            throw new SLifecycleException("Unable to stop some services", firstIssue.get());
        }
    }

    private void executeInClassloader(RunnableWithException runnable) throws Exception {
        final ClassLoader baseClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Set the right classloader only on start and resume because we destroy it on stop and pause anyway
            final ClassLoader serverClassLoader = classLoaderService.getClassLoader(
                    ClassLoaderIdentifier.TENANT);
            Thread.currentThread().setContextClassLoader(serverClassLoader);
            runnable.run();
        } finally {
            // reset previous class loader:
            Thread.currentThread().setContextClassLoader(baseClassLoader);
        }
    }

    protected Long createSession(final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(SessionService.SYSTEM).getId();
    }

    private void inSession(RunnableWithException runnable) throws Exception {
        long currentSessionId;
        try {
            currentSessionId = sessionAccessor.getSessionId();
        } catch (SessionIdNotSetException e) {
            runnable.run();
            return;
        }
        try {
            final long sessionId = createSession(sessionService);
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionId(sessionId);
            runnable.run();
            sessionService.deleteSession(sessionId);
        } finally {
            sessionAccessor.setSessionId(currentSessionId);
        }
    }

    public <T> void inSessionTransaction(final Callable<T> callable) throws Exception {
        inSession(() -> transactionService.executeInTransaction(callable));
    }
}
