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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface FlowNodeExecutor extends ContainerExecutor {

    /**
     * @param flowNodeInstance
     * @param executerId
     * @param executerSubstituteId
     * @return
     * @throws SFlowNodeExecutionException
     */
    FlowNodeState stepForward(SFlowNodeInstance flowNodeInstance,
            Long executerId, Long executerSubstituteId) throws SFlowNodeExecutionException;

    /**
     * force the state of a flow node toa particular state
     *
     * @param flowNodeInstanceId
     * @param stateId
     */
    void setStateByStateId(long flowNodeInstanceId, int stateId) throws SActivityStateExecutionException;

    /**
     * @param childProcInst
     * @param childState
     * @param hasActionsToExecute
     * @throws SBonitaException
     * @since 6.1
     */
    void childReachedState(SProcessInstance childProcInst, ProcessInstanceState childState, boolean hasActionsToExecute)
            throws SBonitaException;

    /**
     * @param processDefinition
     *        the process definition of the SFlowNodeInstance on which to execute the state.
     * @param flowNodeInstance
     *        the <code>SFlowNodeInstance</code> whose state has to be executed
     * @param state
     *        the <code>FlowNodeState</code> to execute
     * @return the next <code>FlowNodeState</code>, after executing current state
     * @throws SActivityStateExecutionException
     *         if an exception occurs when executing current state
     * @throws SActivityExecutionException
     *         if an exception occurs when retrieving next state
     * @since 6.0
     */
    StateCode executeState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance,
            FlowNodeState state)
            throws SActivityStateExecutionException, SActivityExecutionException;

}
