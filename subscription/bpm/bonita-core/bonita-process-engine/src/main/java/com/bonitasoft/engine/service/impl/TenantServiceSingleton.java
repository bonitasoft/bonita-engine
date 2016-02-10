/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public final class TenantServiceSingleton {

    private static Map<Long, TenantServiceAccessor> instances = new HashMap<Long, TenantServiceAccessor>();

    private TenantServiceSingleton() {
        super();
    }

    private static TenantServiceAccessor getTenant(final long tenantId) {
        try {
            return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TenantServiceAccessor getInstance(final long tenantId) {
        if (!instances.containsKey(tenantId)) {
            instances.put(tenantId, getTenant(tenantId));
        }
        return instances.get(tenantId);
    }

}
