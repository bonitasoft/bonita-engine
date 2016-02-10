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
package org.bonitasoft.engine.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public abstract class AbstractSelectWithParametersDescriptor<T> extends AbstractSelectDescriptor<T> {

    private Map<String, Object> inputParameters;

    public AbstractSelectWithParametersDescriptor(final String queryName, final Map<String, Object> inputParameters,
            final Class<? extends PersistentObject> entityType) {
        super(queryName, entityType, (Class<T>) entityType);
        this.inputParameters = inputParameters;
    }

    public AbstractSelectWithParametersDescriptor(final String queryName, final Map<String, Object> inputParameters,
            final Class<? extends PersistentObject> entityType, final Class<T> returnType) {
        super(queryName, entityType, returnType);
        this.inputParameters = inputParameters;
    }

    public Map<String, Object> getInputParameters() {
        if (this.inputParameters == null) {
            return Collections.unmodifiableMap(new HashMap<String, Object>());
        }
        return Collections.unmodifiableMap(inputParameters);
    }

    public Object getInputParameter(final String key) {
        return getInputParameters().get(key);
    }

    public void addInputParameter(final String key, final Object value) {
        if (inputParameters == null) {
            this.inputParameters = new HashMap<String, Object>();
        }
        this.inputParameters.put(key, value);
    }

}
