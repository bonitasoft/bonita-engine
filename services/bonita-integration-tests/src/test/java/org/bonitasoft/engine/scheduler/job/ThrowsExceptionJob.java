package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.StatelessJob;

/**
 * @author Elias Ricken de Medeiros
 */
public class ThrowsExceptionJob implements StatelessJob {

    private static final long serialVersionUID = 3528070481384646426L;

    @Override
    public String getDescription() {
        return "Job to throw a exception";
    }

    @Override
    public void execute() throws JobExecutionException {
        throw new JobExecutionException("exception");
    }

    @Override
    public String getName() {
        return "exception";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
    }

    @Override
    public boolean isWrappedInTransaction() {
        return true;
    }

}
