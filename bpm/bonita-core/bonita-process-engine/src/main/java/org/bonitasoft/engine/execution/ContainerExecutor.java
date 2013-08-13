/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public interface ContainerExecutor {

    /**
     * method called to notify this container executor that a child reached the given state
     * 
     * @param processDefinitionId
     * @param flowNodeInstanceId
     * @param stateId
     * @param parentId
     * @throws SBonitaException
     */
    void childFinished(long processDefinitionId, long flowNodeInstanceId, int stateId, long parentId) throws SBonitaException;

    /**
     * execute a flow node in the context of this container executor
     * 
     * @param contextDependency
     * @param operations
     * @return
     */
    FlowNodeState executeFlowNode(long flowNodeInstanceId, SExpressionContext contextDependency, List<SOperation> operations, long processInstanceId,
            final Long executerId, final Long executerDelegateId) throws SFlowNodeReadException, SFlowNodeExecutionException;

    /**
     * execute a transition in the context of this container executor
     */
    void executeTransition(SProcessDefinition sDefinition, STransitionInstance transitionInstance) throws SBonitaException;

    String getHandledType();

}
