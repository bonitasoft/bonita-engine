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
package org.bonitasoft.engine.core.connector.exception;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Celine Souchet
 */
public class SConnectorDefinitionNotFoundException extends SBonitaException {

    private static final long serialVersionUID = -4558945817033278955L;

    public SConnectorDefinitionNotFoundException(final long id) {
        super("Unable to find connector definition with id \"" + id + "\".");
    }

    public SConnectorDefinitionNotFoundException(final String message) {
        super(message);
    }

    public SConnectorDefinitionNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SConnectorDefinitionNotFoundException(final Throwable cause) {
        super(cause);
    }

}
