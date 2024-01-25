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

import org.bonitasoft.engine.InvalidFileFormatException;

/**
 * thrown to indicate an organization import error caused by a parsing error
 *
 * @author Danila Mazour
 * @since 7.9.0
 */
public class InvalidOrganizationFileFormatException extends OrganizationImportException
        implements InvalidFileFormatException {

    /**
     * create a new exception instance with the given message
     *
     * @param message the exception message
     */
    public InvalidOrganizationFileFormatException(final String message) {
        super(message);
    }

    /**
     * creates a new exception instance with the given exception as cause
     *
     * @param cause the exception cause
     */
    public InvalidOrganizationFileFormatException(final Throwable cause) {
        super(cause);
    }

}
