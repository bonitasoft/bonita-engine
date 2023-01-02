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
package org.bonitasoft.engine;

import java.io.IOException;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.event.PlatformStartedEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected static final Logger LOGGER = LoggerFactory.getLogger(EngineInitializer.class.getName());

    private static PlatformAPI platformAPI;

    public EngineInitializer() {
        super();
    }

    public void initializeEngine() throws Exception {
        LOGGER.info("Initializing Bonita Engine...");
        final long before = System.currentTimeMillis();
        // create a session to call the engine
        final PlatformServiceAccessor platformService = getPlatformService();
        final PlatformSessionService platformSessionService = platformService.getPlatformSessionService();
        final SessionAccessor sessionAccessor = getSessionAccessor();
        final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        final PlatformAPI platformAPI = getPlatformAPI();

        try {
            if (!platformAPI.isPlatformCreated() || !platformAPI.isPlatformInitialized()) {
                throw new PlatformNotFoundException("Can't start or stop platform if it is not created.");
            }
            LOGGER.info("Starting node...");

            logEditionMessage();
            platformAPI.startNode();
            LOGGER.info("Node started successfully.");
            final long after = System.currentTimeMillis();
            LOGGER.info("Initialization of Bonita Engine done! (took " + (after - before) + "ms)");

            LOGGER.debug("Publishing platform started event");
            platformService.getApplicationEventPublisher().publishEvent(new PlatformStartedEvent());
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
        }
    }

    protected void logEditionMessage() {
        LOGGER.info("  ____              _ _           _____                                      _ _         ");
        LOGGER.info(" |  _ \\            (_) |         / ____|                                    (_) |        ");
        LOGGER.info(" | |_) | ___  _ __  _| |_ __ _  | |     ___  _ __ ___  _ __ ___  _   _ _ __  _| |_ _   _ ");
        LOGGER.info(" |  _ < / _ \\| '_ \\| | __/ _` | | |    / _ \\| '_ ` _ \\| '_ ` _ \\| | | | '_ \\| | __| | | |");
        LOGGER.info(" | |_) | (_) | | | | | || (_| | | |___| (_) | | | | | | | | | | | |_| | | | | | |_| |_| |");
        LOGGER.info(
                " |____/ \\___/|_| |_|_|\\__\\__,_|  \\_____\\___/|_| |_| |_|_| |_| |_|\\__,_|_| |_|_|\\__|\\__, |");
        LOGGER.info("                                                                                    __/ |");
        LOGGER.info("                                                                                   |___/ ");
    }

    SessionAccessor getSessionAccessor() throws BonitaHomeNotSetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return getServiceAccessorFactory().createSessionAccessor();
    }

    PlatformServiceAccessor getPlatformService() {
        try {
            return getServiceAccessorFactory().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
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

    private void deletePlatformSession(final PlatformSessionService platformSessionService,
            final SessionAccessor sessionAccessor, final long sessionId)
            throws SSessionNotFoundException {
        platformSessionService.deleteSession(sessionId);
        sessionAccessor.deleteSessionId();
    }

    private long createPlatformSession(final PlatformSessionService platformSessionService,
            final SessionAccessor sessionAccessor) throws SSessionException {
        final SPlatformSession createSession = platformSessionService.createSession("SYSTEM");
        final long sessionId = createSession.getId();
        sessionAccessor.setSessionInfo(sessionId, -1);
        return sessionId;
    }

    public void unloadEngine() throws Exception {
        LOGGER.info("Stopping Bonita Engine...");
        // create a session to call the engine
        final SessionAccessor sessionAccessor = getSessionAccessor();
        final PlatformSessionService platformSessionService = getPlatformService().getPlatformSessionService();
        final long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        final PlatformAPI platformAPI = getPlatformAPI();
        try {
            if (!platformAPI.isNodeStarted()) {
                LOGGER.info("Node is not started, nothing to do.");
                return;
            }
            LOGGER.info("Stopping node...");
            platformAPI.stopNode();
        } catch (final Throwable e) {
            LOGGER.warn("Error while stopping the platform", e);
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
            // after that the engine is unloaded
            getServiceAccessorFactory().destroyAccessors();
            LOGGER.info("Bonita Engine stopped!");
        }

    }

    ServiceAccessorFactory getServiceAccessorFactory() {
        return ServiceAccessorFactory.getInstance();
    }

}
