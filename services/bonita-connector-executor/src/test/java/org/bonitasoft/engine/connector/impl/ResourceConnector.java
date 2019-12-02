/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.connector.impl;

import java.util.Map;

import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;

public class ResourceConnector implements SConnector {

    private final String resourceName;

    public ResourceConnector(final String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public void setInputParameters(final Map<String, Object> parameters) {
        // Nothing to do
    }

    @Override
    public void validate() throws SConnectorValidationException {
        // Nothing to do
    }

    @Override
    public Map<String, Object> execute() throws SConnectorException {
        try {
            Thread.currentThread().getContextClassLoader().loadClass(resourceName);
            return null;
        } catch (final ClassNotFoundException cnfe) {
            throw new SConnectorException(cnfe);
        }
    }

    @Override
    public void connect() throws SConnectorException {
        // Nothing to do
    }

    @Override
    public void disconnect() throws SConnectorException {
        // Nothing to do
    }

}
