package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class UpdateVariable extends GroupJob {

    private static final long serialVersionUID = 8379781766551862114L;

    private String variableName;

    private Object variableValue;

    public UpdateVariable(final String variableName, final Object variableValue) {
        super();
        this.variableName = variableName;
        this.variableValue = variableValue;
    }

    @Override
    public void execute() {
        final VariableStorage storage = VariableStorage.getInstance();
        storage.setVariable(variableName, variableValue);
    }

    @Override
    public String getDescription() {
        return "Change the value of " + variableName + " with " + variableValue;
    }

    @Override
    public void setAttributes(final Map<String, Serializable> attributes) {
        super.setAttributes(attributes);
        variableName = (String) attributes.get("variableName");
        variableValue = attributes.get("variableValue");
    }

}
