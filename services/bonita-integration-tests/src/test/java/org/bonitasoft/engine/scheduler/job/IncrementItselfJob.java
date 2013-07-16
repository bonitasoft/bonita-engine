package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;

/**
 * @author Baptiste Mesta
 */
public class IncrementItselfJob extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private static int value = 0;

    @Override
    public void execute() throws JobExecutionException {
        value++;
    }

    public static int getValue() {
        return value;
    }

    public static void reset() {
        value = 0;
    }

    @Override
    public String getDescription() {
        return "Increment itself ";
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        super.setAttributes(attributes);
    }

}
