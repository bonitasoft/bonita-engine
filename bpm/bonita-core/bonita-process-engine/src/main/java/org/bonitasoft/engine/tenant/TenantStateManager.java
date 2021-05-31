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
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
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

    public long getTenantId() {
        return tenantId;
    }

    /**
     * Stop the tenant:
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
     * Start the tenant:
     * - start services
     * - resume elements if its the only node
     * **Called outside of a transaction in a platform-level session**
     */
    public synchronized void start() throws Exception {
        STenant tenant = getTenantInTransaction();
        tenantServicesManager.initServices();
        if (!tenant.isActivated()) {
            LOGGER.debug("Not starting tenant {}. It is in state {}", tenantId, tenant.getStatus());
            return;
        }
        tenantServicesManager.start();
    }

    protected PlatformServiceAccessor getPlatformAccessor()
            throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    private STenant getTenantInTransaction() throws Exception {
        return transactionService.executeInTransaction(() -> platformService.getTenant(tenantId));
    }

    /**
     * Pause the tenant:
     * - pause services
     * - tenant has the status PAUSED in database
     * - other nodes pause the services
     * **Called outside a transaction with a tenant-level session**
     */
    public synchronized void pause() throws Exception {
        LOGGER.info("Pausing tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isActivated()) {
            throw new UpdateException("Can't pause a tenant in state " + tenant.getStatus());
        }
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        pauseTenantInTransaction();
        pauseSchedulerJobsInTransaction(tenantId);
        tenantServicesManager.pause();
        pauseServicesOnOtherNodes();
        LOGGER.info("Paused tenant {}", tenantId);
    }

    /**
     * Resume the tenant:
     * - resume services
     * - tenant has the status ACTIVATED in database
     * - other nodes resume the services
     * **Called outside a transaction with a tenant-level session**
     */
    public synchronized void resume() throws Exception {
        LOGGER.info("Resuming tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isPaused()) {
            throw new UpdateException("Can't resume a tenant in state " + tenant.getStatus());
        }
        activateTenantInTransaction();
        try {
            tenantServicesManager.resume();
        } catch (Exception e) {
            pauseTenantInTransaction();
            throw e;
        }
        resumeServicesOnOtherNodes();
        resumeSchedulerJobsInTransaction(tenantId);

        LOGGER.info("Resumed tenant {}", tenantId);
    }

    private void resumeSchedulerJobsInTransaction(long tenantId) throws Exception {
        transactionService.executeInTransaction(() -> {
            schedulerService.resumeJobs(tenantId);
            return null;
        });
    }

    private void pauseSchedulerJobsInTransaction(long tenantId) throws Exception {
        transactionService.executeInTransaction(() -> {
            schedulerService.pauseJobs(tenantId);
            return null;
        });
    }

    private void pauseTenantInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.pauseTenant(tenantId);
            return null;
        });
    }

    private void activateTenantInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.activateTenant(tenantId);
            return null;
        });
    }

    private void deactivateTenantInTransaction() throws Exception {
        transactionService.executeInTransaction(() -> {
            platformService.deactivateTenant(tenantId);
            return null;
        });
    }

    /**
     * Activate the tenant:
     * - resume elements
     * - start services
     * - tenant has the status ACTIVATED in database
     * - services are started on other nodes
     * **Called outside a transaction in a platform-level session**
     */
    public synchronized void activate() throws Exception {
        LOGGER.info("Activating tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isDeactivated()) {
            throw new STenantActivationException(
                    "Tenant activation failed. Tenant is not deactivated: current state " + tenant.getStatus());
        }
        activateTenantInTransaction();
        tenantServicesManager.start();
        startServicesOnOtherNodes();
        resumeSchedulerJobsInTransaction(tenantId);
        LOGGER.info("Activated tenant {}", tenantId);
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
            execute = broadcastService.executeOnOthersAndWait(new ChangesServicesStateCallable(action, tenantId),
                    tenantId);
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
     * Deactivate the tenant:
     * - stop services
     * - tenant has the status DEACTIVATED in database
     * - services are stopped on other nodes
     * **Called outside a transaction with a platform-level session**
     */
    public synchronized void deactivate() throws Exception {
        LOGGER.info("Deactivating tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        String previousStatus = tenant.getStatus();
        if (previousStatus.equals(STenant.DEACTIVATED)) {
            throw new STenantDeactivationException("Tenant deactivated failed. Tenant is already deactivated");
        }
        sessionService.deleteSessionsOfTenant(tenantId);
        deactivateTenantInTransaction();
        if (previousStatus.equals(STenant.ACTIVATED)) {
            pauseSchedulerJobsInTransaction(tenantId);
            tenantServicesManager.stop();
            stopServicesOnOtherNodes();
        }
        LOGGER.info("Deactivated tenant {}", tenantId);
    }

    public synchronized <T> T executeTenantManagementOperation(String operationName, Callable<T> operation)
            throws Exception {
        LOGGER.info("Executing synchronized tenant maintenance operation {} for tenant {}", operationName, tenantId);
        T operationReturn = operation.call();
        LOGGER.info("Successful synchronized tenant maintenance operation {} for tenant {}", operationName, tenantId);
        return operationReturn;
    }
}
