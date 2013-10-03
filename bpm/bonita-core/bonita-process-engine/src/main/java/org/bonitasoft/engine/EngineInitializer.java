/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
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

    public void initializeEngine() throws Exception {
        LOGGER.log(Level.INFO, "Initializing Bonita Engine...");
        long before = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Initializing spring context...");
        // initialize spring context
        SpringPlatformFileSystemBeanAccessor.initializeContext(null);
        // create a session to call the engine
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        PlatformSessionService platformSessionService = platformAccessor.getPlatformSessionService();
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        PlatformAPIImpl platformAPI = new PlatformAPIImpl();
        // final PlatformService platformService = platformAccessor.getPlatformService();
        // long defaultTenantId;
        // initialization of the platform
        try {
            initAndStartPlatform(platformAPI);
            // we always have a default tenant here
            // defaultTenantId = platformService.getDefaultTenant().getId();
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
        }
        // // create a session on the default tenant
        // TenantServiceAccessor serviceAccessor = TenantServiceSingleton.getInstance(defaultTenantId);
        // SessionService sessionService = serviceAccessor.getSessionService();
        //
        // SSession tenantSession = sessionService.createSession(defaultTenantId, platformProperties.getTenantAdminUsername(defaultTenantId));
        // sessionAccessor.setSessionInfo(tenantSession.getId(), defaultTenantId);
        // try {
        // initializeDefaultTenant();
        // } finally {
        // sessionService.deleteSession(tenantSession.getId());
        // sessionAccessor.deleteSessionId();
        // }
        long after = System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Initialization of Bonita Engine done! ( took " + (after - before) + "ms)");
    }

    private void deletePlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor, final long sessionId)
            throws SSessionNotFoundException {
        platformSessionService.deleteSession(sessionId);
        sessionAccessor.deleteSessionId();
    }

    private long createPlatformSession(final PlatformSessionService platformSessionService, final SessionAccessor sessionAccessor) throws SSessionException {
        SPlatformSession createSession = platformSessionService.createSession(platformProperties.getPlatformAdminUsername());
        long sessionId = createSession.getId();
        sessionAccessor.setSessionInfo(sessionId, -1);
        return sessionId;
    }

    protected void initAndStartPlatform(final PlatformAPIImpl platformAPI) throws Exception {
        if (platformProperties.shouldCreatePlatform()) {
            LOGGER.log(Level.INFO, "Creating platform...");
            platformManager.createPlatform(platformAPI);
        }
        if (platformProperties.shouldStartPlatform()) {
            LOGGER.log(Level.INFO, "Starting platform...");
            platformManager.startPlatform(platformAPI);
        }
    }

    // /**
    // * Initialize default tenant configuration folder
    // *
    // * @throws IOException
    // * @throws BonitaException
    // */
    // protected void initializeDefaultTenant() throws Exception {
    // LOGGER.log(Level.INFO, "Initializing default tenant...");
    // try {
    // final boolean wasDirectoryCreated = TenantsManagementUtils.addDirectoryForTenant(tenantId, platformManager.getPlatformSession());
    // if (wasDirectoryCreated) {
    // createDefaultProfiles(session);
    // }
    // TenantAPIAccessor.getLoginAPI().logout(session);
    // } catch (final NumberFormatException e) {
    // if (LOGGER.isLoggable(Level.SEVERE)) {
    // final String msg = "Error while casting default tenant id";
    // LOGGER.log(Level.SEVERE, msg, e);
    // }
    // throw e;
    // } catch (final IOException e) {
    // if (LOGGER.isLoggable(Level.SEVERE)) {
    // final String msg = "Error while creating tenant directory";
    // LOGGER.log(Level.SEVERE, msg, e);
    // }
    // throw e;
    // } catch (final BonitaException e) {
    // if (LOGGER.isLoggable(Level.SEVERE)) {
    // final String msg = "Bonita exception while creating tenant directory";
    // LOGGER.log(Level.SEVERE, msg, e);
    // }
    // throw e;
    // }
    // }

    public void unloadEngine() throws Exception {
        LOGGER.log(Level.INFO, "Stopping Bonita Engine...");
        // create a session to call the engine
        PlatformSessionService platformSessionService;
        PlatformServiceAccessor platformAccessor = getPlatformAccessor();
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        platformSessionService = platformAccessor.getPlatformSessionService();
        long sessionId = createPlatformSession(platformSessionService, sessionAccessor);
        PlatformAPIImpl platformAPI = new PlatformAPIImpl();
        try {
            if (platformProperties.shouldStopPlatform()) {
                platformManager.stopPlatform(platformAPI);
            }
        } finally {
            deletePlatformSession(platformSessionService, sessionAccessor, sessionId);
        }
    }
    //
    // protected void createDefaultProfiles(final APISession session) throws IOException {
    // importProfilesFromResourceFile(session, "InitProfiles.xml");
    // }
    //
    // @SuppressWarnings("unchecked")
    // protected void importProfilesFromResourceFile(final APISession session, final String xmlFileName) throws IOException {
    // final InputStream xmlStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFileName);
    // final byte[] xmlContent = IOUtils.toByteArray(xmlStream);
    //
    // final CommandCaller addProfiles = new CommandCaller(session, "importProfilesCommand");
    // addProfiles.addParameter("xmlContent", xmlContent);
    // addProfiles.addParameter("importPolicy", ImportPolicy.MERGE_DUPLICATES);
    // final List<String> warningMsgs = (List<String>) addProfiles.run();
    //
    // }
}
