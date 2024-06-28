/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class PlatformManagementUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformManagementUtils.class.getName());

    private final ConfigurationFilesManager configurationFilesManager = ConfigurationFilesManager.getInstance();

    //package local for testing purpose
    boolean isLocal() throws UnknownAPITypeException, ServerAPIException, IOException {
        return ApiAccessType.LOCAL.equals(APITypeManager.getAPIType());
    }

    //package local for testing purpose
    PlatformAPI getPlatformAPI(final PlatformSession platformSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformAPI(platformSession);
    }

    //package local for testing purpose
    PlatformLoginAPI getPlatformLoginAPI()
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    public PlatformSession platformLogin() throws BonitaException, IOException {
        if (isLocal()) {
            try {
                return localPlatformLogin();
            } catch (final Exception e) {
                throw new ServerAPIException("Unable to login locally", e);
            }
        } else {
            String username = System.getProperty("org.bonitasoft.platform.username");
            String password = System.getProperty("org.bonitasoft.platform.password");
            try {

                return getPlatformLoginAPI().login(username,
                        password);
            } catch (InvalidPlatformCredentialsException e) {
                throw new InvalidPlatformCredentialsException("The portal is not able to login to the engine because " +
                        "system properties org.bonitasoft.platform.username and org.bonitasoft.platform.password are " +
                        "not set correctly to the platform administrator credentials.\n " +
                        "These properties must be set when connecting to a Bonita engine that is not local. " +
                        "If the engine is local, change the connection to LOCAL using the APITypeManager");
            }
        }
    }

    PlatformSession localPlatformLogin()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
            InstantiationException {
        final Class<?> api = Class.forName("org.bonitasoft.engine.LocalLoginMechanism");
        return (PlatformSession) api.getDeclaredMethod("login").invoke(api.getDeclaredConstructor().newInstance());
    }

    void platformLogout(final PlatformSession platformSession) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(platformSession);
    }

    private void retrieveTenantsConfiguration(final PlatformAPI platformAPI) throws IOException {
        configurationFilesManager.setTenantConfigurationFiles(platformAPI.getClientTenantConfigurations());
    }

    private void retrievePlatformConfiguration(final PlatformAPI platformAPI) throws IOException {
        final Map<String, byte[]> clientPlatformConfigurations = platformAPI.getClientPlatformConfigurations();
        configurationFilesManager.setPlatformConfigurations(clientPlatformConfigurations);
    }

    public void initializePlatformConfiguration() throws BonitaException, IOException {
        final PlatformSession platformSession = platformLogin();
        final PlatformAPI platformAPI = getPlatformAPI(platformSession);
        retrievePlatformConfiguration(platformAPI);
        retrieveTenantsConfiguration(platformAPI);
        platformLogout(platformSession);
    }

    public void updateConfigurationFile(final String file, final byte[] content) throws IOException, BonitaException {
        final PlatformSession platformSession = platformLogin();
        final PlatformAPI platformAPI = getPlatformAPI(platformSession);
        platformAPI.updateClientTenantConfigurationFile(getDefaultTenantId(), file, content);
        platformLogout(platformSession);
    }

    long getDefaultTenantId() {
        return TenantsManagementUtils.getDefaultTenantId();
    }

    /**
     * String => configuration file name
     * byte[] => content of the configuration file
     */
    public Map<String, byte[]> readTenantConfigurationsFromEngine() {
        try {
            final PlatformSession platformSession = platformLogin();
            try {
                return getPlatformAPI(platformSession).getClientTenantConfigurations();
            } finally {
                platformLogout(platformSession);
            }
        } catch (BonitaException | IOException e) {
            LOGGER.error("Cannot retrieve tenant configurations", e);
            return Collections.emptyMap();
        }
    }

    public boolean isPlatformAvailable() {
        try {
            final PlatformSession platformSession = platformLogin();
            try {
                PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
                return platformAPI.isNodeStarted();
            } finally {
                platformLogout(platformSession);
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Platform is not available.", e);
            } else if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Platform is not available.");
            }
            return false;
        }
    }
}
