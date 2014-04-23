/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
