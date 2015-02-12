/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.impl.scheduler.TenantJobListenerManager;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;

import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * Register TenantJobListeners. This class must extend {@link java.util.concurrent.Callable} because it will be executed by
 * {@link org.bonitasoft.engine.service.BroadcastService} in order to register the tenant job listeners in all cluster nodes.
 * 
 * @author Elias Ricken de Medeiros
 */
public class RegisterTenantJobListeners implements Callable<Void>, Serializable {

    private final long tenantId;

    public RegisterTenantJobListeners(long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Void call() throws Exception {
        TenantServiceAccessor tenantServiceAccessor = getPlatformAccessor().getTenantServiceAccessor(tenantId);
        registerTenantJobListeners(tenantServiceAccessor, tenantId);
        return null;
    }

    private void registerTenantJobListeners(final TenantServiceAccessor tenantServiceAccessor, final Long tenantId) throws SSchedulerException {
        TenantJobListenerManager tenantJobListenerManager = getTenantJobListenerManager(tenantServiceAccessor);
        tenantJobListenerManager.registerListeners(tenantServiceAccessor.getTenantConfiguration().getJobListeners(), tenantId);
    }

    protected TenantJobListenerManager getTenantJobListenerManager(final TenantServiceAccessor tenantServiceAccessor) {
        return new TenantJobListenerManager(tenantServiceAccessor.getSchedulerService());
    }

    protected PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

}
