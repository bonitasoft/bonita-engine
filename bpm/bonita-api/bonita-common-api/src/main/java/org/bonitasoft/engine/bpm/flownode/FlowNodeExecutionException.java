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
package org.bonitasoft.engine.bpm.flownode;

import org.bonitasoft.engine.exception.ExecutionException;

/**
 * Thrown when a BPM flow node fails to execute.
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class FlowNodeExecutionException extends ExecutionException {

    private static final long serialVersionUID = 1873896404277831296L;

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     *            The detail message why the flow node failed to execute(which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public FlowNodeExecutionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *            The cause of the flow node failure (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public FlowNodeExecutionException(final Throwable cause) {
        super(cause);
    }

}
