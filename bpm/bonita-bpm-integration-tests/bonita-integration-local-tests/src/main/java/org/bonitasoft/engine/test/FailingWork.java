package org.bonitasoft.engine.test;

import java.util.Map;

import org.bonitasoft.engine.work.BonitaWork;

final class FailingWork extends BonitaWork {

    private static final long serialVersionUID = 1L;

    @Override
    public String getDescription() {
        return "MyJob";
    }

    @Override
    public String getRecoveryProcedure() {
        return "The recovery procedure";
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        throw new Exception("an unexpected exception");

    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        throw new Exception("unable to handle failure");
    }
}
