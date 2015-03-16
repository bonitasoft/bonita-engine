/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public final class ServiceAccessorFactory {

    private static ServiceAccessorFactory instance = new ServiceAccessorFactory();

    private final org.bonitasoft.engine.service.impl.ServiceAccessorFactory bosServiceAccessorFactory;

    private ServiceAccessorFactory() {
        bosServiceAccessorFactory = org.bonitasoft.engine.service.impl.ServiceAccessorFactory.getInstance();
    }

    public static ServiceAccessorFactory getInstance() {
        return instance;
    }

    public PlatformServiceAccessor createPlatformServiceAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return (PlatformServiceAccessor) bosServiceAccessorFactory.createPlatformServiceAccessor();
    }

    public TenantServiceAccessor createTenantServiceAccessor(final long tenantId) throws SBonitaException, BonitaHomeNotSetException, IOException,
            BonitaHomeConfigurationException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        return (TenantServiceAccessor) bosServiceAccessorFactory.createTenantServiceAccessor(tenantId);
    }

    public SessionAccessor createSessionAccessor() throws BonitaHomeNotSetException, BonitaHomeConfigurationException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, IOException {
        return bosServiceAccessorFactory.createSessionAccessor();
    }

}
