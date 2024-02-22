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
package org.bonitasoft.test.toolkit.exception;

/**
 * @author Vincent Elcrin
 */
public class TestToolkitException extends RuntimeException {

    /**
     * ID used by serialization.
     */
    private static final long serialVersionUID = -2678731898708770494L;

    /**
     * Default constructor.
     */
    public TestToolkitException() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param message
     */
    public TestToolkitException(final String message) {
        super(message);
    }

    /**
     * Default constructor.
     *
     * @param message
     */
    public TestToolkitException(final String message, final Exception e) {
        super(message, e);
    }
}
