package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.work.BonitaWork;

public abstract class WrappingBonitaWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = 1L;

    private final BonitaWork wrappedWork;

    public WrappingBonitaWork(final BonitaWork wrappedWork) {
        this.wrappedWork = wrappedWork;
        wrappedWork.setParent(this);
    }

    @Override
    public String getDescription() {
        return wrappedWork.getDescription();
    }

    @Override
    public String getRecoveryProcedure() {
        return wrappedWork.getRecoveryProcedure();
    }

    public BonitaWork getWrappedWork() {
        return wrappedWork;
    }

    @Override
    public String toString() {
        return wrappedWork.toString();
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        wrappedWork.handleFailure(e, context);
    }

    @Override
    public void setTenantId(final long tenantId) {
        wrappedWork.setTenantId(tenantId);
    }

    @Override
    public long getTenantId() {
        return wrappedWork.getTenantId();
    }

}
