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
package org.bonitasoft.engine.core.connector.exception;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SConnectorInstanceNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -4558945817033278955L;

    public SConnectorInstanceNotFoundException(final long connectorInstanceId) {
        super("Unable to find connector instance with id " + connectorInstanceId);
    }

    public SConnectorInstanceNotFoundException(final String message) {
        super(message);
    }

    public SConnectorInstanceNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SConnectorInstanceNotFoundException(final Throwable cause) {
        super(cause);
    }

}
