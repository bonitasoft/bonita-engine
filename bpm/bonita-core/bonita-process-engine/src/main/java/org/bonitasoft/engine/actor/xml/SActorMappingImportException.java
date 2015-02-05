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
package org.bonitasoft.engine.actor.xml;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * Thrown when it's not possible to import the actor mappings.
 * 
 * The class SActorMappingImportException is a form of Throwable that indicates conditions that a reasonable application might want to catch.
 * The class SActorMappingImportException that is not also subclasses of {@link RuntimeException} are checked exceptions.
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SActorMappingImportException extends SBonitaException {

    private static final long serialVersionUID = -6338979347415161630L;

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public SActorMappingImportException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public SActorMappingImportException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public SActorMappingImportException(final Throwable cause) {
        super(cause);
    }
}
