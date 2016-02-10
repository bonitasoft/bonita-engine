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
package org.bonitasoft.engine.execution;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface ContainerExecutor {

    /**
     * Method called to notify this container executor that a child reached the given state
     * 
     * @param processDefinitionId
     *            The identifier of the process definition
     * @param flowNodeInstanceId
     *            The identifier of the flow node instance
     * @param parentId
     *            The identifier of the parent of the flow node
     * @throws SBonitaException
     */
    void childFinished(long processDefinitionId, long flowNodeInstanceId, long parentId) throws SBonitaException;

    /**
     * Execute a flow node in the context of this container executor
     * 
     * @param flowNodeInstanceId
     *            The identifier of the flow node instance
     * @param contextDependency
     * @param operations
     * @param processInstanceId
     *            The identifier of the process instance
     * @param executerId
     *            The identifier of the user which execute the flow node
     * @param executerSubstituteId
     *            The identifier of the delegated user which execute the flow node
     * @return The new state of the flow node after execution
     * @throws SFlowNodeReadException
     * @throws SFlowNodeExecutionException
     *             Throw if there is an error when execute the flow node
     */
    FlowNodeState executeFlowNode(long flowNodeInstanceId, SExpressionContext contextDependency, List<SOperation> operations, long processInstanceId,
            final Long executerId, final Long executerSubstituteId) throws SFlowNodeReadException, SFlowNodeExecutionException;

    /**
     * @return The handled type
     */
    String getHandledType();

}
