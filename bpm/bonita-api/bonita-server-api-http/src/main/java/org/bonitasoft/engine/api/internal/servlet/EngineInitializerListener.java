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
package org.bonitasoft.engine.api.internal.servlet;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineInitializerListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(EngineInitializerListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        try {
            PlatformSetupAccessor.getPlatformSetup().init(); // init tables and default configuration
            new EngineInitializer().initializeEngine();
        } catch (final Throwable e) {
            throw new RuntimeException("Error while initializing the Engine", e);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {
        try {
            new EngineInitializer().unloadEngine();
        } catch (final Throwable e) {
            log.error("Error while unloading the Engine", e);
        }
    }

}
