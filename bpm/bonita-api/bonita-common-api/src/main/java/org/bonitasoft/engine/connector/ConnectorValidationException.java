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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Baptiste Mesta
 */
public class ConnectorValidationException extends BonitaException {

    private static final long serialVersionUID = -7641578992494154217L;

    public ConnectorValidationException(final String message) {
        super(message);
    }

    public ConnectorValidationException(final Connector connector, final String... messages) {
        super(getMessage(connector, Arrays.asList(messages)));
    }

    public ConnectorValidationException(final Connector connector, final List<String> messages) {
        super(getMessage(connector, messages));
    }

    private static String getMessage(final Connector connector, final List<String> messages) {
        final StringBuilder mergedMessages = new StringBuilder("Error validating connector ");
        mergedMessages.append(connector.getClass().getName());
        mergedMessages.append(":\n");
        for (final String message : messages) {
            mergedMessages.append(message);
            mergedMessages.append('\n');
        }
        return mergedMessages.toString();
    }

}
