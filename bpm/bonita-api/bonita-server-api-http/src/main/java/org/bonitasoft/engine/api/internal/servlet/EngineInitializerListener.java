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
package org.bonitasoft.engine.api.internal.servlet;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.engine.EngineInitializer;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class EngineInitializerListener implements ServletContextListener {

    static final String UPDATE_ONLY_STARTUP_PROPERTY = "bonita.runtime.startup.update-only";
    private static final Logger log = LoggerFactory.getLogger(EngineInitializerListener.class);

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        var engineInitializer = getEngineInitializer();
        try {
            var webApplicationContext = initializeWebApplicationContext(event, engineInitializer);
            boolean updateOnly = webApplicationContext.getEnvironment().getProperty(UPDATE_ONLY_STARTUP_PROPERTY,
                    Boolean.class,
                    Boolean.FALSE);
            if (updateOnly) {
                log.info("'{}' enabled. Shutting down JVM.", UPDATE_ONLY_STARTUP_PROPERTY);
                engineInitializer.unloadEngine();
                exit(0);
            }
        } catch (final Throwable e) {
            try {
                engineInitializer.unloadEngine();
            } catch (Exception ex) {
                log.warn("Error while unloading the Engine", ex);
            }
            log.error("Error occurred while initializing the Engine. Shutting down JVM...", e);
            exit(1);
        }
    }

    AnnotationConfigWebApplicationContext initializeWebApplicationContext(ServletContextEvent event,
            EngineInitializer engineInitializer) throws Exception {
        getPlatformSetup().init(); // init tables and default configuration
        engineInitializer.initializeEngine();
        ApplicationContext engineContext = ServiceAccessorFactory.getInstance()
                .createServiceAccessor()
                .getContext();

        AnnotationConfigWebApplicationContext webApplicationContext = initializeWebContext(event, engineContext);
        webApplicationContext.refresh();
        return webApplicationContext;
    }

    protected PlatformSetup getPlatformSetup() throws NamingException {
        return PlatformSetupAccessor.getInstance().getPlatformSetup();
    }

    void exit(int code) {
        System.exit(code);
    }

    protected AnnotationConfigWebApplicationContext initializeWebContext(ServletContextEvent event,
            ApplicationContext engineContext) {
        AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
        webApplicationContext.setParent(engineContext);
        webApplicationContext.setServletContext(event.getServletContext());
        //A web application context needs to be referenced in the Servlet context so that servlet and filters beans handled by Spring web can use it
        event.getServletContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                webApplicationContext);
        return webApplicationContext;
    }

    protected EngineInitializer getEngineInitializer() {
        return new EngineInitializer();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {
        try {
            getEngineInitializer().unloadEngine();
        } catch (final Throwable e) {
            log.error("Error while unloading the Engine", e);
        }
    }

}
