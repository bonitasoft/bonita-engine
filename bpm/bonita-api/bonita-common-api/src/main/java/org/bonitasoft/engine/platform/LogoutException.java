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
package org.bonitasoft.engine.platform;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Indicates that a problem occurred during tenant logout action
 *
 * @author Matthieu Chaffotte
 */
public class LogoutException extends BonitaException {

    private static final long serialVersionUID = -5327606222750615923L;

    /**
     * @param message a String indicating the exception message
     */
    public LogoutException(final String message) {
        super(message);
    }

    /**
     * @param cause a Throwable indicating the root cause
     */
    public LogoutException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message a String indicating the exception message
     * @param cause a Throwable indicating the root cause
     */
    public LogoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
