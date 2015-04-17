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
package org.bonitasoft.engine.bpm.process;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
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
 * <li>search indexes</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Laurent Leseigneur
 * @version 6.3.5
 * @since 6.0.0
 */
public interface DesignProcessDefinition extends ProcessDefinition {

    /**
     * Retrieves the displayed name of the process definition, as set at design-time.
     *
     * @return The displayed name of the process definition, as set at design-time.
     */
    String getDisplayName();

    /**
     * Retrieves the displayed description of the process definition, as set at design-time.
     *
     * @return The displayed description of the process definition, as set at design-time.
     */
    String getDisplayDescription();

    /**
     * Retrieves the definition of the FlowElementContainerDefinition of the process container.
     * <p>
     *     This method is deprecated. Please, use {@link #getFlowElementContainer()} instead.
     * </p>
     * Th
     *
     * @return The {@link FlowElementContainerDefinition} of the process container.<br>
     *         return type FlowElementContainerDefinition in this package is deprecated. Instead use
     *         {@link org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition}
     */
    @Deprecated
    FlowElementContainerDefinition getProcessContainer();

    /**
     * Returns a {@link org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition} containing all flow elements of this process.
     * @return a {@code FlowElementContainerDefinition} containing all flow elements of this process.
     * @see org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition
     * @since 6.4.1
     */
    org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition getFlowElementContainer();

    /**
     * Retrieves a Set of ParameterDefinition objects from a ProcessDefinition
     *
     * @return A set of {@link ParameterDefinition} objects
     */
    Set<ParameterDefinition> getParameters();

    /**
     * Retrieves a Set of ActorDefinition objects from a ProcessDefinition
     *
     * @return A set of {@link ActorDefinition} objects.
     *         <br>If no actors have been defined, return an empty Set.
     * @see #getActorsList()
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #getActorsList()} which return the same information as a list
     */
    @Deprecated
    Set<ActorDefinition> getActors();

    /**
     * Gets the list of all actors defined on this process.
     *
     * @return The list of {@link ActorDefinition} objects defined in this process.
     *         <br>If no actors have been defined, return an empty List.
     * @since 6.1
     */
    List<ActorDefinition> getActorsList();

    /**
     * Retrieves the ActorDefinition of process's actor defined as initiator.
     *
     * @return The {@link ActorDefinition} of process's actor defined as initiator.
     * @since 6.1
     */
    ActorDefinition getActorInitiator();


    /**
     * Retrieves the label for the ProcessDefinition given search index.
     * <p>
     * You can define up to five search indexes for a process. See more at <a href="http://documentation.bonitasoft.com/define-search-index">Define a search
     * index</a> Bonitasoft documentation page
     * </p>
     *
     * @param index
     *        The position of search index to retrieve. Valid values are between 1 and 5 (inclusive)
     * @throws IndexOutOfBoundsException if index is invalid
     * @return The label the Expression of the search index
     */

    String getStringIndexLabel(int index);

    /**
     * Retrieves the Expression for the ProcessDefinition given search index.
     * <p>
     * You can define up to five search indexes for a process. See more at <a href="http://documentation.bonitasoft.com/define-search-index">Define a search
     * index</a> Bonitasoft documentation page
     * </p>
     *
     * @param index
     *        The position of search index to retrieve. Valid values are between 1 and 5 (inclusive)
     * @throws IndexOutOfBoundsException if index is invalid
     * @return The {@link Expression} of the search index
     */
    Expression getStringIndexValue(int index);

    /**
     * Contract that must be respected when starting an instance of this process
     * 
     * @return
     *         the process instantiation contract
     */
    ContractDefinition getContract();


    List<ContextEntry> getContext();
}
