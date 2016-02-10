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
package org.bonitasoft.engine.connectors;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Baptiste Mesta
 */
public class TestConnectorThatThrowException extends AbstractConnector {

    public static final String BUSINESS_LOGIC_EXCEPTION_MESSAGE = "unexpected ══════════════════════════ during connector execution";

    public static final String DISCONNECT = "disconnect";

    public static final String CONNECT = "connect";

    public static final String NONE = "none";

    public static final String RUNTIME = "runtime";

    public static final String KIND = "kind";

    public static final String NORMAL = "normal";

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final Object inputParameter = getInputParameter(KIND);
        if (!NORMAL.equals(inputParameter) && !RUNTIME.equals(inputParameter) && !NONE.equals(inputParameter) && !CONNECT.equals(inputParameter)) {
            throw new ConnectorValidationException("bad kind of exception");
        }
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        final String kind = (String) getInputParameter(KIND);
        if (kind.equals(NORMAL)) {
            throw new ConnectorException(BUSINESS_LOGIC_EXCEPTION_MESSAGE);
        } else if (kind.equals(RUNTIME)) {
            throw new RuntimeException("unexpected");
        }
    }

    @Override
    public void connect() {
        final String kind = (String) getInputParameter(KIND);
        if (kind.equals(CONNECT)) {
            throw new RuntimeException("unexpected error in connect");
        }
    }

    @Override
    public void disconnect() {
        final String kind = (String) getInputParameter(KIND);
        if (kind.equals(DISCONNECT)) {
            throw new RuntimeException("unexpected error in connect");
        }
    }

}
