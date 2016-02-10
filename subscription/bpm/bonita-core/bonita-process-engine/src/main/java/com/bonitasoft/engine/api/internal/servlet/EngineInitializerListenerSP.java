/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.internal.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.engine.EngineInitializerProperties;
import org.bonitasoft.engine.PlatformTenantManager;

import com.bonitasoft.engine.EngineInitializerSP;

public class EngineInitializerListenerSP implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(EngineInitializerListenerSP.class.getName());

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {
        try {
            new EngineInitializerSP(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).unloadEngine();
        } catch (final Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while unloading the Engine", e);
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        try {
            new EngineInitializerSP(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).initializeEngine();
        } catch (final Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while initializing the Engine", e);
        }
    }

}
