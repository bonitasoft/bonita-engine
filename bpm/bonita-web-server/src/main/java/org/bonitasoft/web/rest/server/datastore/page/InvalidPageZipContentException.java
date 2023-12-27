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
package org.bonitasoft.web.rest.server.datastore.page;

/**
 * @author Fabio Lombardi
 */
public class InvalidPageZipContentException extends Exception {

    /**
     * UID
     */
    private static final long serialVersionUID = 1L;

    public InvalidPageZipContentException() {
        super();
    }

    public InvalidPageZipContentException(final String message) {
        super(message);
    }

    public InvalidPageZipContentException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidPageZipContentException(final Throwable cause) {
        super(cause);
    }
}
