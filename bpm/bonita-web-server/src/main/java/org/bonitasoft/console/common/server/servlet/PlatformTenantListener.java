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
package org.bonitasoft.console.common.server.servlet;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zhiheng Yang, Anthony Birembaut
 */
public class PlatformTenantListener implements ServletContextListener {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformTenantListener.class.getName());

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        PlatformManagementUtils platformManagementUtils = new PlatformManagementUtils();
        try {
            platformManagementUtils.initializePlatformConfiguration();
            // Create temporary folder specific to portal at startup:
            WebBonitaConstantsUtils.getPlatformInstance().getTempFolder();
        } catch (BonitaException e) {
            LOGGER.error(
                    "Error initializing platform configuration. Engine most likely failed to start. Check previous error logs for more details.");
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while retrieving configuration", e);
            }
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
    }

}
