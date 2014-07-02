package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Matthieu Chaffotte
 */
public class IncrementVariableJobWithMultiTenancy extends GroupJob {

    private static final long serialVersionUID = 3707724945060118636L;

    private String variableName;

    private int throwExceptionAfterNIncrements;

    private static ReadSessionAccessor readSessionAccessor;

    @Override
    public void execute() throws SJobExecutionException {
        synchronized (IncrementVariableJobWithMultiTenancy.class) {

            VariableStorageByTenant storage;
            try {
                storage = VariableStorageByTenant.getInstance(readSessionAccessor.getTenantId());
            } catch (final STenantIdNotSetException e) {
                throw new SJobExecutionException(e);
            }
            final Integer value = (Integer) storage.getVariableValue(variableName);
            if (value == null) {
                storage.setVariable(variableName, 1);
            } else if (value + 1 == throwExceptionAfterNIncrements) {
                throw new SJobExecutionException("Increment reached");
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
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        variableName = (String) attributes.get("variableName");
        throwExceptionAfterNIncrements = (Integer) attributes.get("throwExceptionAfterNIncrements");
    }

    public static void setSessionAccessor(final ReadSessionAccessor readSessionAccessor) {
        IncrementVariableJobWithMultiTenancy.readSessionAccessor = readSessionAccessor;
    }

}
