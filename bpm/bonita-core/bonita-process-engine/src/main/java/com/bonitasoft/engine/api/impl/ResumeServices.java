/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Baptiste Mesta
 */
public class ResumeServices implements Callable<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    private final long tenantId;

    ResumeServices(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Void call() throws Exception {
        final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessor();
        final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        final WorkService workService = tenantServiceAccessor.getWorkService();
        final BusinessDataRepository businessDataRepository = tenantServiceAccessor.getBusinessDataRepository();
        try {
            workService.resume();
            businessDataRepository.resume();
        } catch (final SBonitaException e) {
            throw new UpdateException("Unable to resume the work service.", e);
        }
        return null;
    }

    PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException, BonitaHomeConfigurationException {
        return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
    }

}
