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

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.work.failurewrapping.ConnectorDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.FlowNodeDefinitionAndInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.MessageInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessDefinitionContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.ProcessInstanceContextWork;
import org.bonitasoft.engine.execution.work.failurewrapping.TriggerSignalWork;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkDescriptor;

/**
 * Factory to construct works
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class WorkFactory implements org.bonitasoft.engine.work.WorkFactory {

    private static final String EXECUTE_ACTIVITY_CONNECTOR = "EXECUTE_ACTIVITY_CONNECTOR";
    private static final String EXECUTE_PROCESS_CONNECTOR = "EXECUTE_PROCESS_CONNECTOR";
    private static final String EXECUTE_FLOWNODE = "EXECUTE_FLOWNODE";
    private static final String FINISH_FLOWNODE = "FINISH_FLOWNODE";
    private static final String EXECUTE_MESSAGE = "EXECUTE_MESSAGE";
    private static final String TRIGGER_SIGNAL = "TRIGGER_SIGNAL";
    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    private static final String PROCESS_INSTANCE_ID = "processInstanceId";
    private static final String FLOW_NODE_DEFINITION_ID = "flowNodeDefinitionId";
    private static final String FLOW_NODE_INSTANCE_ID = "flowNodeInstanceId";
    private static final String CONNECTOR_INSTANCE_ID = "connectorInstanceId";
    private static final String CONNECTOR_DEFINITION_NAME = "connectorDefinitionName";
    private static final String ROOT_PROCESS_INSTANCE_ID = "rootProcessInstanceId";

    private BonitaWork createExecuteConnectorOfActivity(WorkDescriptor workDescriptor) {
        final long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        final long connectorInstanceId = workDescriptor.getLong(CONNECTOR_INSTANCE_ID);
        final String connectorDefinitionName = workDescriptor.getString(CONNECTOR_DEFINITION_NAME);
        BonitaWork wrappedWork = new ExecuteConnectorOfActivity(processDefinitionId, processInstanceId, workDescriptor.getLong(FLOW_NODE_DEFINITION_ID), flowNodeInstanceId,
                connectorInstanceId, connectorDefinitionName);
        wrappedWork = new ConnectorDefinitionAndInstanceContextWork(wrappedWork, connectorDefinitionName, connectorInstanceId);
        wrappedWork = withFlowNodeContext(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return withFailureHandling(wrappedWork);
    }

    public WorkDescriptor createExecuteConnectorOfActivityDescriptor(final long processDefinitionId,
            final long processInstanceId, final long flowNodeDefinitionId,
            final long flowNodeInstanceId, final long connectorInstanceId, final String connectorDefinitionName) {
        return WorkDescriptor.create(EXECUTE_ACTIVITY_CONNECTOR)
                .withParameter(PROCESS_DEFINITION_ID, processDefinitionId)
                .withParameter(PROCESS_INSTANCE_ID, processInstanceId)
                .withParameter(FLOW_NODE_DEFINITION_ID, flowNodeDefinitionId)
                .withParameter(FLOW_NODE_INSTANCE_ID, flowNodeInstanceId)
                .withParameter(CONNECTOR_INSTANCE_ID, connectorInstanceId)
                .withParameter(CONNECTOR_DEFINITION_NAME, connectorDefinitionName);
    }

    private BonitaWork createExecuteConnectorOfProcess(WorkDescriptor workDescriptor) {
        final long processDefinitionId = getLongParameter(workDescriptor);
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long rootProcessInstanceId = workDescriptor.getLong(ROOT_PROCESS_INSTANCE_ID);
        final long connectorInstanceId = workDescriptor.getLong(CONNECTOR_INSTANCE_ID);
        final String connectorDefinitionName = workDescriptor.getString(CONNECTOR_DEFINITION_NAME);
        final ConnectorEvent activationEvent = ((ConnectorEvent) workDescriptor.getParameter("activationEvent"));
        BonitaWork wrappedWork = withConnectorContext(connectorInstanceId, connectorDefinitionName, activationEvent,
                withProcessContext(processDefinitionId, processInstanceId,
                        rootProcessInstanceId,
                        new ExecuteConnectorOfProcess(processDefinitionId, connectorInstanceId, connectorDefinitionName, processInstanceId,
                                rootProcessInstanceId, activationEvent, ((FlowNodeSelector) workDescriptor.getParameter("flowNodeSelector")))));
        return withFailureHandling(wrappedWork);
    }

    private Long getLongParameter(WorkDescriptor workDescriptor) {
        return workDescriptor.getLong(PROCESS_DEFINITION_ID);
    }


    public WorkDescriptor createExecuteConnectorOfProcessDescriptor(final long processDefinitionId, final long processInstanceId, final long rootProcessInstanceId,
                                                             final long connectorInstanceId, final String connectorDefinitionName, final ConnectorEvent activationEvent,
                                                             final FlowNodeSelector flowNodeSelector) {
        return WorkDescriptor.create(EXECUTE_PROCESS_CONNECTOR)
                .withParameter(PROCESS_DEFINITION_ID, processDefinitionId)
                .withParameter(PROCESS_INSTANCE_ID, processInstanceId)
                .withParameter(ROOT_PROCESS_INSTANCE_ID, rootProcessInstanceId)
                .withParameter(CONNECTOR_INSTANCE_ID, connectorInstanceId)
                .withParameter(CONNECTOR_DEFINITION_NAME, connectorDefinitionName)
                .withParameter("activationEvent", activationEvent)
                //TODO flowNodeSelector should be constructed on execution, not put in the descriptor
                .withParameter("flowNodeSelector", flowNodeSelector);
    }

    private FailureHandlingBonitaWork withFailureHandling(BonitaWork wrappedWork) {
        return new FailureHandlingBonitaWork(wrappedWork);
    }

    private BonitaWork withConnectorContext(long connectorInstanceId, String connectorDefinitionName, ConnectorEvent activationEvent,
            ProcessInstanceContextWork processInstanceContextWork) {
        return new ConnectorDefinitionAndInstanceContextWork(processInstanceContextWork, connectorDefinitionName, connectorInstanceId,
                activationEvent);
    }

    public WorkDescriptor createExecuteFlowNodeWorkDescriptor(final long processDefinitionId,
            final long processInstanceId, final long flowNodeInstanceId) {
        return WorkDescriptor.create(EXECUTE_FLOWNODE).withParameter(PROCESS_DEFINITION_ID, processDefinitionId)
                .withParameter(PROCESS_INSTANCE_ID, processInstanceId)
                .withParameter(FLOW_NODE_INSTANCE_ID, flowNodeInstanceId).withParameter("readyHumanTask", false);
    }

    private BonitaWork createExecuteFlowNodeWork(WorkDescriptor workDescriptor) {
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        if (processInstanceId <= 0) {
            throw new RuntimeException("It is forbidden to create a ExecuteFlowNodeWork with a processInstanceId equals to " + processInstanceId);
        }
        ExecuteFlowNodeWork executeFlowNodeWork = new ExecuteFlowNodeWork(flowNodeInstanceId);
        Object readyHumanTask = workDescriptor.getParameter("readyHumanTask");
        executeFlowNodeWork.setReadyHumanTask(readyHumanTask != null ? ((Boolean) readyHumanTask) : false);
        BonitaWork wrappedWork = executeFlowNodeWork;
        wrappedWork = withLock(processInstanceId, withTx(wrappedWork));
        wrappedWork = withFlowNodeContext(workDescriptor.getLong(PROCESS_DEFINITION_ID), processInstanceId, flowNodeInstanceId, wrappedWork);
        return withFailureHandling(wrappedWork);
    }

    public WorkDescriptor createExecuteReadyHumanTaskWorkDescriptor(final long processDefinitionId, final long processInstanceId, final long flowNodeInstanceId) {
        return createExecuteFlowNodeWorkDescriptor(processDefinitionId, processInstanceId, flowNodeInstanceId).withParameter("readyHumanTask", true);
    }

    public WorkDescriptor createExecuteMessageCoupleWorkDescriptor(final SMessageInstance messageInstance,
            final SWaitingMessageEvent waitingMessage) {
        return WorkDescriptor.create(EXECUTE_MESSAGE).withParameter("messageInstance", messageInstance)
                .withParameter("waitingMessage", waitingMessage);
    }

    private BonitaWork createExecuteMessageCoupleWork(WorkDescriptor workDescriptor) {
        final SMessageInstance messageInstance = ((SMessageInstance) workDescriptor.getParameter("messageInstance"));
        final SWaitingMessageEvent waitingMessage = ((SWaitingMessageEvent) workDescriptor
                .getParameter("waitingMessage"));
        // no target process: we do not wrap in a LockProcessInstanceWork
        BonitaWork wrappedWork = withTx(new ExecuteMessageCoupleWork(messageInstance.getId(), waitingMessage.getId()));
        if (waitingMessage.getParentProcessInstanceId() > 0) {
            wrappedWork = withLock(waitingMessage.getParentProcessInstanceId(), wrappedWork);
        }
        wrappedWork = new MessageInstanceContextWork(wrappedWork, messageInstance, waitingMessage);
        wrappedWork = withProcessContext(waitingMessage.getProcessDefinitionId(), waitingMessage.getParentProcessInstanceId(),
                waitingMessage.getRootProcessInstanceId(), waitingMessage.getFlowNodeInstanceId(), wrappedWork);
        return withFailureHandling(wrappedWork);
    }

    private BonitaWork createNotifyChildFinishedWork(WorkDescriptor workDescriptor) {
        final long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        BonitaWork wrappedWork = new NotifyChildFinishedWork(processDefinitionId, flowNodeInstanceId, workDescriptor.getLong("parentId"), workDescriptor.getString("parentType"));
        wrappedWork = withLock(processInstanceId, withTx(wrappedWork));
        wrappedWork = withFlowNodeContext(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return withFailureHandling(wrappedWork);
    }

    public WorkDescriptor createNotifyChildFinishedWorkDescriptor(final long processDefinitionId,
            final long processInstanceId, final long flowNodeInstanceId,
            final long parentId, final String parentType) {
        return WorkDescriptor.create(FINISH_FLOWNODE).withParameter(PROCESS_DEFINITION_ID, processDefinitionId)
                .withParameter(PROCESS_INSTANCE_ID, processInstanceId)
                .withParameter(FLOW_NODE_INSTANCE_ID, flowNodeInstanceId)
                .withParameter("parentId", parentId).withParameter("parentType", parentType);
    }

    private BonitaWork withLock(long processInstanceId, BonitaWork work) {
        return new LockProcessInstanceWork(work, processInstanceId);
    }

    private BonitaWork withTx(BonitaWork wrappedWork) {
        return new TxBonitaWork(wrappedWork);
    }

    private BonitaWork withFlowNodeContext(final long processDefinitionId, final long processInstanceId,
            final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork, processDefinitionId);
        final ProcessInstanceContextWork processInstanceContextWork = new ProcessInstanceContextWork(processDefinitionContextWork, processInstanceId);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private BonitaWork withProcessContext(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessInstanceContextWork processInstanceContextWork = withProcessContext(processDefinitionId, processInstanceId,
                rootProcessInstanceId, wrappedWork);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private ProcessInstanceContextWork withProcessContext(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork, processDefinitionId);
        return new ProcessInstanceContextWork(processDefinitionContextWork, processInstanceId, rootProcessInstanceId);
    }

    public WorkDescriptor createTriggerSignalWorkDescriptor(SWaitingSignalEvent listeningSignal) {
        return WorkDescriptor.create(TRIGGER_SIGNAL).withParameter("listeningSignal", listeningSignal);
    }

    private BonitaWork createTriggerSignalWork(WorkDescriptor workDescriptor) {
        SWaitingSignalEvent listeningSignal = ((SWaitingSignalEvent) workDescriptor.getParameter("listeningSignal"));
        BonitaWork triggerSignalWork = new TriggerSignalWork(listeningSignal.getId(), listeningSignal.getSignalName());
        triggerSignalWork = withTx(triggerSignalWork);
        long parentProcessInstanceId = listeningSignal.getParentProcessInstanceId();
        if (parentProcessInstanceId > 0) {
            triggerSignalWork = withLock(parentProcessInstanceId, triggerSignalWork);
        }
        return withFailureHandling(triggerSignalWork);
    }

    @Override
    public BonitaWork create(WorkDescriptor workDescriptor) {
        switch (workDescriptor.getType()) {
            case EXECUTE_ACTIVITY_CONNECTOR:
                return createExecuteConnectorOfActivity(workDescriptor);
            case EXECUTE_PROCESS_CONNECTOR:
                return createExecuteConnectorOfProcess(workDescriptor);
            case EXECUTE_FLOWNODE:
                return createExecuteFlowNodeWork(workDescriptor);
            case FINISH_FLOWNODE:
                return createNotifyChildFinishedWork(workDescriptor);
            case TRIGGER_SIGNAL:
                return createTriggerSignalWork(workDescriptor);
            case EXECUTE_MESSAGE:
                return createExecuteMessageCoupleWork(workDescriptor);
            default:
                throw new IllegalArgumentException("Unkown type of work:" + workDescriptor.getType());
        }
    }
}
