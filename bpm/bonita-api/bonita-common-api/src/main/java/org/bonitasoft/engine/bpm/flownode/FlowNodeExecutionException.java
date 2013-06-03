/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
 * Thrown when a BPM Flow node fails to execute.
 * 
 * @author Emmanuel Duchastenier
 */
public class FlowNodeExecutionException extends ExecutionException {

    private static final long serialVersionUID = 1873896404277831296L;

    /**
     * @param message
     *            the error message describing why the Flow node failed to execute.
     */
    public FlowNodeExecutionException(final String message) {
        super(message);
    }

    /**
     * @param cause
     *            the cause of the Flow node failure.
     */
    public FlowNodeExecutionException(final Throwable cause) {
        super(cause);
    }

}
