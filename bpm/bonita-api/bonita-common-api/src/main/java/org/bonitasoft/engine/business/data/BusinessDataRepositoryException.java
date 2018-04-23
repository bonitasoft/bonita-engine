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
package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * Thrown if an exception occurs dealing with the Business Data Repository.
 *
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class BusinessDataRepositoryException extends BonitaException {

    private static final long serialVersionUID = -1056166500737611443L;

    /**
     * Constructs a BusinessDataRepositoryException with the specified cause.
     *
     * @param cause the cause
     */
    public BusinessDataRepositoryException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BusinessDataRepositoryException with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessDataRepositoryException(final String message) {
        super(message);
    }

    /**
     * Constructs a BusinessDataRepositoryException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessDataRepositoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
