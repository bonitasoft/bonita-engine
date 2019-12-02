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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * thrown when a {@link User} is not found in the organization
 *
 * @author Matthieu Chaffotte
 * @see User
 * @since 6.0.0
 */
public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 8392620780257647179L;

    /**
     * creates a new exception instance with the given message
     *
     * @param message the exception message
     */
    public UserNotFoundException(final String message) {
        super(message);
    }

    /**
     * creates a new exception instance with the given exception as cause
     *
     * @param cause the exception cause
     */
    public UserNotFoundException(final Throwable cause) {
        super(cause);
    }

}
