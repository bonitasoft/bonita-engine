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
