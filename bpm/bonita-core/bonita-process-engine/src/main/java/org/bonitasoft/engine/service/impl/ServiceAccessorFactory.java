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
package org.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
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

    public synchronized PlatformServiceAccessor createPlatformServiceAccessor() throws BonitaHomeNotSetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return getServiceAccessors().getPlatformServiceAccessor();
    }

    private synchronized ServiceAccessors getServiceAccessors() throws BonitaHomeConfigurationException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (serviceAccessors == null) {
            serviceAccessors = (ServiceAccessors) loadClassFromPropertyName(SERVICE_ACCESSORS).newInstance();
        }
        return serviceAccessors;
    }

    public TenantServiceAccessor createTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        return getServiceAccessors().getTenantServiceAccessor(tenantId);
    }

    public SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return createPlatformInitServiceAccessor().getSessionAccessor();
    }

    private PlatformInitServiceAccessor createPlatformInitServiceAccessor()
            throws IOException, BonitaHomeConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        return getServiceAccessors().getPlatformInitServiceAccessor();
    }

    public synchronized APIAccessResolver createAPIAccessResolver() throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (apiAccessResolver == null) {
            apiAccessResolver = (APIAccessResolver) loadClassFromPropertyName(API_ACCESS_RESOLVER_CLASS_NAME).newInstance();
        }
        return apiAccessResolver;
    }

    private Class<?> loadClassFromPropertyName(String propertyName) throws IOException, BonitaHomeConfigurationException, ClassNotFoundException {
        final String sessionAccessorStr = BonitaHomeServer.getInstance().getPlatformProperties().getProperty(propertyName);
        if (sessionAccessorStr == null) {
            throw new BonitaHomeConfigurationException(propertyName + " not set in bonita-platform-private-community.properties");
        }
        return Class.forName(sessionAccessorStr);
    }

    public synchronized void destroyAccessors() {
        serviceAccessors.destroy();
    }
}
