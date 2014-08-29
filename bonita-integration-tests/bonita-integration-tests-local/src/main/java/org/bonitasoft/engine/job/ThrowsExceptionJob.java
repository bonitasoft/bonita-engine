package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThrowsExceptionJob implements StatelessJob {

    private static final long serialVersionUID = 3528070481384646426L;

    private boolean throwException = true;

    @Override
    public String getDescription() {
        return "Job that throws a exception";
    }

    @Override
    public void execute() throws SJobExecutionException {
        if (throwException) {
            throw new SJobExecutionException("This job throws an arbitrary exception if parameter 'throwException' is provided.");
        }
    }

    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        final Boolean result = (Boolean) attributes.get("throwException");
        if (result != null) {
            throwException = result;
        }
    }

}
