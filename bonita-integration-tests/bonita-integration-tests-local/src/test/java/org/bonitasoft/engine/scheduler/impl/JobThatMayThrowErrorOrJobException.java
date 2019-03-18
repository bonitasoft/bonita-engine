package org.bonitasoft.engine.scheduler.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.scheduler.exception.SJobExecutionException;
import org.bonitasoft.engine.scheduler.job.GroupJob;
import org.bonitasoft.engine.scheduler.job.VariableStorage;

public class JobThatMayThrowErrorOrJobException extends GroupJob {

    static final String TYPE = "type";
    static final String ERROR = "ERROR";
    static final String JOBEXCEPTION = "JOBEXCEPTION";
    static final String NO_EXCEPTION = "NO_EXCEPTION";
    private boolean throwsJobExecutionException;
    private boolean throwsError;
    private VariableStorage variableStorage = VariableStorage.getInstance();

    @Override
    public String getDescription() {
        return "throw error";
    }


    @Override
    public void setAttributes(Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        Serializable type = attributes.get(TYPE);
        throwsJobExecutionException = JOBEXCEPTION.equals(type);
        throwsError = ERROR.equals(type);
    }

    @Override
    public void execute() throws SJobExecutionException {
        if (throwsError) {
            variableStorage.setVariable("nbError", ((Integer) variableStorage.getVariableValue("nbError", 0)) + 1);
            throw new Error("an Error");
        }
        if (throwsJobExecutionException) {
            variableStorage.setVariable("nbJobException", ((Integer) variableStorage.getVariableValue("nbJobException", 0)) + 1);
            throw new SJobExecutionException("a Job exception");
        }
        variableStorage.setVariable("nbSuccess", ((Integer) variableStorage.getVariableValue("nbSuccess", 0)) + 1);
    }
}
