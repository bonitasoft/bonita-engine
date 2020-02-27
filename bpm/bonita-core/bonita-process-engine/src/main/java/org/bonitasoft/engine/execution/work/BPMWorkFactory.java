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
package org.bonitasoft.engine.execution.work;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
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
import org.bonitasoft.engine.work.WorkFactory;

/**
 * Factory to construct works
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class BPMWorkFactory implements WorkFactory {

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
    public static final String STATE_ID = "stateId";
    private static final String STATE_EXECUTING = "stateExecuting";
    private static final String STATE_ABORTING = "stateAborting";
    private static final String STATE_CANCELING = "stateCanceling";
    private static final String ACTIVATION_EVENT = "activationEvent";
    private static final String SUB_PROCESS_DEFINITION_ID = "subProcessDefinitionId";
    private static final String FLOW_NODE_DEFINITIONS_FILTER = "flowNodeDefinitionsFilter";
    private static final String MESSAGE_INSTANCE_ID = "messageInstanceId";
    private static final String MESSAGE_INSTANCE_NAME = "messageInstanceName";
    private static final String MESSAGE_INSTANCE_TARGET_PROCESS = "messageInstanceTargetProcess";
    private static final String MESSAGE_INSTANCE_TARGET_FLOW_NODE = "messageInstanceTargetFlowNode";
    private static final String WAITING_MESSAGE_ID = "waitingMessageId";
    private static final String WAITING_MESSAGE_EVENT_TYPE = "waitingMessageEventType";
    private static final String PARENT_TYPE = "parentType";
    private static final String PARENT_ID = "parentId";
    private static final String LISTENING_SIGNAL_ID = "listeningSignalId";
    private static final String LISTENING_SIGNAL_NAME = "listeningSignalName";

    private Map<String, Function<WorkDescriptor, BonitaWork>> extensions = new HashMap<>();

    private BonitaWork createExecuteConnectorOfActivity(WorkDescriptor workDescriptor) {
        final long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        final long connectorInstanceId = workDescriptor.getLong(CONNECTOR_INSTANCE_ID);
        final String connectorDefinitionName = workDescriptor.getString(CONNECTOR_DEFINITION_NAME);
        BonitaWork wrappedWork = new ExecuteConnectorOfActivity(processDefinitionId, processInstanceId,
                workDescriptor.getLong(FLOW_NODE_DEFINITION_ID), flowNodeInstanceId,
                connectorInstanceId, connectorDefinitionName);
        wrappedWork = new ConnectorDefinitionAndInstanceContextWork(wrappedWork, connectorDefinitionName,
                connectorInstanceId);
        wrappedWork = withFlowNodeContext(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return withSession(wrappedWork);
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
        long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        long rootProcessInstanceId = workDescriptor.getLong(ROOT_PROCESS_INSTANCE_ID);
        long connectorInstanceId = workDescriptor.getLong(CONNECTOR_INSTANCE_ID);
        String connectorDefinitionName = workDescriptor.getString(CONNECTOR_DEFINITION_NAME);
        ConnectorEvent activationEvent = (ConnectorEvent.valueOf(workDescriptor.getString(ACTIVATION_EVENT)));
        String flowNodeIds = workDescriptor.getString(FLOW_NODE_DEFINITIONS_FILTER);
        List<Long> flowNodeDefinitionsFilter = getListOfFlowNodeDefinitionsToStart(flowNodeIds);
        Long subProcessDefinitionId = workDescriptor.getLong(SUB_PROCESS_DEFINITION_ID);

        BonitaWork wrappedWork = withConnectorContext(connectorInstanceId, connectorDefinitionName, activationEvent,
                withProcessContext(processDefinitionId, processInstanceId,
                        rootProcessInstanceId,
                        new ExecuteConnectorOfProcess(processDefinitionId, connectorInstanceId, connectorDefinitionName,
                                processInstanceId,
                                rootProcessInstanceId, activationEvent, flowNodeDefinitionsFilter,
                                subProcessDefinitionId)));
        return withSession(wrappedWork);
    }

    private List<Long> getListOfFlowNodeDefinitionsToStart(String flowNodeIds) {
        if (flowNodeIds == null) {
            //no flow node selector
            return null;
        }
        if (flowNodeIds.isEmpty()) {
            //a flow node selector that selects no elements
            return emptyList();
        }
        return stream(flowNodeIds.split(",")).map(Long::valueOf).collect(toList());

    }

    public WorkDescriptor createExecuteConnectorOfProcessDescriptor(final long processDefinitionId,
            final long processInstanceId, final long rootProcessInstanceId,
            final long connectorInstanceId, final String connectorDefinitionName, final ConnectorEvent activationEvent,
            final FlowNodeSelector flowNodeSelector) {
        return WorkDescriptor.create(EXECUTE_PROCESS_CONNECTOR)
                .withParameter(PROCESS_DEFINITION_ID, processDefinitionId)
                .withParameter(PROCESS_INSTANCE_ID, processInstanceId)
                .withParameter(ROOT_PROCESS_INSTANCE_ID, rootProcessInstanceId)
                .withParameter(CONNECTOR_INSTANCE_ID, connectorInstanceId)
                .withParameter(CONNECTOR_DEFINITION_NAME, connectorDefinitionName)
                .withParameter(ACTIVATION_EVENT, activationEvent.name())
                .withParameter(FLOW_NODE_DEFINITIONS_FILTER,
                        flowNodeSelector != null ? flowNodeSelector.getFilteredElements().stream()
                                .map(f -> f.getId().toString()).collect(Collectors.joining(",")) : null)
                .withParameter(SUB_PROCESS_DEFINITION_ID,
                        flowNodeSelector != null ? flowNodeSelector.getSubProcessDefinitionId() : null);
    }

    private InSessionBonitaWork withSession(BonitaWork wrappedWork) {
        return new InSessionBonitaWork(wrappedWork);
    }

    private BonitaWork withConnectorContext(long connectorInstanceId, String connectorDefinitionName,
            ConnectorEvent activationEvent,
            ProcessInstanceContextWork processInstanceContextWork) {
        return new ConnectorDefinitionAndInstanceContextWork(processInstanceContextWork, connectorDefinitionName,
                connectorInstanceId,
                activationEvent);
    }

    public WorkDescriptor createExecuteFlowNodeWorkDescriptor(SFlowNodeInstance flowNodeInstance) {
        return WorkDescriptor.create(EXECUTE_FLOWNODE)
                .withParameter(PROCESS_DEFINITION_ID, flowNodeInstance.getProcessDefinitionId())
                .withParameter(PROCESS_INSTANCE_ID, flowNodeInstance.getParentProcessInstanceId())
                .withParameter(FLOW_NODE_INSTANCE_ID, flowNodeInstance.getId())
                .withParameter(STATE_ID, flowNodeInstance.getStateId())
                .withParameter(STATE_EXECUTING, flowNodeInstance.isStateExecuting())
                .withParameter(STATE_ABORTING, flowNodeInstance.isAborting())
                .withParameter(STATE_CANCELING, flowNodeInstance.isCanceling());
    }

    private BonitaWork createExecuteFlowNodeWork(WorkDescriptor workDescriptor) {
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        if (processInstanceId <= 0) {
            throw new RuntimeException(
                    "It is forbidden to create a ExecuteFlowNodeWork with a processInstanceId equals to "
                            + processInstanceId);
        }
        BonitaWork wrappedWork = new ExecuteFlowNodeWork(flowNodeInstanceId,
                workDescriptor.getInteger(STATE_ID),
                workDescriptor.getBoolean(STATE_EXECUTING),
                workDescriptor.getBoolean(STATE_ABORTING),
                workDescriptor.getBoolean(STATE_CANCELING));
        wrappedWork = withLock(processInstanceId, withTx(wrappedWork));
        wrappedWork = withFlowNodeContext(workDescriptor.getLong(PROCESS_DEFINITION_ID), processInstanceId,
                flowNodeInstanceId, wrappedWork);
        return withSession(wrappedWork);
    }

    public WorkDescriptor createExecuteMessageCoupleWorkDescriptor(final SMessageInstance messageInstance,
            final SWaitingMessageEvent waitingMessage) {
        return WorkDescriptor.create(EXECUTE_MESSAGE)
                .withParameter(MESSAGE_INSTANCE_ID, messageInstance.getId())
                .withParameter(MESSAGE_INSTANCE_NAME, messageInstance.getMessageName())
                .withParameter(MESSAGE_INSTANCE_TARGET_PROCESS, messageInstance.getTargetProcess())
                .withParameter(MESSAGE_INSTANCE_TARGET_FLOW_NODE, messageInstance.getTargetFlowNode())
                .withParameter(WAITING_MESSAGE_ID, waitingMessage.getId())
                .withParameter(WAITING_MESSAGE_EVENT_TYPE, waitingMessage.getEventType().name())
                .withParameter(PROCESS_INSTANCE_ID, waitingMessage.getParentProcessInstanceId())
                .withParameter(PROCESS_DEFINITION_ID, waitingMessage.getProcessDefinitionId())
                .withParameter(FLOW_NODE_INSTANCE_ID, waitingMessage.getFlowNodeInstanceId())
                .withParameter(ROOT_PROCESS_INSTANCE_ID, waitingMessage.getRootProcessInstanceId());
    }

    private BonitaWork createExecuteMessageCoupleWork(WorkDescriptor workDescriptor) {
        // no target process: we do not wrap in a LockProcessInstanceWork
        long messageId = workDescriptor.getLong(MESSAGE_INSTANCE_ID);
        String messageName = workDescriptor.getString(MESSAGE_INSTANCE_NAME);
        String targetProcess = workDescriptor.getString(MESSAGE_INSTANCE_TARGET_PROCESS);
        String targetFlowNode = workDescriptor.getString(MESSAGE_INSTANCE_TARGET_FLOW_NODE);

        long waitingMessageId = workDescriptor.getLong(WAITING_MESSAGE_ID);
        long parentProcessInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        long rootProcessInstanceId = workDescriptor.getLong(ROOT_PROCESS_INSTANCE_ID);
        String eventType = workDescriptor.getString(WAITING_MESSAGE_EVENT_TYPE);

        BonitaWork wrappedWork = withTx(new ExecuteMessageCoupleWork(messageId, waitingMessageId));
        if (parentProcessInstanceId > 0) {
            wrappedWork = withLock(parentProcessInstanceId, wrappedWork);
        }
        wrappedWork = new MessageInstanceContextWork(wrappedWork, messageName, targetProcess,
                targetFlowNode, eventType);
        wrappedWork = withProcessContext(processDefinitionId, parentProcessInstanceId,
                rootProcessInstanceId, flowNodeInstanceId, wrappedWork);
        return withSession(wrappedWork);
    }

    private BonitaWork createNotifyChildFinishedWork(WorkDescriptor workDescriptor) {
        final long processDefinitionId = workDescriptor.getLong(PROCESS_DEFINITION_ID);
        final long processInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        final long flowNodeInstanceId = workDescriptor.getLong(FLOW_NODE_INSTANCE_ID);
        BonitaWork wrappedWork = new NotifyChildFinishedWork(processDefinitionId, flowNodeInstanceId,
                workDescriptor.getInteger(STATE_ID),
                workDescriptor.getBoolean(STATE_EXECUTING),
                workDescriptor.getBoolean(STATE_ABORTING),
                workDescriptor.getBoolean(STATE_CANCELING));
        wrappedWork = withLock(processInstanceId, withTx(wrappedWork));
        wrappedWork = withFlowNodeContext(processDefinitionId, processInstanceId, flowNodeInstanceId, wrappedWork);
        return withSession(wrappedWork);
    }

    public WorkDescriptor createNotifyChildFinishedWorkDescriptor(SFlowNodeInstance sFlowNodeInstance) {
        return WorkDescriptor.create(FINISH_FLOWNODE)
                .withParameter(PROCESS_DEFINITION_ID, sFlowNodeInstance.getProcessDefinitionId())
                .withParameter(PROCESS_INSTANCE_ID, sFlowNodeInstance.getParentProcessInstanceId())
                .withParameter(FLOW_NODE_INSTANCE_ID, sFlowNodeInstance.getId())
                .withParameter(STATE_ID, sFlowNodeInstance.getStateId())
                .withParameter(STATE_EXECUTING, sFlowNodeInstance.isStateExecuting())
                .withParameter(STATE_ABORTING, sFlowNodeInstance.isAborting())
                .withParameter(STATE_CANCELING, sFlowNodeInstance.isCanceling());
    }

    private BonitaWork withLock(long processInstanceId, BonitaWork work) {
        return new LockProcessInstanceWork(work, processInstanceId);
    }

    private BonitaWork withTx(BonitaWork wrappedWork) {
        return new TxBonitaWork(wrappedWork);
    }

    private BonitaWork withFlowNodeContext(final long processDefinitionId, final long processInstanceId,
            final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork,
                processDefinitionId);
        final ProcessInstanceContextWork processInstanceContextWork = new ProcessInstanceContextWork(
                processDefinitionContextWork, processInstanceId);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private BonitaWork withProcessContext(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final long flowNodeInstanceId, final BonitaWork wrappedWork) {
        final ProcessInstanceContextWork processInstanceContextWork = withProcessContext(processDefinitionId,
                processInstanceId,
                rootProcessInstanceId, wrappedWork);
        return new FlowNodeDefinitionAndInstanceContextWork(processInstanceContextWork, flowNodeInstanceId);
    }

    private ProcessInstanceContextWork withProcessContext(final long processDefinitionId, final long processInstanceId,
            final long rootProcessInstanceId, final BonitaWork wrappedWork) {
        final ProcessDefinitionContextWork processDefinitionContextWork = new ProcessDefinitionContextWork(wrappedWork,
                processDefinitionId);
        return new ProcessInstanceContextWork(processDefinitionContextWork, processInstanceId, rootProcessInstanceId);
    }

    public WorkDescriptor createTriggerSignalWorkDescriptor(SWaitingSignalEvent listeningSignal) {
        return WorkDescriptor.create(TRIGGER_SIGNAL)
                .withParameter(LISTENING_SIGNAL_ID, listeningSignal.getId())
                .withParameter(LISTENING_SIGNAL_NAME, listeningSignal.getSignalName())
                .withParameter(PROCESS_INSTANCE_ID, listeningSignal.getParentProcessInstanceId());
    }

    private BonitaWork createTriggerSignalWork(WorkDescriptor workDescriptor) {
        BonitaWork triggerSignalWork = new TriggerSignalWork(workDescriptor.getLong(LISTENING_SIGNAL_ID),
                workDescriptor.getString(LISTENING_SIGNAL_NAME));
        triggerSignalWork = withTx(triggerSignalWork);
        long parentProcessInstanceId = workDescriptor.getLong(PROCESS_INSTANCE_ID);
        if (parentProcessInstanceId > 0) {
            triggerSignalWork = withLock(parentProcessInstanceId, triggerSignalWork);
        }
        return withSession(triggerSignalWork);
    }

    @Override
    public BonitaWork create(WorkDescriptor workDescriptor) {
        BonitaWork work;
        switch (workDescriptor.getType()) {
            case EXECUTE_ACTIVITY_CONNECTOR:
                work = createExecuteConnectorOfActivity(workDescriptor);
                break;
            case EXECUTE_PROCESS_CONNECTOR:
                work = createExecuteConnectorOfProcess(workDescriptor);
                break;
            case EXECUTE_FLOWNODE:
                work = createExecuteFlowNodeWork(workDescriptor);
                break;
            case FINISH_FLOWNODE:
                work = createNotifyChildFinishedWork(workDescriptor);
                break;
            case TRIGGER_SIGNAL:
                work = createTriggerSignalWork(workDescriptor);
                break;
            case EXECUTE_MESSAGE:
                work = createExecuteMessageCoupleWork(workDescriptor);
                break;
            default:
                work = createFromExtension(workDescriptor);
                break;
        }
        Long tenantId = workDescriptor.getTenantId();
        if (tenantId != null) {
            work.setTenantId(tenantId);
        }
        return work;
    }

    private BonitaWork createFromExtension(WorkDescriptor workDescriptor) {
        if (!extensions.containsKey(workDescriptor.getType())) {
            throw new IllegalArgumentException("Unkown type of work:" + workDescriptor.getType());
        }
        return extensions.get(workDescriptor.getType()).apply(workDescriptor);
    }

    public void addExtension(String workType, Function<WorkDescriptor, BonitaWork> workFactoryOfType) {
        extensions.put(workType, workFactoryOfType);
    }
}
