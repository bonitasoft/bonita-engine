/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
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
 * Thrown when a process cannot be enabled / disabled, or when a startProcess cannot be performed because the process is not enabled.
 * 
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ProcessActivationException extends ExecutionException {

    private static final long serialVersionUID = -425713003229819771L;

    public ProcessActivationException(final Exception e) {
        super(e);
    }

    public ProcessActivationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ProcessActivationException(final String message) {
        super(message);
    }

    public ProcessActivationException(final long processDefinitionId, final String name, final String version) {
        super("The process definition is not enabled !!");
        setProcessDefinitionIdOnContext(processDefinitionId);
        setProcessDefinitionNameOnContext(name);
        setProcessDefinitionVersionOnContext(version);
    }

}
