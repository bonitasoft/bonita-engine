package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.BonitaWork;

public class FailureHandlingBonitaWorkExtended extends FailureHandlingBonitaWork {

    private final TenantServiceAccessor tenantServiceAccessor;

    public FailureHandlingBonitaWorkExtended(final BonitaWork work, final TenantServiceAccessor tenantServiceAccessor) {
        super(work);
        this.tenantServiceAccessor = tenantServiceAccessor;
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        return tenantServiceAccessor;
    }
}
