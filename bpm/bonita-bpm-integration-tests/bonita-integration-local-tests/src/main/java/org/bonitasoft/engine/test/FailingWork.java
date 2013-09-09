package org.bonitasoft.engine.test;

import org.bonitasoft.engine.execution.work.AbstractBonitaWork;

final class FailingWork extends AbstractBonitaWork {

    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "MyJob";
    }

    @Override
    protected void work() throws Exception {
        throw new Exception("an unexpected exception");

    }

    @Override
    protected boolean isTransactional() {
        return true;
    }

    @Override
    protected void handleFailure(final Exception e) throws Exception {
        throw new Exception("unable to handle failure");
    }

    @Override
    protected String getRecoveryProcedure() {
        return "The recovery procedure";
    }
}
