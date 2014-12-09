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
package org.bonitasoft.engine.bpm.actor;

import java.io.Serializable;

/**
 * ActorDefinition forms part of the ProcessDefinition. It is used to design an actor in the context of a process.
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see org.bonitasoft.engine.bpm.process.ProcessDefinition
 * @see "The BPMN specification"
 */
public interface ActorDefinition extends Serializable {

    /**
     * @return The logical name of the actor.
     */
    String getName();

    /**
     * @return The description given to this actor.
     */
    String getDescription();

    /**
     * @return True if this actor is the actor initiator of the process, that is, the actor that can start the process.
     */
    boolean isInitiator();

}
