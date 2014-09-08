/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
 * Thrown when a process fails to deploy.
 * It also gives access to the ID of the process Definition that tried to be deployed, through method {@link #getProcessDefinitionId()}.
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see #getProcessDefinitionId()
 */
public class ProcessDeployException extends ExecutionException {

    private static final long serialVersionUID = 3104389074405599228L;

    private Long processDefinitionId;

    public ProcessDeployException(final String message) {
        super(message);
    }

    public ProcessDeployException(final Throwable cause) {
        super(cause);
    }

    /**
     * @return the processDefinitionId
     */
    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

}
