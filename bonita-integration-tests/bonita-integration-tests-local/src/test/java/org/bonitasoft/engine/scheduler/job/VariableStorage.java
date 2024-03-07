/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
        return getVariableValue(name, null);
    }

    public <T> T getVariableValue(final String name, T defaultValue) {
        synchronized (lock) {
            return ((T) variables.getOrDefault(name, defaultValue));
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
