package org.bonitasoft.engine.platform;

import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.transaction.platform.CheckPlatformVersion;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.tenant.TenantManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handle the lifecycle of the platform: start, stop.
 */
@Component
public class PlatformManager {

    private static Logger logger = LoggerFactory.getLogger(PlatformManager.class);

    private PlatformState state = PlatformState.STOPPED;
    private NodeConfiguration nodeConfiguration;
    private SchedulerService schedulerService;
    private UserTransactionService transactionService;
    private PlatformService platformService;

    public PlatformManager(NodeConfiguration nodeConfiguration, SchedulerService schedulerService, UserTransactionService transactionService, PlatformService platformService) {
        this.nodeConfiguration = nodeConfiguration;
        this.schedulerService = schedulerService;
        this.transactionService = transactionService;
        this.platformService = platformService;
    }

    /**
     * @return the current state of the platform
     */
    public PlatformState getState() {
        return state;
    }

    /**
     * Stop the platform and its tenants
     */
    public synchronized void stop() throws Exception {
        logger.error("stopping platform: ");
        if (state == PlatformState.STOPPED) {
            logger.info("Platform already stopped, nothing to do.");
            return;
        }
        schedulerService.stop();
        List<TenantManager> tenantManagers = getTenantManagers();
        for (TenantManager tenantManager : tenantManagers) {
            tenantManager.stop();
        }
        //does not contain the scheduler
        for (final PlatformLifecycleService serviceWithLifecycle : nodeConfiguration.getLifecycleServices()) {
            logger.info("Stop service of platform: {}", serviceWithLifecycle.getClass().getName());
            serviceWithLifecycle.stop();
        }
        state = PlatformState.STOPPED;
    }

    /**
     * Start the platform and its tenants
     */
    public synchronized void start() throws Exception {
        if (state == PlatformState.STARTED) {
            return;
        }
        checkPlatformVersion();
        startPlatformServices();
        List<TenantManager> tenantManagers = getTenantManagers();

        for (TenantManager tenantManager : tenantManagers) {
            tenantManager.start();
        }
        startScheduler();
        try {

            restartHandlersOfPlatform();
            state = PlatformState.STARTED;
            for (TenantManager tenantManager : tenantManagers) {
                tenantManager.afterStart();
            }
        } catch (Exception e) {
            //FIXME should we also stop tenants/services of platform, state of platform to UNHEALTHY

            // If an exception is thrown, stop the platform that was started.
            try {
                schedulerService.stop();
            } catch (final Exception exp) {
                throw new StartNodeException("Platform stopping failed : " + exp.getMessage(), e);
            }
            throw e;
        }
    }

    private List<TenantManager> getTenantManagers() throws Exception {
        List<STenant> sTenants = transactionService.executeInTransaction(() -> platformService.getTenants(QueryOptions.ALL_RESULTS));
        return sTenants.stream()
                .map(t ->
                        TenantServiceSingleton.getInstance(t.getId()).getTenantManager()
                ).collect(Collectors.toList());
    }


    private void restartHandlersOfPlatform() throws Exception {
        for (final RestartHandler restartHandler : nodeConfiguration.getRestartHandlers()) {

            transactionService.executeInTransaction(() -> {
                restartHandler.execute();
                return null;
            });
        }
    }

    private void startScheduler() throws SBonitaException {
        if (!schedulerService.isStarted()) {
            schedulerService.initializeScheduler();
            schedulerService.start();
        }
    }

    private void checkPlatformVersion() throws Exception {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        if (!transactionService.executeInTransaction(checkPlatformVersion)) {
            throw new StartNodeException(checkPlatformVersion.getErrorMessage());
        }
    }


    private void startPlatformServices() throws SBonitaException {
        final List<PlatformLifecycleService> servicesToStart = nodeConfiguration.getLifecycleServices();
        for (final PlatformLifecycleService serviceWithLifecycle : servicesToStart) {
            logger.info("Start service of platform : {}", serviceWithLifecycle.getClass().getName());
            // scheduler might be already running
            // skip service start
            if (!serviceWithLifecycle.getClass().isInstance(schedulerService) || !schedulerService.isStarted()) {
                serviceWithLifecycle.start();
            }
        }
    }


}
