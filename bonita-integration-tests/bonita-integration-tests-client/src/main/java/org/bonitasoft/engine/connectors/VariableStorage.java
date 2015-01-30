/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.connectors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Matthieu Chaffotte
 */
public final class VariableStorage implements Serializable {

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

    public Map<String, Object> getVariables() {
        return variables;
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
