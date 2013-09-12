package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

public abstract class TenantAwareBonitaWork extends BonitaWork {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String TENANT_ACCESSOR = "tenantAccessor";

    public TenantAwareBonitaWork() {
        super();
    }

    protected TenantServiceAccessor getTenantAccessor(final Map<String, Object> context) {
        return (TenantServiceAccessor) context.get(FailureHandlingBonitaWork.TENANT_ACCESSOR);
    }

}
