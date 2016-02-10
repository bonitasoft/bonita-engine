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

import java.util.Map;

/**
 * @author Feng Hui
 */
public interface Connector {

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
     * @throws ConnectorValidationException
     *      when the input parameters are not valid
     */
    void validateInputParameters() throws ConnectorValidationException;

    /**
     * Execute the connector.
     * 
     * @return the connector outputs map corresponding to the output definition.
     * @throws ConnectorException
     *      when something went wrong during connector execution
     */
    Map<String, Object> execute() throws ConnectorException;

    /**
     * Called by the engine before the connector is executed
     * This method can be implemented by connectors to handle here opening of connections like database connection
     * 
     * @throws ConnectorException
     *      when something went wrong during connector connection
     */
    void connect() throws ConnectorException;

    /**
     * Called by the engine after the connector and its output operations are executed
     * This method can be implemented by connectors to close connections here.
     * The typical use of this is to be able to return connected objects that will be used in output operation and then disconnect them.
     * 
     * @throws ConnectorException
     *      when something went wrong during connector disconnection
     */
    void disconnect() throws ConnectorException;
}
