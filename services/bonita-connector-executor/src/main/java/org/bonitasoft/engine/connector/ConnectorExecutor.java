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

import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.connector.exception.SConnectorException;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface ConnectorExecutor extends TenantLifecycleService {

    /**
     * Executes a connector.
     *
     * @param sConnector
     *        The connector will be executed
     * @param inputParameters
     *        The input parameters of connector
     * @param classLoader
     *        The classLoader within the connector will be executed
     * @return
     *         The output after connector executing
     * @throws SConnectorException
     *         Error thrown when error occurs in connector executing
     */
    Map<String, Object> execute(SConnector sConnector, Map<String, Object> inputParameters, final ClassLoader classLoader) throws SConnectorException;

    /**
     * call disconnect method of the connector
     *
     * @param sConnector
     * @throws SConnectorException
     */
    void disconnect(SConnector sConnector) throws SConnectorException;

}
