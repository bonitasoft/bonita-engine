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
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while unloading the Engine", e);
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        try {
            new EngineInitializer(PlatformTenantManager.getInstance(), new EngineInitializerProperties()).initializeEngine();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Error while initializing the Engine", e);
        }
    }

}
