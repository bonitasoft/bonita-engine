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
package org.bonitasoft.engine.core.connector.impl;

import java.util.Map;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;

/**
 * Adapter to execute client connector objects in the server side
 * 
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class SConnectorAdapter implements SConnector {

    private final Connector connector;

    public SConnectorAdapter(final Connector connector) {
        NullCheckingUtil.checkArgsNotNull(connector);
        this.connector = connector;
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        final APIAccessor apiAccessor = (APIAccessor) parameters.remove("connectorApiAccessor");
        final EngineExecutionContext executionContext = (EngineExecutionContext) parameters.remove("engineExecutionContext");
        if (connector instanceof AbstractConnector) {
            ((AbstractConnector) connector).setAPIAccessor(apiAccessor);
            if (executionContext != null) {
                ((AbstractConnector) connector).setExecutionContext(executionContext);
            }
        }
        connector.setInputParameters(parameters);
    }

    @Override
    public void validate() throws SConnectorValidationException {
        try {
            connector.validateInputParameters();
        } catch (final ConnectorValidationException e) {
            throw new SConnectorValidationException(e);
        }
    }

    @Override
    public Map<String, Object> execute() throws SConnectorException {
        try {
            return connector.execute();
        } catch (final ConnectorException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public void connect() throws SConnectorException {
        try {
            connector.connect();
        } catch (final ConnectorException e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public void disconnect() throws SConnectorException {
        try {
            connector.disconnect();
        } catch (final ConnectorException e) {
            throw new SConnectorException(e);
        }
    }

}
