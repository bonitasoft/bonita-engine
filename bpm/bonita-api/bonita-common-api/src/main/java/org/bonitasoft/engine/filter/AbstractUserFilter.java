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
package org.bonitasoft.engine.filter;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;

/**
 * @author Feng Hui
 * @author Romain Bioteau
 * @author Baptiste Mesta
 */
public abstract class AbstractUserFilter implements UserFilter {

    private final Map<String, Object> inputParameters;

    private APIAccessor apiAccessor;

    private EngineExecutionContext executionContext;

    public AbstractUserFilter() {
        inputParameters = new HashMap<String, Object>();
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        inputParameters.putAll(parameters);
    }

    protected Object getInputParameter(final String paramName) throws IllegalStateException {
        return inputParameters.get(paramName);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getOptinalInputParameter(final String paramName) {
        return (T) inputParameters.get(paramName);
    }

    protected String getStringInputParameter(final String paramName) {
        return (String) getInputParameter(paramName);
    }

    protected void validateStringInputParameterIsNotNulOrEmpty(final String paramName) throws ConnectorValidationException {
        final String paramValue = (String) getInputParameter(paramName);
        if (paramValue == null || "".equals(paramValue.trim())) {
            throw new ConnectorValidationException("The input parameter '" + paramName + "' cannot be null or empty");
        }
    }

    /**
     * {@inheritDoc} Default implementation returns true
     */
    @Override
    public boolean shouldAutoAssignTaskIfSingleResult() {
        return true;
    }

    public void setAPIAccessor(final APIAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
    }

    public void setExecutionContext(final EngineExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public APIAccessor getAPIAccessor() {
        return apiAccessor;
    }

    public EngineExecutionContext getExecutionContext() {
        return executionContext;
    }

}
