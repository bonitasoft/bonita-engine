package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthieu Chaffotte
 */
public class VariableStorageByTenant implements Serializable {

    private static final long serialVersionUID = -4195221111626812999L;

    private final Map<String, Object> variables;

    private final Object lock = new Object();

    private VariableStorageByTenant() {
        variables = new HashMap<String, Object>();
    }

    private static class VariableStorageHolder {

        public static final Map<Long, VariableStorageByTenant> instances = new HashMap<Long, VariableStorageByTenant>();
    }

    public static VariableStorageByTenant getInstance(final long tenantId) {
        VariableStorageByTenant variableStorage = VariableStorageHolder.instances.get(tenantId);
        if (variableStorage == null) {
            variableStorage = new VariableStorageByTenant();
            VariableStorageHolder.instances.put(tenantId, variableStorage);
        }
        return variableStorage;
    }

    public void setVariable(final String name, final Object value) {
        synchronized (lock) {
            variables.put(name, value);
        }
    }

    public Object getVariableValue(final String name) {
        synchronized (lock) {
            return variables.get(name);
        }
    }

    public static void clearAll() {
        for (final Entry<Long, VariableStorageByTenant> entry : VariableStorageHolder.instances.entrySet()) {
            entry.getValue().clear();
        }
        VariableStorageHolder.instances.clear();
    }

    public void clear() {
        synchronized (lock) {
            variables.clear();
        }
    }

}
