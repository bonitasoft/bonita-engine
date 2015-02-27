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
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;

/**
 * @author Celine Souchet
 */
public class PlatformTestUtil {

    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";

    public static final String DEFAULT_TENANT = "default";

    public PlatformSession loginOnPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    public LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

    public PlatformAPI getPlatformAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformAPI(session);
    }

    public void deletePlatformStructure() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

    public void deleteStopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

    public void createPlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = getPlatformAPI(session);
        createPlatformStructure(platformAPI, false);
        platformLoginAPI.logout(session);
    }

    private void createPlatformStructure(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        if (platformAPI.isPlatformCreated()) {
            if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
                stopPlatformAndTenant(platformAPI, deployCommands);
            }
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
        }
        platformAPI.createPlatform();
    }

    public void createInitializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        createPlatformStructure(platformAPI, deployCommands);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutOnPlatform(session);
    }

    public void initializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutOnPlatform(session);
    }

    public void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();

        if (deployCommands) {
            deployCommandsOnDefaultTenant();
        }
    }

    public void stopAndStartPlatform() throws BonitaException {
        final PlatformSession loginPlatform = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        platformAPI.startNode();
        logoutOnPlatform(loginPlatform);
    }

    public void deployCommandsOnDefaultTenant() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        final APISession session = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
        ClientEventUtil.deployCommand(session);
        loginAPI.logout(session);
    }

    public void stopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        logoutOnPlatform(session);
    }

    public void stopAndCleanPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (platformAPI.isNodeStarted()) {
            stopPlatformAndTenant(platformAPI, undeployCommands);
            cleanPlatform(platformAPI);
        }
    }

    public void stopPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (undeployCommands) {
            final LoginAPI loginAPI = getLoginAPI();
            final APISession session = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
            ClientEventUtil.undeployCommand(session);
            loginAPI.logout(session);
        }

        platformAPI.stopNode();
    }

    public static void cleanPlatform(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.cleanPlatform();
    }

    public void createEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();
        logoutOnPlatform(session);
    }

    public void destroyEnvironmentWithoutTenant() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        platformAPI.stopNode();
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

}
