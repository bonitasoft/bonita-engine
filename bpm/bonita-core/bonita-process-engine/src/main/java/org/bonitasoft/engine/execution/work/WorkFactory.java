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
package org.bonitasoft.engine.execution.work;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.Filter;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.work.failurewrapping.ConnectorDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.FlowNodeDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.MessageInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessDefinitionContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessInstanceContextWork;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * Factory to construct works
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class WorkFactory {

    private WorkFactory() {
        // Utility classes should not have a public constructor
    }

    public static BonitaWork createExecuteConnectorOfActivity(final long processDefinitionId, final long processInstanceId, final long flowNodeDefinitionId,
            final long flowNodeInstanceId, final long connectorInstanceId, final String connectorDefinitionName) {
        BonitaWork wrappedWork = new ExecuteConnectorOfActivity(processDefinitionId, flowNodeDefinitionId, flowNodeInstanceId,
                connectorInstanceId, connectorDefinitionName);
        wrappedWork = new ConnectorDefinitionAndInstanceContextWork(wrappedWork, connectorDefinitionName, connectorInstanceId);
        wrappedWork = buildFlowNodeDefinitionAndInstanceContextWork(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    public static BonitaWork createExecuteConnectorOfProcess(final long processDefinitionId, final long processInstanceId, final long rootProcessInstanceId,
            final long connectorInstanceId, final String connectorDefinitionName, final ConnectorEvent activationEvent,
            final FlowNodeSelector flowNodeSelector) {
        BonitaWork wrappedWork = new ExecuteConnectorOfProcess(processDefinitionId, connectorInstanceId, connectorDefinitionName, processInstanceId,
                rootProcessInstanceId, activationEvent, flowNodeSelector);
        final ProcessInstanceContextWork processInstanceContextWork = buildProcessInstanceContextWork(processDefinitionId, processInstanceId, rootProcessInstanceId,
                wrappedWork);
        wrappedWork = new ConnectorDefinitionAndInstanceContextWork(processInstanceContextWork, connectorDefinitionName, connectorInstanceId,
                activationEvent);
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    public static BonitaWork createExecuteFlowNodeWork(final long processDefinitionId, final long processInstanceId, final long flowNodeInstanceId,
            final List<SOperation> operations, final SExpressionContext contextDependency) {
        if (processInstanceId <= 0) {
            throw new RuntimeException("It is forbidden to create a ExecuteFlowNodeWork with a processInstanceId equals to " + processInstanceId);
        }
        BonitaWork wrappedWork = new ExecuteFlowNodeWork(flowNodeInstanceId, operations, contextDependency, processInstanceId);
        wrappedWork = new LockProcessInstanceWork(new TxBonitaWork(wrappedWork), processInstanceId);
        wrappedWork = buildFlowNodeDefinitionAndInstanceContextWork(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    public static BonitaWork createExecuteMessageCoupleWork(final SMessageInstance messageInstance, final SWaitingMessageEvent waitingMessage) {
        // no target process: we do not wrap in a LockProcessInstanceWork
        BonitaWork wrappedWork = new TxBonitaWork(new ExecuteMessageCoupleWork(messageInstance.getId(), waitingMessage.getId()));
        if (waitingMessage.getParentProcessInstanceId() > 0) {
            wrappedWork = new LockProcessInstanceWork(wrappedWork, waitingMessage.getParentProcessInstanceId());
        }
        wrappedWork = new MessageInstanceContextWork(wrappedWork, messageInstance, waitingMessage);
        wrappedWork = buildFlowNodeDefinitionAndInstanceContextWork(waitingMessage.getProcessDefinitionId(), waitingMessage.getParentProcessInstanceId(),
                waitingMessage.getRootProcessInstanceId(), waitingMessage.getFlowNodeInstanceId(), wrappedWork);
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    public static BonitaWork createNotifyChildFinishedWork(final long processDefinitionId, final long processInstanceId, final long flowNodeInstanceId,
            final long parentId, final String parentType) {
        BonitaWork wrappedWork = new NotifyChildFinishedWork(processDefinitionId, flowNodeInstanceId, parentId, parentType);
        wrappedWork = new LockProcessInstanceWork(new TxBonitaWork(wrappedWork), processInstanceId);
        wrappedWork = buildFlowNodeDefinitionAndInstanceContextWork(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    private static BonitaWork buildFlowNodeDefinitionAndInstanceContextWork(final long processDefinitionId, final long processInstanceId,
            final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork, processDefinitionId);
        final ProcessInstanceContextWork processInstanceContextWork = new ProcessInstanceContextWork(processDefinitionContextWork, processInstanceId);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private static BonitaWork buildFlowNodeDefinitionAndInstanceContextWork(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessInstanceContextWork processInstanceContextWork = buildProcessInstanceContextWork(processDefinitionId, processInstanceId,
                rootProcessInstanceId, wrappedWork);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private static ProcessInstanceContextWork buildProcessInstanceContextWork(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork, processDefinitionId);
        return new ProcessInstanceContextWork(processDefinitionContextWork, processInstanceId, rootProcessInstanceId);
    }
}
