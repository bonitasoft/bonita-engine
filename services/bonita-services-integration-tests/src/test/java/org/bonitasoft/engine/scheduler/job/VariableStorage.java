package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthieu Chaffotte
 */
public class VariableStorage implements Serializable {

    private static final long serialVersionUID = -4195221111626812999L;

    private final Map<String, Object> variables;

    private VariableStorage() {
        variables = new HashMap<String, Object>();
    }

    private static class VariableStorageHolder {

        public static final VariableStorage INSTANCE = new VariableStorage();

        public static final Map<Long, VariableStorage> instances = new HashMap<Long, VariableStorage>();
    }

    public static VariableStorage getInstance() {
        return VariableStorageHolder.INSTANCE;
    }

    public static VariableStorage getInstance(final long tenantId) {
        VariableStorage variableStorage = VariableStorageHolder.instances.get(tenantId);
        if (variableStorage == null) {
            variableStorage = new VariableStorage();
            VariableStorageHolder.instances.put(tenantId, variableStorage);
        }
        return variableStorage;
    }

    public synchronized void setVariable(final String name, final Object value) {
        variables.put(name, value);
    }

    public Object getVariableValue(final String name) {
        return variables.get(name);
    }

    public static void clearAll() {
        VariableStorageHolder.INSTANCE.clear();
        for (final Entry<Long, VariableStorage> entry : VariableStorageHolder.instances.entrySet()) {
            entry.getValue().clear();
        }
        VariableStorageHolder.instances.clear();
    }

    public void clear() {
        variables.clear();
    }

}
