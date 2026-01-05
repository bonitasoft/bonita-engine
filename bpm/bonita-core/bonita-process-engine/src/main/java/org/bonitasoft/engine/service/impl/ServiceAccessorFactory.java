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
package org.bonitasoft.engine.service.impl;

import java.io.IOException;

import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Singleton factory that creates and manages the main {@link ServiceAccessor} instance for the Bonita Engine.
 * <p>
 * This factory is the main entry point to access all engine services and API implementations. It manages
 * the lifecycle of the Spring application context that contains all engine services (100+ beans) and
 * provides access to core infrastructure components.
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li><b>Service Accessor Creation</b>: Creates and caches the main {@link ServiceAccessor} instance,
 * which wraps the engine's Spring application context</li>
 * <li><b>Session Accessor Creation</b>: Provides {@link SessionAccessor} for thread-local session management</li>
 * <li><b>API Access Resolver</b>: Creates {@link APIAccessResolver} for API implementation resolution</li>
 * <li><b>Lifecycle Management</b>: Handles cleanup via {@link #destroyAccessors()} during engine shutdown</li>
 * <li><b>Configuration Loading</b>: Loads class names from bonita-platform-private-community.properties</li>
 * </ul>
 * <p>
 * <b>Configuration Properties:</b>
 * The factory reads the following properties from {@code bonita-platform-private-community.properties}:
 * <ul>
 * <li>{@code serviceAccessors}: Class name of {@link ServiceAccessors} implementation (default:
 * ServiceAccessorsImpl)</li>
 * <li>{@code apiAccessResolver}: Class name of {@link APIAccessResolver} implementation</li>
 * </ul>
 * <p>
 * <b>Usage Pattern:</b>
 *
 * <pre>
 *
 * ServiceAccessor accessor = ServiceAccessorFactory.getInstance().createServiceAccessor();
 * PlatformService platformService = accessor.getPlatformService();
 * </pre>
 *
 * <p>
 * <b>Spring Context Hierarchy:</b>
 * The {@link ServiceAccessor} created by this factory contains the root Spring context for the engine,
 * which serves as the parent context for the web application context.
 * <p>
 * <b>Thread Safety:</b> All factory methods are synchronized to ensure thread-safe singleton creation.
 *
 * @see ServiceAccessor
 * @see SessionAccessor
 * @see APIAccessResolver
 */
public class ServiceAccessorFactory {

    private static final ServiceAccessorFactory INSTANCE = new ServiceAccessorFactory();
    private static final String API_ACCESS_RESOLVER_CLASS_NAME = "apiAccessResolver";
    private static final String SERVICE_ACCESSORS = "serviceAccessors";

    private APIAccessResolver apiAccessResolver;
    private ServiceAccessors serviceAccessors;

    protected ServiceAccessorFactory() {
        super();
    }

    public static ServiceAccessorFactory getInstance() {
        return INSTANCE;
    }

    public synchronized ServiceAccessor createServiceAccessor() throws BonitaHomeConfigurationException, IOException,
            ReflectiveOperationException {
        return getServiceAccessors().getServiceAccessor();
    }

    private synchronized ServiceAccessors getServiceAccessors() throws BonitaHomeConfigurationException, IOException,
            ReflectiveOperationException {
        if (serviceAccessors == null) {
            serviceAccessors = (ServiceAccessors) loadClassFromPropertyName(SERVICE_ACCESSORS).getDeclaredConstructor()
                    .newInstance();
        }
        return serviceAccessors;
    }

    public SessionAccessor createSessionAccessor()
            throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            ReflectiveOperationException {
        return createServiceAccessor().getSessionAccessor();
    }

    public synchronized APIAccessResolver createAPIAccessResolver()
            throws IOException, BonitaHomeConfigurationException, ReflectiveOperationException {
        if (apiAccessResolver == null) {
            apiAccessResolver = (APIAccessResolver) loadClassFromPropertyName(API_ACCESS_RESOLVER_CLASS_NAME)
                    .getDeclaredConstructor().newInstance();
        }
        return apiAccessResolver;
    }

    private Class<?> loadClassFromPropertyName(String propertyName)
            throws IOException, BonitaHomeConfigurationException, ClassNotFoundException {
        final String sessionAccessorStr = BonitaHomeServer.getInstance().getPlatformProperties()
                .getProperty(propertyName);
        if (sessionAccessorStr == null) {
            throw new BonitaHomeConfigurationException(
                    propertyName + " not set in bonita-platform-private-community.properties");
        }
        return Class.forName(sessionAccessorStr);
    }

    public synchronized void destroyAccessors() {
        serviceAccessors.destroy();
    }
}
