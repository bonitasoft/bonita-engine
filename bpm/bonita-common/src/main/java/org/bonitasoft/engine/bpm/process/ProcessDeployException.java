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
package org.bonitasoft.engine.bpm.process;

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * Thrown when a process fails to deploy.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessDeployException extends ExecutionException {

    private static final long serialVersionUID = 3104389074405599228L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public ProcessDeployException(final String message) {
        super(message);
    }

    public ProcessDeployException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value
     *        is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ProcessDeployException(final Throwable cause) {
        super(cause);
    }

    /**
     * Get the identifier of the process definition who failed.
     *
     * @return The identifier of the process definition who failed.
     * @deprecated always return 0, as the processDefinitionId cannot be determined if process failed to deploy
     */
    @Deprecated(forRemoval = true, since = "8.0")
    public Long getProcessDefinitionId() {
        return 0L;
    }

}
