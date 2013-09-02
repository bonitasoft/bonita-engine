/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.connectors;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Baptiste Mesta
 */
public class TestConnectorThatThrowException extends AbstractConnector {

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final Object inputParameter = getInputParameter("kind");
        if (!"normal".equals(inputParameter) && !"runtime".equals(inputParameter) && !"none".equals(inputParameter) && !"connect".equals(inputParameter)) {
            throw new ConnectorValidationException("bad kind of exception");
        }
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final String kind = (String) getInputParameter("kind");
        if (kind.equals("normal")) {
            throw new ConnectorException("unexpected");
        } else if (kind.equals("runtime")) {
            throw new RuntimeException("unexpected");
        }
    }

    @Override
    public void connect() throws ConnectorException {
        final String kind = (String) getInputParameter("kind");
        if (kind.equals("connect")) {
            throw new RuntimeException("unexpected error in connect");
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        final String kind = (String) getInputParameter("kind");
        if (kind.equals("disconnect")) {
            throw new RuntimeException("unexpected error in connect");
        }
    }

}
