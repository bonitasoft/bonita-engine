/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.platform.PlatformNotFoundException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * 
 * Initialize the engine and create/start or not the platform based on bonita-platform.xml
 * properties used are:
 * platform.create -- create the platform on startup
 * node.start -- start the platform (node) on startup
 * node.stop -- stop the platform (node) on shutdown
 *
 * @author Baptiste Mesta
 * 
 */
public class EngineInitializer {

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(EngineInitializer.class.getName());

    private final PlatformTenantManager platformManager;

    private final EngineInitializerProperties platformProperties;

    private static PlatformAPI platformAPI;

    public EngineInitializer(final PlatformTenantManager platformManager, final EngineInitializerProperties platformProperties) {
        super();
        this.platformManager = platformManager;
        this.platformProperties = platformProperties;
    }

    protected PlatformServiceAccessor getPlatformAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public static void init() throws Exception {
        new EngineInitializer(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).initializeEngine();
    }

    public static void unload() throws Exception {
        new EngineInitializer(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).unloadEngine();
    }

    public void initializeEngine() throws Exception {
        try {
            LOGGER.log(Level.INFO, "Initializing Bonita Engine...");
            final long before = System.currentTimeMillis();
            LOGGER.log(Level.INFO, "Initializing Spring context...");
            // create a session to call the engine
            final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final PlatformSessionService platformSessionService = platformAccessor.getPlatformSessionService();
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
            final PlatformAPI platformAPI = getPlatformAPI();

            try {
                // initialization of the platform
                try {
                    LOGGER.log(Level.INFO, "Initializing platform...");
                    initPlatform(platformAPI);
                    LOGGER.log(Level.INFO, "Platform initialized successfully.");
                } catch (final Exception e) {
                    LOGGER.log(Level.INFO, "Platform is already initialized.", e);
                    // platform is already initialized.
                }
                // start of the platform (separated from previous call as in a cluster deployment, platform may already exist but the second node still has to
                // start
                startPlatform(platformAPI);
            } finally {
                deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
            }
            final long after = System.currentTimeMillis();
            LOGGER.log(Level.INFO, "Initialization of Bonita Engine done! ( took " + (after - before) + "ms)");
        } catch (final Exception e) {
            LOGGER.log(Level.INFO, "Exception while initializing the engine: " + e);
            throw e;
        }
    }

    protected PlatformAPI getPlatformAPI() {
        if (platformAPI == null) {
            platformAPI = buildPlatformAPIInstance();
        }
        return platformAPI;
    }

    protected PlatformAPI buildPlatformAPIInstance() {
        return new PlatformAPIImpl();
    }

    private void deletePlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor, final long sessionId)
            throws SSessionNotFoundException {
        platformSessionService.deleteSession(sessionId);
        sessionAccessor.deleteSessionId();
    }

    private long createPlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor) throws SSessionException {
        final SPlatformSession createSession = platformSessionService.createSession(platformProperties.getPlatformAdminUsername());
        final long sessionId = createSession.getId();
        sessionAccessor.setSessionInfo(sessionId, -1);
        return sessionId;
    }

    protected void initPlatform(final PlatformAPI platformAPI) throws Exception {
        if (platformProperties.shouldCreatePlatform()) {
            LOGGER.log(Level.INFO, "Creating platform...");
            platformManager.createPlatform(platformAPI);
        }
    }

    protected void startPlatform(final PlatformAPI platformAPI) throws Exception {
        if (platformProperties.shouldStartPlatform()) {
            LOGGER.log(Level.INFO, "Starting platform...");
            platformManager.startPlatform(platformAPI);
            LOGGER.log(Level.INFO, "Platform started successfully");
        }
    }

    public void unloadEngine() throws Exception {
        LOGGER.log(Level.INFO, "Stopping Bonita Engine...");
        // create a session to call the engine
        PlatformSessionService platformSessionService;
        final PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        platformSessionService = platformAccessor.getPlatformSessionService();
        final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        final PlatformAPI platformAPI = getPlatformAPI();
        try {
            if (platformProperties.shouldStopPlatform()) {
                LOGGER.log(Level.INFO, "Stopping the services...");
                platformManager.stopPlatform(platformAPI);
            }
        } catch (final PlatformNotFoundException e) {
            LOGGER.log(Level.WARNING, "The platform cannot be stopped because it does not exist!");
        } catch (final Throwable e) {
            LOGGER.log(Level.SEVERE, "Issue while stopping the platform", e);
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
        }
        // after that the engine is unloaded
        ServiceAccessorFactory.getInstance().destroyAccessors();
        LOGGER.log(Level.INFO, "Bonita Engine stopped!");

    }

}
