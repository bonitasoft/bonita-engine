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
import java.util.List;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SCorrelationDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SCorrelationContainerBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class MessageEventHandlerStrategy extends CoupleEventHandlerStrategy {

    private static final String CORRELATION_NO_VALUE = "NONE";

    private final ExpressionResolverService expressionResolverService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final DataInstanceService dataInstanceService;

    private final ProcessDefinitionService processDefinitionService;

    public MessageEventHandlerStrategy(final ExpressionResolverService expressionResolverService,
            final EventInstanceService eventInstanceService, final BPMInstancesCreator bpmInstancesCreator, final DataInstanceService dataInstanceService,
            final ProcessDefinitionService processDefinitionService) {
        super(eventInstanceService);
        this.expressionResolverService = expressionResolverService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.dataInstanceService = dataInstanceService;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final SCatchMessageEventTriggerDefinition messageTrigger = (SCatchMessageEventTriggerDefinition) sEventTriggerDefinition;
        final String messageName = messageTrigger.getMessageName();
        final String processName = processDefinition.getName();
        final SWaitingMessageEventBuilder builder;
        SExpressionContext expressionContext;
        switch (eventDefinition.getType()) {
            case BOUNDARY_EVENT:
                builder = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).createNewWaitingMessageBoundaryEventInstance(processDefinition.getId(),
                        eventInstance.getRootContainerId(), eventInstance.getParentProcessInstanceId(), eventInstance.getId(), messageName, processName,
                        eventInstance.getFlowNodeDefinitionId(),
                        eventInstance.getName());
                expressionContext = new SExpressionContext(eventInstance.getParentContainerId(), getParentContainerType(eventInstance).name(),
                        processDefinition.getId());
                break;
            case INTERMEDIATE_CATCH_EVENT:
                builder = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).createNewWaitingMessageIntermediateEventInstance(
                        processDefinition.getId(),
                        eventInstance.getRootContainerId(), eventInstance.getParentProcessInstanceId(), eventInstance.getId(), messageName, processName,
                        eventInstance.getFlowNodeDefinitionId(),
                        eventInstance.getName());
                expressionContext = new SExpressionContext(eventInstance.getParentContainerId(), getParentContainerType(eventInstance).name(),
                        processDefinition.getId());
                break;
            case START_EVENT:
                builder = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).createNewWaitingMessageStartEventInstance(processDefinition.getId(),
                        messageTrigger.getMessageName(), processDefinition.getName(), eventDefinition.getId(), eventDefinition.getName());
                expressionContext = new SExpressionContext();
                expressionContext.setProcessDefinitionId(processDefinition.getId());
                break;
            default:
                throw new SWaitingEventCreationException(eventDefinition.getType() + " is not a catch event.");
        }
        fillCorrelation(builder, messageTrigger.getCorrelations(), expressionContext);
        getEventInstanceService().createWaitingEvent(builder.done());

    }

    public void handleCatchEvent(final SProcessDefinition processDefinition, final SReceiveTaskInstance receiveTaskInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final SCatchMessageEventTriggerDefinition messageTrigger = (SCatchMessageEventTriggerDefinition) sEventTriggerDefinition;
        final String messageName = messageTrigger.getMessageName();
        final String processName = processDefinition.getName();
        final SWaitingMessageEventBuilder builder;
        SExpressionContext expressionContext;

        builder = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).createNewWaitingMessageIntermediateEventInstance(processDefinition.getId(),
                receiveTaskInstance.getRootContainerId(), receiveTaskInstance.getParentProcessInstanceId(), receiveTaskInstance.getId(), messageName,
                processName, receiveTaskInstance.getFlowNodeDefinitionId(),
                receiveTaskInstance.getName());
        expressionContext = new SExpressionContext(receiveTaskInstance.getParentContainerId(), getParentContainerType(receiveTaskInstance).name(),
                processDefinition.getId());

        fillCorrelation(builder, messageTrigger.getCorrelations(), expressionContext);
        getEventInstanceService().createWaitingEvent(builder.done());

    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final Long processDefinitionId = processDefinition.getId();
        final SExpressionContext expressionContext = new SExpressionContext(eventInstance.getParentContainerId(), getParentContainerType(eventInstance).name(),
                processDefinitionId);

        handleThrowMessage(sEventTriggerDefinition, eventInstance.getId(), eventInstance.getName(), processDefinitionId, expressionContext);
    }

    public void handleThrowEvent(final SProcessDefinition processDefinition, final SSendTaskInstance sendTaskInstance,
            final SThrowMessageEventTriggerDefinition messageTrigger) throws SEventTriggerInstanceCreationException, SMessageInstanceCreationException,
            SDataInstanceException, SExpressionException {
        final SExpressionContext expressionContext = new SExpressionContext(sendTaskInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                processDefinition.getId());
        handleThrowMessage(messageTrigger, sendTaskInstance.getId(), sendTaskInstance.getName(), processDefinition.getId(), expressionContext);
    }

    private void handleThrowMessage(final SEventTriggerDefinition sEventTriggerDefinition, final long eventInstanceId, final String eventInstanceName,
            final Long processDefinitionId, final SExpressionContext expressionContext) throws SEventTriggerInstanceCreationException,
            SMessageInstanceCreationException, SDataInstanceException, SExpressionException {
        final SThrowMessageEventTriggerDefinition messageTrigger = (SThrowMessageEventTriggerDefinition) sEventTriggerDefinition;
        final String messageName = messageTrigger.getMessageName();
        final SExpression targetProcess = messageTrigger.getTargetProcess();
        final SExpression targetFlowNode = messageTrigger.getTargetFlowNode();
        // evaluate expression
        final String stringTargetProcess = (String) expressionResolverService.evaluate(targetProcess, expressionContext);
        String stringTargetFlowNode = null;
        if (targetFlowNode != null) {
            stringTargetFlowNode = (String) expressionResolverService.evaluate(targetFlowNode, expressionContext);
        }

        final SThrowMessageEventTriggerInstance messageEventTriggerInstance = BuilderFactory.get(SThrowMessageEventTriggerInstanceBuilderFactory.class)
                .createNewInstance(eventInstanceId, messageName, stringTargetProcess, stringTargetFlowNode).done();
        getEventInstanceService().createEventTriggerInstance(messageEventTriggerInstance);
        final SMessageInstanceBuilder builder = BuilderFactory.get(SMessageInstanceBuilderFactory.class).createNewInstance(messageEventTriggerInstance,
                processDefinitionId, eventInstanceName);
        final List<SCorrelationDefinition> correlations = messageTrigger.getCorrelations();
        fillCorrelation(builder, correlations, expressionContext);
        final SMessageInstance messageInstance = builder.done();
        // evaluate and add correlations
        getEventInstanceService().createMessageInstance(messageInstance);

        // create data
        if (!messageTrigger.getDataDefinitions().isEmpty()) {
            bpmInstancesCreator.createDataInstances(messageTrigger.getDataDefinitions(), messageInstance.getId(), DataInstanceContainer.MESSAGE_INSTANCE,
                    expressionContext);
            final TechnicalLoggerService logger = bpmInstancesCreator.getLogger();
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(
                        this.getClass(),
                        TechnicalLogSeverity.DEBUG,
                        "Initialized variables for message instance [name: <" + messageInstance.getMessageName() + ">, id: <" + messageInstance.getId()
                                + ">, flow node: <" + messageInstance.getFlowNodeName() + ">, target flow node: <" + messageInstance.getTargetFlowNode()
                                + ">, target process: <" + messageInstance.getTargetProcess() + ">, process definition: <"
                                + messageInstance.getProcessDefinitionId() + ">]");
            }
        }
    }

    private void fillCorrelation(final SCorrelationContainerBuilder builder, final List<SCorrelationDefinition> correlations,
            final SExpressionContext expressionContext) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        final int size = Math.min(5, correlations.size());
        final List<SExpression> toEval = new ArrayList<SExpression>(size * 2);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                final SCorrelationDefinition sCorrelationDefinition = correlations.get(i);
                toEval.add(sCorrelationDefinition.getKey());
                toEval.add(sCorrelationDefinition.getValue());
            }
            final List<Object> res = expressionResolverService.evaluate(toEval, expressionContext);

            final List<String> keys = new ArrayList<String>(size);
            final List<String> values = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                keys.add(String.valueOf(res.get(i * 2)));
                values.add(String.valueOf(res.get(i * 2 + 1)));
            }
            final List<String> sortedKeys = new ArrayList<String>(keys);
            Collections.sort(sortedKeys);
            for (int i = 0; i < size; i++) {
                final String key = sortedKeys.get(i);
                builder.setCorrelation(i + 1, key + "-$-" + values.get(keys.indexOf(key)));
            }

        }
        for (int i = size; i < 5; i++) {
            builder.setCorrelation(i + 1, CORRELATION_NO_VALUE);
        }
    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) throws SBonitaException {
        final SMessageInstance messageInstance = getEventInstanceService().getMessageInstance(triggeringElementID);
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(waitingEvent.getProcessDefinitionId());
        final SExpressionContext context = new SExpressionContext(messageInstance.getId(), DataInstanceContainer.MESSAGE_INSTANCE.name(),
                processDefinition.getId());
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SFlowNodeDefinition targetFlowNode = processContainer.getFlowNode(waitingEvent.getFlowNodeName());
        SCatchMessageEventTriggerDefinition messageEventTrigger = null;
        if (targetFlowNode instanceof SReceiveTaskDefinition) {
            messageEventTrigger = ((SReceiveTaskDefinition) targetFlowNode).getTrigger();
        } else {
            SCatchEventDefinition catchEvent;
            if (SBPMEventType.BOUNDARY_EVENT.equals(waitingEvent.getEventType())) {
                catchEvent = processContainer.getBoundaryEvent(messageInstance.getTargetFlowNode());
            } else {
                catchEvent = (SCatchEventDefinition) targetFlowNode;
            }
            messageEventTrigger = catchEvent.getMessageEventTriggerDefinition(messageInstance.getMessageName());
        }
        final List<SOperation> operations = messageEventTrigger.getOperations();
        return new OperationsWithContext(context, operations);
    }

    @Override
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        handleThrowMessage(sEventTriggerDefinition, -1L, "", -1L, null);
    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final SWaitingMessageEventBuilderFactory builderFact = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class);
        final SMessageEventTriggerDefinition messageEventTriggerDefinition = (SMessageEventTriggerDefinition) sEventTriggerDefinition;
        final SWaitingMessageEventBuilder builder = builderFact.createNewWaitingMessageEventSubProcInstance(processDefinition.getId(),
                parentProcessInstance.getId(), parentProcessInstance.getRootProcessInstanceId(), messageEventTriggerDefinition.getMessageName(),
                processDefinition.getName(), eventDefinition.getId(), eventDefinition.getName(), subProcessId);
        final SExpressionContext expressionContext = new SExpressionContext(parentProcessInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                processDefinition.getId());
        fillCorrelation(builder, messageEventTriggerDefinition.getCorrelations(), expressionContext);
        final SWaitingMessageEvent event = builder.done();
        getEventInstanceService().createWaitingEvent(event);
    }

    @Override
    public boolean handlePostThrowEvent(final SProcessDefinition processDefinition, final SEndEventDefinition sEventDefinition,
            final SThrowEventInstance sThrowEventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition, final SFlowNodeInstance sFlowNodeInstance) {
        // nothing to do
        return false;
    }

}
