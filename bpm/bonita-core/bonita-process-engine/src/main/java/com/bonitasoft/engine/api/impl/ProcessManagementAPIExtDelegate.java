/*******************************************************************************
 * Copyright (C) 2013, 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.ProcessManagementAPIImplDelegate;
import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.impl.transaction.process.DeleteProcessExt;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessManagementAPIExtDelegate extends ProcessManagementAPIImplDelegate {

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    protected DeleteProcess instantiateDeleteProcessTransactionContent(final long processId) {
        return new DeleteProcessExt(getTenantAccessor(), processId);
    }


}
