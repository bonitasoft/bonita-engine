/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Date;

import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.DescriptionElement;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @since 6.0.0
 * @version 6.4.1
 */
public interface FlowNodeInstance extends DescriptionElement, BaseElement {

    /**
     * Returns the task's direct container ID. For a sub-task or CallActivity, would point to the containing activity ID
     * of the current element.
     * For a normal Task / Activity, would point to the ID of the process instance containing the task / activity.
     * For a multi-instanciated task, each task has its parentContainerId pointing to the containing multi-instance task
     * (basic container for all task
     * instances).
     *
     * @return the ID of the direct containing element (activity instance of process instance).
     */
    long getParentContainerId();

    /**
     * Returns the root container ID. It is the ID of the root-level containing process instance.
     *
     * @return the root container ID.
     */
    long getRootContainerId();

    /**
     * Returns the ID of the process definition where this <code>FlowNodeInstance</code> is defined.
     *
     * @return the ID of the process definition.
     */
    long getProcessDefinitionId();

    /**
     * Always returns the directly containing process instance ID (at the lower level, if several levels of containing
     * processes).
     *
     * @return the ID of the lowest-level containing process instance.
     */
    long getParentProcessInstanceId();

    /**
     * Returns the name of the state the flow node instance is in.
     * <p>
     * List of existing states:
     * <p>
     * States that are transitional:
     * <ul>
     * <li>initializing: preparing for execution</li>
     * <li>executing: executing logic, e.g. operations</li>
     * <li>completing: executing logic before completion, e.g. ON_FINISH connectors of call activity</li>
     * <li>completing activity with boundary: completing related boundary events</li>
     * <li>cancelling: waiting for related elements to be cancelled</li>
     * <li>aborting: waiting for related elements to be aborted</li>
     * <li>aborting activity with boundary: waiting for boundaries to be aborted</li>
     * <li>aborting call activity: waiting for called element to be aborted</li>
     * <li>canceling call activity: waiting for called element to be cancelled</li>
     * <li>cancelling subtasks: waiting for cancellation of sub tasks</li>
     * </ul>
     * <p>
     * States where flow node is waiting for something:
     * <ul>
     * <li>ready: human task is ready to be executed</li>
     * <li>waiting: flow node is waiting for a non-human interaction, e.g. a BPMN message</li>
     * <li>failed: when an error occurred, flow node can be skipped or replayed</li>
     * </ul>
     * <p>
     * Final States:
     * <ul>
     * <li>completed: final state</li>
     * <li>aborted: flow node was aborted by another flow of the process</li>
     * <li>skipped: flow node was manually skipped</li>
     * <li>interrupted: final state, interrupted</li>
     * <li>cancelled: flow node was cancelled by a human</li>
     * </ul>
     *
     * @return this FlowNodeInstance state
     */
    String getState();

    StateCategory getStateCategory();

    /**
     * Returns the <code>FlowNodeType</code> that precises the concrete type of this <code>FlowNodeInstance</code>.
     *
     * @return the <code>FlowNodeType</code>
     */
    FlowNodeType getType();

    String getDisplayDescription();

    String getDisplayName();

    /**
     * @return The identifier of the user who executed the flow node
     * @since 6.0.1
     */
    long getExecutedBy();

    /**
     * @return The identifier of the substitute user (as Process manager or Administrator) who executed the flow node.
     * @since 6.3.0
     */
    long getExecutedBySubstitute();

    /**
     * Returns the ID of the flow node definition of this instance.
     *
     * @return the ID of the flow node definition that this <code>FlowNodeInstance</code> is an instance of.
     */
    long getFlownodeDefinitionId();

    /**
     * @return The date when the flownode instance reached its state ({@link #getState()})
     */
    Date getReachedStateDate();

    /**
     * @return The last date when the activity instance was updated
     */
    Date getLastUpdateDate();
}
