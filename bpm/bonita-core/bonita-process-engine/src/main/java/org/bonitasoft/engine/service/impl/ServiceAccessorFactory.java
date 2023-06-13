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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Main entry point to access services and api implementation of the engine
 * {@link ServiceAccessors} and {@link APIAccessResolver} classes can be overridden in the configuration
 * under the name `serviceAccessors` and `apiAccessResolver`
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

    /**
     * @deprecated since 9.0.0, use {@link #createServiceAccessor()} instead
     */
    @Deprecated(forRemoval = true, since = "9.0.0")
    public synchronized PlatformServiceAccessor createPlatformServiceAccessor()
            throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            ReflectiveOperationException {
        return getServiceAccessors().getPlatformServiceAccessor();
    }

    private synchronized ServiceAccessors getServiceAccessors() throws BonitaHomeConfigurationException, IOException,
            ReflectiveOperationException {
        if (serviceAccessors == null) {
            serviceAccessors = (ServiceAccessors) loadClassFromPropertyName(SERVICE_ACCESSORS).getDeclaredConstructor()
                    .newInstance();
        }
        return serviceAccessors;
    }

    /**
     * @deprecated since 9.0.0, use {@link #createServiceAccessor()} instead
     */
    @Deprecated(forRemoval = true, since = "9.0.0")
    public TenantServiceAccessor createTenantServiceAccessor()
            throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, ReflectiveOperationException {
        return getServiceAccessors().getTenantServiceAccessor();
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
