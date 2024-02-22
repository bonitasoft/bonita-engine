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
package org.bonitasoft.engine.core.process.instance.api.states;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface FlowNodeState {

    int ID_ACTIVITY_EXECUTING = 1;
    int ID_ACTIVITY_READY = 4;
    int ID_ACTIVITY_FAILED = 3;

    int getId();

    /**
     * @param processDefinition
     * @param flowNodeInstance
     * @return true the state must be executed, false if the execution must skip this state and go directly to the next
     *         one
     * @throws SActivityExecutionException
     */
    boolean shouldExecuteState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance)
            throws SActivityExecutionException;

    /**
     * Return true if flowNodeInstance instance of SHumanTaskInstance
     *
     * @param flowNodeInstance
     * @return true or false
     * @since 6.0
     */
    boolean mustAddSystemComment(SFlowNodeInstance flowNodeInstance);

    /**
     * Add a system comment "User XYZ has XYZ(state change) task XYZ(task name)"
     *
     * @param flowNodeInstance
     * @return system comment "User XYZ has XYZ(state change) task XYZ(task name)"
     * @since 6.0
     */
    String getSystemComment(SFlowNodeInstance flowNodeInstance);

    StateCode execute(SProcessDefinition processDefinition, SFlowNodeInstance instance)
            throws SActivityStateExecutionException;

    /**
     * Called when a child of the flow node parentInstance finishes.
     * Triggers what's next, if applicable.
     * returns if all children activity is finished / triggered.
     *
     * @return true if the state is finished (the flow node will continue its flow),
     *         false if there are still some children to be triggered / to wait for.
     */
    default boolean notifyChildFlowNodeHasFinished(SProcessDefinition processDefinition,
            SFlowNodeInstance parentInstance, SFlowNodeInstance childInstance)
            throws SActivityStateExecutionException {

        return false;
    }

    String getName();

    /**
     * @return true if the state is stable
     *         a final state is stable
     */
    boolean isStable();

    /**
     * Checks whether the state is a terminal one.
     *
     * @return true is the state is a terminal one; false otherwise
     */
    boolean isTerminal();

    /**
     * Get the state's category
     *
     * @return the state's category
     */
    SStateCategory getStateCategory();
}
