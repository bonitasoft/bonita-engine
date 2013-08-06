package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.ProcessManagementAPIImplDelegate;
import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.impl.transaction.process.DeleteProcessExt;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

public class ProcessManagementAPIExtDelegate extends ProcessManagementAPIImplDelegate {

    protected static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    protected DeleteProcess instantiateDeleteProcessTransactionContent(long processId) {
        return new DeleteProcessExt(getTenantAccessor(), processId);
    }
}
