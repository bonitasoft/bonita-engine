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
 * Thrown when a reference to an inexistant {@link ProcessInstance} is made, generally through its ID.
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ProcessInstanceNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 2829055979970789709L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public ProcessInstanceNotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ProcessInstanceNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception and the message with the identifier of the process instance.
     * 
     * @param processInstanceId
     *        The identifier of the process instance
     */
    public ProcessInstanceNotFoundException(final long processInstanceId) {
        super("No process instance found !!");
        setProcessInstanceIdOnContext(processInstanceId);
    }

}
