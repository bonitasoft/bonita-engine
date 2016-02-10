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
package org.bonitasoft.engine.exception;

/**
 * Thrown when it's not possible to create a object, because it already exists.
 * The class AlreadyExistsException is a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class AlreadyExistsException that is not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class AlreadyExistsException extends CreationException {

    private static final long serialVersionUID = 6709231545504567159L;
    private String name;

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public AlreadyExistsException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public AlreadyExistsException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and name of existing object.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param name the name of the conflicting object
     * @since 6.4
     */
    public AlreadyExistsException(final String message, String name) {
        super(message);
        this.name = name;
    }

    /**
     * Retrieves the name of conflicting object or null if the name was not supplied
     * 
     * @return the name of conflicting object or null if the name was not supplied.
     * @since 6.4
     */
    public String getName() {
        return name;
    }
}
