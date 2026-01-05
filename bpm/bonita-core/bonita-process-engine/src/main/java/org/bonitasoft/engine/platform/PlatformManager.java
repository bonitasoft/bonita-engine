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
import org.bonitasoft.engine.service.BonitaTaskExecutor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages the lifecycle of the Bonita Platform node, coordinating the start and stop of all platform services.
 * <p>
 * The PlatformManager is responsible for orchestrating the startup and shutdown sequences of the Bonita Engine.
 * It manages the state transitions of the platform node (the current JVM instance) and coordinates all
 * {@link PlatformLifecycleService} implementations to ensure proper initialization and cleanup.
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li><b>Platform State Management</b>: Tracks and manages platform state (STARTED, STOPPED, STARTING, STOPPING)
 * via {@link PlatformStateProvider}</li>
 * <li><b>Service Lifecycle Coordination</b>: Starts and stops all {@link PlatformLifecycleService} instances
 * in the correct order (SchedulerService, WorkService, ConnectorExecutor, ClassLoaderService, etc.)</li>
 * <li><b>Tenant Management</b>: Coordinates with {@link TenantStateManager} to start/stop tenant services</li>
 * <li><b>Version Validation</b>: Verifies platform binaries version matches database schema via
 * {@link PlatformVersionChecker}</li>
 * <li><b>Restart Handlers</b>: Executes {@link PlatformRestartHandler} instances after successful startup
 * (e.g., resume interrupted work, reschedule jobs)</li>
 * </ul>
 * <p>
 * <b>Startup Sequence:</b>
 * <ol>
 * <li>Validates platform state allows starting (via {@link PlatformStateProvider#initializeStart()})</li>
 * <li>Checks platform version compatibility via {@link PlatformVersionChecker}</li>
 * <li>Starts all {@link PlatformLifecycleService} instances in order</li>
 * <li>Updates state to STARTED</li>
 * <li>Starts tenant services via {@link TenantStateManager}</li>
 * <li>Executes platform restart handlers asynchronously</li>
 * </ol>
 * <p>
 * <b>Shutdown Sequence:</b>
 * <ol>
 * <li>Validates platform state allows stopping (via {@link PlatformStateProvider#initializeStop()})</li>
 * <li>Stops tenant services via {@link TenantStateManager}</li>
 * <li>Stops all {@link PlatformLifecycleService} instances in order</li>
 * <li>Updates state to STOPPED</li>
 * </ol>
 * <p>
 * <b>Thread Safety:</b> All start/stop methods are synchronized to prevent concurrent state modifications.
 * <p>
 * <b>Typical Platform Services Managed:</b>
 * <ul>
 * <li>SchedulerService - Quartz-based job scheduling</li>
 * <li>WorkService - Asynchronous work execution via thread pools</li>
 * <li>ConnectorExecutorService - Connector execution management</li>
 * <li>ClassLoaderService - Process-specific classloader management</li>
 * <li>EventService - Event publishing and subscription</li>
 * <li>CacheService - Data caching infrastructure</li>
 * </ul>
 *
 * @see PlatformLifecycleService
 * @see PlatformStateProvider
 * @see TenantStateManager
 * @see PlatformVersionChecker
 * @see PlatformRestartHandler
 */
@Component
public class PlatformManager {

    private static final Logger logger = LoggerFactory.getLogger(PlatformManager.class);
    private final BonitaTaskExecutor bonitaTaskExecutor;
    private final NodeConfiguration nodeConfiguration;
    private final List<PlatformLifecycleService> platformServices;
    private final PlatformStateProvider platformStateProvider;
    private final PlatformVersionChecker platformVersionChecker;

    public PlatformManager(NodeConfiguration nodeConfiguration, List<PlatformLifecycleService> platformServices,
            PlatformStateProvider platformStateProvider, BonitaTaskExecutor bonitaTaskExecutor,
            PlatformVersionChecker platformVersionChecker) {
        this.nodeConfiguration = nodeConfiguration;
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
     * Stop the platform
     *
     * @return true if the node was stopped, false if it was not stoppable (already stopped, starting or stopping)
     */
    public synchronized boolean stop() throws Exception {
        logger.info("Stopping platform:");
        if (!platformStateProvider.initializeStop()) {
            return false;
        }
        getTenantStateManager().stop();
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

        getTenantStateManager().start();

        restartHandlersOfPlatform();
        logger.info("Platform started.");
        return true;
    }

    TenantStateManager getTenantStateManager() {
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

}
