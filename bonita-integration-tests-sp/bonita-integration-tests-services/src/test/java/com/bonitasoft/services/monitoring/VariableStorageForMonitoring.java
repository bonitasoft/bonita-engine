/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services.monitoring;

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
