/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.service.impl.SessionAccessorAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public final class ServiceAccessorFactory {

    private static ServiceAccessorFactory instance = new ServiceAccessorFactory();

    private Properties properties = null;

    private SessionAccessorAccessor sessionAccessorAccessor;

    private PlatformServiceAccessor platformServiceAccessor;

    private final Map<Long, TenantServiceAccessor> tenantServiceAccessor = new HashMap<Long, TenantServiceAccessor>();

    private ServiceAccessorFactory() {
        super();
    }

    public static ServiceAccessorFactory getInstance() {
        return instance;
    }

    public PlatformServiceAccessor createPlatformServiceAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        if (platformServiceAccessor == null) {
            initPropertiesIfNeeded();
            final String platformClassName = properties.getProperty("platformClassName");
            if (platformClassName == null) {
                throw new BonitaHomeConfigurationException("platformClassName not set in bonita-platform.xml");
            }
            platformServiceAccessor = (PlatformServiceAccessor) Class.forName(platformClassName).newInstance();
        }
        return platformServiceAccessor;
    }

    private void initPropertiesIfNeeded() throws BonitaHomeNotSetException, IOException {
        if (properties == null) {
            properties = BonitaHomeServer.getInstance().getPlatformProperties();
        }
    }

    public TenantServiceAccessor createTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        if (!tenantServiceAccessor.containsKey(tenantId)) {
            initPropertiesIfNeeded();
            final String tenantClassName = properties.getProperty("tenantClassName");
            if (tenantClassName == null) {
                throw new BonitaHomeConfigurationException("tenantClassName not set in bonita-platform.xml");
            }
            final Class<TenantServiceAccessor> tenantClass = ClassReflector.getClass(TenantServiceAccessor.class, tenantClassName);
            final Constructor<TenantServiceAccessor> constructor = tenantClass.getConstructor(Long.class);
            tenantServiceAccessor.put(tenantId, constructor.newInstance(tenantId));
        }
        return tenantServiceAccessor.get(tenantId);
    }

    public SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException, BonitaHomeConfigurationException {
        if (sessionAccessorAccessor == null) {
            initPropertiesIfNeeded();
            final String sessionAccessorStr = properties.getProperty("sessionAccessor");
            if (sessionAccessorStr == null) {
                throw new BonitaHomeConfigurationException("sessionAccessor not set in bonita-platform.xml");
            }
            sessionAccessorAccessor = (SessionAccessorAccessor) Class.forName(sessionAccessorStr).newInstance();
        }
        return sessionAccessorAccessor.getSessionAccessor();
    }

}
