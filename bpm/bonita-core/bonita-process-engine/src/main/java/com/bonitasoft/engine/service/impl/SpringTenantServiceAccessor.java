/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.parameter.ParameterService;

import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SpringTenantServiceAccessor extends org.bonitasoft.engine.service.impl.SpringTenantServiceAccessor implements TenantServiceAccessor {

    private ParameterService parameterService;

    public SpringTenantServiceAccessor(final Long tenantId) {
        super(tenantId);
    }

    @Override
    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = getBeanAccessor().getService(ParameterService.class);
        }
        return parameterService;
    }

}
