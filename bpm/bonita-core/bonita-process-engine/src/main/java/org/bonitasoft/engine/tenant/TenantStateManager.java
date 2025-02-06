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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.tenant.TenantServicesManager.ServiceAction;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantStateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantStateManager.class);

    private UserTransactionService transactionService;
    private PlatformService platformService;
    private NodeConfiguration nodeConfiguration;
    private SessionService sessionService;
    private SchedulerService schedulerService;
    private BroadcastService broadcastService;
    private TenantServicesManager tenantServicesManager;
    private long tenantId;

    public TenantStateManager(UserTransactionService transactionService, PlatformService platformService,
            NodeConfiguration nodeConfiguration, SessionService sessionService, @Value("${tenantId}") long tenantId,
            SchedulerService schedulerService, BroadcastService broadcastService,
            TenantServicesManager tenantServicesManager) {
        this.transactionService = transactionService;
        this.platformService = platformService;
        this.nodeConfiguration = nodeConfiguration;
        this.sessionService = sessionService;
        this.tenantId = tenantId;
        this.schedulerService = schedulerService;
        this.broadcastService = broadcastService;
        this.tenantServicesManager = tenantServicesManager;
    }

    /**
     * Stop the platform:
     * - stop services
     * - delete session if its the only node
     * **Called outside of a transaction in a platform-level session**
     */
    public synchronized void stop() throws Exception {
        if (nodeConfiguration.shouldClearSessions()) {
            sessionService.deleteSessions();
        }
        tenantServicesManager.stop();
    }

    /**
     * Start the platform:
     * - start services
     * - resume elements if its the only node
     * **Called outside of a transaction in a platform-level session**
     */
    public synchronized void start() throws Exception {
        tenantServicesManager.initServices();
        final SPlatform platform = getPlatformInTransaction();
        if (!platform.isActivated()) {
            LOGGER.debug("Not starting platform. It is in state {}", platform.getStatus());
            return;
        }
        tenantServicesManager.start();
    }

    protected ServiceAccessor getServiceAccessor()
            throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            ReflectiveOperationException {
        return ServiceAccessorFactory.getInstance().createServiceAccessor();
    }

    private SPlatform getPlatformInTransaction() throws Exception {
        return transactionService.executeInTransaction(() -> platformService.getPlatform());
    }

    /**
     * Pause the platform:
     * - pause services
     * - platform has the status PAUSED in database
     * - other nodes pause the services
     * **Called outside a transaction with a platform-level session**
     */
    public synchronized void pause() throws Exception {
        LOGGER.info("Pausing platform");
        SPlatform platform = getPlatformInTransaction();
        if (!platform.isActivated()) {
            throw new UpdateException("Can't pause platform in state " + platform.getStatus());
        }
        pauseServicesInTransaction();
        pauseSchedulerJobsInTransaction();
        tenantServicesManager.pause();
        pauseServicesOnOtherNodes();
        LOGGER.info("Paused platform");
    }

    /**
     * Resume the platform:
     * - resume services
     * - platform has the status ACTIVATED in database
     * - other nodes resume the services
     * **Called outside a transaction with a platform-level session**
     */
    public synchronized void resume() throws Exception {
        LOGGER.info("Resuming platform");
        SPlatform platform = getPlatformInTransaction();
        if (!platform.isPaused()) {
            throw new UpdateException("Can't resume platform in state " + platform.getStatus());
        }
        activateServicesInTransaction();
        try {
            tenantServicesManager.resume();
        } catch (Exception e) {
            pauseServicesInTransaction();
            throw e;
        }
        resumeServicesOnOtherNodes();
        resumeSchedulerJobsInTransaction();

        LOGGER.info("Resumed platform");
    }

    private void resumeSchedulerJobsInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            schedulerService.resumeJobs();
            return null;
        });
    }

    private void pauseSchedulerJobsInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            schedulerService.pauseJobs();
            return null;
        });
    }

    private void pauseServicesInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.pauseServices();
            return null;
        });
    }

    private void activateServicesInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.activateServices();
            return null;
        });
    }

    private void deactivateServicesInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.deactivateServices();
            return null;
        });
    }

    /**
     * Activate the services:
     * - resume elements
     * - start services
     * - platform has the status ACTIVATED in database
     * - services are started on other nodes
     * **Called outside a transaction in a platform-level session**
     */
    public synchronized void activate() throws Exception {
        LOGGER.info("Activating platform");
        SPlatform platform = getPlatformInTransaction();
        if (!platform.isDeactivated()) {
            throw new SPlatformUpdateException(
                    "Platform activation failed. Platform is not deactivated: current state " + platform.getStatus());
        }
        activateServicesInTransaction();
        tenantServicesManager.start();
        startServicesOnOtherNodes();
        resumeSchedulerJobsInTransaction();
        LOGGER.info("Activated platform");
    }

    private void startServicesOnOtherNodes() {
        executeOnOtherNodes(START);
    }

    private void pauseServicesOnOtherNodes() {
        executeOnOtherNodes(PAUSE);
    }

    private void resumeServicesOnOtherNodes() {
        executeOnOtherNodes(RESUME);
    }

    private void stopServicesOnOtherNodes() {
        executeOnOtherNodes(STOP);
    }

    private void executeOnOtherNodes(ServiceAction action) {
        Map<String, TaskResult<Void>> execute;
        try {
            execute = broadcastService.executeOnOthersAndWait(new ChangesServicesStateCallable(action));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Unable to update services on other nodes", e);
        }
        for (Map.Entry<String, TaskResult<Void>> resultEntry : execute.entrySet()) {
            if (resultEntry.getValue().isError()) {
                throw new IllegalStateException(resultEntry.getValue().getThrowable());
            }
        }
    }

    /**
     * Deactivate the platform:
     * - stop services
     * - platform has the status DEACTIVATED in database
     * - services are stopped on other nodes
     * **Called outside a transaction with a platform-level session**
     */
    public synchronized void deactivate() throws Exception {
        LOGGER.info("Deactivating platform");
        SPlatform platform = getPlatformInTransaction();
        String previousStatus = platform.getStatus();
        if (previousStatus.equals(SPlatform.DEACTIVATED)) {
            throw new STenantDeactivationException("Tenant deactivated failed. Tenant is already deactivated");
        }
        sessionService.deleteAllSessions();
        deactivateServicesInTransaction();
        if (previousStatus.equals(SPlatform.ACTIVATED)) {
            pauseSchedulerJobsInTransaction();
            tenantServicesManager.stop();
            stopServicesOnOtherNodes();
        }
        LOGGER.info("Deactivated platform");
    }

    public synchronized <T> T executeManagementOperation(String operationName, Callable<T> operation)
            throws Exception {
        LOGGER.info("Executing synchronized maintenance operation {}", operationName);
        T operationReturn = operation.call();
        LOGGER.info("Successful synchronized maintenance operation {}", operationName);
        return operationReturn;
    }

    public synchronized String getStatus() throws Exception {
        return getPlatformInTransaction().getStatus();
    }
}
