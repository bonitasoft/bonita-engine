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
package org.bonitasoft.engine.connector.impl;

import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;

/**
 * Execute connectors directly
 * 
 * @author Baptiste Mesta
 */
public class ConnectorExecutorImpl implements ConnectorExecutor {

    public ConnectorExecutorImpl() {
    }

    @Override
    public Map<String, Object> execute(final SConnector sConnector, final Map<String, Object> inputParameters) throws SConnectorException {
        try {
            sConnector.setInputParameters(inputParameters);
            try {
                sConnector.validate();
            } catch (final SConnectorValidationException e) {
                throw new SConnectorException(e);
            }
            sConnector.connect();
            return sConnector.execute();
        } catch (final SConnectorException e) {
            throw e;
        } catch (final Throwable e) {
            throw new SConnectorException(e);
        }
    }

    @Override
    public void disconnect(final SConnector sConnector) throws SConnectorException {
        sConnector.disconnect();
    }

}
