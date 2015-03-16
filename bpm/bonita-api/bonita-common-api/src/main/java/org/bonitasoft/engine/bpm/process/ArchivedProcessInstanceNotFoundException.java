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
package org.bonitasoft.engine.bpm.process;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * <p>Thrown when it's not possible to find the archived process instance.</p>
 * <p>The class ArchivedProcessInstanceNotFoundException is a form of Throwable that indicates conditions that a reasonable application might want to catch.<br>
 * The class ArchivedProcessInstanceNotFoundException that is not also subclasses of {@link RuntimeException} are checked exceptions.<br>
 * Checked exceptions need to be declared in a method or constructor's {@literal throws} clause if they can be thrown by the execution of the method or
 * constructor and propagate outside the method or constructor boundary.</p>
 * 
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class ArchivedProcessInstanceNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 8745379570098982138L;

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ArchivedProcessInstanceNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception and its message
     * 
     * @param archivedProcessInstanceId
     *            The identifier of the archived process instance not found
     * 
     */
    public ArchivedProcessInstanceNotFoundException(final long archivedProcessInstanceId) {
        super("Archived process instance with id <" + archivedProcessInstanceId + "> not found");
    }

}
