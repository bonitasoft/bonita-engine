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
package org.bonitasoft.engine.bpm.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.api.impl.transaction.EvaluateExpression;
import org.bonitasoft.engine.api.impl.transaction.activity.CreateActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.actor.GetActor;
import org.bonitasoft.engine.api.impl.transaction.connector.CreateConnectorInstances;
import org.bonitasoft.engine.api.impl.transaction.data.CreateSDataInstances;
import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.api.impl.transaction.flownode.CreateGatewayInstance;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SStandardLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateThrowEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SAutomaticTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SCallActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SHumanTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SLoopActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SManualTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SReceiveTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SSendTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SSubProcessActivityInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateCatchEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SIntermediateThrowEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SStartEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.data.instance.model.exceptions.SDataInstanceNotWellFormedException;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class BPMInstancesCreator {

    private final ActivityInstanceService activityInstanceService;

    private final BPMInstanceBuilders instanceBuilders;

    private final ActorMappingService actorMappingService;

    private final GatewayInstanceService gatewayInstanceService;

    private final EventInstanceService eventInstanceService;

    private final ConnectorInstanceService connectorInstanceService;

    private Map<SFlowNodeType, Integer> firstStateIds;

    private Map<SFlowNodeType, String> firstStateNames;

    private final SDataInstanceBuilders sDataInstanceBuilders;

    private final ExpressionResolverService expressionResolverService;

    private final DataInstanceService dataInstanceService;

    public BPMInstancesCreator(final ActivityInstanceService activityInstanceService, final BPMInstanceBuilders instanceBuilders,
            final ActorMappingService actorMappingService, final GatewayInstanceService gatewayInstanceService,
            final EventInstanceService eventInstanceService,
            final ConnectorInstanceService connectorInstanceService, final SDataInstanceBuilders sDataInstanceBuilders,
            final ExpressionResolverService expressionResolverService, final DataInstanceService dataInstanceService) {
        super();
        this.activityInstanceService = activityInstanceService;
        this.instanceBuilders = instanceBuilders;
        this.actorMappingService = actorMappingService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.eventInstanceService = eventInstanceService;
        this.connectorInstanceService = connectorInstanceService;
        this.sDataInstanceBuilders = sDataInstanceBuilders;
        this.expressionResolverService = expressionResolverService;
        this.dataInstanceService = dataInstanceService;
    }

    public List<SFlowNodeInstance> createFlowNodeInstances(final SProcessDefinition sProcessDefinition, final long rootContainerId,
            final long parentContainerId, final List<SFlowNodeDefinition> flowNodeDefinitions, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final SStateCategory stateCategory, final Long tokenRefId) throws SBonitaException {
        final List<SFlowNodeInstance> flownNodeInstances = new ArrayList<SFlowNodeInstance>(flowNodeDefinitions.size());
        for (final SFlowNodeDefinition sFlowNodeDefinition : flowNodeDefinitions) {
            flownNodeInstances.add(createFlowNodeInstance(sProcessDefinition, rootContainerId, parentContainerId, SFlowElementsContainerType.PROCESS,
                    sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, false, -1, stateCategory, -1, tokenRefId));
        }
        return flownNodeInstances;
    }

    public SFlowNodeInstance createFlowNodeInstance(final SProcessDefinition sProcessDefinition, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final boolean createInnerActivity, final int loopCounter, final SStateCategory stateCategory,
            final long relatedActivityInstanceId, final Long tokenRefId) throws SBonitaException {
        final SFlowNodeInstance flownNodeInstance = toFlowNodeInstance(sProcessDefinition.getId(), rootContainerId, parentContainerId, parentContainerType,
                sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, createInnerActivity, loopCounter, stateCategory,
                relatedActivityInstanceId, tokenRefId);
        final TransactionContent transactionContent;
        if (SFlowNodeType.GATEWAY.equals(flownNodeInstance.getType())) {
            transactionContent = new CreateGatewayInstance((SGatewayInstance) flownNodeInstance, gatewayInstanceService);
        } else if (flownNodeInstance instanceof SActivityInstance) {
            transactionContent = new CreateActivityInstance((SActivityInstance) flownNodeInstance, activityInstanceService);
        } else {
            transactionContent = new CreateEventInstance((SEventInstance) flownNodeInstance, eventInstanceService);
        }
        transactionContent.execute();
        createConnectorInstances(flownNodeInstance, sFlowNodeDefinition.getConnectors(), SConnectorInstance.FLOWNODE_TYPE);
        return flownNodeInstance;
    }

    public void addChildDataContainer(final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        String parentContainerType;
        if (SFlowElementsContainerType.FLOWNODE.equals(flowNodeInstance.getParentContainerType())) {
            parentContainerType = DataInstanceContainer.ACTIVITY_INSTANCE.toString();
        } else {
            parentContainerType = DataInstanceContainer.PROCESS_INSTANCE.toString();
        }
        try {
            dataInstanceService.addChildContainer(flowNodeInstance.getParentContainerId(), parentContainerType, flowNodeInstance.getId(),
                    DataInstanceContainer.ACTIVITY_INSTANCE.toString());
        } catch (final SDataInstanceException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    public SFlowNodeInstance toFlowNodeInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final boolean createInnerActivity, final int loopCounter, final SStateCategory stateCategory,
            final long relatedActivityInstanceId, final Long tokenRefId) throws SActorNotFoundException, SActivityReadException {
        if (!createInnerActivity && sFlowNodeDefinition instanceof SActivityDefinition) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) sFlowNodeDefinition;
            final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
            if (loopCharacteristics != null) {
                SFlowNodeInstanceBuilder builder;
                if (loopCharacteristics instanceof SStandardLoopCharacteristics) {
                    builder = createLoopActivityInstance(processDefinitionId, rootContainerId, parentContainerId, rootProcessInstanceId,
                            parentProcessInstanceId, activityDefinition, loopCharacteristics);
                } else {
                    builder = createMultiInstanceActivityInstance(processDefinitionId, rootContainerId, parentContainerId, rootProcessInstanceId,
                            parentProcessInstanceId, activityDefinition, (SMultiInstanceLoopCharacteristics) loopCharacteristics);
                }
                builder.setState(firstStateIds.get(builder.getFlowNodeType()), false, false, firstStateNames.get(builder.getFlowNodeType()));
                builder.setStateCategory(stateCategory);
                builder.setTokenRefId(tokenRefId);
                return builder.done();
            }
        }

        SFlowNodeInstanceBuilder builder;
        switch (sFlowNodeDefinition.getType()) {
            case AUTOMATIC_TASK:
                builder = createAutomaticTaskInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        parentProcessInstanceId);
                break;
            case END_EVENT:
                builder = createEndEventInstance(processDefinitionId, rootContainerId, parentContainerId, sFlowNodeDefinition, rootProcessInstanceId,
                        parentProcessInstanceId);
                break;
            case GATEWAY:
                builder = createGatewayInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case INTERMEDIATE_CATCH_EVENT:
                builder = createIntermediateCatchEventInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType,
                        sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId);
                break;
            case INTERMEDIATE_THROW_EVENT:
                builder = createIntermediateThrowEventInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType,
                        sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId);
                break;
            case MANUAL_TASK:
                builder = createManualTaskInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case START_EVENT:
                builder = createStartEventInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case USER_TASK:
                builder = createUserTaskInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case RECEIVE_TASK:
                builder = createReceiveTaskInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        parentProcessInstanceId);
                break;
            case SEND_TASK:
                builder = createSendTaskInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        parentProcessInstanceId);
                break;
            case CALL_ACTIVITY:
                builder = createCallActivityInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case SUB_PROCESS:
                builder = createSubProcessActivityInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType, sFlowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId);
                break;
            case BOUNDARY_EVENT:
                builder = createBoundaryEventInstance(processDefinitionId, rootContainerId, parentContainerId, parentContainerType,
                        (SBoundaryEventDefinition) sFlowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, relatedActivityInstanceId);
                break;
            default:
                throw new SActivityReadException("Activity type not found: " + sFlowNodeDefinition.getType());
        }
        builder.setLoopCounter(loopCounter);
        builder.setState(firstStateIds.get(builder.getFlowNodeType()), false, false, firstStateNames.get(builder.getFlowNodeType()));
        builder.setStateCategory(stateCategory);
        builder.setTokenRefId(tokenRefId);
        return builder.done();
    }

    private SCallActivityInstanceBuilder createCallActivityInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SCallActivityDefinition callActivityDef = (SCallActivityDefinition) sFlowNodeDefinition;
        final SCallActivityInstanceBuilder builder = instanceBuilders.getSCallActivityInstanceBuilder();
        builder.createNewCallActivityInstance(callActivityDef.getName(), callActivityDef.getId(), rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId, parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SSubProcessActivityInstanceBuilder createSubProcessActivityInstance(final long processDefinitionId, final long rootContainerId,
            final long parentContainerId, final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition,
            final long rootProcessInstanceId, final long parentProcessInstanceId) {
        final SSubProcessDefinition subProcessActivityDef = (SSubProcessDefinition) sFlowNodeDefinition;
        final SSubProcessActivityInstanceBuilder builder = instanceBuilders.getSSubProcessActivityInstanceBuilder();
        builder.createNewSubProcessActivityInstance(subProcessActivityDef.getName(), subProcessActivityDef.getId(), rootContainerId, parentContainerId,
                processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, subProcessActivityDef.isTriggeredByEvent());
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SHumanTaskInstanceBuilder createUserTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId) throws SActorNotFoundException {
        final SHumanTaskInstanceBuilder builder = createHumanTaskInstance(processDefinitionId, rootContainerId, parentContainerId, sFlowNodeDefinition,
                rootProcessInstanceId, parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SStartEventInstanceBuilder createStartEventInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SStartEventDefinition startEventDef = (SStartEventDefinition) sFlowNodeDefinition;
        final SStartEventInstanceBuilder startEventInstanceBuilder = instanceBuilders.getSStartEventInstanceBuilder();
        final SStartEventInstanceBuilder builder = startEventInstanceBuilder.createNewStartEventInstance(startEventDef.getName(), startEventDef.getId(),
                rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        return builder;
    }

    private SHumanTaskInstanceBuilder createManualTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId) throws SActorNotFoundException {
        final SHumanTaskInstanceBuilder builder = createHumanTaskInstance(processDefinitionId, rootContainerId, parentContainerId, sFlowNodeDefinition,
                rootProcessInstanceId, parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SIntermediateThrowEventInstanceBuilder createIntermediateThrowEventInstance(final long processDefinitionId, final long rootContainerId,
            final long parentContainerId, final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition,
            final long rootProcessInstanceId, final long parentProcessInstanceId) {
        final SIntermediateThrowEventDefinition intermediateThrowEvent = (SIntermediateThrowEventDefinition) sFlowNodeDefinition;
        final SIntermediateThrowEventInstanceBuilder intermediateThrowEventInstanceBuilder = instanceBuilders.getSIntermediateThrowEventInstanceBuilder();
        final SIntermediateThrowEventInstanceBuilder builder = intermediateThrowEventInstanceBuilder.createNewIntermediateThrowEventInstance(
                intermediateThrowEvent.getName(), intermediateThrowEvent.getId(), rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId, parentProcessInstanceId);
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        return builder;
    }

    private SIntermediateCatchEventInstanceBuilder createIntermediateCatchEventInstance(final long processDefinitionId, final long rootContainerId,
            final long parentContainerId, final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition,
            final long rootProcessInstanceId, final long parentProcessInstanceId) {
        final SIntermediateCatchEventDefinition intermediateCatchEvent = (SIntermediateCatchEventDefinition) sFlowNodeDefinition;
        final SIntermediateCatchEventInstanceBuilder builder = instanceBuilders.getSIntermediateCatchEventInstanceBuilder()
                .createNewIntermediateCatchEventInstance(intermediateCatchEvent.getName(), intermediateCatchEvent.getId(), rootContainerId, parentContainerId,
                        processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        return builder;
    }

    private SBoundaryEventInstanceBuilder createBoundaryEventInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SBoundaryEventDefinition boundaryEvent, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final long activityInstanceId) {
        final SBoundaryEventInstanceBuilder builder = instanceBuilders.getSBoundaryEventInstanceBuilder().createNewBoundaryEventInstance(
                boundaryEvent.getName(), boundaryEvent.isInterrupting(), boundaryEvent.getId(), rootContainerId, parentContainerId, processDefinitionId,
                rootProcessInstanceId, parentProcessInstanceId, activityInstanceId);
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        return builder;
    }

    private SGatewayInstanceBuilder createGatewayInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId,
            final long parentProcessInstanceId) {
        final SGatewayInstanceBuilder builder = instanceBuilders.getSGatewayInstanceBuilder().createNewInstance(sFlowNodeDefinition.getName(),
                sFlowNodeDefinition.getId(), rootContainerId, parentContainerId, ((SGatewayDefinition) sFlowNodeDefinition).getGatewayType(),
                processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        return builder;
    }

    protected SEndEventInstanceBuilder createEndEventInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId, final long parentProcessInstanceId) {
        final SEndEventDefinition endEventDef = (SEndEventDefinition) sFlowNodeDefinition;
        final SEndEventInstanceBuilder endEventInstanceBuilder = instanceBuilders.getSEndEventInstanceBuilder();
        return endEventInstanceBuilder.createNewEndEventInstance(endEventDef.getName(), endEventDef.getId(), rootContainerId, parentContainerId,
                processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
    }

    private SAutomaticTaskInstanceBuilder createAutomaticTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long parentProcessInstanceId) {
        final SAutomaticTaskInstanceBuilder builder = instanceBuilders.getSAutomaticTaskInstanceBuilder().createNewAutomaticTaskInstance(
                sFlowNodeDefinition.getName(), sFlowNodeDefinition.getId(), rootContainerId, parentContainerId, processDefinitionId, rootContainerId,
                parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SFlowNodeInstanceBuilder createReceiveTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long parentProcessInstanceId) {
        final SReceiveTaskInstanceBuilder builder = instanceBuilders.getSReceiveTaskInstanceBuilder().createNewReceiveTaskInstance(
                sFlowNodeDefinition.getName(), sFlowNodeDefinition.getId(), rootContainerId, parentContainerId, processDefinitionId, rootContainerId,
                parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private SFlowNodeInstanceBuilder createSendTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowElementsContainerType parentContainerType, final SFlowNodeDefinition sFlowNodeDefinition, final long parentProcessInstanceId) {
        final SSendTaskInstanceBuilder builder = instanceBuilders.getSSendTaskInstanceBuilder().createNewSendTaskInstance(sFlowNodeDefinition.getName(),
                sFlowNodeDefinition.getId(), rootContainerId, parentContainerId, processDefinitionId, rootContainerId, parentProcessInstanceId);
        updateActivityInstance(parentContainerId, parentContainerType, sFlowNodeDefinition, builder);
        return builder;
    }

    private void updateActivityInstance(final long parentContainerId, final SFlowElementsContainerType parentContainerType,
            final SFlowNodeDefinition sFlowNodeDefinition, final SActivityInstanceBuilder builder) {
        updateFlowNodeInstance(parentContainerId, parentContainerType, builder);
        builder.setDescription(sFlowNodeDefinition.getDescription());
    }

    private void updateFlowNodeInstance(final long parentContainerId, final SFlowElementsContainerType parentContainerType,
            final SFlowNodeInstanceBuilder builder) {
        long logicalGroup3;
        if (SFlowElementsContainerType.FLOWNODE.equals(parentContainerType)) {
            logicalGroup3 = parentContainerId;
        } else {
            logicalGroup3 = 0;
        }
        builder.setParentActivityInstanceId(logicalGroup3);
    }

    private SMultiInstanceActivityInstanceBuilder createMultiInstanceActivityInstance(final long processDefinitionId, final long rootContainerId,
            final long parentContainerId, final long rootProcessInstanceId, final long parentProcessInstanceId, final SActivityDefinition activityDefinition,
            final SMultiInstanceLoopCharacteristics loopCharacteristics) {
        final SMultiInstanceActivityInstanceBuilder builder = instanceBuilders.getSMultiInstanceActivityInstanceBuilder().createNewOuterTaskInstance(
                activityDefinition.getName(), activityDefinition.getId(), rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId,
                parentProcessInstanceId, loopCharacteristics.isSequential());
        builder.setLoopDataInputRef(loopCharacteristics.getLoopDataInputRef());
        builder.setLoopDataOutputRef(loopCharacteristics.getLoopDataOutputRef());
        builder.setDataInputItemRef(loopCharacteristics.getDataInputItemRef());
        builder.setDataOutputItemRef(loopCharacteristics.getDataOutputItemRef());
        return builder;
    }

    public SLoopActivityInstanceBuilder createLoopActivityInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final long rootProcessInstanceId, final long parentProcessInstanceId, final SActivityDefinition activityDefinition,
            final SLoopCharacteristics loopCharacteristics) {
        final SLoopActivityInstanceBuilder builder = instanceBuilders.getSLoopActivityInstanceBuilder().createNewOuterTaskInstance(
                activityDefinition.getName(), activityDefinition.getId(), rootContainerId, parentContainerId, processDefinitionId, rootProcessInstanceId,
                parentProcessInstanceId);
        return builder;
    }

    private SHumanTaskInstanceBuilder createHumanTaskInstance(final long processDefinitionId, final long rootContainerId, final long parentContainerId,
            final SFlowNodeDefinition sFlowNodeDefinition, final long rootProcessInstanceId, final long parentProcessInstanceId) throws SActorNotFoundException {
        final SHumanTaskDefinition humanTaskDefinition = (SHumanTaskDefinition) sFlowNodeDefinition;
        final String actorName = humanTaskDefinition.getActorName();

        final GetActor getSActor = new GetActor(actorMappingService, actorName, processDefinitionId);
        try {
            getSActor.execute();
        } catch (final SBonitaException sbe) {
            throw new SActorNotFoundException(sbe);
        }
        final SActor actor = getSActor.getResult();
        SHumanTaskInstanceBuilder builder;
        if (sFlowNodeDefinition instanceof SUserTaskDefinition) {
            final SUserTaskInstanceBuilder sUserTaskInstanceBuilder = instanceBuilders.getSUserTaskInstanceBuilder();
            builder = sUserTaskInstanceBuilder.createNewUserTaskInstance(humanTaskDefinition.getName(), humanTaskDefinition.getId(), rootContainerId,
                    parentContainerId, actor.getId(), processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
        } else {
            // manual task
            final SManualTaskInstanceBuilder sManualTaskInstanceBuilder = instanceBuilders.getSManualTaskInstanceBuilder();
            builder = sManualTaskInstanceBuilder.createNewManualTaskInstance(humanTaskDefinition.getName(), humanTaskDefinition.getId(), rootContainerId,
                    parentContainerId, actor.getId(), processDefinitionId, rootProcessInstanceId, parentProcessInstanceId);
        }
        // Creation date:
        builder.setReachedStateDate(System.currentTimeMillis());
        final Long expectedDuration = humanTaskDefinition.getExpectedDuration();
        if (expectedDuration != null) {
            builder.setExpectedEndDate(System.currentTimeMillis() + expectedDuration);
        }
        final String priority = humanTaskDefinition.getPriority();
        if (priority != null) {
            // FIXME: use enum STaskPriority in client definition model:
            final STaskPriority sPriority = STaskPriority.valueOf(priority);
            builder.setPriority(sPriority);
        }
        return builder;
    }

    public void createConnectorInstances(final PersistentObject container, final List<SConnectorDefinition> connectors, final String containerType)
            throws SBonitaException {
        final List<SConnectorInstance> connectorInstances = new ArrayList<SConnectorInstance>(connectors.size());
        int executionOrder = 0;
        for (final SConnectorDefinition sConnectorDefinition : connectors) {
            final SConnectorInstanceBuilder sConnectorInstanceBuilder = instanceBuilders.getSConnectorInstanceBuilder();
            connectorInstances.add(sConnectorInstanceBuilder.createNewInstance(sConnectorDefinition.getName(), container.getId(), containerType,
                    sConnectorDefinition.getConnectorId(), sConnectorDefinition.getVersion(), sConnectorDefinition.getActivationEvent(), executionOrder++)
                    .done());
        }
        final CreateConnectorInstances transaction = new CreateConnectorInstances(connectorInstances, connectorInstanceService);
        transaction.execute();
    }

    public void setFirstStateIds(final Map<SFlowNodeType, Integer> firstStateIds) {
        this.firstStateIds = firstStateIds;
    }

    public void setFirstStateNames(final Map<SFlowNodeType, String> firstStateNames) {
        this.firstStateNames = firstStateNames;
    }

    public void createDataInstances(final SProcessInstance processInstance, final SFlowElementContainerDefinition processContainer,
            final SProcessDefinition processDefinition, final SExpressionContext expressionContext, final List<SOperation> operations,
            final Map<String, Object> context) throws SBonitaException {
        final List<SDataDefinition> sDataDefinitions = processContainer.getDataDefinitions();
        final List<SDataInstance> sDataInstances = new ArrayList<SDataInstance>(sDataDefinitions.size());
        for (final SDataDefinition sDataDefinition : sDataDefinitions) {
            Serializable defaultValue = null;
            boolean hasOperation = false;
            SExpression expression = null;
            SExpressionContext currentExpressionContext = null;
            if (operations != null) {
                final SOperation operation = getOperationToSetData(sDataDefinition.getName(), operations);
                if (operation != null) {
                    hasOperation = true;
                    expression = operation.getRightOperand();
                    if (expressionContext != null) {
                        expressionContext.setInputValues(context);
                        currentExpressionContext = expressionContext;
                    } else {
                        currentExpressionContext = new SExpressionContext(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                                processInstance.getProcessDefinitionId());
                        currentExpressionContext.setInputValues(context);
                    }
                    operations.remove(operation);
                }
            }
            // If there was no operations in entry OR if no operation should set the current data, we take the default data definition value:
            if (!hasOperation) {
                expression = sDataDefinition.getDefaultValueExpression();
                currentExpressionContext = new SExpressionContext(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                        processInstance.getProcessDefinitionId());
            }
            if (expression != null) {
                final EvaluateExpression evaluateExpression = new EvaluateExpression(expressionResolverService, currentExpressionContext, expression);
                evaluateExpression.execute();
                defaultValue = evaluateExpression.getResult();
            }
            final SDataInstanceBuilder sDataInstanceBuilder = sDataInstanceBuilders.getDataInstanceBuilder();
            try {
                final SDataInstance dataInstance = sDataInstanceBuilder.createNewInstance(sDataDefinition).setContainerId(processInstance.getId())
                        .setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name()).setValue(defaultValue).done();
                sDataInstances.add(dataInstance);
            } catch (final ClassCastException e) {
                throw new SBonitaException("Trying to set variable \"" + sDataDefinition.getName() + "\" with incompatible type: " + e.getMessage()) {
                };
            }
        }
        if (hasLocalOrInheritedData(processDefinition, processContainer)) {
            final CreateSDataInstances transaction = new CreateSDataInstances(sDataInstances, dataInstanceService, processInstance, activityInstanceService,
                    instanceBuilders, processDefinition);
            transaction.execute();
        }
    }

    private boolean hasLocalOrInheritedData(final SProcessDefinition processDefinition, final SFlowElementContainerDefinition processContainer) {
        // processContainer is different of processDefinition.getProcessContainer() if it's a sub-process
        return !processContainer.getDataDefinitions().isEmpty() || !processDefinition.getProcessContainer().getDataDefinitions().isEmpty();
    }

    private SOperation getOperationToSetData(final String dataName, final List<SOperation> operations) {
        SOperation dataOperation = null;
        final Iterator<SOperation> iterator = operations.iterator();
        boolean found = false;
        while (iterator.hasNext() && !found) {
            final SOperation operation = iterator.next();
            if (SOperatorType.ASSIGNMENT.equals(operation.getType()) && dataName.equals(operation.getLeftOperand().getName())) {
                found = true;
                dataOperation = operation;
            }
        }
        return dataOperation;
    }

    public BPMInstanceBuilders getBPMInstanceBuilders() {
        return instanceBuilders;
    }

    private void createDataInstances(final List<SDataDefinition> dataDefinitions, final long containerId, final DataInstanceContainer containerType,
            final SExpressionContext expressionContext, final ExpressionResolverService expressionResolverService,
            final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders, final String loopDataInputRef, final int index,
            final String dataInputRef, final long parentContainerId) throws SDataInstanceException, SExpressionException {
        for (final SDataDefinition dataDefinition : dataDefinitions) {
            Serializable dataValue = null;
            if (dataDefinition.getName().equals(dataInputRef)) {
                final SDataInstance dataInstance = dataInstanceService.getDataInstance(loopDataInputRef, parentContainerId,
                        DataInstanceContainer.ACTIVITY_INSTANCE.name());// in a multi instance
                if (dataInstance != null) {
                    try {
                        dataValue = (Serializable) ((List<?>) dataInstance.getValue()).get(index);
                    } catch (final ClassCastException e) {
                        throw new SDataInstanceException("loopDataInput ref named " + loopDataInputRef + " in " + containerId + " " + containerType
                                + " is not a list or the value is not serializable");
                    }
                } else {
                    throw new SDataInstanceException("loopDataInput ref named " + loopDataInputRef + " is not visible for " + containerId + " " + containerType);
                }
            } else {
                final SExpression defaultValueExpression = dataDefinition.getDefaultValueExpression();
                if (defaultValueExpression != null) {
                    dataValue = (Serializable) expressionResolverService.evaluate(dataDefinition.getDefaultValueExpression(), expressionContext);
                }
            }
            final SDataInstance dataInstance;
            try {
                dataInstance = buildDataInstance(dataDefinition, containerId, containerType, dataValue, sDataInstanceBuilders);
            } catch (final SDataInstanceNotWellFormedException e) {
                throw new SDataInstanceException(e);
            }
            dataInstanceService.createDataInstance(dataInstance);
        }
    }

    public void createDataInstances(final List<SDataDefinition> dataDefinitions, final long containerId, final DataInstanceContainer containerType,
            final SExpressionContext expressionContext, final ExpressionResolverService expressionResolverService,
            final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders) throws SDataInstanceException,
            SExpressionException {
        createDataInstances(dataDefinitions, containerId, containerType, expressionContext, expressionResolverService, dataInstanceService,
                sDataInstanceBuilders, null, -1, null, -1);
    }

    private SDataInstance buildDataInstance(final SDataDefinition correlation, final long dataContainerId, final DataInstanceContainer dataContainerType,
            final Serializable dataValue, final SDataInstanceBuilders sDataInstanceBuilders) throws SDataInstanceNotWellFormedException {
        return sDataInstanceBuilders.getDataInstanceBuilder().createNewInstance(correlation).setContainerId(dataContainerId)
                .setContainerType(dataContainerType.name()).setValue(dataValue).done();

    }

    public boolean createDataInstances(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SExpressionContext sExpressionContext) throws SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        if (activityDefinition != null) {// can be null if the activity was added in runtime
            try {
                final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
                final SExpressionContext expressionContext;
                if (sExpressionContext == null) {
                    expressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                            processDefinition.getId());
                } else {
                    expressionContext = sExpressionContext;
                }
                final List<SDataDefinition> sDataDefinitions = activityDefinition.getSDataDefinitions();
                if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics
                        && ((SMultiInstanceLoopCharacteristics) loopCharacteristics).getDataInputItemRef() != null) {
                    final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                    createDataInstances(sDataDefinitions, flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE, expressionContext,
                            expressionResolverService, dataInstanceService, sDataInstanceBuilders, miLoop.getLoopDataInputRef(),
                            flowNodeInstance.getLoopCounter(), miLoop.getDataInputItemRef(), flowNodeInstance.getParentContainerId());
                } else {
                    createDataInstances(sDataDefinitions, flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE, expressionContext,
                            expressionResolverService, dataInstanceService, sDataInstanceBuilders);
                }
                return sDataDefinitions.size() > 0;
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
        return false;
    }

}
