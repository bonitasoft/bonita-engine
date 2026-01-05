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

/**
 * Servlet container entry point that orchestrates the complete Bonita Engine initialization sequence.
 * <p>
 * This listener is triggered by the servlet container (Tomcat) during application startup
 * and shutdown. It coordinates the following initialization phases:
 * <ol>
 * <li><b>Platform Setup Phase</b>: Initializes the database schema and configuration via {@link PlatformSetup}</li>
 * <li><b>Engine Initialization Phase</b>: Starts the engine and loads all services via {@link EngineInitializer}</li>
 * <li><b>Web Context Phase</b>: Creates the web application context with the engine context as parent</li>
 * </ol>
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Invokes {@link PlatformSetup#init()} to create/update database tables and configuration</li>
 * <li>Calls {@link EngineInitializer#initializeEngine()} to start the platform node</li>
 * <li>Creates an {@link AnnotationConfigWebApplicationContext} with the engine Spring context as parent</li>
 * <li>Registers the web context in servlet context for access by REST controllers and filters</li>
 * <li>Handles graceful shutdown via {@link EngineInitializer#unloadEngine()}</li>
 * <li>Supports update-only mode via {@code bonita.runtime.startup.update-only} property</li>
 * </ul>
 * <p>
 * The resulting Spring context hierarchy is:
 *
 * <pre>
 * Engine Context (parent) → contains all engine services (100+ beans)
 *     └── Web Context (child) → contains REST API controllers, filters, web config
 * </pre>
 *
 * @see EngineInitializer
 * @see PlatformSetup
 * @see ServiceAccessorFactory
 */
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
    public void contextDestroyed(final ServletContextEvent event) {
        try {
            getEngineInitializer().unloadEngine();
        } catch (final Throwable e) {
            log.error("Error while unloading the Engine", e);
        }
    }

}
