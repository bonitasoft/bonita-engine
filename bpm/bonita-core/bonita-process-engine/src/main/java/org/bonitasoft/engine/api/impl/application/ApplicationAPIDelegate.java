package org.bonitasoft.engine.api.impl.application;

import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationAPIDelegate {

    private TenantServiceAccessor tenantAccessor;

    public ApplicationAPIDelegate(TenantServiceAccessor tenantAccessor) {
        this.tenantAccessor = tenantAccessor;
    }

    public ExecutionResult deployApplication(byte[] applicationArchive) {
        return ExecutionResult.OK;
    }

}
