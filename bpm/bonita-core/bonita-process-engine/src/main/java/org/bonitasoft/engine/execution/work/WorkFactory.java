/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * 
 * Factory to construct works
 * 
 * @author Baptiste Mesta
 * 
 */
public class WorkFactory {

    public static BonitaWork createExecuteConnectorOfActivity(final long processDefinitionId, final long flowNodeDefinitionId,
            final long flowNodeInstanceId,
            final long connectorInstanceId, final String connectorDefinitionName) {
        return new FailureHandlingBonitaWork(new ExecuteConnectorOfActivity(processDefinitionId, flowNodeDefinitionId, flowNodeInstanceId, connectorInstanceId,
                connectorDefinitionName));
    }

    public static BonitaWork createExecuteConnectorOfProcess(final long processDefinitionId, final long connectorInstanceId,
            final String connectorDefinitionName,
            final long processInstanceId, final long rootProcessInstanceId, final ConnectorEvent activationEvent) {
        return new FailureHandlingBonitaWork(new ExecuteConnectorOfProcess(processDefinitionId, connectorInstanceId, connectorDefinitionName,
                processInstanceId, rootProcessInstanceId,
                activationEvent));
    }

    public static BonitaWork createExecuteFlowNodeWork(final long flowNodeInstanceId, final List<SOperation> operations,
            final SExpressionContext contextDependency,
            final long processInstanceId) {
        return new FailureHandlingBonitaWork(new LockProcessInstanceWork(new TxBonitaWork(new ExecuteFlowNodeWork(flowNodeInstanceId, operations,
                contextDependency, processInstanceId)), processInstanceId));
    }

    public static BonitaWork createExecuteMessageCoupleWork(final long messageInstanceId, final long waitingMessageId) {
        return new FailureHandlingBonitaWork(new TxBonitaWork(new ExecuteMessageCoupleWork(messageInstanceId, waitingMessageId)));
    }

    public static BonitaWork createNotifyChildFinishedWork(final long processDefinitionId, final long processInstanceId,
            final long flowNodeInstanceId,
            final long parentId, final String parentType, final int stateId) {
        return new FailureHandlingBonitaWork(new LockProcessInstanceWork(new TxBonitaWork(new NotifyChildFinishedWork(processDefinitionId, processInstanceId,
                flowNodeInstanceId, parentId, parentType, stateId)), parentId));
    }
}
