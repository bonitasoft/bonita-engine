package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.JobExecutionException;
import org.bonitasoft.engine.scheduler.SJobConfigurationException;
import org.bonitasoft.engine.scheduler.impl.VariableStorage;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Matthieu Chaffotte
 */
public class IncrementVariableJobWithMultiTenancy extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private String variableName;

    private int throwExceptionAfterNIncrements;

    private static ReadSessionAccessor readSessionAccessor;

    @Override
    public void execute() throws JobExecutionException {
        synchronized (IncrementVariableJobWithMultiTenancy.class) {

            VariableStorage storage;
            try {
                storage = VariableStorage.getInstance(readSessionAccessor.getTenantId());
            } catch (final TenantIdNotSetException e) {
                throw new JobExecutionException(e);
            }
            final Integer value = (Integer) storage.getVariableValue(variableName);
            if (value == null) {
                storage.setVariable(variableName, 1);
            } else if (value + 1 == throwExceptionAfterNIncrements) {
                throw new JobExecutionException("Increment reached");
            } else {
                storage.setVariable(variableName, value + 1);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Increment the variable " + variableName;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) throws SJobConfigurationException {
        super.setAttributes(attributes);
        variableName = (String) attributes.get("variableName");
        throwExceptionAfterNIncrements = (Integer) attributes.get("throwExceptionAfterNIncrements");
    }

    public static void setSessionAccessor(final ReadSessionAccessor readSessionAccessor) {
        IncrementVariableJobWithMultiTenancy.readSessionAccessor = readSessionAccessor;
    }

}
