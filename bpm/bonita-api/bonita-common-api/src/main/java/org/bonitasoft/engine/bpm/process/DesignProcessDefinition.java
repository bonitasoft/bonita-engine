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
package org.bonitasoft.engine.bpm.process;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.parameter.ParameterDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * Represents the Design Definition of a process. It gives access to process attributes.
 * <ul>
 * <li>display name</li>
 * <li>description</li>
 * <li>parameters</li>
 * <li>actors</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Laurent Leseigneur
 */
@SuppressWarnings("deprecation")
public interface DesignProcessDefinition extends ProcessDefinition {

    /**
     * Retrieve the displayed name of the process definition, as set at design-time.
     *
     * @return the displayed name of the process definition, as set at design-time.
     *         Gets the display name of the process definition.
     */
    String getDisplayName();

    /**
     * Retrieve the displayed description of the process definition, as set at design-time.
     *
     * @return the displayed description of the process definition, as set at design-time.
     */
    String getDisplayDescription();

    /**
     * Retrieve the definition of the FlowElementContainerDefinition of the process container
     *
     * @return the {@link FlowElementContainerDefinition} of the process container.<br/>
     *         return type FlowElementContainerDefinition has been deprecated due to move to a new package:
     *         {@link org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition}
     */
    @Deprecated
    FlowElementContainerDefinition getProcessContainer();

    /**
     * Retrieve a Set of ParameterDefinition involved in ProcessDefinition
     *
     * @return as set of {@link ParameterDefinition}
     */
    Set<ParameterDefinition> getParameters();

    /**
     * Retrieve a Set of ActorDefinition involved in ProcessDefinition
     *
     * @return A set of {@link ActorDefinition}.
     *         <br/>If no actors have been defined, return an empty Set.
     * @see #getActorsList()
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #getActorsList()} which return the same information as a list
     */
    @Deprecated
    Set<ActorDefinition> getActors();

    /**
     * Gets the list of all actors defined on this process.
     *
     * @return The list of {@link ActorDefinition} defined in this process.
     *         <br/>If no actors have been defined, return an empty List.
     * @since 6.1
     */
    List<ActorDefinition> getActorsList();

    /**
     * Retrieve the ActorDefinition of process's actor defined as initiator.
     *
     * @return the {@link ActorDefinition} of process's actor defined as initiator.
     * @since 6.1
     */
    ActorDefinition getActorInitiator();

    /**
     * Retrieve the label of the ProcessDefinition for a given index
     *
     * @param index the position of the label to retrieve
     * @return as String description
     */
    String getStringIndexLabel(int index);

    /**
     * Retrieve the Expression of the ProcessDefinition for a given index
     *
     * @param index the position of the expression to retrieve
     * @return the {@link Expression} associated to the given index
     */
    Expression getStringIndexValue(int index);

}
