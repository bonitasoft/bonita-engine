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
