/**
 * Copyright (C) 2011-2012, 2014 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * Once a process is deployed, the associated actors are instances of class <code>ActorInstance</code>.
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see ActorDefinition
 */
public interface ActorInstance extends DescriptionElement, BaseElement {

    /**
     * @return The identifier of the process definition where this actor is defined.
     */
    long getProcessDefinitionId();

    /**
     * @return The display name of the actor, as designed is {@link ActorDefinition}
     */
    String getDisplayName();

    /**
     * @return True if this actor is the actor initiator of the process, that is, the actor that can start the process.
     */
    boolean isInitiator();

}
