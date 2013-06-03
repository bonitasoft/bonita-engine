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

import java.util.Map;

import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;

/**
 * @author Feng Hui
 */
public interface SConnector {

    /**
     * Set the input parameter for a connector.
     * 
     * @param parameters
     *            parameters is a map with parameter names and their value.
     */
    void setInputParameters(Map<String, Object> parameters);

    /**
     * Validate the input parameters. Check the parameters types and boundaries.
     * 
     * @throws SConnectorValidationException
     */
    void validate() throws SConnectorValidationException;

    /**
     * Execute the connector.
     * 
     * @return the connector outputs map corresponding to the output definition.
     * @throws SConnectorException
     */
    Map<String, Object> execute() throws SConnectorException;

    /**
     * Called by the engine before the connector is executed
     * This method can be implemented by connectors to handle here opening of connections like database connection
     * 
     * @throws SConnectorException
     */
    void connect() throws SConnectorException;

    /**
     * Called by the engine after the connector and its output operations are executed
     * This method can be implemented by connectors to close connections here.
     * The typical use of this is to be able to return connected objects that will be used in output operation and then disconnect them.
     * 
     * @throws SConnectorException
     */
    void disconnect() throws SConnectorException;
}
