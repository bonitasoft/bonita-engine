package org.bonitasoft.engine.platform;

import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.transaction.platform.CheckPlatformVersion;
import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.RestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.STenant;
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
    private UserTransactionService transactionService;
    private PlatformService platformService;
    private List<PlatformLifecycleService> platformServices;

    public PlatformManager(NodeConfiguration nodeConfiguration,
                           UserTransactionService transactionService,
                           PlatformService platformService,
                           List<PlatformLifecycleService> platformServices) {
        this.nodeConfiguration = nodeConfiguration;
        this.transactionService = transactionService;
        this.platformService = platformService;
        this.platformServices = platformServices;
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
        logger.info("Stopping platform:");
        if (state == PlatformState.STOPPED) {
            logger.info("Platform already stopped, nothing to do.");
            return;
        }
        List<TenantManager> tenantManagers = getTenantManagers();
        for (TenantManager tenantManager : tenantManagers) {
            tenantManager.stop();
        }
        for (final PlatformLifecycleService platformService : platformServices) {
            logger.info("Stop service of platform: {}", platformService);
            platformService.stop();
        }
        state = PlatformState.STOPPED;
        logger.info("Platform stopped.");
    }

    /**
     * Start the platform and its tenants
     */
    public synchronized void start() throws Exception {
        logger.info("Starting platform:");
        if (state == PlatformState.STARTED) {
            logger.info("Platform already started.");
            return;
        }
        checkPlatformVersion();
        startPlatformServices();
        List<TenantManager> tenantManagers = getTenantManagers();

        for (TenantManager tenantManager : tenantManagers) {
            tenantManager.start();
        }
        restartHandlersOfPlatform();
        state = PlatformState.STARTED;
        logger.info("Platform started.");
    }

    TenantManager getTenantManager(STenant tenant) {
        return TenantServiceSingleton.getInstance(tenant.getId()).getTenantManager();
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

    private void checkPlatformVersion() throws Exception {
        final CheckPlatformVersion checkPlatformVersion = new CheckPlatformVersion(platformService);
        if (!transactionService.executeInTransaction(checkPlatformVersion)) {
            throw new StartNodeException(checkPlatformVersion.getErrorMessage());
        }
    }


    private void startPlatformServices() throws SBonitaException {
        for (final PlatformLifecycleService platformService : platformServices) {
            logger.info("Start service of platform : {}", platformService);
            platformService.start();
        }
    }


    public void activateTenant(long tenantId) throws Exception {
        STenant tenant = platformService.getTenant(tenantId);
        if (!STenant.DEACTIVATED.equals(tenant.getStatus())) {
            throw new STenantActivationException("Tenant activation failed. Tenant is not deactivated: current state " + tenant.getStatus());
        }
        getTenantManager(tenant).activate();
    }

    public void deactivateTenant(long tenantId) throws Exception {
        final STenant tenant = platformService.getTenant(tenantId);
        if (STenant.DEACTIVATED.equals(tenant.getStatus())) {
            throw new STenantDeactivationException("Tenant deactivation failed. Tenant is already deactivated");
        }
        getTenantManager(tenant).deactivate();
    }

}
