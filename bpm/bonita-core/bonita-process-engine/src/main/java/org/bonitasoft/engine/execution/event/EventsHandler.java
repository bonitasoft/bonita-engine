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
package org.bonitasoft.engine.execution.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSendTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.TransactionContainedProcessInstanceInterruptor;
import org.bonitasoft.engine.execution.TransactionalProcessInstanceInterruptor;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;

/**
 * Handle event depending on its type
 * TODO
 * * Move all code that instantiate process/execute flow node after a event was triggered here + make it call reachedCatchEvent
 * * For event sub process: the instantiate process must cancel all activities then instantiate the process
 * * the instantiate event subprocess method is like the start of the process executor but with less things: factorise it
 * * check that there is no execution issues with event sub process (add more test)
 * * add test for each kind of start event in event sub process
 * * try to trigger event subprocess with multiple events
 *
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class EventsHandler {

    private final Map<SEventTriggerType, EventHandlerStrategy> handlers;

    private final ContainerRegistry containerRegistry;

    private final ProcessDefinitionService processDefinitionService;

    private final EventInstanceService eventInstanceService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final ProcessInstanceService processInstanceService;

    private final TechnicalLoggerService logger;

    private ProcessExecutor processExecutor;

    public EventsHandler(final SchedulerService schedulerService, final ExpressionResolverService expressionResolverService,
            final EventInstanceService eventInstanceService, final BPMInstancesCreator bpmInstancesCreator, final DataInstanceService dataInstanceService,
            final ProcessDefinitionService processDefinitionService, final ContainerRegistry containerRegistry,
            final ProcessInstanceService processInstanceService, final FlowNodeInstanceService flowNodeInstanceService, final TechnicalLoggerService logger) {
        this.eventInstanceService = eventInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.containerRegistry = containerRegistry;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.processInstanceService = processInstanceService;
        this.logger = logger;
        handlers = new HashMap<SEventTriggerType, EventHandlerStrategy>(4);
        handlers.put(SEventTriggerType.TIMER, new TimerEventHandlerStrategy(expressionResolverService, schedulerService, eventInstanceService, logger));
        handlers.put(SEventTriggerType.MESSAGE, new MessageEventHandlerStrategy(expressionResolverService, eventInstanceService,
                bpmInstancesCreator, dataInstanceService, processDefinitionService));
        handlers.put(SEventTriggerType.SIGNAL, new SignalEventHandlerStrategy(this, eventInstanceService));
        handlers.put(SEventTriggerType.TERMINATE, new TerminateEventHandlerStrategy(processInstanceService, eventInstanceService,
                containerRegistry, logger));
        handlers.put(SEventTriggerType.ERROR, new ErrorEventHandlerStrategy(eventInstanceService, processInstanceService, flowNodeInstanceService,
                containerRegistry,
                processDefinitionService, this, logger));
    }

    public void setProcessExecutor(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;

    }

    /**
     * called when a catchEvent is reached
     * e.g. we are going on a catch event in the flow of a process
     * This is different of trigger catch event:
     * e.g. for a message handleCatchEvent will create the waiting event and triggerCatchEvent is called when the message is received
     *
     * @param processDefinition
     * @param eventDefinition
     * @param eventInstance
     * @throws SBonitaException
     */
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SEventInstance eventInstance)
            throws SBonitaException {
        final List<SEventTriggerDefinition> eventTriggers = eventDefinition.getEventTriggers();
        for (final SEventTriggerDefinition sEventTriggerDefinition : eventTriggers) {
            final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
            eventHandlerStrategy.handleCatchEvent(processDefinition, eventDefinition, (SCatchEventInstance) eventInstance,
                    sEventTriggerDefinition);
        }
    }

    public void handleCatchMessage(final SProcessDefinition processDefinition, final SReceiveTaskDefinition receiveTaskDefinition,
            final SReceiveTaskInstance receiveTaskInstance) throws SBonitaException {
        final SEventTriggerDefinition eventTrigger = receiveTaskDefinition.getTrigger();
        final MessageEventHandlerStrategy messageEventHandlerStrategy = (MessageEventHandlerStrategy) handlers.get(SEventTriggerType.MESSAGE);
        messageEventHandlerStrategy.handleCatchEvent(processDefinition, receiveTaskInstance, eventTrigger);
    }

    /**
     * called when a star subprocess is reached
     * e.g. we are going on a catch event in the flow of a process
     * This is different of trigger catch event:
     * e.g. for a message handleCatchEvent will create the waiting event and triggerCatchEvent is called when the message is received
     *
     * @param processDefinition
     * @param eventDefinition
     * @param parentProcessInstance
     * @throws SBonitaException
     */
    private void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final long subProcessId, final SProcessInstance parentProcessInstance) throws SBonitaException {
        final List<SEventTriggerDefinition> eventTriggers = eventDefinition.getEventTriggers();
        for (final SEventTriggerDefinition sEventTriggerDefinition : eventTriggers) {
            final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
            eventHandlerStrategy.handleEventSubProcess(processDefinition, eventDefinition, sEventTriggerDefinition, subProcessId,
                    parentProcessInstance);
        }
    }

    public void handleEventSubProcess(final SProcessDefinition sDefinition, final SProcessInstance parentProcessInstance) throws SBonitaException {
        final Set<SActivityDefinition> activities = sDefinition.getProcessContainer().getActivities();
        for (final SActivityDefinition activity : activities) {
            if (SFlowNodeType.SUB_PROCESS.equals(activity.getType()) && ((SSubProcessDefinition) activity).isTriggeredByEvent()) {
                final SSubProcessDefinition eventSubProcess = (SSubProcessDefinition) activity;
                final SStartEventDefinition sStartEventDefinition = eventSubProcess.getSubProcessContainer().getStartEvents().get(0);
                handleEventSubProcess(sDefinition, sStartEventDefinition, activity.getId(), parentProcessInstance);
            }
        }
    }

    private void unregisterEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final long subProcessId,
            final SProcessInstance parentProcessInstance) throws SBonitaException {
        final List<SEventTriggerDefinition> eventTriggers = eventDefinition.getEventTriggers();
        for (final SEventTriggerDefinition sEventTriggerDefinition : eventTriggers) {
            final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
            eventHandlerStrategy.unregisterCatchEvent(processDefinition, eventDefinition, sEventTriggerDefinition, subProcessId, parentProcessInstance);
        }
    }

    public void unregisterEventSubProcess(final SProcessDefinition sDefinition, final SProcessInstance parentProcessInstance) throws SBonitaException {
        final Set<SActivityDefinition> activities = sDefinition.getProcessContainer().getActivities();
        for (final SActivityDefinition activity : activities) {
            if (SFlowNodeType.SUB_PROCESS.equals(activity.getType()) && ((SSubProcessDefinition) activity).isTriggeredByEvent()) {
                final SSubProcessDefinition eventSubProcess = (SSubProcessDefinition) activity;
                final SStartEventDefinition sStartEventDefinition = eventSubProcess.getSubProcessContainer().getStartEvents().get(0);
                unregisterEventSubProcess(sDefinition, sStartEventDefinition, activity.getId(), parentProcessInstance);
            }
        }
    }

    /**
     * called when we reach a throw event in the flow of a process
     *
     * @param processDefinition
     * @param eventDefinition
     * @param eventInstance
     * @throws SBonitaException
     */
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SEventInstance eventInstance)
            throws SBonitaException {
        final List<SEventTriggerDefinition> eventTriggers = eventDefinition.getEventTriggers();
        for (final SEventTriggerDefinition sEventTriggerDefinition : eventTriggers) {
            final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
            if (eventHandlerStrategy != null) {
                eventHandlerStrategy.handleThrowEvent(processDefinition, eventDefinition, (SThrowEventInstance) eventInstance, sEventTriggerDefinition);
            }
        }
    }

    public void handleThrowMessage(final SProcessDefinition processDefinition, final SSendTaskDefinition sendTaskDefinition,
            final SSendTaskInstance sendTaskInstance) throws SEventTriggerInstanceCreationException, SMessageInstanceCreationException, SDataInstanceException,
            SExpressionException {
        final SThrowMessageEventTriggerDefinition eventTrigger = sendTaskDefinition.getMessageTrigger();
        final MessageEventHandlerStrategy messageEventHandlerStrategy = (MessageEventHandlerStrategy) handlers.get(SEventTriggerType.MESSAGE);
        messageEventHandlerStrategy.handleThrowEvent(processDefinition, sendTaskInstance, eventTrigger);
    }

    public boolean handlePostThrowEvent(final SProcessDefinition sProcessDefinition, final SEndEventDefinition sEndEventDefinition,
            final SThrowEventInstance sThrowEventInstance, final SFlowNodeInstance sFlowNodeInstance) throws SBonitaException {
        boolean hasActionsToExecute = false;
        if (sEndEventDefinition == null) {
            /*
             * If the eventDefinition is not set that mean that the event was
             * triggered by a connector and that it's an error event
             * ...yep that's a lot of assumptions
             */
            final String errorCode = sThrowEventInstance.getName();

            final SThrowErrorEventTriggerDefinition errorEventTriggerDefinition = BuilderFactory.get(SThrowErrorEventTriggerDefinitionBuilderFactory.class)
                    .createNewInstance(errorCode).done();
            hasActionsToExecute = handlers.get(SEventTriggerType.ERROR).handlePostThrowEvent(sProcessDefinition, null, sThrowEventInstance,
                    errorEventTriggerDefinition, sFlowNodeInstance);
        } else {
            final List<SEventTriggerDefinition> eventTriggers = sEndEventDefinition.getEventTriggers();
            for (final SEventTriggerDefinition sEventTriggerDefinition : eventTriggers) {
                final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
                if (eventHandlerStrategy != null) {
                    hasActionsToExecute = hasActionsToExecute
                            || eventHandlerStrategy.handlePostThrowEvent(sProcessDefinition, sEndEventDefinition, sThrowEventInstance, sEventTriggerDefinition,
                                    sFlowNodeInstance);
                }
            }
        }
        return hasActionsToExecute;
    }

    /**
     * called when a BPM event is triggered by the API
     *
     * @param sEventTriggerDefinition
     * @throws SBonitaException
     */
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final EventHandlerStrategy eventHandlerStrategy = handlers.get(sEventTriggerDefinition.getEventTriggerType());
        if (eventHandlerStrategy != null) {
            eventHandlerStrategy.handleThrowEvent(sEventTriggerDefinition);
        }
    }

    /**
     * When a trigger is 'launched' the catch event is reached and is waken up/created using its waiting event
     * Depending on the type it will execute the catch event of instantiate the process/subprocess
     *
     * @param waitingEvent
     * @param triggeringElementID
     * @throws SBonitaException
     */
    public void triggerCatchEvent(final SWaitingEvent waitingEvent, final Long triggeringElementID) throws SBonitaException {
        final SBPMEventType eventType = waitingEvent.getEventType();
        final long processDefinitionId = waitingEvent.getProcessDefinitionId();
        final long targetSFlowNodeDefinitionId = waitingEvent.getFlowNodeDefinitionId();
        final long flowNodeInstanceId = waitingEvent.getFlowNodeInstanceId();
        final OperationsWithContext operations = handlers.get(waitingEvent.getEventTriggerType()).getOperations(waitingEvent, triggeringElementID);
        if (SBPMEventType.EVENT_SUB_PROCESS.equals(waitingEvent.getEventType())) {
            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final SStartEventDefinition startEvent = (SStartEventDefinition) processDefinition.getProcessContainer().getFlowNode(
                    waitingEvent.getFlowNodeDefinitionId());
            triggerCatchStartEventSubProcess(waitingEvent.getEventTriggerType(), processDefinitionId, targetSFlowNodeDefinitionId, operations,
                    waitingEvent.getSubProcessId(), waitingEvent.getParentProcessInstanceId(), waitingEvent.getRootProcessInstanceId(),
                    startEvent.isInterrupting());
        } else {
            triggerCatchEvent(eventType, processDefinitionId, targetSFlowNodeDefinitionId, waitingEvent, flowNodeInstanceId, operations);
        }
    }

    private void triggerInTransaction(final SBPMEventType eventType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final SWaitingEvent waitingEvent, final Long flowNodeInstanceId, final OperationsWithContext operations) throws SBonitaException {
        final TransactionContent transactionContent = new TransactionContent() {

            @Override
            public void execute() throws SBonitaException {
                triggerCatchEvent(eventType, processDefinitionId, targetSFlowNodeDefinitionId, waitingEvent, flowNodeInstanceId, operations);
            }
        };
        transactionContent.execute();
    }

    private void triggerInTransaction(final SEventTriggerType eventTriggerType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final OperationsWithContext operations, final long subProcessId, final Long parentProcessInstanceId, final Long rootProcessInstanceId,
            final Boolean isInterrupting) throws SBonitaException {
        final TransactionContent transactionContent = new TransactionContent() {

            @Override
            public void execute() throws SBonitaException {
                triggerCatchStartEventSubProcess(eventTriggerType, processDefinitionId, targetSFlowNodeDefinitionId, operations, subProcessId,
                        parentProcessInstanceId, rootProcessInstanceId, isInterrupting);
            }
        };
        transactionContent.execute();
    }

    private void triggerCatchEvent(final SBPMEventType eventType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final SWaitingEvent waitingEvent, final Long flowNodeInstanceId, final OperationsWithContext operations) throws SBonitaException {
        switch (eventType) {
            case START_EVENT:
                instantiateProcess(processDefinitionId, targetSFlowNodeDefinitionId, operations);
                break;
            default:
                if (waitingEvent != null) { // is null if it's a timer
                    eventInstanceService.deleteWaitingEvent(waitingEvent);
                    executeFlowNode(flowNodeInstanceId, operations, waitingEvent.getParentProcessInstanceId());
                } else {
                    final long processInstanceId = eventInstanceService.getFlowNodeInstance(flowNodeInstanceId).getParentProcessInstanceId();
                    executeFlowNode(flowNodeInstanceId, operations, processInstanceId);
                }
                break;
        }
    }

    private void triggerCatchStartEventSubProcess(final SEventTriggerType triggerType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final OperationsWithContext operations, final long subProcessId, final long parentProcessInstanceId, final Long rootProcessInstanceId,
            final Boolean isInterrupting) throws SBonitaException {
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinition.getProcessContainer().getFlowNode(subProcessId);
        final SFlowNodeInstance subProcflowNodeInstance = bpmInstancesCreator.createFlowNodeInstance(processDefinitionId, rootProcessInstanceId,
                parentProcessInstanceId, SFlowElementsContainerType.PROCESS, sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, false, 0,
                SStateCategory.NORMAL, -1);
        final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        if (triggerType.equals(SEventTriggerType.ERROR)) {
            // if error interrupt directly.
            final TransactionContainedProcessInstanceInterruptor interruptor = new TransactionContainedProcessInstanceInterruptor(
                    processInstanceService, eventInstanceService, containerRegistry, logger);
            interruptor.interruptProcessInstance(parentProcessInstanceId, SStateCategory.ABORTING, -1, subProcflowNodeInstance.getId());
        } else if (isInterrupting) {
            // other interrupting catch
            final TransactionalProcessInstanceInterruptor interruptor = new TransactionalProcessInstanceInterruptor(processInstanceService,
                    eventInstanceService,
                    processExecutor, logger);
            interruptor.interruptProcessInstance(parentProcessInstanceId, SStateCategory.ABORTING, -1, subProcflowNodeInstance.getId());
        }
        processExecutor.start(processDefinitionId, targetSFlowNodeDefinitionId, 0, 0, operations.getContext(), operations.getOperations(),
                null, null, subProcflowNodeInstance.getId(), subProcessId, null); // Should we support process contract inputs on EventSubProcess?
        unregisterEventSubProcess(processDefinition, parentProcessInstance);
    }

    public void triggerCatchEvent(final String eventType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final Long flowNodeInstanceId, final String containerType) throws SBonitaException {
        final SBPMEventType type = SBPMEventType.valueOf(eventType);
        triggerInTransaction(type, processDefinitionId, targetSFlowNodeDefinitionId, null, flowNodeInstanceId, new OperationsWithContext(null, null,
                containerType));
    }

    public void triggerCatchEvent(final SEventTriggerType eventTriggerType, final Long processDefinitionId, final Long targetSFlowNodeDefinitionId,
            final String containerType, final long subProcessId, final Long parentProcessInstanceId, final Long rootProcessInstanceId,
            final Boolean isInterrupting) throws SBonitaException {
        triggerInTransaction(eventTriggerType, processDefinitionId, targetSFlowNodeDefinitionId, new OperationsWithContext(null, null, containerType),
                subProcessId, parentProcessInstanceId, rootProcessInstanceId, isInterrupting);
    }

    private void executeFlowNode(final long flowNodeInstanceId, final OperationsWithContext operations, final long processInstanceId)
            throws SFlowNodeReadException, SFlowNodeExecutionException {
        // in same thread because we delete the message instance after triggering the catch event. The data is of the message
        // is deleted so we will be unable to execute the flow node instance
        containerRegistry.executeFlowNodeInSameThread(processInstanceId, flowNodeInstanceId, operations.getContext(), operations.getOperations(),
                operations.getContainerType());
    }

    private void instantiateProcess(final long processDefinitionId, final long targetSFlowNodeDefinitionId, final OperationsWithContext operations)
            throws SProcessInstanceCreationException {
        processExecutor.start(processDefinitionId, targetSFlowNodeDefinitionId, 0, 0, operations.getContext(), operations.getOperations(),
                null, null, -1, -1, null);
    }

    public EventHandlerStrategy getHandler(final SEventTriggerType triggerType) {
        return handlers.get(triggerType);
    }

}
