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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;

/**
 * @author Feng Hui
 * @author Romain Bioteau
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class AbstractConnector implements Connector {

    private final Map<String, Object> inputParameters;

    private final Map<String, Object> outputParameters;

    protected APIAccessor apiAccessor;

    private EngineExecutionContext executionContext;

    public AbstractConnector() {
        inputParameters = new HashMap<String, Object>();
        outputParameters = new HashMap<String, Object>();
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        inputParameters.putAll(parameters);
    }

    protected Object getInputParameter(final String paramName) {
        return inputParameters.get(paramName);
    }

    /**
     * get the input parameter or the default value if the parameter is not set
     * 
     * @param parameterKey
     *      name of the parameter
     * @param defaultValue
     *      value of the parameter if not set
     * @return
     *      the value of the parameter
     */
    protected Object getInputParameter(final String parameterKey, final Serializable defaultValue) {
        final Object param = getInputParameter(parameterKey);
        return param == null ? defaultValue : param;
    }

    protected void setOutputParameter(final String paramName, final Object value) {
        outputParameters.put(paramName, value);
    }

    protected Map<String, Object> getOutputParameters() {
        return outputParameters;
    }

    @Override
    public final Map<String, Object> execute() throws ConnectorException {
        executeBusinessLogic();
        return getOutputParameters();
    }

    @Override
    public void connect() throws ConnectorException {
        // default implementation do nothing
    }

    @Override
    public void disconnect() throws ConnectorException {
        // default implementation do nothing
    }

    protected abstract void executeBusinessLogic() throws ConnectorException;

    public void setAPIAccessor(final APIAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
    }

    public APIAccessor getAPIAccessor() {
        return apiAccessor;
    }

    public EngineExecutionContext getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(final EngineExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}
