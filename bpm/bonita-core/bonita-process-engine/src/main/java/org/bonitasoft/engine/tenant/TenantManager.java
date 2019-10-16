package org.bonitasoft.engine.tenant;

import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.PAUSE;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.RESUME;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.START;
import static org.bonitasoft.engine.api.impl.transaction.SetServiceState.ServiceAction.STOP;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TenantManager {

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

    public TenantManager(UserTransactionService transactionService, PlatformService platformService,
                         NodeConfiguration nodeConfiguration, SessionService sessionService,
                         SessionAccessor sessionAccessor, @Value("${tenantId}") long tenantId,
                         ClassLoaderService classLoaderService, TenantConfiguration tenantConfiguration,
                         SchedulerService schedulerService, BroadcastService broadcastService) {
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
    }

    public long getTenantId() {
        return tenantId;
    }

    public void stop() throws Exception {
        if (nodeConfiguration.shouldClearSessions()) {
            sessionService.deleteSessions();
        }
        // stop the tenant services:
        logger.info("Stopping tenant {}", tenantId);
        SetServiceState setServiceState = new SetServiceState(tenantId, STOP);
        transactionService.executeInTransaction(() -> setServiceState.changeServiceState(classLoaderService, tenantConfiguration));
        logger.info("Stopped tenant {}", tenantId);
    }

    public void start() throws Exception {
        beforeServicesStartOfRestartHandlersOfTenant();
        startServicesOfTenant();
    }

    public void afterStart() throws Exception {
        if (nodeConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId)).executeAfterServicesStart(tenantRestartHandlers);

        }
    }

    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

    private STenant getTenantInTransaction() throws Exception {
        return transactionService.executeInTransaction(() -> platformService.getTenant(tenantId));
    }

    private void beforeServicesStartOfRestartHandlersOfTenant() throws Exception {
        if (nodeConfiguration.shouldResumeElements()) {
            // Here get all elements that are not "finished"
            // * FlowNodes that have flag: stateExecuting to true: call execute on them (connectors were executing)
            // * Process instances with token count == 0 (either not started again or finishing) -> same thing connectors were executing
            // * transitions that are in state created: call execute on them
            // * flow node that are completed and not deleted : call execute to make it create transitions and so on
            // * all element that are in not stable state
            STenant tenant = getTenantInTransaction();
            if (!tenant.isPaused()) {
                final long tenantId = tenant.getId();
                long sessionId = -1;
                long platformSessionId = -1;
                try {
                    platformSessionId = sessionAccessor.getSessionId();
                    sessionAccessor.deleteSessionId();
                    sessionId = createSessionAndMakeItActive(sessionAccessor, tenantId);
                    transactionService.executeInTransaction(() -> {
                        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
                        return new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId)).executeBeforeServicesStart();
                    });
                } finally {
                    sessionService.deleteSession(sessionId);
                    cleanSessionAccessor(sessionAccessor, platformSessionId);
                }
            }
        }

    }

    private void startServicesOfTenant() throws Exception {
        STenant tenant = getTenantInTransaction();
        if (!tenant.isPaused() && tenant.isActivated()) {
            long sessionId = -1;
            long platformSessionId = -1;
            try {
                platformSessionId = sessionAccessor.getSessionId();
                sessionAccessor.deleteSessionId();
                sessionId = createSessionAndMakeItActive(sessionAccessor, tenantId);
                final SetServiceState startService = new SetServiceState(tenantId, START);
                transactionService.executeInTransaction(() -> startService.changeServiceState(classLoaderService, tenantConfiguration));
            } finally {
                sessionService.deleteSession(sessionId);
                cleanSessionAccessor(sessionAccessor, platformSessionId);
            }
        }
    }

    private long createSessionAndMakeItActive(final SessionAccessor sessionAccessor, final long tenantId) throws Exception {
        final SessionService sessionService = getPlatformAccessor().getTenantServiceAccessor(tenantId).getSessionService();
        final long sessionId = createSession(tenantId, sessionService);
        sessionAccessor.setSessionInfo(sessionId, tenantId);
        return sessionId;
    }

    protected Long createSession(final long tenantId, final SessionService sessionService) throws SBonitaException {
        return sessionService.createSession(tenantId, SessionService.SYSTEM).getId();
    }

    private void cleanSessionAccessor(final SessionAccessor sessionAccessor, final long platformSessionId) {
        if (sessionAccessor != null) {
            sessionAccessor.deleteSessionId();
            if (platformSessionId != -1) {
                sessionAccessor.setSessionInfo(platformSessionId, -1);
            }
        }
    }

    public void pause() throws Exception {
        STenant tenant = platformService.getTenant(tenantId);
        if (!STenant.ACTIVATED.equals(tenant.getStatus())) {
            throw new UpdateException("Can't pause a tenant in state " + tenant.getStatus());
        }
        pauseJobsOfTenant();
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        changeStateOfServices(PAUSE);
        platformService.pauseTenant(tenantId);
    }

    private void pauseJobsOfTenant() throws UpdateException {
        try {
            schedulerService.pauseJobs(tenantId);
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to pause jobs of tenant " + tenantId, e);
        }
    }


    void changeStateOfServices(final SetServiceState.ServiceAction serviceStrategy) throws UpdateException {
        final SetServiceState setServiceState = new SetServiceState(tenantId, serviceStrategy);
        final Map<String, TaskResult<Void>> result;
        try {
            execute(setServiceState);
            result = broadcastService.executeOnOthersAndWait(setServiceState, tenantId);
        } catch (Exception e) {
            throw new UpdateException(e);
        }
        handleResult(result);
    }

    void execute(SetServiceState setServiceState) throws Exception {
        setServiceState.call();
    }

    private void handleResult(final Map<String, TaskResult<Void>> result) throws UpdateException {
        for (final Map.Entry<String, TaskResult<Void>> entry : result.entrySet()) {
            if (entry.getValue().isError()) {
                throw new UpdateException("There is at least one exception on the node " + entry.getKey(), entry.getValue().getThrowable());
            }
            if (entry.getValue().isTimeout()) {
                throw new UpdateException("There is at least one timeout after " + entry.getValue().getTimeout() + " " + entry.getValue().getTimeunit()
                        + " on the node " + entry.getKey());
            }
        }
    }

    public void resume() throws Exception {
        STenant tenant = platformService.getTenant(tenantId);
        if (!STenant.PAUSED.equals(tenant.getStatus())) {
            throw new UpdateException("Can't resume a tenant in state " + tenant.getStatus());
        }
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        TenantRestarter tenantRestarter = new TenantRestarter(platformAccessor, platformAccessor.getTenantServiceAccessor(tenantId));
        List<TenantRestartHandler> tenantRestartHandlers = tenantRestarter.executeBeforeServicesStart();
        schedulerService.resumeJobs(tenantId);

        // on all nodes
        changeStateOfServices(RESUME);

        tenantRestarter.executeAfterServicesStart(tenantRestartHandlers);
        platformService.activateTenant(tenantId);
    }

}
