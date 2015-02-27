/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.service.impl;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.service.PlatformServiceAccessor;

/**
 * @author Baptiste Mesta
 * 
 * @param <T>
 */
public class InTransactionCallable<T> implements Callable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Callable<T> callable;

    private final Long tenantId;

    InTransactionCallable(final Callable<T> callable, final Long tenantId) {
        this.callable = callable;
        this.tenantId = tenantId;
    }

    @Override
    public T call() throws Exception {
        PlatformServiceAccessor createPlatformServiceAccessor = com.bonitasoft.engine.service.impl.ServiceAccessorFactory.getInstance()
                .createPlatformServiceAccessor();
        SessionAccessor sessionAccessor = com.bonitasoft.engine.service.impl.ServiceAccessorFactory.getInstance().createSessionAccessor();
        if (tenantId != null) {
            sessionAccessor.setTenantId(tenantId);
        }
        try {
            return createPlatformServiceAccessor.getTransactionService().executeInTransaction(callable);
        } finally {
            sessionAccessor.deleteTenantId();
        }
    }
}
