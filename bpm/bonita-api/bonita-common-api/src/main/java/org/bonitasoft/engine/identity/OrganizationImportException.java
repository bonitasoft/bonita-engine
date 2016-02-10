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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * thrown to indicate an organization import error
 *
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @since 6.0.0
 */
public class OrganizationImportException extends ExecutionException {

    private static final long serialVersionUID = -6086310296760629566L;

    /**
     * create a new exception instance with the given message
     *
     * @param message the exception message
     */
    public OrganizationImportException(final String message) {
        super(message);
    }

    /**
     * creates a new exception instance with the given exception as cause
     *
     * @param cause the exception cause
     */
    public OrganizationImportException(final Throwable cause) {
        super(cause);
    }

}
