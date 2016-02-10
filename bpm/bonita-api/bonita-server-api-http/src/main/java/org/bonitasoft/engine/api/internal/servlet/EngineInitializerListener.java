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
package org.bonitasoft.engine.api.internal.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.EngineInitializerProperties;
import org.bonitasoft.engine.PlatformTenantManager;

public class EngineInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(EngineInitializerListener.class.getName());

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {
        try {
            new EngineInitializer(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).unloadEngine();
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while unloading the Engine", e);
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        try {
            new EngineInitializer(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).initializeEngine();
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while initializing the Engine", e);
        }
    }

}
