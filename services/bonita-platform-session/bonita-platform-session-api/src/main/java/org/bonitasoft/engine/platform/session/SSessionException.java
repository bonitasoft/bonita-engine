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
package org.bonitasoft.engine.platform.session;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Elias Ricken de Medeiros
 */
public class SSessionException extends SBonitaException {

    private static final long serialVersionUID = 7690456826693549647L;

    public SSessionException(final String message) {
        super(message);
    }

    public SSessionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SSessionException(final Object... arguments) {
        super(arguments);
    }

    public SSessionException(final Throwable cause, final Object... arguments) {
        super(cause, arguments);
    }

    public SSessionException(final Throwable cause) {
        super(cause);
    }

}
