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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SCallActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingErrorEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.TransactionContainedProcessInstanceInterruptor;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ErrorEventHandlerStrategy extends CoupleEventHandlerStrategy {

    private static final OperationsWithContext EMPTY = new OperationsWithContext(null, null);

    private final ProcessInstanceService processInstanceService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final ContainerRegistry containerRegistry;

    private final ProcessDefinitionService processDefinitionService;

    private final EventsHandler eventsHandler;

    private final TechnicalLoggerService logger;

    public ErrorEventHandlerStrategy(final EventInstanceService eventInstanceService, final ProcessInstanceService processInstanceService,
            final FlowNodeInstanceService flowNodeInstanceService, final ContainerRegistry containerRegistry,
            final ProcessDefinitionService processDefinitionService, final EventsHandler eventsHandler, final TechnicalLoggerService logger) {
        super(eventInstanceService);
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.containerRegistry = containerRegistry;
        this.processDefinitionService = processDefinitionService;
        this.eventsHandler = eventsHandler;
        this.logger = logger;
    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Error event is thrown, error code = "
                    + ((SErrorEventTriggerDefinition) sEventTriggerDefinition).getErrorCode() + " process instance = " + eventInstance.getRootContainerId());
        }
        final TransactionContainedProcessInstanceInterruptor processInstanceInterruptor = new TransactionContainedProcessInstanceInterruptor(
                processInstanceService, getEventInstanceService(), containerRegistry, logger);
        updateInterruptorErrorEvent(eventInstance);
        processInstanceInterruptor.interruptChildrenOnly(eventInstance.getParentContainerId(), SStateCategory.ABORTING, -1, eventInstance.getId());
    }

    private void updateInterruptorErrorEvent(final SThrowEventInstance eventInstance) throws SProcessInstanceNotFoundException, SProcessInstanceReadException,
            SProcessInstanceModificationException {
        final SIntermediateThrowEventInstanceBuilderFactory throwEventKeyProvider = BuilderFactory.get(SIntermediateThrowEventInstanceBuilderFactory.class);
        final SProcessInstanceUpdateBuilder updateBuilder = BuilderFactory.get(SProcessInstanceUpdateBuilderFactory.class).createNewInstance();
        final long parentProcessInstanceId = eventInstance.getLogicalGroup(throwEventKeyProvider.getParentProcessInstanceIndex());
        updateBuilder.updateInterruptingEventId(eventInstance.getId());
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        processInstanceService.updateProcess(processInstance, updateBuilder.done());
    }

    @Override
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) {
        // NOT supported. Must to be implemented with errors can be sent via the API
    }

    @Override
    public boolean handlePostThrowEvent(final SProcessDefinition processDefinition, final SEndEventDefinition sEventDefinition,
            final SThrowEventInstance sThrowEventInstance, final SEventTriggerDefinition sEventTriggerDefinition, final SFlowNodeInstance sFlowNodeInstance)
            throws SBonitaException {
        boolean hasActionToExecute = false;
        final SFlowNodeInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SIntermediateThrowEventInstanceBuilderFactory.class);
        final long parentProcessInstanceId = sThrowEventInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());
        final SErrorEventTriggerDefinition errorTrigger = (SErrorEventTriggerDefinition) sEventTriggerDefinition;
        final SWaitingErrorEvent waitingErrorEvent = getWaitingErrorEvent(processDefinition.getProcessContainer(), parentProcessInstanceId, errorTrigger,
                sThrowEventInstance, sFlowNodeInstance);

        if (waitingErrorEvent != null) {
            eventsHandler.triggerCatchEvent(waitingErrorEvent, sThrowEventInstance.getId());
            hasActionToExecute = true;
        } else if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                final StringBuilder logBuilder = new StringBuilder();
                logBuilder.append("No catch error event was defined to handle the error code '");
                logBuilder.append(errorTrigger.getErrorCode());
                logBuilder.append("' defined in the process [name: ");
                logBuilder.append(processDefinition.getName());
                logBuilder.append(", version: ");
                logBuilder.append(processDefinition.getVersion());
                logBuilder.append("]");
                if (sEventDefinition != null) {
                    logBuilder.append(", throw event: ");
                    logBuilder.append(sEventDefinition.getName());
                }
                logBuilder.append(". This throw error event will act as a Terminate Event.");
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, logBuilder.toString());
        }
        return hasActionToExecute;
    }

    private SWaitingErrorEvent getWaitingErrorEvent(final SFlowElementContainerDefinition container, final long parentProcessInstanceId,
            final SErrorEventTriggerDefinition errorTrigger, final SThrowEventInstance eventInstance, final SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        final String errorCode = errorTrigger.getErrorCode();
        SWaitingErrorEvent waitingErrorEvent;

        // check on direct boundary
        waitingErrorEvent = getWaitingErrorEventFromBoundary(eventInstance, errorCode, flowNodeInstance);
        // check on event sub-process
        if (waitingErrorEvent == null) {
            waitingErrorEvent = getWaitingErrorEventSubProcess(container, parentProcessInstanceId, errorCode);
        }
        // check on call activities (recursive)
        if (waitingErrorEvent == null && processInstance.getCallerId() != -1 && SFlowNodeType.CALL_ACTIVITY.equals(processInstance.getCallerType())) {
            // check on call activities
            waitingErrorEvent = getWaitingErrorEventFromCallActivity(errorTrigger, processInstance, eventInstance, errorCode,
                    flowNodeInstance);
        }
        return waitingErrorEvent;
    }

    protected SWaitingErrorEvent getWaitingErrorEventFromBoundary(final SThrowEventInstance eventInstance,
            final String errorCode, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        final SFlowNodeInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SBoundaryEventInstanceBuilderFactory.class);
        // get the parent activity of the boundary
        final long logicalGroup = eventInstance.getLogicalGroup(flowNodeKeyProvider.getParentActivityInstanceIndex());
        if (logicalGroup <= 0) {
            // not in an activity = no boundary
            return null;
        }
        final long processDefinitionId = flowNodeInstance.getLogicalGroup(flowNodeKeyProvider.getProcessDefinitionIndex());
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SActivityDefinition flowNode = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                flowNodeInstance.getFlowNodeDefinitionId());
        final List<SBoundaryEventDefinition> boundaryEventDefinitions = flowNode.getBoundaryEventDefinitions();
        SWaitingErrorEvent waitingErrorEvent;
        if (flowNode.getLoopCharacteristics() == null) {
            waitingErrorEvent = getWaitingErrorEventFromBoundary(errorCode, flowNodeInstance, boundaryEventDefinitions);
        } else {
            final long multipleInstanceActivityId = flowNodeInstance.getLogicalGroup(flowNodeKeyProvider.getParentActivityInstanceIndex());
            final SFlowNodeInstance miActivityInstance = flowNodeInstanceService.getFlowNodeInstance(multipleInstanceActivityId);
            waitingErrorEvent = getWaitingErrorEventFromBoundary(errorCode, miActivityInstance, boundaryEventDefinitions);
        }
        return waitingErrorEvent;
    }

    private SWaitingErrorEvent getWaitingErrorEventFromCallActivity(final SErrorEventTriggerDefinition errorTrigger,
            final SProcessInstance processInstance, final SThrowEventInstance eventInstance,
            final String errorCode, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        final SFlowNodeInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SCallActivityInstanceBuilderFactory.class);
        final SCallActivityInstance callActivityInstance = (SCallActivityInstance) getEventInstanceService().getFlowNodeInstance(processInstance.getCallerId());
        final long processDefinitionId = callActivityInstance.getLogicalGroup(flowNodeKeyProvider.getProcessDefinitionIndex());
        final SProcessDefinition callActivityContainer = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SCallActivityDefinition callActivityDef = (SCallActivityDefinition) callActivityContainer.getProcessContainer().getFlowNode(
                callActivityInstance.getFlowNodeDefinitionId());
        final List<SBoundaryEventDefinition> boundaryEventDefinitions = callActivityDef.getBoundaryEventDefinitions();
        SWaitingErrorEvent waitingErrorEvent;
        if (callActivityDef.getLoopCharacteristics() != null) {
            final long multipleInstanceActivityId = callActivityInstance.getLogicalGroup(flowNodeKeyProvider.getParentActivityInstanceIndex());
            final SFlowNodeInstance miActivityInstance = flowNodeInstanceService.getFlowNodeInstance(multipleInstanceActivityId);
            waitingErrorEvent = getWaitingErrorEventFromBoundary(errorCode, miActivityInstance, boundaryEventDefinitions);
        } else {
            waitingErrorEvent = getWaitingErrorEventFromBoundary(errorCode, callActivityInstance, boundaryEventDefinitions);
        }
        if (waitingErrorEvent == null) {
            final long callActivityParentProcInstId = callActivityInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());
            waitingErrorEvent = getWaitingErrorEvent(callActivityContainer.getProcessContainer(), callActivityParentProcInstId, errorTrigger, eventInstance,
                    flowNodeInstance);
        }
        return waitingErrorEvent;
    }

    protected SWaitingErrorEvent getWaitingErrorEventFromBoundary(final String errorCode, final SFlowNodeInstance flowNodeInstance,
            final List<SBoundaryEventDefinition> boundaryEventDefinitions) throws SWaitingEventReadException {
        boolean canHandleError;
        String catchingErrorCode = errorCode;
        canHandleError = containsHandler(boundaryEventDefinitions, catchingErrorCode);
        if (!canHandleError) {
            catchingErrorCode = null; // catch all errors
            canHandleError = containsHandler(boundaryEventDefinitions, catchingErrorCode); // check for a handler that is able to catch all error codes
        }
        if (canHandleError) {
            return getEventInstanceService().getBoundaryWaitingErrorEvent(flowNodeInstance.getId(), catchingErrorCode);
        }
        return null;
    }

    private SWaitingErrorEvent getWaitingErrorEventSubProcess(final SFlowElementContainerDefinition container, final long parentProcessInstanceId,
            final String errorCode) throws SBonitaReadException, SBPMEventHandlerException {
        String catchingErrorCode = errorCode;
        boolean canHandleError = hasEventSubProcessCatchingError(container, catchingErrorCode);
        if (!canHandleError) {
            catchingErrorCode = null;
            canHandleError = hasEventSubProcessCatchingError(container, catchingErrorCode);
        }
        SWaitingErrorEvent waitingErrorEvent = null;
        if (canHandleError) {
            final SWaitingErrorEventBuilderFactory waitingErrorEventKeyProvider = BuilderFactory.get(SWaitingErrorEventBuilderFactory.class);
            final OrderByOption orderByOption = new OrderByOption(SWaitingEvent.class, waitingErrorEventKeyProvider.getFlowNodeNameKey(), OrderByType.ASC);

            final List<FilterOption> filters = new ArrayList<FilterOption>(3);
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getErrorCodeKey(), catchingErrorCode));
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getEventTypeKey(), SBPMEventType.EVENT_SUB_PROCESS.name()));
            filters.add(new FilterOption(SWaitingErrorEvent.class, waitingErrorEventKeyProvider.getParentProcessInstanceIdKey(), parentProcessInstanceId));
            final QueryOptions queryOptions = new QueryOptions(0, 2, Collections.singletonList(orderByOption), filters, null);
            final List<SWaitingErrorEvent> waitingEvents = getEventInstanceService().searchWaitingEvents(SWaitingErrorEvent.class, queryOptions);
            if (waitingEvents.size() != 1) {
                final StringBuilder stb = new StringBuilder();
                stb.append("One and only one error start event sub-process was expected for the process instance ");
                stb.append(parentProcessInstanceId);
                stb.append(" and error code ");
                stb.append(catchingErrorCode);
                stb.append(", but ");
                stb.append(waitingEvents.size());
                stb.append(" was found.");
                throw new SBPMEventHandlerException(stb.toString());
            }
            waitingErrorEvent = waitingEvents.get(0);
        }
        return waitingErrorEvent;
    }

    private boolean containsHandler(final List<SBoundaryEventDefinition> boundaryEventDefinitions, final String errorCode) {
        boolean found = false;
        final Iterator<SBoundaryEventDefinition> iterator = boundaryEventDefinitions.iterator();
        while (iterator.hasNext() && !found) {
            final SBoundaryEventDefinition boundaryEventDefinition = iterator.next();
            final SCatchErrorEventTriggerDefinition currentErrorTrigger = boundaryEventDefinition.getErrorEventTriggerDefinition(errorCode);
            if (currentErrorTrigger != null) {
                found = true;
            }
        }
        return found;
    }

    private boolean hasEventSubProcessCatchingError(final SFlowElementContainerDefinition container, final String errorCode) {
        boolean found = false;
        final Iterator<SActivityDefinition> iterator = container.getActivities().iterator();
        while (iterator.hasNext() && !found) {
            final SActivityDefinition activity = iterator.next();
            if (SFlowNodeType.SUB_PROCESS.equals(activity.getType()) && ((SSubProcessDefinition) activity).isTriggeredByEvent()) {
                final SSubProcessDefinition eventSubProcess = (SSubProcessDefinition) activity;
                final SStartEventDefinition startEventDefinition = eventSubProcess.getSubProcessContainer().getStartEvents().get(0);
                if (startEventDefinition.getErrorEventTriggerDefinition(errorCode) != null) {
                    found = true;
                }
            }

        }
        return found;
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final SWaitingErrorEventBuilderFactory builderFact = BuilderFactory.get(SWaitingErrorEventBuilderFactory.class);
        final SErrorEventTriggerDefinition errorEventTriggerDefinition = (SErrorEventTriggerDefinition) sEventTriggerDefinition;
        final SEventInstanceBuilderFactory eventInstanceKeyProvider = BuilderFactory.get(SIntermediateCatchEventInstanceBuilderFactory.class);
        switch (eventDefinition.getType()) {
            case BOUNDARY_EVENT:
                final SBoundaryEventInstance boundary = (SBoundaryEventInstance) eventInstance;
                final long rootProcessInstanceId = eventInstance.getLogicalGroup(eventInstanceKeyProvider.getRootProcessInstanceIndex());
                final long parentProcessInstanceId = eventInstance.getLogicalGroup(eventInstanceKeyProvider.getParentProcessInstanceIndex());
                final SWaitingErrorEventBuilder builder = builderFact.createNewWaitingErrorBoundaryEventInstance(processDefinition.getId(),
                        rootProcessInstanceId, parentProcessInstanceId, eventInstance.getId(), errorEventTriggerDefinition.getErrorCode(),
                        processDefinition.getName(), eventInstance.getFlowNodeDefinitionId(), eventInstance.getName(), boundary.getActivityInstanceId());
                final SWaitingErrorEvent errorEvent = builder.done();
                getEventInstanceService().createWaitingEvent(errorEvent);
                break;
            case INTERMEDIATE_CATCH_EVENT:
            case START_EVENT:
                throw new SWaitingEventCreationException("Catch error event cannot be put in " + eventDefinition.getType()
                        + ". They must be used as boundary events or start event subprocess.");
            default:
                throw new SWaitingEventCreationException(eventDefinition.getType() + " is not a catch event.");
        }
    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) {
        return EMPTY;
    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final SWaitingErrorEventBuilderFactory builderFact = BuilderFactory.get(SWaitingErrorEventBuilderFactory.class);
        final SErrorEventTriggerDefinition trigger = (SErrorEventTriggerDefinition) sEventTriggerDefinition;
        final SWaitingErrorEventBuilder builder = builderFact.createNewWaitingErrorEventSubProcInstance(processDefinition.getId(),
                parentProcessInstance.getId(), parentProcessInstance.getRootProcessInstanceId(), trigger.getErrorCode(), processDefinition.getName(),
                eventDefinition.getId(), eventDefinition.getName(), subProcessId);

        final SWaitingErrorEvent event = builder.done();
        getEventInstanceService().createWaitingEvent(event);
    }

}
