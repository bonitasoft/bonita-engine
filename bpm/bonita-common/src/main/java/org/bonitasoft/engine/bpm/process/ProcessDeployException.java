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
 * Thrown when a process fails to deploy. <br>
 * It also gives access to the ID of the process Definition that tried to be deployed, through method {@link #getProcessDefinitionId()}.
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see #getProcessDefinitionId()
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessDeployException extends ExecutionException {

    private static final long serialVersionUID = 3104389074405599228L;

    private Long processDefinitionId;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public ProcessDeployException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ProcessDeployException(final Throwable cause) {
        super(cause);
    }

    /**
     * Get the identifier of the process definition who failed.
     * 
     * @return The identifier of the process definition who failed.
     */
    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

}
