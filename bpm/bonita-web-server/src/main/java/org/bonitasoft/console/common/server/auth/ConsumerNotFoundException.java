/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.auth;

import javax.servlet.ServletException;

/**
 * This class cannot be removed without the risk of breaking
 * old {@link AuthenticationManager} implementations that may still use it
 *
 * @deprecated since 7.16.0
 * @author Chong Zhao
 */
@Deprecated
public class ConsumerNotFoundException extends ServletException {

    /**
     * serial UID
     */
    private static final long serialVersionUID = -8891020375098355542L;

    /**
     * Constructor
     */
    public ConsumerNotFoundException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public ConsumerNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public ConsumerNotFoundException(final String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ConsumerNotFoundException(final Throwable cause) {
        super(cause);
    }

}
