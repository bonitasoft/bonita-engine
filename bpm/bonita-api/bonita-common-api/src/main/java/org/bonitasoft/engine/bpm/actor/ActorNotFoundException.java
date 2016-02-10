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
package org.bonitasoft.engine.bpm.actor;

import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.exception.NotFoundException;

/**
 * Thrown when it's not possible to find an actor mapping.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public class ActorNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 2518419716527606128L;

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *        cause is nonexistent or unknown.)
     */
    public ActorNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public ActorNotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception and the specified detail message.
     *
     * @param actorName
     *        The not found actor
     * @param processDefinition
     *        The process definition where we search the actor
     */
    public ActorNotFoundException(final String actorName, final ProcessDefinition processDefinition) {
        super("Actor " + actorName + " not found");
        setProcessDefinitionNameOnContext(processDefinition.getName());
        setProcessDefinitionIdOnContext(processDefinition.getId());
        setProcessDefinitionVersionOnContext(processDefinition.getVersion());
    }

}
