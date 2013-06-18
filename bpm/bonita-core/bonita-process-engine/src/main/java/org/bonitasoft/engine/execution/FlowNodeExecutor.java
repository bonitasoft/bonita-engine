/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
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

    FlowNodeState stepForward(long flowNodeInstanceId, final SExpressionContext expressionContext, final List<SOperation> operations, Long executedBy,
            Long processInstanceId) throws SFlowNodeExecutionException;

    FlowNodeState gotoNextStableState(long flowNodeInstanceId, final SExpressionContext expressionContext, final List<SOperation> operations, Long executedBy,
            Long processInstanceId) throws SActivityInterruptedException, SFlowNodeExecutionException;

    void setStateByStateId(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance, int stateId) throws SActivityStateExecutionException;

    void childReachedState(SProcessDefinition childProcDef, SProcessInstance childProcInst, ProcessInstanceState childState, boolean hasActionToExecute)
            throws SBonitaException;

    /**
     * Archive the flownode instance given as parameter. Also archive all related object that needs to be archived as well.
     * 
     * @param flowNodeInstance
     *            the flow node instance to be archived.
     * @param deleteAfterArchive
     * @param processDefinition
     *            the process definition
     * @throws SActivityExecutionException
     *             in case an error occurs
     */
    void archiveFlowNodeInstance(SFlowNodeInstance flowNodeInstance, boolean deleteAfterArchive, SProcessDefinition processDefinition)
            throws SActivityExecutionException;

    /**
     * @param processDefinition
     *            the process definition of the SFlowNodeInstance on which to execute the state.
     * @param flowNodeInstance
     *            the <code>SFlowNodeInstance</code> whose state has to be executed
     * @param state
     *            the <code>FlowNodeState</code> to execute
     * @return the next <code>FlowNodeState</code>, after executing current state
     * @throws SActivityStateExecutionException
     *             if an exception occurs when executing current state
     * @throws SActivityExecutionException
     *             if an exception occurs when retrieving next state
     * @since 6.0
     */
    StateCode executeState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance, FlowNodeState state)
            throws SActivityStateExecutionException, SActivityExecutionException;

}
