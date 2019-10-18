package org.bonitasoft.engine.tenant;

import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.PAUSE;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.RESUME;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.START;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.STOP;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.execution.work.TenantRestarter;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantManager {

    public enum State {STOPPED, STARTING, STARTED, STOPPING}

    private static Logger logger = LoggerFactory.getLogger(TenantManager.class);
    private UserTransactionService transactionService;
    private PlatformService platformService;
    private NodeConfiguration nodeConfiguration;
    private SessionService sessionService;
    private SessionAccessor sessionAccessor;
    private long tenantId;
    private ClassLoaderService classLoaderService;
    private TenantConfiguration tenantConfiguration;
    private SchedulerService schedulerService;
    private BroadcastService broadcastService;
    private PlatformManager platformManager;

    private State state = State.STOPPED;

    public TenantManager(UserTransactionService transactionService, PlatformService platformService,
                         NodeConfiguration nodeConfiguration, SessionService sessionService,
                         SessionAccessor sessionAccessor, @Value("${tenantId}") long tenantId,
                         ClassLoaderService classLoaderService, TenantConfiguration tenantConfiguration,
                         SchedulerService schedulerService, BroadcastService broadcastService,
                         PlatformManager platformManager) {
        this.transactionService = transactionService;
        this.platformService = platformService;
        this.nodeConfiguration = nodeConfiguration;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.tenantId = tenantId;
        this.classLoaderService = classLoaderService;
        this.tenantConfiguration = tenantConfiguration;
        this.schedulerService = schedulerService;
        this.broadcastService = broadcastService;
        this.platformManager = platformManager;
    }

    public long getTenantId() {
        return tenantId;
    }

    /**
     *
     * Stop the tenant:
     *  - stop services
     *  - delete session if its the only node
     *
     *  **Called outside of a transaction**
     */
    public void stop() throws Exception {
        logger.info("Stopping tenant {}", tenantId);
        if (!state.equals(State.STARTED)) {
            logger.info("Tenant {} already stopped.", tenantId);
            return;
        }
        if (nodeConfiguration.shouldClearSessions()) {
            sessionService.deleteSessions();
        }
        // stop the tenant services:
        inTenantSession(this::stopServices);
        logger.info("Stopped tenant {}", tenantId);
        state = State.STOPPED;
    }

    /**
     *
     * Stop the tenant:
     *  - start services
     *  - resume elements if its the only node
     *
     *  **Called outside of a transaction**
     */
    public void start() throws Exception {
        logger.info("Starting tenant {}", tenantId);
        if (!state.equals(State.STOPPED)) {
            logger.info("Tenant {} already started.", tenantId);
            return;
        }
        state = State.STARTING;
        STenant tenant = getTenantInTransaction();
        if (!tenant.isActivated()) {
            logger.debug("Not starting tenant {}. It is in state {}", tenantId, tenant.getStatus());
            return;
        }
        inTenantSession(() -> {
            boolean shouldResumeElements = nodeConfiguration.shouldResumeElements();
            if (shouldResumeElements) {
                prepareResumeOfElements();
            }
            startServices();
            if (shouldResumeElements) {
                resumeElements();
            }
            //resume job in case we activated the tenant on a stopped platform
            schedulerService.resumeJobs(tenantId);
        });
        logger.info("Started tenant {}", tenantId);
        state = State.STARTED;
    }

    // Here get all elements that are not "finished"
    // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
    // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
    // * transitions that are in state created: call execute on them
    // * flow node that are completed and not deleted : call execute to make it create transitions and so on
    // * all element that are in not stable state
    private void resumeElements() throws Exception {
        List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId)).executeAfterServicesStart(tenantRestartHandlers);
    }

    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    private STenant getTenantInTransaction() throws Exception {
        return transactionService.executeInTransaction(() -> platformService.getTenant(tenantId));
    }

    private void prepareResumeOfElements() throws Exception {
        // Here get all elements that are not "finished"
        // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
        // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
        // * transitions that are in state created: call execute on them
        // * flow node that are completed and not deleted : call execute to make it create transitions and so on
        // * all element that are in not stable state
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId)).executeBeforeServicesStart();
    }

    private void startServices() throws Exception {
        transactionService.executeInTransaction(() -> new SetServiceState(tenantId, START).changeServiceState(classLoaderService, tenantConfiguration));
    }
    private void startServicesInTx() throws Exception {
        new SetServiceState(tenantId, START).changeServiceState(classLoaderService, tenantConfiguration);
    }

    private void resumeServices() throws Exception {
        new SetServiceState(tenantId, RESUME).changeServiceState(classLoaderService, tenantConfiguration);
        schedulerService.resumeJobs(tenantId);
    }

    private void pauseServices() throws Exception {
        schedulerService.pauseJobs(tenantId);
        new SetServiceState(tenantId, PAUSE).changeServiceState(classLoaderService, tenantConfiguration);
    }

    private void stopServices() throws Exception {
        transactionService.executeInTransaction(() -> new SetServiceState(tenantId, STOP).changeServiceState(classLoaderService, tenantConfiguration));
    }
    private void stopServicesInTx() throws Exception {
        new SetServiceState(tenantId, STOP).changeServiceState(classLoaderService, tenantConfiguration);
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    /**
     *
     * Pause the tenant:
     *  - pause services
     *  - tenant has the status PAUSED in database
     *  - other nodes pause the services
     *
     *  **Called in a transaction**
     */
    public void pause() throws Exception {
        logger.info("Pausing tenant {}", tenantId);
        STenant tenant = platformService.getTenant(tenantId);
        if (!tenant.isActivated()) {
            throw new UpdateException("Can't pause a tenant in state " + tenant.getStatus());
        }
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        pauseServices();
        pauseServicesOnOtherNodes();
        platformService.pauseTenant(tenantId);
        logger.info("Paused tenant {}", tenantId);
    }

    /**
     *
     * Resume the tenant:
     *  - resume services
     *  - tenant has the status ACTIVATED in database
     *  - other nodes resume the services
     *
     *  **Called in a transaction**
     */
    public void resume() throws Exception {
        logger.info("Resuming tenant {}", tenantId);
        STenant tenant = platformService.getTenant(tenantId);
        if (!tenant.isPaused()) {
            throw new UpdateException("Can't resume a tenant in state " + tenant.getStatus());
        }
        prepareResumeOfElements();
        resumeServices();
        resumeElements();
        resumeServicesOnOtherNodes();
        platformService.activateTenant(tenantId);
        logger.info("Resumed tenant {}", tenantId);
    }

    /**
     * Activate the tenant:
     *  - resume elements
     *  - start services
     *  - tenant has the status ACTIVATED in database
     *  - services are started on other nodes
     *
     *  **Called in a transaction**
     */
    public void activate() throws Exception {
        logger.info("Activating tenant {}", tenantId);
        STenant tenant = platformService.getTenant(tenantId);
        if (!tenant.isDeactivated()) {
            throw new STenantActivationException("Tenant activation failed. Tenant is not deactivated: current state " + tenant.getStatus());
        }
        platformService.activateTenant(tenantId);
        if (platformManager.getState() == PlatformState.STARTED) {
            inTenantSession(() -> {
                prepareResumeOfElements();
                startServicesInTx();
                resumeElements();
                startServicesOnOtherNodes();
                schedulerService.resumeJobs(tenantId);
            });
            state = State.STARTED;
            logger.info("Activated tenant {}", tenantId);
        } else {
            //not starting elements, node is not started
            return;
        }
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

    private void executeOnOtherNodes(SetServiceState.ServiceAction action) {
        if (transactionService.isTransactionActive()) {
            try {
                transactionService.registerBonitaSynchronization((txState -> doExecuteOnOhterNodes(action)));
            } catch (STransactionNotFoundException e) {
                throw new IllegalStateException("Unable to register synchronization to notify other node of services state change", e);
            }
        } else {
            doExecuteOnOhterNodes(action);
        }
    }

    private void doExecuteOnOhterNodes(SetServiceState.ServiceAction action) {
        Map<String, TaskResult<Void>> execute;
        try {
            //FIXME set the status of tenant manager on other NODES!!!
            execute = broadcastService.executeOnOthersAndWait(new SetServiceState(tenantId, action), tenantId);
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
     *  - stop services
     *  - tenant has the status DEACTIVATED in database
     *  - services are stopped on other nodes
     *
     *  **Called in a transaction**
     */
    public void deactivate() throws Exception {
        logger.info("Deactivating tenant {}", tenantId);
        state = State.STOPPING;
        STenant tenant = platformService.getTenant(tenantId);
        String previousStatus = tenant.getStatus();
        if (previousStatus.equals(STenant.DEACTIVATED)) {
            throw new STenantDeactivationException("Tenant deactivated failed. Tenant is already deactivated");
        }
        platformService.deactivateTenant(tenantId);
        if (previousStatus.equals(STenant.ACTIVATED)) {
            inTenantSession(this::stopServicesInTx);
            stopServicesOnOtherNodes();
            schedulerService.pauseJobs(tenantId);
        }
        state = State.STOPPED;
        logger.info("Deactivated tenant {}", tenantId);
    }

    public boolean isStarted() {
        return state == State.STARTED;
    }

    private void inTenantSession(RunnableWithException runnable) throws Exception {
        long platformSessionId = sessionAccessor.getSessionId();
        try {

            final long sessionId = createSession(tenantId, sessionService);
            sessionAccessor.deleteSessionId();
            sessionAccessor.setSessionInfo(sessionId, tenantId);
            runnable.run();
            sessionService.deleteSession(sessionId);
            sessionService.deleteSessionsOfTenant(tenantId);
        } finally {
            sessionAccessor.setSessionInfo(platformSessionId, -1);
        }
    }

    private interface RunnableWithException {

        void run() throws Exception;

    }
}
