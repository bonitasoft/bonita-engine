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

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * Once the {@link org.bonitasoft.engine.bpm.process.ProcessDefinition} is deployed, the associated {@link ActorDefinition}s are instantiated.
 * This object represents this instance of {@link ActorDefinition}.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public interface ActorInstance extends DescriptionElement, BaseElement {

    /**
     * Get the identifier of the process definition where this actor is defined.
     *
     * @return The identifier of the process definition where this actor is defined.
     */
    long getProcessDefinitionId();

    /**
     * The display name of the actor defined in {@link ActorDefinition#getName()}.
     *
     * @return The display name of the actor.
     */
    String getDisplayName();

    /**
     * Can this actor start the process ?
     * Defined in {@link ActorDefinition#isInitiator()}.
     *
     * @return <code>true</code>} if this actor can start the process, <code>false</code> otherwise.
     */
    boolean isInitiator();

}
