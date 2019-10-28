package org.bonitasoft.engine.tenant;

import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
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
    public void stop() throws Exception {
        if (nodeConfiguration.shouldClearSessions()) {
            sessionService.deleteSessions();
        }
        tenantServicesManager.stop();
    }

    /**
     * Stop the tenant:
     * - start services
     * - resume elements if its the only node
     * **Called outside of a transaction in a platform-level session**
     */
    public void start() throws Exception {
        STenant tenant = getTenantInTransaction();
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
    public void pause() throws Exception {
        LOGGER.info("Pausing tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isActivated()) {
            throw new UpdateException("Can't pause a tenant in state " + tenant.getStatus());
        }
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        pauseTenantInTransaction();
        schedulerService.pauseJobs(tenantId);
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
    public void resume() throws Exception {
        LOGGER.info("Resuming tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isPaused()) {
            throw new UpdateException("Can't resume a tenant in state " + tenant.getStatus());
        }
        activateTenantInTransaction();
        tenantServicesManager.resume();
        resumeServicesOnOtherNodes();
        schedulerService.resumeJobs(tenantId);

        LOGGER.info("Resumed tenant {}", tenantId);
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
    public void activate() throws Exception {
        LOGGER.info("Activating tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        if (!tenant.isDeactivated()) {
            throw new STenantActivationException(
                    "Tenant activation failed. Tenant is not deactivated: current state " + tenant.getStatus());
        }
        activateTenantInTransaction();
        tenantServicesManager.start();
        startServicesOnOtherNodes();
        schedulerService.resumeJobs(tenantId);
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
    public void deactivate() throws Exception {
        LOGGER.info("Deactivating tenant {}", tenantId);
        STenant tenant = getTenantInTransaction();
        String previousStatus = tenant.getStatus();
        if (previousStatus.equals(STenant.DEACTIVATED)) {
            throw new STenantDeactivationException("Tenant deactivated failed. Tenant is already deactivated");
        }
        sessionService.deleteSessionsOfTenant(tenantId);
        deactivateTenantInTransaction();
        if (previousStatus.equals(STenant.ACTIVATED)) {
            schedulerService.pauseJobs(tenantId);
            tenantServicesManager.stop();
            stopServicesOnOtherNodes();
        }
        LOGGER.info("Deactivated tenant {}", tenantId);
    }

}
