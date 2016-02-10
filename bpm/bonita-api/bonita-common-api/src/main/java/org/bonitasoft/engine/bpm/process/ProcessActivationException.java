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

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * Thrown when a process definition cannot be enabled / disabled, or when a {@link org.bonitasoft.engine.api.ProcessAPI#startProcess(long)} (and its variants)
 * cannot be performed
 * because the process definition is not enabled.
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long)
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long, long)
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long, java.util.Map)
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long, java.util.List, java.util.Map)
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long, long, java.util.Map)
 * @see org.bonitasoft.engine.api.ProcessAPI#startProcess(long, long, java.util.List, java.util.Map)
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessActivationException extends ExecutionException {

    private static final long serialVersionUID = -425713003229819771L;

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param e
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ProcessActivationException(final Exception e) {
        super(e);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ProcessActivationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public ProcessActivationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception and the message with the given informations of the process definition with the problem.
     * 
     * @param processDefinitionId
     *        The identifier of the process definition
     * @param name
     *        The name of the process definition
     * @param version
     *        The version of the process definition
     */
    public ProcessActivationException(final long processDefinitionId, final String name, final String version) {
        super("The process definition is not enabled !!");
        setProcessDefinitionIdOnContext(processDefinitionId);
        setProcessDefinitionNameOnContext(name);
        setProcessDefinitionVersionOnContext(version);
    }

}
