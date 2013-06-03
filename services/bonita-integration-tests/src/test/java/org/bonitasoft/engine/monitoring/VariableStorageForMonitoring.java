package org.bonitasoft.engine.monitoring;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class VariableStorageForMonitoring implements Serializable {

    private static final long serialVersionUID = -4195221111626812999L;

    private final Map<String, Object> variables;

    private VariableStorageForMonitoring() {
        this.variables = new HashMap<String, Object>();
    }

    private static class VariableStorageHolder {

        public static final VariableStorageForMonitoring INSTANCE = new VariableStorageForMonitoring();
    }

    public static VariableStorageForMonitoring getInstance() {
        return VariableStorageHolder.INSTANCE;
    }

    public synchronized void setVariable(final String name, final Object value) {
        this.variables.put(name, value);
    }

    public Object getVariableValue(final String name) {
        return this.variables.get(name);
    }

    public void clear() {
        this.variables.clear();
    }

}
