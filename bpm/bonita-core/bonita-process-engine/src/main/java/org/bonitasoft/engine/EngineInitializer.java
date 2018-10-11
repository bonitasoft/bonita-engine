/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
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
 * Initialize the engine and create/start or not the platform based on bonita-platform.xml
 * properties used are:
 * platform.create -- create the platform on startup
 * node.start -- start the platform (node) on startup
 * node.stop -- stop the platform (node) on shutdown
 *
 * @author Baptiste Mesta
 */
public class EngineInitializer {

    private static final Logger LOGGER = Logger.getLogger(EngineInitializer.class.getName());

    private static PlatformAPI platformAPI;

    public EngineInitializer() {
        super();
    }

    public void initializeEngine() throws Exception {
        LOGGER.log(Level.INFO, "Initializing Bonita Engine...");
        final long before = System.currentTimeMillis();
        // create a session to call the engine
        final PlatformSessionService platformSessionService = getPlatformSessionService();
        final SessionAccessor sessionAccessor = getSessionAccessor();
        final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        final PlatformAPI platformAPI = getPlatformAPI();

        try {
            if (!platformAPI.isPlatformCreated()) {
                throw new PlatformNotFoundException("Can't start or stop platform if it is not created.");
            }
            // initialization of the platform
            if (!platformAPI.isPlatformInitialized()) {
                LOGGER.log(Level.INFO, "First run on this platform, initializing it...");
                platformAPI.initializePlatform();
                LOGGER.log(Level.INFO, "Platform initialized successfully.");
            } else {
                LOGGER.log(Level.INFO, "Platform is already initialized.");
            }
            LOGGER.log(Level.INFO, "Starting node...");
            platformAPI.startNode();
            LOGGER.log(Level.INFO, "Node started successfully.");
            final long after = System.currentTimeMillis();
            LOGGER.log(Level.INFO, "Initialization of Bonita Engine done! ( took " + (after - before) + "ms)");
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
        }
    }

    SessionAccessor getSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return getServiceAccessorFactory().createSessionAccessor();
    }

    PlatformSessionService getPlatformSessionService() {
        PlatformServiceAccessor result;
        try {
            result = getServiceAccessorFactory().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
        return result.getPlatformSessionService();
    }

    // Visible for testing
    PlatformAPI getPlatformAPI() {
        if (platformAPI == null) {
            //in local only
            platformAPI = newPlatformAPI();
        }
        return platformAPI;
    }

    protected PlatformAPI newPlatformAPI() {
        return new PlatformAPIImpl();
    }

    private void deletePlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor, final long sessionId)
            throws SSessionNotFoundException {
        platformSessionService.deleteSession(sessionId);
        sessionAccessor.deleteSessionId();
    }

    private long createPlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor) throws SSessionException {
        final SPlatformSession createSession = platformSessionService.createSession("SYSTEM");
        final long sessionId = createSession.getId();
        sessionAccessor.setSessionInfo(sessionId, -1);
        return sessionId;
    }

    public void unloadEngine() throws Exception {
        LOGGER.log(Level.INFO, "Stopping Bonita Engine...");
        // create a session to call the engine
        final SessionAccessor sessionAccessor = getSessionAccessor();
        PlatformSessionService platformSessionService = getPlatformSessionService();
        final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        final PlatformAPI platformAPI = getPlatformAPI();
        try {
            if (!platformAPI.isNodeStarted()) {
                LOGGER.log(Level.INFO, "Node is not started, nothing to do.");
                return;
            }
            LOGGER.log(Level.INFO, "Stopping node...");
            platformAPI.stopNode();
        } catch (final Throwable e) {
            LOGGER.log(Level.SEVERE, "Issue while stopping the platform", e);
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
            // after that the engine is unloaded
            getServiceAccessorFactory().destroyAccessors();
            LOGGER.log(Level.INFO, "Bonita Engine stopped!");
        }

    }

    ServiceAccessorFactory getServiceAccessorFactory() {
        return ServiceAccessorFactory.getInstance();
    }

}
