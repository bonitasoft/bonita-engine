package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThrowsExceptionJob implements StatelessJob {

    private static final long serialVersionUID = 3528070481384646426L;

    private boolean throwException = true;

    @Override
    public String getDescription() {
        return "Job to throw a exception";
    }

    @Override
    public void execute() throws SJobExecutionException {
        if (throwException) {
            throw new SJobExecutionException("exception");
        }
    }

    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        final Boolean result = (Boolean) attributes.get("throwException");
        if (result != null) {
            throwException = result;
        }
    }

}
