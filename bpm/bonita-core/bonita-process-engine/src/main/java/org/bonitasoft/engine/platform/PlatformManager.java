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
package org.bonitasoft.engine.platform;

import java.util.List;

import org.bonitasoft.engine.commons.PlatformLifecycleService;
import org.bonitasoft.engine.commons.PlatformRestartHandler;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.exception.STenantActivationException;
import org.bonitasoft.engine.platform.exception.STenantDeactivationException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handle the lifecycle of the platform: start, stop.
 */
@Component
public class PlatformManager {

    private static final Logger logger = LoggerFactory.getLogger(PlatformManager.class);
    private final BonitaTaskExecutor bonitaTaskExecutor;
    private final NodeConfiguration nodeConfiguration;
    private final UserTransactionService transactionService;
    private final PlatformService platformService;
    private final List<PlatformLifecycleService> platformServices;
    private final PlatformStateProvider platformStateProvider;
    private final PlatformVersionChecker platformVersionChecker;

    public PlatformManager(NodeConfiguration nodeConfiguration,
            UserTransactionService transactionService,
            PlatformService platformService,
            List<PlatformLifecycleService> platformServices,
            PlatformStateProvider platformStateProvider,
            BonitaTaskExecutor bonitaTaskExecutor, PlatformVersionChecker platformVersionChecker) {
        this.nodeConfiguration = nodeConfiguration;
        this.transactionService = transactionService;
        this.platformService = platformService;
        this.platformServices = platformServices;
        this.platformStateProvider = platformStateProvider;
        this.bonitaTaskExecutor = bonitaTaskExecutor;
        this.platformVersionChecker = platformVersionChecker;
    }

    /**
     * @return the current state of the platform
     */
    public PlatformState getState() {
        return platformStateProvider.getState();
    }

    /**
     * Stop the platform and default tenant
     *
     * @return true if the node was stopped, false if it was not stoppable (already stopped, starting or stopping)
     */
    public synchronized boolean stop() throws Exception {
        logger.info("Stopping platform:");
        if (!platformStateProvider.initializeStop()) {
            return false;
        }
        getDefaultTenantStateManager().stop();
        for (final PlatformLifecycleService platformService : platformServices) {
            logger.info("Stop service of platform: {}", platformService);
            platformService.stop();
        }
        platformStateProvider.setStopped();
        logger.info("Platform stopped.");
        return true;
    }

    /**
     * Start the platform and default tenant
     *
     * @return true if the node was started, false if it was not startable (already started, starting or stopping)
     */
    public synchronized boolean start() throws Exception {
        logger.info("Starting platform:");
        if (!platformStateProvider.initializeStart()) {
            logger.info("Platform cannot be started, it is: {}", platformStateProvider.getState());
            return false;
        }
        checkPlatformVersion();
        startPlatformServices();
        platformStateProvider.setStarted();

        getDefaultTenantStateManager().start();

        restartHandlersOfPlatform();
        logger.info("Platform started.");
        return true;
    }

    TenantStateManager getDefaultTenantStateManager() {
        return ServiceAccessorSingleton.getInstance().getTenantStateManager();
    }

    private void restartHandlersOfPlatform() {
        for (final PlatformRestartHandler platformRestartHandler : nodeConfiguration.getPlatformRestartHandlers()) {
            bonitaTaskExecutor.execute(platformRestartHandler::execute);
        }
    }

    private void checkPlatformVersion() throws Exception {
        if (!platformVersionChecker.verifyPlatformVersion()) {
            throw new StartNodeException(platformVersionChecker.getErrorMessage());
        }
    }

    private void startPlatformServices() throws SBonitaException {
        for (final PlatformLifecycleService platformService : platformServices) {
            logger.info("Start service of platform : {}", platformService);
            platformService.start();
        }
    }

    public void activateTenant() throws Exception {
        STenant tenant = getTenantInTransaction();
        if (!STenant.DEACTIVATED.equals(tenant.getStatus())) {
            throw new STenantActivationException(
                    "Tenant activation failed. Tenant is not deactivated: current state " + tenant.getStatus());
        }
        getDefaultTenantStateManager().activate();
    }

    public void deactivateTenant() throws Exception {
        final STenant tenant = getTenantInTransaction();
        if (STenant.DEACTIVATED.equals(tenant.getStatus())) {
            throw new STenantDeactivationException("Tenant deactivation failed. Tenant is already deactivated");
        }
        getDefaultTenantStateManager().deactivate();
    }

    private STenant getTenantInTransaction() throws Exception {
        return transactionService.executeInTransaction(() -> platformService.getDefaultTenant());
    }

}
