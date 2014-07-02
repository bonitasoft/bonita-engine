package org.bonitasoft.engine.scheduler.job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class VariableStorage implements Serializable {

    private static final long serialVersionUID = -4195221111626812999L;
    private final Object lock = new Object();
    private final Map<String, Object> variables;
    public static final VariableStorage INSTANCE = new VariableStorage();

    private VariableStorage() {
        variables = new HashMap<String, Object>();
    }

    public static VariableStorage getInstance() {
        return INSTANCE;
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
        INSTANCE.clear();
    }

    public void clear() {
        synchronized (lock) {
            variables.clear();
        }
    }

}
