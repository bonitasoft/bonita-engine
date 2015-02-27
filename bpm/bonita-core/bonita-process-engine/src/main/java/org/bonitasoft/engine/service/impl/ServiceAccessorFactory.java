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
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.commons.ClassReflector;
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
public final class ServiceAccessorFactory {

    private static final String DB_VENDOR = "db.vendor";

    private static final ServiceAccessorFactory INSTANCE = new ServiceAccessorFactory();

    private Properties properties = null;

    private SessionAccessorAccessor sessionAccessorAccessor;

    private PlatformServiceAccessor platformServiceAccessor;

    private final Map<Long, TenantServiceAccessor> tenantServiceAccessor = new HashMap<Long, TenantServiceAccessor>();

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
            initPropertiesIfNeeded();
            final String platformClassName = properties.getProperty("platformClassName");
            if (platformClassName == null) {
                throw new BonitaHomeConfigurationException("platformClassName not set in bonita-platform.properties");
            }
            platformServiceAccessor = (PlatformServiceAccessor) Class.forName(platformClassName).newInstance();
        }
        return platformServiceAccessor;
    }

    private void initPropertiesIfNeeded() throws BonitaHomeNotSetException, IOException {
        if (properties == null) {
            properties = BonitaHomeServer.getInstance().getPlatformProperties();
            // set system properties
            final String dbVendor = properties.getProperty(DB_VENDOR);
            System.setProperty(DB_VENDOR, dbVendor);
        }
    }

    public synchronized TenantServiceAccessor createTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (!tenantServiceAccessor.containsKey(tenantId)) {
            initPropertiesIfNeeded();
            final String tenantClassName = properties.getProperty("tenantClassName");
            if (tenantClassName == null) {
                throw new BonitaHomeConfigurationException("tenantClassName not set in bonita-platform.properties");
            }
            final Class<TenantServiceAccessor> tenantClass = ClassReflector.getClass(TenantServiceAccessor.class, tenantClassName);
            final Constructor<TenantServiceAccessor> constructor = tenantClass.getConstructor(Long.class);
            tenantServiceAccessor.put(tenantId, constructor.newInstance(tenantId));
        }
        return tenantServiceAccessor.get(tenantId);
    }

    public synchronized SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        if (sessionAccessorAccessor == null) {
            initPropertiesIfNeeded();
            final String sessionAccessorStr = properties.getProperty("sessionAccessor");
            if (sessionAccessorStr == null) {
                throw new BonitaHomeConfigurationException("sessionAccessor not set in bonita-platform.properties");
            }
            sessionAccessorAccessor = (SessionAccessorAccessor) Class.forName(sessionAccessorStr).newInstance();
        }
        return sessionAccessorAccessor.getSessionAccessor();
    }

    public synchronized APIAccessResolver createAPIAccessResolver() throws BonitaHomeNotSetException, IOException, BonitaHomeConfigurationException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (apiAccessResolver == null) {
            initPropertiesIfNeeded();
            final String sessionAccessorStr = properties.getProperty("apiAccessResolver");
            if (sessionAccessorStr == null) {
                throw new BonitaHomeConfigurationException("ApiAccessResolver not set in bonita-platform.properties");
            }
            apiAccessResolver = (APIAccessResolver) Class.forName(sessionAccessorStr).newInstance();
        }
        return apiAccessResolver;
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
        if (sessionAccessorAccessor != null) {
            sessionAccessorAccessor.destroy();
            sessionAccessorAccessor = null;
        }
    }
}
