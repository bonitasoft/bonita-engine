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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceModificationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.FilterResult;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SReceiveTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSendTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SIntermediateCatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SThrowEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAAutomaticTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SMultiInstanceActivityInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SPendingActivityMappingBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SBoundaryEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.business.data.SFlowNodeSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.event.OperationsWithContext;
import org.bonitasoft.engine.execution.work.WorkFactory;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class StateBehaviors {

    public static final int BEFORE_ON_ENTER = 1;

    public static final int DURING_ON_ENTER = 1 << 1;

    public static final int BEFORE_ON_FINISH = 1 << 2;

    public static final int DURING_ON_FINISH = 1 << 3;

    public static final int AFTER_ON_FINISH = 1 << 4;

    private static final int BATCH_SIZE = 20;

    private static final int MAX_NUMBER_OF_RESULTS = 100;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final EventsHandler eventsHandler;

    private final ActivityInstanceService activityInstanceService;

    private final UserFilterService userFilterService;

    private final ClassLoaderService classLoaderService;

    private final ActorMappingService actorMappingService;

    private final ExpressionResolverService expressionResolverService;

    private final ProcessDefinitionService processDefinitionService;

    private final DataInstanceService dataInstanceService;

    private final OperationService operationService;

    private final WorkService workService;

    private final ContainerRegistry containerRegistry;

    private final EventInstanceService eventInstanceService;

    private final ConnectorInstanceService connectorInstanceService;

    private ProcessExecutor processExecutor;

    private final SCommentService commentService;

    private final IdentityService identityService;

    protected final ParentContainerResolver parentContainerResolver;
    private final WaitingEventsInterrupter waitingEventsInterrupter;
    private final RefBusinessDataService refBusinessDataService;


    public StateBehaviors(final BPMInstancesCreator bpmInstancesCreator, final EventsHandler eventsHandler,
            final ActivityInstanceService activityInstanceService, final UserFilterService userFilterService, final ClassLoaderService classLoaderService,
            final ActorMappingService actorMappingService, final ConnectorInstanceService connectorInstanceService,
            final ExpressionResolverService expressionResolverService, final ProcessDefinitionService processDefinitionService,
            final DataInstanceService dataInstanceService, final OperationService operationService, final WorkService workService,
            final ContainerRegistry containerRegistry, final EventInstanceService eventInstanceService, final SCommentService commentService,
            final IdentityService identityService, final ProcessInstanceService processInstanceService,
            final ParentContainerResolver parentContainerResolver, final WaitingEventsInterrupter waitingEventsInterrupter,
            final TechnicalLoggerService logger, final RefBusinessDataService refBusinessDataService) {
        super();
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.eventsHandler = eventsHandler;
        this.activityInstanceService = activityInstanceService;
        this.userFilterService = userFilterService;
        this.classLoaderService = classLoaderService;
        this.actorMappingService = actorMappingService;
        this.connectorInstanceService = connectorInstanceService;
        this.expressionResolverService = expressionResolverService;
        this.processDefinitionService = processDefinitionService;
        this.dataInstanceService = dataInstanceService;
        this.operationService = operationService;
        this.workService = workService;
        this.containerRegistry = containerRegistry;
        this.eventInstanceService = eventInstanceService;
        this.commentService = commentService;
        this.identityService = identityService;
        this.parentContainerResolver = parentContainerResolver;
        this.refBusinessDataService = refBusinessDataService;
        this.waitingEventsInterrupter = waitingEventsInterrupter;
    }

    public void setProcessExecutor(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public DataInstanceContainer getParentContainerType(final SFlowNodeInstance flowNodeInstance) {
        DataInstanceContainer parentContainerType;
        if (flowNodeInstance.getLogicalGroup(2) <= 0) {
            parentContainerType = DataInstanceContainer.PROCESS_INSTANCE;
        } else {
            parentContainerType = DataInstanceContainer.ACTIVITY_INSTANCE;
        }
        return parentContainerType;
    }

    public DataInstanceService getDataInstanceService() {
        return dataInstanceService;
    }

    public void mapDataOutputOfMultiInstance(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (flowNodeInstance instanceof SActivityInstance && !SFlowNodeType.MULTI_INSTANCE_ACTIVITY.equals(flowNodeInstance.getType())) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SActivityDefinition activityDefinition = (SActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {// can be null if the activity was added in runtime
                try {
                    final SLoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
                    if (loopCharacteristics instanceof SMultiInstanceLoopCharacteristics
                            && ((SMultiInstanceLoopCharacteristics) loopCharacteristics).getDataOutputItemRef() != null) {
                        final SMultiInstanceLoopCharacteristics miLoop = (SMultiInstanceLoopCharacteristics) loopCharacteristics;
                        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataOutputRef());
                        if (businessData == null) {
                            mapDataOutputOfMultiInstance(flowNodeInstance, miLoop);
                        } else {
                            MapMultiInstanceBusinessDataOutput(flowNodeInstance, miLoop);
                        }
                    }
                } catch (final SBonitaException sbe) {
                    throw new SActivityStateExecutionException(sbe);
                }
            }
        }
    }

    private void MapMultiInstanceBusinessDataOutput(final SFlowNodeInstance flowNodeInstance, final SMultiInstanceLoopCharacteristics miLoop)
            throws SRefBusinessDataInstanceNotFoundException, SBonitaReadException, SRefBusinessDataInstanceModificationException {
        final SRefBusinessDataInstance outputMIRef = refBusinessDataService.getFlowNodeRefBusinessDataInstance(
                miLoop.getDataOutputItemRef(), flowNodeInstance.getId());
        final SRefBusinessDataInstance outputMILoopRef = refBusinessDataService.getRefBusinessDataInstance(
                miLoop.getLoopDataOutputRef(), flowNodeInstance.getParentProcessInstanceId());
        final SMultiRefBusinessDataInstance multiRefBusinessDataInstance = (SMultiRefBusinessDataInstance) outputMILoopRef;
        List<Long> dataIds = multiRefBusinessDataInstance.getDataIds();
        if (dataIds == null) {
            dataIds = new ArrayList<Long>();
        }
        final Long dataId = ((SFlowNodeSimpleRefBusinessDataInstance) outputMIRef).getDataId();
        dataIds.add(dataId);
        refBusinessDataService.updateRefBusinessDataInstance(multiRefBusinessDataInstance, dataIds);
    }

    @SuppressWarnings("unchecked")
    public void mapDataOutputOfMultiInstance(final SFlowNodeInstance flowNodeInstance, final SMultiInstanceLoopCharacteristics miLoop)
            throws SActivityExecutionException, SBonitaException {
        final SDataInstance outputData = dataInstanceService.getDataInstance(miLoop.getDataOutputItemRef(), flowNodeInstance.getId(),
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
        final SDataInstance loopData = dataInstanceService.getDataInstance(miLoop.getLoopDataOutputRef(), flowNodeInstance.getId(),
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
        if (outputData != null && loopData != null) {
            final Serializable value = loopData.getValue();
            final int index = flowNodeInstance.getLoopCounter();
            if (value instanceof List<?>) {
                ((List<Serializable>) value).set(index, outputData.getValue());
            } else {
                throw new SActivityExecutionException("unable to map the ouput of the multi instanciated activity "
                        + flowNodeInstance.getName() + " the output loop data named " + loopData.getName() + " is not a list but "
                        + loopData.getClassName());
            }
            final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField("value", value);
            dataInstanceService.updateDataInstance(loopData, entityUpdateDescriptor);
        }
    }

    public void mapActors(final SFlowNodeInstance flowNodeInstance, final SFlowElementContainerDefinition processContainer)
            throws SActivityStateExecutionException {
        if (SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType())) {
            try {
                final SHumanTaskDefinition humanTaskDefinition = (SHumanTaskDefinition) processContainer
                        .getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
                if (humanTaskDefinition != null) {
                    final String actorName = humanTaskDefinition.getActorName();
                    final long processDefinitionId = flowNodeInstance.getLogicalGroup(0);
                    final SUserFilterDefinition sUserFilterDefinition = humanTaskDefinition.getSUserFilterDefinition();
                    if (sUserFilterDefinition != null) {
                        mapUsingUserFilters(flowNodeInstance, humanTaskDefinition, actorName, processDefinitionId, sUserFilterDefinition);
                    } else {
                        mapUsingActors(flowNodeInstance, actorName, processDefinitionId);
                    }
                }
            } catch (final SActivityStateExecutionException e) {
                throw e;
            } catch (final Exception e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    private void mapUsingActors(final SFlowNodeInstance flowNodeInstance, final String actorName, final long processDefinitionId)
            throws SActorNotFoundException, SActivityCreationException {
        final SActor actor = actorMappingService.getActor(actorName, processDefinitionId);
        final SPendingActivityMapping mapping = BuilderFactory.get(SPendingActivityMappingBuilderFactory.class)
                .createNewInstanceForActor(flowNodeInstance.getId(), actor.getId()).done();
        activityInstanceService.addPendingActivityMappings(mapping);
    }

    void mapUsingUserFilters(final SFlowNodeInstance flowNodeInstance, final SHumanTaskDefinition humanTaskDefinition, final String actorName,
            final long processDefinitionId, final SUserFilterDefinition sUserFilterDefinition) throws SClassLoaderException, SUserFilterExecutionException,
            SActivityStateExecutionException, SActivityCreationException, SFlowNodeNotFoundException, SFlowNodeReadException, SActivityModificationException {
        final ClassLoader processClassloader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
        final SExpressionContext expressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                flowNodeInstance.getLogicalGroup(0));
        final FilterResult result = userFilterService.executeFilter(processDefinitionId, sUserFilterDefinition, sUserFilterDefinition.getInputs(),
                processClassloader, expressionContext, actorName);
        final List<Long> userIds = result.getResult();
        if (userIds == null || userIds.isEmpty() || userIds.contains(0L) || userIds.contains(-1L)) {
            throw new SActivityStateExecutionException("no user id returned by the user filter " + sUserFilterDefinition + " on activity "
                    + humanTaskDefinition.getName());
        }
        for (final Long userId : new TreeSet<Long>(userIds)) {
            final SPendingActivityMapping mapping = BuilderFactory.get(SPendingActivityMappingBuilderFactory.class)
                    .createNewInstanceForUser(flowNodeInstance.getId(), userId).done();
            activityInstanceService.addPendingActivityMappings(mapping);
        }
        if (userIds.size() == 1 && result.shouldAutoAssignTaskIfSingleResult()) {
            final Long userId = userIds.get(0);
            activityInstanceService.assignHumanTask(flowNodeInstance.getId(), userId);
            //system comment is added after the evaluation of the display name
        }
    }

    public void handleCatchEvents(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        // handle catch event
        if (flowNodeInstance instanceof SIntermediateCatchEventInstance) {
            final SCatchEventInstance intermediateCatchEventInstance = (SCatchEventInstance) flowNodeInstance;
            // handleEventTriggerInstances(processDefinition, intermediateCatchEventInstance);
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SIntermediateCatchEventDefinition intermediateCatchEventDefinition = (SIntermediateCatchEventDefinition) processContainer
                    .getFlowNode(intermediateCatchEventInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleCatchEvent(processDefinition, intermediateCatchEventDefinition, intermediateCatchEventInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle catch event " + flowNodeInstance, e);
            }
        } else if (flowNodeInstance instanceof SReceiveTaskInstance) {
            final SReceiveTaskInstance receiveTaskInstance = (SReceiveTaskInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SReceiveTaskDefinition receiveTaskIDefinition = (SReceiveTaskDefinition) processContainer.getFlowNode(receiveTaskInstance
                    .getFlowNodeDefinitionId());
            try {
                eventsHandler.handleCatchMessage(processDefinition, receiveTaskIDefinition, receiveTaskInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle catch event " + flowNodeInstance, e);
            }
        }
    }

    public void handleBoundaryEvent(final SProcessDefinition processDefinition, final SBoundaryEventInstance boundaryInstance)
            throws SActivityStateExecutionException {
        final long activityInstanceId = boundaryInstance.getActivityInstanceId();
        // FIXME: add activity name in SBoundaryEventInstance to avoid the getActivityInstance below
        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);

            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    activityInstance.getFlowNodeDefinitionId());
            final SBoundaryEventDefinition boundaryEventDefinition = activityDefinition.getBoundaryEventDefinition(boundaryInstance.getName());
            eventsHandler.handleCatchEvent(processDefinition, boundaryEventDefinition, boundaryInstance);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("unable to handle catch event " + boundaryInstance, e);
        }
    }

    /**
     * Return the phases and connectors to execute, as a couple of (phase, couple of (connector instance, connector definition))
     *
     * @param processDefinition
     *        the process where the connectors are defined.
     * @param flowNodeInstance
     *        the instance of the flow node to execute possible connectors on.
     * @param executeConnectorsOnEnter
     *        do we want to consider the connectors ON_ENTER or ignore them?
     * @param executeConnectorsOnFinish
     *        do we want to consider the connectors ON_FINISH or ignore them?
     * @return the phases and connectors to execute
     * @throws SActivityStateExecutionException
     */
    public BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteAndFlag(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance, final boolean executeConnectorsOnEnter, final boolean executeConnectorsOnFinish)
                    throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNodeDefinition = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNodeDefinition != null) {
                boolean onEnterExecuted = false;
                final List<SConnectorDefinition> connectorsOnEnter = flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER);
                if (connectorsOnEnter.size() > 0 && executeConnectorsOnEnter) {
                    final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorToExecuteOnEnter = getConnectorToExecuteOnEnter(
                            flowNodeInstance, connectorsOnEnter);
                    if (connectorToExecuteOnEnter != null) {
                        return connectorToExecuteOnEnter;
                    }
                    // All connectors ON ENTER have already been executed:
                    onEnterExecuted = true;
                }
                // no on enter connector to execute
                final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorToExecuteOnFinish = getConnectorToExecuteOnFinish(
                        flowNodeDefinition, flowNodeInstance, executeConnectorsOnFinish, onEnterExecuted);
                if (connectorToExecuteOnFinish != null) {
                    return connectorToExecuteOnFinish;
                }
                // no ON ENTER no ON FINISH active
                if (flowNodeInstance.isStateExecuting()) {
                    // there was a connector executed but no more: execute only before and after finish
                    return getConnectorWithFlag(null, null, BEFORE_ON_FINISH | AFTER_ON_FINISH);
                }
            }
            // no connector and was just starting
            return getConnectorWithFlag(null, null, BEFORE_ON_ENTER | BEFORE_ON_FINISH | AFTER_ON_FINISH);
        } catch (final SConnectorInstanceReadException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteOnFinish(final SFlowNodeDefinition flowNodeDefinition,
            final SFlowNodeInstance flowNodeInstance, final boolean executeConnectorsOnFinish, final boolean onEnterExecuted)
            throws SConnectorInstanceReadException,
            SActivityStateExecutionException {
        final List<SConnectorDefinition> connectorsOnFinish = flowNodeDefinition.getConnectors(ConnectorEvent.ON_FINISH);
        if (connectorsOnFinish.size() > 0 && executeConnectorsOnFinish) {
            final SConnectorInstance nextConnectorInstanceToExecute = getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_FINISH);
            if (nextConnectorInstanceToExecute != null) {
                if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                        && connectorsOnFinish.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                    // first finish connector
                    final SConnectorDefinition connectorDefinition = connectorsOnFinish.get(0);
                    if (onEnterExecuted) {
                        // some connectors were already executed
                        return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_FINISH | DURING_ON_FINISH);
                    }
                    // on finish but the first connector
                    return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_ENTER | BEFORE_ON_FINISH
                            | DURING_ON_FINISH);
                }
                // no the first, don't execute before
                return getConnectorWithFlagIfIsNextToExecute(flowNodeInstance, connectorsOnFinish, nextConnectorInstanceToExecute, DURING_ON_FINISH);
            }
            // all finish connectors executed
            return getConnectorWithFlag(null, null, AFTER_ON_FINISH);
        }
        return null;
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteOnEnter(final SFlowNodeInstance flowNodeInstance,
            final List<SConnectorDefinition> connectorsOnEnter) throws SConnectorInstanceReadException, SActivityStateExecutionException {
        final SConnectorInstance nextConnectorInstanceToExecute = getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_ENTER);
        if (nextConnectorInstanceToExecute != null) {
            // Have we already executed the 'before on enter' phase?
            if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                    && connectorsOnEnter.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                // first enter connector:
                return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorsOnEnter.get(0), BEFORE_ON_ENTER | DURING_ON_ENTER);
                // Or do we have to skip the 'before on enter' phase:
            }
            // not the first connector, or first connector not in state TO_BE_EXECUTED => don't execute phase BEFORE_ON_ENTER:
            return getConnectorWithFlagIfIsNextToExecute(flowNodeInstance, connectorsOnEnter, nextConnectorInstanceToExecute, DURING_ON_ENTER);
        }
        return null;
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorWithFlagIfIsNextToExecute(final SFlowNodeInstance flowNodeInstance,
            final List<SConnectorDefinition> sConnectorDefinitions, final SConnectorInstance nextConnectorInstanceToExecute, final int flag)
                    throws SActivityStateExecutionException {
        for (final SConnectorDefinition sConnectorDefinition : sConnectorDefinitions) {
            if (sConnectorDefinition.getName().equals(nextConnectorInstanceToExecute.getName())) {
                return getConnectorWithFlag(nextConnectorInstanceToExecute, sConnectorDefinition, flag);
            }
        }
        throw new SActivityStateExecutionException("Connector definition of " + nextConnectorInstanceToExecute + " not found on "
                + flowNodeInstance);
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorWithFlag(final SConnectorInstance nextConnectorInstance,
            final SConnectorDefinition connectorDefinition, final int flag) {
        return new BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>>(flag, new BEntry<SConnectorInstance, SConnectorDefinition>(
                nextConnectorInstance, connectorDefinition));
    }

    /**
     * Return next connector instance in state TO_BE_EXECUTED or EXECUTING or TO_RE_EXECUTE for the given ConnectorEvent defined on given flowNodeInstance.
     */
    private SConnectorInstance getNextConnectorInstance(final SFlowNodeInstance flowNodeInstance, final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        return connectorInstanceService.getNextExecutableConnectorInstance(flowNodeInstance.getId(), SConnectorInstance.FLOWNODE_TYPE, event);
    }

    public void createData(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        if (flowNodeInstance instanceof SActivityInstance) {
            final String containerType = getParentContainerType(flowNodeInstance).name();
            final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getParentContainerId(), containerType,
                    processDefinition.getId());
            bpmInstancesCreator.createDataInstances(processDefinition, flowNodeInstance, sExpressionContext);
        }
    }

    public void handleCallActivity(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (isCallActivity(flowNodeInstance)) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            try {
                final SCallActivityDefinition callActivity = (SCallActivityDefinition) processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
                if (callActivity == null) {
                    final StringBuilder stb = new StringBuilder("unable to find call activity definition with name '");
                    stb.append(flowNodeInstance.getName());
                    stb.append("' in procecess definition '");
                    stb.append(processDefinition.getId());
                    stb.append("'");
                    throw new SActivityStateExecutionException(stb.toString());
                }

                final SExpressionContext expressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String callableElement = (String) expressionResolverService.evaluate(callActivity.getCallableElement(), expressionContext);
                String callableElementVersion = null;
                if (callActivity.getCallableElementVersion() != null) {
                    callableElementVersion = (String) expressionResolverService.evaluate(callActivity.getCallableElementVersion(), expressionContext);
                }

                final long targetProcessDefinitionId = getTargetProcessDefinitionId(callableElement, callableElementVersion);
                instantiateProcess(processDefinition, callActivity, flowNodeInstance, targetProcessDefinitionId);
                final SCallActivityInstance callActivityInstance = (SCallActivityInstance) flowNodeInstance;
                // update token count
                activityInstanceService.setTokenCount(callActivityInstance, callActivityInstance.getTokenCount() + 1);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    private long getTargetProcessDefinitionId(final String callableElement, final String callableElementVersion) throws SProcessDefinitionReadException,
    SProcessDefinitionNotFoundException {
        if (callableElementVersion != null) {
            return processDefinitionService.getProcessDefinitionId(callableElement, callableElementVersion);
        }
        return processDefinitionService.getLatestProcessDefinitionId(callableElement);
    }

    private boolean isCallActivity(final SFlowNodeInstance flowNodeInstance) {
        return SFlowNodeType.CALL_ACTIVITY.equals(flowNodeInstance.getType());
    }

    private void instantiateProcess(final SProcessDefinition callerProcessDefinition, final SCallActivityDefinition callActivityDefinition,
            final SFlowNodeInstance callActivityInstance, final long targetProcessDefinitionId) throws SProcessInstanceCreationException {
        final long callerProcessDefinitionId = callerProcessDefinition.getId();
        final long callerId = callActivityInstance.getId();
        final List<SOperation> operationList = callActivityDefinition.getDataInputOperations();
        final SExpressionContext context = new SExpressionContext(callerId, DataInstanceContainer.ACTIVITY_INSTANCE.name(), callerProcessDefinitionId);
        final OperationsWithContext operations = new OperationsWithContext(context, operationList);
        processExecutor.start(targetProcessDefinitionId, -1, 0, 0, operations.getContext(), operations.getOperations(), null, null, callerId, -1, null); // Change this last NULL when inputs are supported in CallActivity
    }

    public void updateDisplayNameAndDescription(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNode != null) {
                final SExpression displayNameExpression = flowNode.getDisplayName();
                final SExpression displayDescriptionExpression = flowNode.getDisplayDescription();
                final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String displayName;
                if (displayNameExpression != null) {
                    displayName = (String) expressionResolverService.evaluate(displayNameExpression, sExpressionContext);
                } else {
                    displayName = flowNode.getName();
                }
                final String displayDescription;
                if (displayDescriptionExpression != null) {
                    displayDescription = (String) expressionResolverService.evaluate(displayDescriptionExpression, sExpressionContext);
                } else {
                    displayDescription = flowNode.getDescription();
                }
                activityInstanceService.updateDisplayName(flowNodeInstance, displayName);
                activityInstanceService.updateDisplayDescription(flowNodeInstance, displayDescription);
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        }
    }

    public void updateDisplayDescriptionAfterCompletion(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            if (flowNode != null) {
                final SExpression displayDescriptionAfterCompletionExpression = flowNode.getDisplayDescriptionAfterCompletion();
                final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                final String displayDescriptionAfterCompletion;
                if (displayDescriptionAfterCompletionExpression != null) {
                    displayDescriptionAfterCompletion = (String) expressionResolverService.evaluate(displayDescriptionAfterCompletionExpression,
                            sExpressionContext);
                    activityInstanceService.updateDisplayDescription(flowNodeInstance, displayDescriptionAfterCompletion);
                }
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("error while updating display name and description", e);
        }
    }

    public void executeOperations(final SProcessDefinition processDefinition, final SActivityInstance activityInstance) throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNode = processContainer.getFlowNode(activityInstance.getFlowNodeDefinitionId());
            if (flowNode instanceof SActivityDefinition) {
                final SActivityDefinition activityDefinition = (SActivityDefinition) flowNode;
                final List<SOperation> sOperations = activityDefinition.getSOperations();
                final SExpressionContext sExpressionContext = new SExpressionContext(activityInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                        processDefinition.getId());
                operationService.execute(sOperations, sExpressionContext);
            }
        } catch (final SOperationExecutionException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    public void handleThrowEvent(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        if (flowNodeInstance instanceof SThrowEventInstance) {
            final SThrowEventInstance throwEventInstance = (SThrowEventInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SThrowEventDefinition eventDefinition = (SThrowEventDefinition) processContainer.getFlowNode(throwEventInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleThrowEvent(processDefinition, eventDefinition, throwEventInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle throw event " + flowNodeInstance, e);
            }
        } else if (SFlowNodeType.SEND_TASK.equals(flowNodeInstance.getType())) {
            final SSendTaskInstance sendTaskInstance = (SSendTaskInstance) flowNodeInstance;
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SSendTaskDefinition sendTaskDefinition = (SSendTaskDefinition) processContainer.getFlowNode(sendTaskInstance.getFlowNodeDefinitionId());
            try {
                eventsHandler.handleThrowMessage(processDefinition, sendTaskDefinition, sendTaskInstance);
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException("unable to handle throw message " + flowNodeInstance, e);
            }
        }
    }

    public void executeChildrenActivities(final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        try {
            int i = 0;
            List<SActivityInstance> childrenOfAnActivity;
            do {
                childrenOfAnActivity = activityInstanceService.getChildrenOfAnActivity(flowNodeInstance.getId(), i, BATCH_SIZE);
                for (final SActivityInstance sActivityInstance : childrenOfAnActivity) {
                    containerRegistry.executeFlowNode(flowNodeInstance.getProcessDefinitionId(),
                            sActivityInstance.getLogicalGroup(BuilderFactory.get(SAAutomaticTaskInstanceBuilderFactory.class).getParentProcessInstanceIndex()),
                            sActivityInstance.getId(), null, null);
                }
                i += BATCH_SIZE;
            } while (childrenOfAnActivity.size() == BATCH_SIZE);
        } catch (final SBonitaException e) {
            throw new SActivityExecutionException(e);
        }
    }

    public void interruptSubActivities(final long parentActivityInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        QueryOptions queryOptions = activityInstanceService.buildQueryOptionsForSubActivitiesInNormalStateAndNotTerminal(parentActivityInstanceId,
                MAX_NUMBER_OF_RESULTS);
        List<SActivityInstance> childrenToEnd;
        do {
            childrenToEnd = activityInstanceService.searchActivityInstances(SActivityInstance.class, queryOptions);
            for (final SActivityInstance child : childrenToEnd) {
                activityInstanceService.setStateCategory(child, stateCategory);
                if (child.isStable()) {
                    containerRegistry.executeFlowNode(child.getProcessDefinitionId(),
                            child.getLogicalGroup(BuilderFactory.get(SAAutomaticTaskInstanceBuilderFactory.class).getParentProcessInstanceIndex()),
                            child.getId(), null, null);
                }
            }
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (!childrenToEnd.isEmpty());
    }

    public void executeConnectorInWork(final Long processDefinitionId, final long processInstanceId, final long flowNodeDefinitionId,
            final long flowNodeInstanceId, final SConnectorInstance connector, final SConnectorDefinition sConnectorDefinition)
                    throws SActivityStateExecutionException {
        final long connectorInstanceId = connector.getId();
        // final Long connectorDefinitionId = sConnectorDefinition.getId();// FIXME: Uncomment when generate id
        final String connectorDefinitionName = sConnectorDefinition.getName();
        try {
            connectorInstanceService.setState(connector, ConnectorState.EXECUTING.name());
            workService.registerWork(WorkFactory.createExecuteConnectorOfActivity(processDefinitionId, processInstanceId, flowNodeDefinitionId,
                    flowNodeInstanceId, connectorInstanceId, connectorDefinitionName));
        } catch (final SConnectorInstanceModificationException e) {
            throw new SActivityStateExecutionException("Unable to set ConnectorState to EXECUTING", e);
        } catch (final SWorkRegisterException e) {
            throw new SActivityStateExecutionException("Unable to register the work that execute the connector " + connector + " on " + flowNodeInstanceId, e);
        }
    }

    public void createAttachedBoundaryEvents(final SProcessDefinition processDefinition, final SActivityInstance activityInstance)
            throws SActivityStateExecutionException {
        final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                activityInstance.getFlowNodeDefinitionId());
        if (mustAddBoundaryEvents(activityInstance, activityDefinition)) {
            createAttachedBoundaryEvents(processDefinition, activityInstance, activityDefinition);
        }
    }

    private void createAttachedBoundaryEvents(final SProcessDefinition processDefinition, final SActivityInstance activityInstance,
            final SActivityDefinition activityDefinition) throws SActivityStateExecutionException {
        final List<SBoundaryEventDefinition> boundaryEventDefinitions = activityDefinition.getBoundaryEventDefinitions();
        try {
            final SBoundaryEventInstanceBuilderFactory boundaryEventInstanceBuilder = BuilderFactory
                    .get(SBoundaryEventInstanceBuilderFactory.class);
            final long rootProcessInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getRootProcessInstanceIndex());
            final long parentProcessInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getParentProcessInstanceIndex());
            final SFlowElementsContainerType containerType = getContainerType(activityInstance, boundaryEventInstanceBuilder);

            for (final SBoundaryEventDefinition boundaryEventDefinition : boundaryEventDefinitions) {
                createBoundaryEvent(processDefinition, activityInstance, rootProcessInstanceId, parentProcessInstanceId, containerType,
                        boundaryEventDefinition);
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("Unable to create boundary events attached to activity " + activityInstance.getName(), e);
        }
    }

    private void createBoundaryEvent(final SProcessDefinition processDefinition, final SActivityInstance activityInstance, final long rootProcessInstanceId,
            final long parentProcessInstanceId, final SFlowElementsContainerType containerType, final SBoundaryEventDefinition boundaryEventDefinition)
                    throws SBonitaException {
        final SBoundaryEventInstance boundaryEventInstance = (SBoundaryEventInstance) bpmInstancesCreator.createFlowNodeInstance(processDefinition.getId(),
                rootProcessInstanceId, activityInstance.getParentContainerId(), containerType, boundaryEventDefinition,
                rootProcessInstanceId, parentProcessInstanceId, false, -1, SStateCategory.NORMAL, activityInstance.getId()
                );

        // no need to handle failed state, creation is in the same tx
        containerRegistry.executeFlowNodeInSameThread(parentProcessInstanceId, boundaryEventInstance.getId(), null, null, containerType.name());
    }

    private SFlowElementsContainerType getContainerType(final SActivityInstance activityInstance,
            final SBoundaryEventInstanceBuilderFactory boundaryEventInstanceBuilder) {
        SFlowElementsContainerType containerType = SFlowElementsContainerType.PROCESS;
        final long parentActivityInstanceId = activityInstance.getLogicalGroup(boundaryEventInstanceBuilder.getParentActivityInstanceIndex());
        if (parentActivityInstanceId > 0) {
            containerType = SFlowElementsContainerType.FLOWNODE;
        }
        return containerType;
    }

    private boolean mustAddBoundaryEvents(final SActivityInstance activityInstance, final SActivityDefinition activityDefinition) {
        // avoid to add boundary events in children of multi instance
        return activityDefinition != null && !activityDefinition.getBoundaryEventDefinitions().isEmpty()
                && !isChildOfLoopOrMultiInstance(activityInstance, activityDefinition);
    }

    private boolean isChildOfLoopOrMultiInstance(final SActivityInstance activityInstance, final SActivityDefinition activityDefinition) {
        return activityDefinition.getLoopCharacteristics() != null
                && !(SFlowNodeType.MULTI_INSTANCE_ACTIVITY.equals(activityInstance.getType()) || SFlowNodeType.LOOP_ACTIVITY.equals(activityInstance.getType()));
    }

    public void interruptAttachedBoundaryEvent(final SProcessDefinition processDefinition, final SActivityInstance activityInstance,
            final SStateCategory categoryState) throws SActivityStateExecutionException {
        final SBoundaryEventInstanceBuilderFactory keyProvider = BuilderFactory.get(SBoundaryEventInstanceBuilderFactory.class);
        try {
            final List<SBoundaryEventInstance> boundaryEventInstances = eventInstanceService.getActivityBoundaryEventInstances(activityInstance.getId(), 0,
                    QueryOptions.UNLIMITED_NUMBER_OF_RESULTS);
            for (final SBoundaryEventInstance boundaryEventInstance : boundaryEventInstances) {
                // don't abort boundary event that put this activity in aborting state
                if (activityInstance.getAbortedByBoundary() != boundaryEventInstance.getId()) {
                    final boolean stable = boundaryEventInstance.isStable();
                    final SCatchEventDefinition catchEventDef = processDefinition.getProcessContainer().getBoundaryEvent(boundaryEventInstance.getName());
                    waitingEventsInterrupter.interruptWaitingEvents(processDefinition, boundaryEventInstance, catchEventDef);
                    activityInstanceService.setStateCategory(boundaryEventInstance, categoryState);
                    if (stable) {
                        containerRegistry.executeFlowNode(processDefinition.getId(),
                                boundaryEventInstance.getLogicalGroup(keyProvider.getParentProcessInstanceIndex()), boundaryEventInstance.getId(), null,
                                null);
                    }
                }
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException("Unable cancel boundary events attached to activity " + activityInstance.getName(), e);
        }
    }

    public void addAssignmentSystemCommentIfTaskWasAutoAssign(final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        if (SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType())) {
            final long userId = ((SHumanTaskInstance) flowNodeInstance).getAssigneeId();
            if (userId > 0) {
                try {
                    addAssignmentSystemComment(flowNodeInstance, userId);
                } catch (final SBonitaException e) {
                    throw new SActivityStateExecutionException("error while updating display name and description", e);
                }
            }
        }

    }

    public void addAssignmentSystemComment(final SFlowNodeInstance flowNodeInstance, final long userId) throws SUserNotFoundException, SCommentAddException {
        final SUser user = identityService.getUser(userId);
        if (commentService.isCommentEnabled(SystemCommentType.STATE_CHANGE)) {
            commentService.addSystemComment(flowNodeInstance.getRootContainerId(), "The task \"" + flowNodeInstance.getDisplayName() + "\" is now assigned to "
                    + user.getUserName());
        }
    }

    public List<SFlowNodeInstance> createInnerInstances(final long processDefinitionId, final SActivityDefinition activity,
            final SMultiInstanceActivityInstance flowNodeInstance, final int numberOfInstanceToCreate) throws SBonitaException {
        final SMultiInstanceActivityInstanceBuilderFactory keyProvider = BuilderFactory.get(SMultiInstanceActivityInstanceBuilderFactory.class);
        final long rootProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getRootProcessInstanceIndex());
        final long parentProcessInstanceId = flowNodeInstance.getLogicalGroup(keyProvider.getParentProcessInstanceIndex());
        int nbOfcreatedInstances = 0;
        final int nbOfInstances = flowNodeInstance.getNumberOfInstances();
        final List<SFlowNodeInstance> createdInstances = new ArrayList<SFlowNodeInstance>();
        for (int i = nbOfInstances; i < nbOfInstances + numberOfInstanceToCreate; i++) {
            createdInstances.add(bpmInstancesCreator.createFlowNodeInstance(processDefinitionId, flowNodeInstance.getRootContainerId(),
                    flowNodeInstance.getId(), SFlowElementsContainerType.FLOWNODE, activity, rootProcessInstanceId, parentProcessInstanceId, true, i,
                    SStateCategory.NORMAL, -1));
            nbOfcreatedInstances++;
        }
        activityInstanceService.addMultiInstanceNumberOfActiveActivities(flowNodeInstance, nbOfcreatedInstances);
        final int tokenCount = flowNodeInstance.getTokenCount() + nbOfcreatedInstances;
        activityInstanceService.setTokenCount(flowNodeInstance, tokenCount);
        return createdInstances;
    }

    public int getNumberOfInstancesToCreateFromInputRef(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SMultiInstanceLoopCharacteristics miLoop, final int numberOfInstanceMax) throws SDataInstanceException, SActivityStateExecutionException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataInputRef());
        if (businessData == null) {
            return getNumberOfInstanceToCreateFromSimpleData(processDefinition, flowNodeInstance, miLoop, numberOfInstanceMax);
        }
        try {
            return refBusinessDataService.getNumberOfDataOfMultiRefBusinessData(businessData.getName(), flowNodeInstance.getParentProcessInstanceId());
        } catch (final SBonitaReadException sbre) {
            throw new SActivityStateExecutionException(sbre);
        }

    }

    private int getNumberOfInstanceToCreateFromSimpleData(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SMultiInstanceLoopCharacteristics miLoop, final int numberOfInstanceMax) throws SDataInstanceException, SActivityStateExecutionException {
        final SDataInstance loopDataInput = dataInstanceService.getDataInstance(miLoop.getLoopDataInputRef(), flowNodeInstance.getId(),
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
        if (loopDataInput != null) {
            final Serializable value = loopDataInput.getValue();
            if (value instanceof List) {
                final List<?> loopDataInputCollection = (List<?>) value;
                return loopDataInputCollection.size();
            }
            throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName() + " of process "
                    + processDefinition.getName() + " " + processDefinition.getVersion() + " have a loop data input which is not a java.util.List");
        }
        return numberOfInstanceMax;
    }

    public boolean shouldCreateANewInstance(final SMultiInstanceLoopCharacteristics loopCharacteristics, final int numberOfInstances,
            final SMultiInstanceActivityInstance miActivityInstance) throws SDataInstanceException {
        if (loopCharacteristics.getLoopCardinality() != null) {
            return miActivityInstance.getLoopCardinality() > numberOfInstances;
        }
        List<?> possibleValues = null;
        try {
            //FIXME find if a business data is used if instead of try catch
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) refBusinessDataService.getRefBusinessDataInstance(
                    loopCharacteristics.getLoopDataInputRef(), miActivityInstance.getParentProcessInstanceId());
            possibleValues = multiRef.getDataIds();
        } catch (final SBonitaException sbe) {
            final SDataInstance dataInstance = getDataInstanceService().getDataInstance(loopCharacteristics.getLoopDataInputRef(),
                    miActivityInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
            possibleValues = (List<?>) dataInstance.getValue();
        }
        return possibleValues != null && numberOfInstances < possibleValues.size();
    }

    public void updateOutputData(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SMultiInstanceLoopCharacteristics miLoop, final int numberOfInstanceMax) throws SDataInstanceException, SActivityStateExecutionException {
        if (!isBusinessData(processDefinition, miLoop)) {
            final String loopDataOutputRef = miLoop.getLoopDataOutputRef();
            if (loopDataOutputRef != null) {
                final SDataInstance loopDataOutput = dataInstanceService.getDataInstance(loopDataOutputRef, flowNodeInstance.getId(),
                        DataInstanceContainer.ACTIVITY_INSTANCE.name(), parentContainerResolver);
                if (loopDataOutput != null) {
                    final Serializable outValue = loopDataOutput.getValue();
                    if (outValue instanceof List) {
                        updateLoopDataOutputWithListContent((List<?>) outValue, loopDataOutput, numberOfInstanceMax);
                    } else if (outValue == null) {
                        updateLoopDataOutputWithNull(loopDataOutput, numberOfInstanceMax);
                    } else {
                        throw new SActivityStateExecutionException("The multi instance on activity " + flowNodeInstance.getName()
                                + " of process " + processDefinition.getName() + " " + processDefinition.getVersion()
                                + " have a loop data output which is not a java.util.List");
                    }
                }
            }
        }
    }

    boolean isBusinessData(final SProcessDefinition processDefinition, final SMultiInstanceLoopCharacteristics miLoop) {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final SBusinessDataDefinition businessData = processContainer.getBusinessDataDefinition(miLoop.getLoopDataOutputRef());
        return businessData != null;
    }

    private void updateLoopDataOutputWithNull(final SDataInstance loopDataOutput, final int numberOfInstanceMax) throws SDataInstanceException {
        final ArrayList<Object> newOutputList = new ArrayList<Object>(numberOfInstanceMax);
        for (int i = 0; i < numberOfInstanceMax; i++) {
            newOutputList.add(null);
        }
        updateLoopDataOutputDataInstance(loopDataOutput, newOutputList);
    }

    private void updateLoopDataOutputWithListContent(final List<?> outValue, final SDataInstance loopDataOutput, final int numberOfInstanceMax)
            throws SDataInstanceException {
        if (outValue.size() < numberOfInstanceMax) {
            // output data is too small
            final ArrayList<Object> newOutputList = new ArrayList<Object>(numberOfInstanceMax);
            newOutputList.addAll(outValue);
            for (int i = outValue.size(); i < numberOfInstanceMax; i++) {
                newOutputList.add(null);
            }
            updateLoopDataOutputDataInstance(loopDataOutput, newOutputList);
        }
    }

    private void updateLoopDataOutputDataInstance(final SDataInstance loopDataOutput, final ArrayList<Object> newOutputList) throws SDataInstanceException {
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        updateDescriptor.addField(fact.getValueKey(), newOutputList);
        dataInstanceService.updateDataInstance(loopDataOutput, updateDescriptor);
    }

}
