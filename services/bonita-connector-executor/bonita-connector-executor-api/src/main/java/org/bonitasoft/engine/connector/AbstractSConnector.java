/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.connector;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;

/**
 * @author Feng Hui
 */
public abstract class AbstractSConnector implements SConnector {

    private final Map<String, Object> inputParameters;

    private final Map<String, Object> outputParameters;

    public AbstractSConnector() {
        inputParameters = new HashMap<String, Object>();
        outputParameters = new HashMap<String, Object>();
    }

    @Override
    public final void setInputParameters(final Map<String, Object> parameters) {
        inputParameters.putAll(parameters);
    }

    protected Object getInputParameter(final String paramName) {
        Object obj = null;
        if (inputParameters.containsKey(paramName)) {
            obj = inputParameters.get(paramName);
        }
        return obj;
    }

    protected void setOutputParameter(final String paramName, final Object value) {
        outputParameters.put(paramName, value);
    }

    protected Map<String, Object> getOutputParameters() {
        return outputParameters;
    }

    @Override
    public abstract void validate() throws SConnectorValidationException;

    @Override
    public abstract Map<String, Object> execute() throws SConnectorException;

}
