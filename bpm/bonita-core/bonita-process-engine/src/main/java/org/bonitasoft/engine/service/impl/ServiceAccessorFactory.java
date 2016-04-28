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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    public static final String PLATFORM_INIT_SERVICE_ACCESSOR_CLASS_NAME = "sessionAccessor";
    public static final String TENANT_SERVICE_ACCESSOR_CLASS_NAME = "tenantClassName";
    public static final String PLATFORM_SERVICE_ACCESSOR_CLASS_NAME = "platformClassName";
    public static final String API_ACCESS_RESOLVER_CLASS_NAME = "apiAccessResolver";

    private PlatformInitServiceAccessor platformInitServiceAccessor;

    private PlatformServiceAccessor platformServiceAccessor;

    private final Map<Long, TenantServiceAccessor> tenantServiceAccessor = new HashMap<>();

    private APIAccessResolver apiAccessResolver;

    protected ServiceAccessorFactory() {
        super();
    }

    public static ServiceAccessorFactory getInstance() {
        return INSTANCE;
    }

    public synchronized PlatformServiceAccessor createPlatformServiceAccessor() throws BonitaHomeNotSetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        if (platformServiceAccessor == null) {
            platformServiceAccessor = (PlatformServiceAccessor) loadClassFromPropertyName(PLATFORM_SERVICE_ACCESSOR_CLASS_NAME).newInstance();
        }
        return platformServiceAccessor;
    }

    public synchronized TenantServiceAccessor createTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        if (!tenantServiceAccessor.containsKey(tenantId)) {
            final Constructor<?> constructor = loadClassFromPropertyName(TENANT_SERVICE_ACCESSOR_CLASS_NAME).getConstructor(Long.class);
            tenantServiceAccessor.put(tenantId, (TenantServiceAccessor) constructor.newInstance(tenantId));
        }
        return tenantServiceAccessor.get(tenantId);
    }

    public synchronized SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return createPlatformInitServiceAccessor().getSessionAccessor();
    }

    private PlatformInitServiceAccessor createPlatformInitServiceAccessor()
            throws IOException, BonitaHomeConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (platformInitServiceAccessor == null) {
            platformInitServiceAccessor = (PlatformInitServiceAccessor) loadClassFromPropertyName(PLATFORM_INIT_SERVICE_ACCESSOR_CLASS_NAME).newInstance();
        }
        return platformInitServiceAccessor;
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
        Set<Entry<Long, TenantServiceAccessor>> tenantAccessors = tenantServiceAccessor.entrySet();
        for (Entry<Long, TenantServiceAccessor> tenantAccessor : tenantAccessors) {
            tenantAccessor.getValue().destroy();
        }
        tenantServiceAccessor.clear();
        if (platformServiceAccessor != null) {
            platformServiceAccessor.destroy();
            platformServiceAccessor = null;
        }
        if (platformInitServiceAccessor != null) {
            platformInitServiceAccessor.destroy();
            platformInitServiceAccessor = null;
        }
    }
}
