/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.actor.mapping.SActorNotFoundException;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocument;
import org.bonitasoft.engine.api.impl.transaction.document.AttachDocumentAndStoreContent;
import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.bar.DocumentsResourcesContribution;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.classloader.ClassLoaderException;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionFailedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SGatewayNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.SToken;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SGatewayInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.ExecuteConnectorOfProcess;
import org.bonitasoft.engine.execution.work.ExecuteConnectorWork;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork;
import org.bonitasoft.engine.execution.work.ExecuteTransitionWork;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessExecutorImpl implements ProcessExecutor {

    private static final String GATEWAY_LOCK = SFlowNodeType.GATEWAY.name();

    private static final String PROCESS_COMPLETION_LOCK = SFlowElementsContainerType.PROCESS.name() + "_finish";

    private final BPMInstanceBuilders instanceBuilders;

    protected final ActivityInstanceService activityInstanceService;

    private final FlowNodeExecutor flowNodeExecutor;

    private final TechnicalLoggerService logger;

    protected final ProcessInstanceService processInstanceService;

    private final TokenService tokenService;

    protected final TransactionExecutor transactionExecutor;

    private final WorkService workService;

    private final ProcessDefinitionService processDefinitionService;

    private final GatewayInstanceService gatewayInstanceService;

    private final TransitionService transitionService;

    private final EventInstanceService eventInstanceService;

    protected final ClassLoaderService classLoaderService;

    protected final ExpressionResolverService expressionResolverService;

    protected final ConnectorService connectorService;

    private final OperationService operationService;

    private final ProcessDocumentService processDocumentService;

    private final SProcessDocumentBuilder documentBuilder;

    private final ReadSessionAccessor sessionAccessor;

    protected final BPMInstancesCreator bpmInstancesCreator;

    private final LockService lockService;

    protected final EventsHandler eventsHandler;

    private final BPMDefinitionBuilders bpmDefinitionBuilders;

    private final ContainerRegistry containerRegistry;

    private final ConnectorInstanceService connectorInstanceService;

    private final SExpressionBuilders expressionBuilders;

    private final SOperationBuilders operationBuilders;

    public ProcessExecutorImpl(final BPMInstanceBuilders instanceBuilders, final ActivityInstanceService activityInstanceService,
            final ProcessInstanceService processInstanceService, final FlowNodeStateManager flowNodeStateManager, final TechnicalLoggerService logger,
            final FlowNodeExecutor flowNodeExecutor, final TransactionExecutor transactionExecutor, final WorkService workService,
            final ProcessDefinitionService processDefinitionService, final GatewayInstanceService gatewayInstanceService,
            final TransitionService transitionService, final EventInstanceService eventInstanceService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final OperationService operationService,
            final SExpressionBuilders expressionBuilders, final ExpressionResolverService expressionResolverService, final EventService eventService,
            final Map<String, SProcessInstanceHandler<SEvent>> handlers, final ProcessDocumentService processDocumentService,
            final SProcessDocumentBuilder documentBuilder, final ReadSessionAccessor sessionAccessor, final ContainerRegistry containerRegistry,
            final BPMInstancesCreator bpmInstancesCreator, final ArchiveService archiveService, final LockService lockService, final TokenService tokenService,
            final EventsHandler eventsHandler, final BPMDefinitionBuilders bpmDefinitionBuilders, final SOperationBuilders operationBuilders) {
        super();
        this.instanceBuilders = instanceBuilders;
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.connectorInstanceService = connectorInstanceService;
        this.expressionBuilders = expressionBuilders;
        this.containerRegistry = containerRegistry;
        this.tokenService = tokenService;
        this.bpmDefinitionBuilders = bpmDefinitionBuilders;
        flowNodeStateManager.setProcessExecutor(this);
        this.logger = logger;
        this.flowNodeExecutor = flowNodeExecutor;
        this.transactionExecutor = transactionExecutor;
        this.workService = workService;
        this.processDefinitionService = processDefinitionService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.transitionService = transitionService;
        this.eventInstanceService = eventInstanceService;
        this.connectorService = connectorService;
        this.classLoaderService = classLoaderService;
        this.operationService = operationService;
        this.operationBuilders = operationBuilders;
        this.expressionResolverService = expressionResolverService;
        this.processDocumentService = processDocumentService;
        this.documentBuilder = documentBuilder;
        this.sessionAccessor = sessionAccessor;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.lockService = lockService;
        this.eventsHandler = eventsHandler;
        eventsHandler.setProcessExecutor(this);
        for (final Entry<String, SProcessInstanceHandler<SEvent>> handler : handlers.entrySet()) {
            try {
                handler.getValue().setProcessInstanceService(processInstanceService);
                eventService.addHandler(handler.getKey(), handler.getValue());// TODO check if it's already here?
            } catch (final HandlerRegistrationException e) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);// just log it
            }
        }
        containerRegistry.addContainerExecutor(this);
    }

    @Override
    public void executeFlowNode(final long flowNodeInstanceId, final SExpressionContext expressionContext, final List<SOperation> operations,
            final Long processInstanceId) throws SFlowNodeExecutionException, SActivityInterruptedException, SActivityReadException {
        flowNodeExecutor.gotoNextStableState(flowNodeInstanceId, expressionContext, operations, null, processInstanceId);
    }

    private List<STransitionDefinition> evaluateOutgoingTransitions(final List<STransitionDefinition> outgoingTransitionDefinitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        List<STransitionDefinition> chosenTransitionDefinitions = Collections.emptyList();
        // int nbOfTokenToMerge = 1;// may be > 1 in case of gateway
        // if is not a normal state don't create new elements
        if (SStateCategory.NORMAL.equals(flowNodeInstance.getStateCategory())) {
            final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                    sDefinition.getId());
            if (SFlowNodeType.GATEWAY.equals(flowNodeInstance.getType())) {
                final SGatewayInstance gatewayInstance = (SGatewayInstance) flowNodeInstance;
                switch (gatewayInstance.getGatewayType()) {
                    case EXCLUSIVE:
                        chosenTransitionDefinitions = evaluateTransitionsExclusively(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                                sExpressionContext);
                        break;
                    case INCLUSIVE:
                        chosenTransitionDefinitions = evaluateTransitionsInclusively(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                                sExpressionContext);
                        break;
                    case PARALLEL:
                        chosenTransitionDefinitions = evaluateTransitionsParallely(outgoingTransitionDefinitions);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unsupported gateway type: " + gatewayInstance.getGatewayType());
                }
            } else if (SFlowNodeType.BOUNDARY_EVENT.equals(flowNodeInstance.getType())) {
                chosenTransitionDefinitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions);
            } else {
                if (outgoingTransitionDefinitions.isEmpty()) {
                    chosenTransitionDefinitions = new ArrayList<STransitionDefinition>(1);
                } else {
                    chosenTransitionDefinitions = evaluateTransitionsForImpliciteGateway(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                            sExpressionContext);
                }
            }
        }
        return chosenTransitionDefinitions;
    }

    private void executeTransitions(final SProcessDefinition sDefinition, final List<STransitionInstance> chosenTransitions) throws SBonitaException {
        for (final STransitionInstance sTransitionInstance : chosenTransitions) {
            workService.registerWork(new ExecuteTransitionWork(this, sDefinition, sTransitionInstance));
        }
    }

    private int getNumberOfTokenToMerge(final SFlowNodeInstance sFlownodeInstance) {
        if (SFlowNodeType.GATEWAY.equals(sFlownodeInstance.getType())) {
            return Integer.parseInt(((SGatewayInstance) sFlownodeInstance).getHitBys().substring(GatewayInstanceService.FINISH.length()));
        } else {
            return 1;
        }
    }

    private List<STransitionDefinition> evaluateTransitionsExclusively(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        boolean transitionFound = false;
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null || condition) {
                transitionFound = true;
                chosenTransitions.add(sTransitionDefinition);
                break;
            }
        }
        if (!transitionFound) {
            final STransitionDefinition defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance);
            if (defaultTransition == null) {
                throw new SActivityExecutionException("There is no default transition on " + flowNodeInstance.getName()
                        + " but no outgoing transition had a valid condition");
            }
            chosenTransitions.add(defaultTransition);
            outgoingTransitionDefinitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    private List<STransitionDefinition> evaluateTransitionsInclusively(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null || condition) {
                chosenTransitions.add(sTransitionDefinition);
            }
        }
        if (chosenTransitions.isEmpty()) {
            final STransitionDefinition defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance);
            if (defaultTransition == null) {
                throw new SActivityExecutionException("There is no default transition on " + flowNodeInstance.getName()
                        + " but no outgoing transition had a valid condition");
            }
            chosenTransitions.add(defaultTransition);
            outgoingTransitionDefinitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    private List<STransitionDefinition> evaluateTransitionsForImpliciteGateway(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final int transitionNumber = outgoingTransitionDefinitions.size();
        final List<STransitionDefinition> conditionalTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> conditionalFalseTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> normalTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null) {
                normalTransitions.add(sTransitionDefinition);
            } else {
                if (condition) {
                    conditionalTransitions.add(sTransitionDefinition);
                } else {
                    conditionalFalseTransitions.add(sTransitionDefinition);
                }
            }
        }
        if (!normalTransitions.isEmpty()) {
            chosenTransitions.addAll(normalTransitions);
        }
        if (!conditionalTransitions.isEmpty()) {
            chosenTransitions.addAll(conditionalTransitions);
        } else if (!conditionalFalseTransitions.isEmpty()) {
            final STransitionDefinition defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance);
            if (defaultTransition == null) {
                throw new SActivityExecutionException("There is no default transition on " + flowNodeInstance.getName()
                        + " but no outgoing transition had a valid condition");
            }
            chosenTransitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    private List<STransitionDefinition> evaluateTransitionsParallely(final List<STransitionDefinition> outgoingTransitionDefinitions) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            chosenTransitions.add(sTransitionDefinition);
        }
        return chosenTransitions;
    }

    protected STransitionDefinition getDefaultTransition(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance)
            throws STransitionCreationException, STransitionReadException {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        return flowNode.getDefaultTransition();
    }

    protected Boolean evaluateCondition(final SProcessDefinition sDefinition, final STransitionDefinition sTransitionDefinition,
            final SExpressionContext contextDependency) throws SExpressionEvaluationException, SExpressionTypeUnknownException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        // TODO find a better method to get expression on transitionDef
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final STransitionDefinition transition = processContainer.getTransition(sTransitionDefinition.getName());
        final SExpression expression = transition.getCondition();
        if (expression == null) {// no condition == true but return null to say it was a transition without condition
            return null;
        }
        if (!Boolean.class.getName().equals(expression.getReturnType())) {
            throw new SExpressionEvaluationException("Condition expression must return a boolean. on transition: " + sTransitionDefinition.getName());
        }
        return (Boolean) expressionResolverService.evaluate(expression, contextDependency);
    }

    private List<SFlowNodeDefinition> getStartNodes(final SProcessDefinition definition, final long targetSFlowNodeDefinitionId) throws SActorNotFoundException {
        return getStartNodes(definition.getProcessContainer(), targetSFlowNodeDefinitionId);
    }

    private List<SFlowNodeDefinition> getStartNodes(final SFlowElementContainerDefinition processContainer, final long targetSFlowNodeDefinitionId) {
        final Set<SFlowNodeDefinition> sFlowNodeDefinitions = processContainer.getFlowNodes();
        final List<SFlowNodeDefinition> filteredSFlowNodeDefinitions = new ArrayList<SFlowNodeDefinition>();
        for (final SFlowNodeDefinition sFlowNodeDefinition : sFlowNodeDefinitions) {
            if (isAStartNode(targetSFlowNodeDefinitionId, sFlowNodeDefinition)) {
                // do not start event sub process (not in the flow)
                filteredSFlowNodeDefinitions.add(sFlowNodeDefinition);
            }
        }
        return filteredSFlowNodeDefinitions;
    }

    private boolean isAStartNode(final long targetSFlowNodeDefinitionId, final SFlowNodeDefinition sFlowNodeDefinition) {
        if (targetSFlowNodeDefinitionId != -1) {
            // When call start by event triggers, run only the target of trigger
            // The starterId equals 0, when start process in work (See InstantiateProcessWork)
            return sFlowNodeDefinition.getId() == targetSFlowNodeDefinitionId;
        }
        // Not started by an event: start all elements that are start point and are not triggered by an event
        return sFlowNodeDefinition.getIncomingTransitions().isEmpty()
                && !(SFlowNodeType.SUB_PROCESS.equals(sFlowNodeDefinition.getType()) && ((SSubProcessDefinition) sFlowNodeDefinition).isTriggeredByEvent())
                && !SFlowNodeType.BOUNDARY_EVENT.equals(sFlowNodeDefinition.getType())
                // is not a start event or if it is, is not triggered by an event
                && (!SFlowNodeType.START_EVENT.equals(sFlowNodeDefinition.getType()) || ((SStartEventDefinition) sFlowNodeDefinition)
                        .getEventTriggers()
                        .isEmpty());
    }

    private List<SFlowNodeDefinition> getStartNodes(final SProcessDefinition definition, final long flowNodeDefinitionId, final long targetSFlowNodeDefinitionId)
            throws SActorNotFoundException {
        final SSubProcessDefinition subProcDef = (SSubProcessDefinition) definition.getProcessContainer().getFlowNode(flowNodeDefinitionId);
        return getStartNodes(subProcDef.getSubProcessContainer(), targetSFlowNodeDefinitionId);
    }

    private SConnectorInstance getNextConnectorInstance(final SProcessInstance processInstance, final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(processInstance.getId(),
                SConnectorInstance.PROCESS_TYPE, event, 0, 1, ConnectorService.TO_BE_EXECUTED);
        return connectorInstances.size() == 1 ? connectorInstances.get(0) : null;
    }

    @Override
    public boolean executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sInstance, final ConnectorEvent activationEvent,
            final ConnectorService connectorService) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final List<SConnectorDefinition> connectors = processContainer.getConnectors(activationEvent);
        if (connectors.size() > 0) {
            SConnectorInstance nextConnectorInstance;
            nextConnectorInstance = getNextConnectorInstance(sInstance, activationEvent);
            if (nextConnectorInstance != null) {
                for (final SConnectorDefinition sConnectorDefinition : connectors) {
                    if (sConnectorDefinition.getName().equals(nextConnectorInstance.getName())) {
                        final SExpressionContext sExpressionContext = new SExpressionContext(sInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                                processDefinition.getId());
                        Map<String, Object> inputParameters = null;
                        try {
                            inputParameters = connectorService.evaluateInputParameters(sConnectorDefinition.getInputs(), sExpressionContext, null);
                        } catch (final SBonitaException sbe) {
                            final ExecuteConnectorWork work = getWork(processDefinition, sInstance, activationEvent, connectorService, nextConnectorInstance,
                                    sConnectorDefinition, inputParameters);
                            work.setErrorThrownWhenEvaluationOfInputParameters(sbe);
                            workService.registerWork(work);
                        }
                        if (inputParameters != null) {
                            workService.registerWork(getWork(processDefinition, sInstance, activationEvent, connectorService, nextConnectorInstance,
                                    sConnectorDefinition, inputParameters));
                        }
                        return true;
                    }
                }
            }
        }
        return false;

    }

    private ExecuteConnectorOfProcess getWork(final SProcessDefinition processDefinition, final SProcessInstance sInstance,
            final ConnectorEvent activationEvent, final ConnectorService connectorService, final SConnectorInstance nextConnectorInstance,
            final SConnectorDefinition sConnectorDefinition, final Map<String, Object> inputParameters) {
        return new ExecuteConnectorOfProcess(transactionExecutor, processInstanceService, classLoaderService, connectorService, connectorInstanceService,
                processDefinition, nextConnectorInstance, sConnectorDefinition, inputParameters, sInstance, this, activationEvent, eventsHandler,
                instanceBuilders, bpmInstancesCreator, bpmDefinitionBuilders, containerRegistry);
    }

    private List<SFlowNodeInstance> initializeFirstExecutableElements(final SProcessInstance sInstance, final SProcessDefinition sDefinition,
            final long flowNodeDefinitionId,
            final long targetSFlowNodeDefinitionId) {
        try {
            List<SFlowNodeDefinition> flownNodeDefinitions;
            if (flowNodeDefinitionId == -1) {
                flownNodeDefinitions = getStartNodes(sDefinition, targetSFlowNodeDefinitionId);
            } else {
                flownNodeDefinitions = getStartNodes(sDefinition, flowNodeDefinitionId, targetSFlowNodeDefinitionId);
            }
            long rootProcessInstanceId = sInstance.getRootProcessInstanceId();
            if (rootProcessInstanceId <= 0) {
                rootProcessInstanceId = sInstance.getId();
            }
            return bpmInstancesCreator.createFlowNodeInstances(sDefinition, rootProcessInstanceId, sInstance.getId(), flownNodeDefinitions,
                    rootProcessInstanceId,
                    sInstance.getId(), SStateCategory.NORMAL, sDefinition.getId());
        } catch (final SBonitaException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            throw new RuntimeException(e);
        }
    }

    private SProcessInstance createProcessInstance(final SProcessDefinition sDefinition, final long userId, final long callerId,
            final SFlowNodeType callerType, final long rootProcessInstanceId) throws SProcessInstanceCreationException {
        final SProcessInstanceBuilder processInstanceBuilder = instanceBuilders.getSProcessInstanceBuilder().createNewInstance(sDefinition)
                .setStartedBy(userId).setCallerId(callerId, callerType).setRootProcessInstanceId(rootProcessInstanceId);
        final SProcessInstance sInstance = processInstanceBuilder.done();
        processInstanceService.createProcessInstance(sInstance);
        return sInstance;
    }

    protected SProcessInstance createProcessInstance(final long userId, final SProcessDefinition processDefinition, final long callerId)
            throws SProcessInstanceCreationException {
        final SActivityInstance callerInstance = getCaller(callerId);
        final SProcessInstance sInstance;
        if (callerInstance != null) {
            sInstance = createProcessInstance(processDefinition, userId, callerId, callerInstance.getType(), callerInstance.getRootContainerId());
        } else {
            sInstance = createProcessInstance(processDefinition, userId, callerId, null, -1);
        }
        return sInstance;
    }

    private SActivityInstance getCaller(final long callerId) throws SProcessInstanceCreationException {
        SActivityInstance callerInstance = null;
        if (callerId > 0) {
            try {
                callerInstance = activityInstanceService.getActivityInstance(callerId);
            } catch (final SBonitaException e) {
                throw new SProcessInstanceCreationException("Unable to get caller", e);
            }
        }
        return callerInstance;
    }

    /*
     * We execute the transition here
     * The merge of the target element will be evaluated
     * The target element is created with the token of the transition
     * Token are deleted/created when the gateway is activated
     */
    @Override
    public void executeTransition(final SProcessDefinition sDefinition, final STransitionInstance fTransitionInstance) throws SBonitaException {
        final STransitionInstanceBuilder transitionInstanceBuilder = bpmInstancesCreator.getBPMInstanceBuilders().getSTransitionInstanceBuilder();
        final long parentProcessInstanceId = fTransitionInstance.getLogicalGroup(transitionInstanceBuilder.getParentProcessInstanceIndex());
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinitionService.getNextFlowNode(sDefinition, fTransitionInstance.getName());
        final String objectType = SFlowElementsContainerType.PROCESS.name();
        lockService.createSharedLockAccess(parentProcessInstanceId, objectType);
        final boolean isGateway = SFlowNodeType.GATEWAY.equals(sFlowNodeDefinition.getType());
        final Long nextFlowNodeInstanceId;
        boolean toExecute = false;
        long gatewayLockId = -1;
        if (isGateway) {
            // if flownode definition is gateway we have an exclusive lock on the transitioninstance.getTarget for this process instance
            gatewayLockId = parentProcessInstanceId + sFlowNodeDefinition.getId();
            lockService.createExclusiveLockAccess(gatewayLockId, GATEWAY_LOCK);
        }
        final boolean txOpened = transactionExecutor.openTransaction();
        // refresh because the transition was get in an other transaction
        final STransitionInstance transitionInstance = transitionService.get(fTransitionInstance.getId());
        try {
            final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
            final SStateCategory stateCategory = parentProcessInstance.getStateCategory();
            if (isGateway) {
                final SGatewayInstance gatewayInstance = createOrRetreiveGateway(sDefinition, transitionInstance, sFlowNodeDefinition, stateCategory);
                gatewayInstanceService.hitTransition(gatewayInstance, transitionInstance.getName());
                nextFlowNodeInstanceId = gatewayInstance.getId();
                if (gatewayInstanceService.checkMergingCondition(sDefinition, gatewayInstance)) {
                    gatewayInstanceService.setFinish(gatewayInstance);
                    toExecute = true;
                }
            } else {
                final long rootProcessInstanceId = transitionInstance.getLogicalGroup(transitionInstanceBuilder.getRootProcessInstanceIndex());
                final SFlowNodeInstance flowNodeInstance = bpmInstancesCreator.toFlowNodeInstance(sDefinition.getId(), transitionInstance.getRootContainerId(),
                        transitionInstance.getParentContainerId(), SFlowElementsContainerType.PROCESS, sFlowNodeDefinition, rootProcessInstanceId,
                        parentProcessInstanceId, true, -1, stateCategory, -1, fTransitionInstance.getTokenRefId());
                final TransactionContent transactionContent = new CreateEventInstance((SEventInstance) flowNodeInstance, eventInstanceService);
                transactionContent.execute();
                nextFlowNodeInstanceId = flowNodeInstance.getId();
                toExecute = true;
            }
            if (toExecute) {
                workService.registerWork(new ExecuteFlowNodeWork(this, nextFlowNodeInstanceId, null, null, parentProcessInstanceId));
            }

            transitionService.archive(transitionInstance, nextFlowNodeInstanceId, TransitionState.TAKEN);
            transitionService.delete(transitionInstance);
        } catch (final SBonitaException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            throw e;
            // FIXME set state of the transition in error
        } finally {
            transactionExecutor.completeTransaction(txOpened);
            lockService.releaseSharedLockAccess(parentProcessInstanceId, objectType);
            if (isGateway) {
                lockService.releaseExclusiveLockAccess(gatewayLockId, GATEWAY_LOCK);
            }
        }
    }

    /*
     * try to gate active gateway.
     * if the gateway is already hit by this transition or by the same token, we create a new gateway
     */
    private SGatewayInstance createOrRetreiveGateway(final SProcessDefinition sDefinition, final STransitionInstance transitionInstance,
            final SFlowNodeDefinition flowNodeDefinition, final SStateCategory stateCategory) throws SBonitaException {
        final SGatewayInstanceBuilder gatewayInstanceBuilder = instanceBuilders.getSGatewayInstanceBuilder();
        final long parentProcessInstanceId = transitionInstance.getLogicalGroup(gatewayInstanceBuilder.getParentProcessInstanceIndex());
        SGatewayInstance gatewayInstance = null;
        try {
            gatewayInstance = gatewayInstanceService.getActiveGatewayInstanceOfTheProcess(parentProcessInstanceId, flowNodeDefinition.getName());
        } catch (final SGatewayNotFoundException e) {
            // no gateway found we create one
        }
        // check if it's already hit
        if (gatewayInstance != null
                && gatewayInstance.getHitBys() != null
                && (gatewayInstance.getHitBys().contains(GatewayInstanceService.FINISH) || Arrays.asList(gatewayInstance.getHitBys().split(",")).contains(
                        transitionInstance.getName())) && gatewayInstance.getTokenRefId() == transitionInstance.getTokenRefId()) {
            gatewayInstance = null;// already hit we create a new one
        }
        if (gatewayInstance != null) {
            return gatewayInstance;
        } else {
            return createGateway(sDefinition, transitionInstance, flowNodeDefinition, stateCategory, gatewayInstanceBuilder, parentProcessInstanceId,
                    transitionInstance.getTokenRefId());
        }
    }

    private SGatewayInstance createGateway(final SProcessDefinition sDefinition, final STransitionInstance transitionInstance,
            final SFlowNodeDefinition flowNodeDefinition, final SStateCategory stateCategory, final SGatewayInstanceBuilder gatewayInstanceBuilder,
            final long parentProcessInstanceId, final Long tokenRefId) throws SBonitaException {
        final long rootProcessInstanceId = transitionInstance.getLogicalGroup(gatewayInstanceBuilder.getRootProcessInstanceIndex());
        return (SGatewayInstance) bpmInstancesCreator
                .createFlowNodeInstance(sDefinition, rootProcessInstanceId, parentProcessInstanceId, SFlowElementsContainerType.PROCESS, flowNodeDefinition,
                        rootProcessInstanceId, parentProcessInstanceId, false, 0, stateCategory, -1, tokenRefId);
    }

    protected void executeOperations(final List<SOperation> operations, final Map<String, Object> context, final SExpressionContext expressionContext,
            final SProcessInstance sInstance)
            throws SBonitaException {
        if (operations != null && !operations.isEmpty()) {
            // Execute operation
            final long processInstanceId = sInstance.getId();
            for (final SOperation operation : operations) {
                // TODO operations can be evaluated in batch here
                expressionContext.setInputValues(context);
                operationService.execute(operation, processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name(), expressionContext);
            }
        }
    }

    protected boolean initialize(final long userId, final SProcessDefinition sDefinition, final SProcessInstance sInstance,
            SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context,
            final SFlowElementContainerDefinition processContainer, final List<ConnectorDefinitionWithInputValues> connectors)
            throws SProcessInstanceCreationException {
        try {
            // Create SDataInstances
            bpmInstancesCreator.createDataInstances(sInstance, processContainer, sDefinition, expressionContext, operations, context);
            createDocuments(sDefinition, sInstance, userId);
            if (connectors != null) {
                executeConnectors(sDefinition, sInstance, connectors);
            }
            if (expressionContext == null) {
                expressionContext = new SExpressionContext();
            }
            executeOperations(operations, context, expressionContext, sInstance);

            // Create connectors
            bpmInstancesCreator.createConnectorInstances(sInstance, processContainer.getConnectors(), SConnectorInstance.PROCESS_TYPE);

            return executeConnectors(sDefinition, sInstance, ConnectorEvent.ON_ENTER, connectorService);
        } catch (final SProcessInstanceCreationException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final BonitaHomeNotSetException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final IOException e) {
            throw new SProcessInstanceCreationException(e);
        } catch (final InvalidEvaluationConnectorConditionException e) {
            throw new SProcessInstanceCreationException(e);
        }
    }

    protected void createDocuments(final SProcessDefinition sDefinition, final SProcessInstance sInstance, final long authorId) throws SBonitaException,
            IOException, BonitaHomeNotSetException {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final List<SDocumentDefinition> documentDefinitions = processContainer.getDocumentDefinitions();
        if (!documentDefinitions.isEmpty()) {
            final String processesFolder = BonitaHomeServer.getInstance().getProcessFolder(sessionAccessor.getTenantId(), sDefinition.getId());
            final File documentsFolder = new File(processesFolder, DocumentsResourcesContribution.DOCUMENTS_FOLDER);
            for (final SDocumentDefinition document : documentDefinitions) {
                if (document.getFile() != null) {
                    final String file = document.getFile();// should always exists...validation on businessarchive
                    final byte[] content = FileUtils.readFileToByteArray(new File(documentsFolder, file));
                    attachDocument(sInstance.getId(), document.getName(), document.getFileName(), document.getContentMimeType(), content, authorId);
                } else if (document.getUrl() != null) {
                    attachDocument(sInstance.getId(), document.getName(), document.getFileName(), document.getContentMimeType(), document.getUrl(), authorId);
                } else {
                    throw new SProcessInstanceCreationException("Unable to create documents, a document was defined without url or content");
                }
            }
        }
    }

    protected SProcessDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url, final long authorId) throws SBonitaException {
        final SProcessDocument attachment = buildExternalProcessDocumentReference(documentBuilder, processInstanceId, documentName, fileName, mimeType,
                authorId, url);
        final AttachDocument attachDocumentTransationContent = new AttachDocument(processDocumentService, attachment);
        transactionExecutor.execute(attachDocumentTransationContent);
        return attachDocumentTransationContent.getResult();
    }

    protected SProcessDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent, final long authorId) throws SBonitaException {
        final SProcessDocument attachment = buildProcessDocument(documentBuilder, processInstanceId, documentName, fileName, mimeType, authorId);

        final AttachDocumentAndStoreContent attachDocumentTransationContent = new AttachDocumentAndStoreContent(processDocumentService, attachment,
                documentContent);
        transactionExecutor.execute(attachDocumentTransationContent);
        return attachDocumentTransationContent.getResult();
    }

    private SProcessDocument buildExternalProcessDocumentReference(final SProcessDocumentBuilder documentBuilder, final long processInstanceId,
            final String documentName, final String fileName, final String mimeType, final long authorId, final String url) {
        initDocumentBuilder(documentBuilder, processInstanceId, documentName, fileName, mimeType, authorId);
        documentBuilder.setURL(url);
        documentBuilder.setHasContent(false);
        return documentBuilder.done();
    }

    private SProcessDocument buildProcessDocument(final SProcessDocumentBuilder documentBuilder, final long processInstanceId, final String documentName,
            final String fileName, final String mimetype, final long authorId) {
        initDocumentBuilder(documentBuilder, processInstanceId, documentName, fileName, mimetype, authorId);
        documentBuilder.setHasContent(true);
        return documentBuilder.done();
    }

    private void initDocumentBuilder(final SProcessDocumentBuilder documentBuilder, final long processInstanceId, final String documentName,
            final String fileName, final String mimetype, final long authorId) {
        documentBuilder.createNewInstance();
        documentBuilder.setName(documentName);
        documentBuilder.setFileName(fileName);
        documentBuilder.setContentMimeType(mimetype);
        documentBuilder.setProcessInstanceId(processInstanceId);
        documentBuilder.setAuthor(authorId);
        documentBuilder.setCreationDate(System.currentTimeMillis());
    }

    @Override
    public void executeActivity(final long flowNodeInstanceId, final Long executedBy) throws SFlowNodeExecutionException, SActivityInterruptedException,
            SActivityReadException {
        flowNodeExecutor.gotoNextStableState(flowNodeInstanceId, null, null, executedBy, null);
    }

    @Override
    public void childReachedState(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child, final FlowNodeState state, final long parentId)
            throws SBonitaException {
        SProcessInstance sProcessInstance;
        if (state.isTerminal()) {
            final SUserTaskInstanceBuilder flowNodeKeyProvider = bpmInstancesCreator.getBPMInstanceBuilders().getUserTaskInstanceBuilder();
            final long processInstanceId = child.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());

            lockService.createExclusiveLockAccess(processInstanceId, PROCESS_COMPLETION_LOCK);// lock process in order to avoid hit 2 times at the same time
            try {
                final boolean txOpened = transactionExecutor.openTransaction();
                try {
                    sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);

                    final int tokensOfProcess = executeValidOutgoingTransitionsAndUpdateTokens(sProcessDefinition, child, sProcessInstance);

                    if (tokensOfProcess == 0) {
                        boolean hasActionsToExecute = false;
                        if (ProcessInstanceState.ABORTING.getId() != sProcessInstance.getStateId()) {
                            hasActionsToExecute = executePostThrowEventHandlers(sProcessDefinition, sProcessInstance, child);
                            // the process instance has maybe changed
                            if (hasActionsToExecute) {
                                sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
                            }
                            eventsHandler.unregisterEventSubProcess(sProcessDefinition, sProcessInstance);
                        }
                        handleProcessCompletion(sProcessDefinition, sProcessInstance, hasActionsToExecute);
                    }
                } catch (final SBonitaException e) {
                    transactionExecutor.setTransactionRollback();
                    throw e;
                } finally {
                    transactionExecutor.completeTransaction(txOpened);
                }
            } finally {
                lockService.releaseExclusiveLockAccess(processInstanceId, PROCESS_COMPLETION_LOCK);
            }
        }
    }

    @Override
    public void handleProcessCompletion(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance, final boolean hasActionsToExecute)
            throws SBonitaException {
        ProcessInstanceState processInstanceState;
        switch (sProcessInstance.getStateCategory()) {
            case ABORTING:
                if (ProcessInstanceState.ABORTING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.ABORTED;
                } else {
                    if (hasActionsToExecute) {
                        processInstanceState = ProcessInstanceState.ABORTING;
                    } else {
                        processInstanceState = ProcessInstanceState.ABORTED;
                    }
                }
                break;
            case CANCELLING:
                processInstanceState = ProcessInstanceState.CANCELLED;
                break;
            default:
                if (ProcessInstanceState.COMPLETING.getId() == sProcessInstance.getStateId()) {
                    processInstanceState = ProcessInstanceState.COMPLETED;
                } else {
                    if (executeConnectors(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_FINISH, connectorService)) {
                        // some connectors were trigger
                        processInstanceState = ProcessInstanceState.COMPLETING;
                    } else {
                        processInstanceState = ProcessInstanceState.COMPLETED;
                    }
                }
                break;
        }
        processInstanceService.setState(sProcessInstance, processInstanceState);
        if (tokenService.getNumberOfToken(sProcessInstance.getId()) == 0) {
            flowNodeExecutor.childReachedState(sProcessDefinition, sProcessInstance, processInstanceState, hasActionsToExecute);
        }

    }

    private boolean executePostThrowEventHandlers(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            final SFlowNodeInstance child) throws SBonitaException {
        boolean hasActionsToExecute = false;
        if (sProcessInstance.getInterruptingEventId() != -1) {
            final SFlowNodeInstance endEventInstance = activityInstanceService.getFlowNodeInstance(sProcessInstance.getInterruptingEventId());
            final SEndEventDefinition endEventDefinition = (SEndEventDefinition) sProcessDefinition.getProcessContainer().getFlowNode(
                    endEventInstance.getFlowNodeDefinitionId());
            hasActionsToExecute = eventsHandler.handlePostThrowEvent(sProcessDefinition, endEventDefinition, (SThrowEventInstance) endEventInstance, child);
            flowNodeExecutor.archiveFlowNodeInstance(endEventInstance, true, sProcessDefinition);
        }
        return hasActionsToExecute;
    }

    /**
     * Evaluate the split of the element
     * The element contains the current token it received
     * 
     * @return
     *         number of token of the process
     */
    private int executeValidOutgoingTransitionsAndUpdateTokens(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        final long parentProcessInstanceId = sProcessInstance.getId();
        final long childId = child.getId();
        // token we merged
        final int numberOfTokenToMerge = getNumberOfTokenToMerge(child);
        final SFlowNodeDefinition flowNode = sProcessDefinition.getProcessContainer().getFlowNode(child.getFlowNodeDefinitionId());
        final int inputTransitionsSize;
        final List<STransitionDefinition> allOutgoingTransitionDefinitions;
        // Retrieve all outgoing transitions
        if (flowNode == null) {
            // not in definition
            inputTransitionsSize = 0;
            allOutgoingTransitionDefinitions = Collections.emptyList();
        } else {
            inputTransitionsSize = flowNode.getIncomingTransitions().size();
            allOutgoingTransitionDefinitions = new ArrayList<STransitionDefinition>(flowNode.getOutgoingTransitions());
        }
        // Evaluate all outgoing transitions, and retrieve valid outgoing transitions
        final List<STransitionDefinition> validOutgoingTransitionDefinitions = evaluateOutgoingTransitions(allOutgoingTransitionDefinitions,
                sProcessDefinition, child);

        final STransitionInstanceBuilder transitionInstanceBuilder = instanceBuilders.getSTransitionInstanceBuilder();
        final long rootProcessInstanceId = child.getLogicalGroup(transitionInstanceBuilder.getRootProcessInstanceIndex());

        final List<STransitionInstance> chosenTransitionInstances = new ArrayList<STransitionInstance>(validOutgoingTransitionDefinitions.size());
        final List<SFlowNodeDefinition> chosenActivityDefinitions = new ArrayList<SFlowNodeDefinition>(validOutgoingTransitionDefinitions.size());
        boolean consumeInputToken = false;
        Long outputTokenRefId = null;
        Long outputParentTokenRefId = null;
        boolean createToken = false;
        boolean isParalleleOrInclusive = false;
        boolean isExclusive = false;
        boolean implicitEnd = false;
        if (flowNode instanceof SGatewayDefinition) {
            final SGatewayType gatewayType = ((SGatewayDefinition) flowNode).getGatewayType();
            switch (gatewayType) {
                case EXCLUSIVE:
                    isExclusive = true;
                    break;
                case INCLUSIVE:
                    isParalleleOrInclusive = true;
                    break;
                case PARALLEL:
                    isParalleleOrInclusive = true;
                    break;
            }
        }
        if (SFlowNodeType.BOUNDARY_EVENT.equals(child.getType())) {
            if (validOutgoingTransitionDefinitions.size() == 0) {
                // no output nothing to do
                consumeInputToken = false;
                createToken = false;
                implicitEnd = false;
            } else {
                if (((SBoundaryEventDefinition) flowNode).isInterrupting()) {
                    // we create the same token that activated the activity
                    // the activity is canceled so a token will be consumed by the aborted activity
                    consumeInputToken = false;
                    createToken = true;
                    final SToken token = tokenService.getToken(sProcessInstance.getId(), child.getTokenRefId());
                    outputTokenRefId = token.getRefId();
                    outputParentTokenRefId = token.getParentRefId();
                } else {
                    // a token with no parent is produced -> not the same "execution" than activity
                    consumeInputToken = false;
                    createToken = true;
                    outputTokenRefId = child.getId();
                    outputParentTokenRefId = null;
                }
            }
        } else {

            if (validOutgoingTransitionDefinitions.size() > 0) {
                // depending on the split type and number of input/output transition we determine if token are created, with with type and if we consume
                // incoming
                // tokens
                if (isExclusive) {
                    // always transmit token
                    consumeInputToken = false;
                    outputTokenRefId = child.getTokenRefId();
                    createToken = false;
                } else {
                    if (inputTransitionsSize <= 1) {
                        if (allOutgoingTransitionDefinitions.size() <= 1) {
                            // 1 input , 1 output
                            // input token is transmit
                            consumeInputToken = false;
                            outputTokenRefId = child.getTokenRefId();
                            createToken = false;
                        } else {
                            // 1 input , >1 output
                            // create children input token
                            consumeInputToken = false;
                            outputTokenRefId = child.getId();
                            outputParentTokenRefId = child.getTokenRefId();
                            createToken = true;
                        }
                    } else {
                        if (allOutgoingTransitionDefinitions.size() <= 1) {
                            // >1 input , 1 output
                            // consume input and transmit parent token for parallele and inclusive
                            if (isParalleleOrInclusive) {
                                // consume input token + transmit parent token
                                consumeInputToken = true;
                                final SToken token = tokenService.getToken(sProcessInstance.getId(), child.getTokenRefId());
                                outputTokenRefId = token.getParentRefId();
                                createToken = false;
                            } else {
                                // implicit gateway: input token is transmit
                                consumeInputToken = false;
                                outputTokenRefId = child.getTokenRefId();
                                createToken = false;
                            }
                        } else {
                            // >1 input , >1 output
                            if (isParalleleOrInclusive) {
                                // consume input tokens and create children token having the same parent
                                consumeInputToken = true;
                                outputTokenRefId = child.getId();
                                // TODO get ParentTokenRefId
                                final SToken token = tokenService.getToken(sProcessInstance.getId(), child.getTokenRefId());
                                outputParentTokenRefId = token.getParentRefId();
                                createToken = true;
                            } else {
                                // implicit gateway: create children token of input
                                consumeInputToken = false;
                                outputTokenRefId = child.getId();
                                outputParentTokenRefId = child.getTokenRefId();
                                createToken = true;
                            }
                        }
                    }
                }
            } else {
                // implicit end
                implicitEnd = true;
                consumeInputToken = false;
                createToken = false;
            }
        }

        for (final STransitionDefinition sTransitionDefinition : allOutgoingTransitionDefinitions) {
            if (!validOutgoingTransitionDefinitions.contains(sTransitionDefinition)) {
                // Archive invalid transitions
                transitionService.archive(sTransitionDefinition, child, TransitionState.ABORTED);
            }
        }
        for (final STransitionDefinition sTransitionDefinition : validOutgoingTransitionDefinitions) {
            final SFlowNodeDefinition flowNodeDefinition = processDefinitionService.getNextFlowNode(sProcessDefinition, sTransitionDefinition.getName());
            if (flowNodeDefinition instanceof SActivityDefinition) {
                // If next flownode is a activity, archive transitionDef, create and execute flownode
                transitionService.archive(sTransitionDefinition, child, TransitionState.TAKEN);
                chosenActivityDefinitions.add(flowNodeDefinition);
            } else {
                // Else, create a transition, and execute it
                final STransitionInstanceBuilder newTransitionInstanceBuilder = transitionInstanceBuilder
                        .createNewInstance(sTransitionDefinition.getName(), rootProcessInstanceId, sProcessDefinition.getId(), parentProcessInstanceId)
                        .setSource(childId).setTokenRefId(outputTokenRefId);
                final STransitionInstance transitionInstance = newTransitionInstanceBuilder.done();
                transitionService.create(transitionInstance);
                chosenTransitionInstances.add(transitionInstance);
            }
        }

        if (childId != sProcessInstance.getInterruptingEventId() || SFlowNodeType.SUB_PROCESS.equals(sProcessInstance.getCallerType())) {
            // Let's archive the final state of the child:
            flowNodeExecutor.archiveFlowNodeInstance(child, true, sProcessDefinition);
        }

        // execute transition/activities
        createAndExecuteActivities(sProcessDefinition, child, parentProcessInstanceId, chosenActivityDefinitions, rootProcessInstanceId, outputTokenRefId);
        executeTransitions(sProcessDefinition, chosenTransitionInstances);

        // handle token creation/deletion
        if (consumeInputToken) {
            tokenService.deleteTokens(sProcessInstance.getId(), child.getTokenRefId(), numberOfTokenToMerge);
        }
        if (createToken) {
            tokenService.createTokens(sProcessInstance.getId(), outputTokenRefId, outputParentTokenRefId, validOutgoingTransitionDefinitions.size());
        }
        if (implicitEnd) {
            final Long tokenRefId = child.getTokenRefId();
            if (tokenRefId == null) {
                throw new SObjectNotFoundException("no token ref id set for " + child);
            }
            implcitEnd(sProcessDefinition, sProcessInstance.getId(), numberOfTokenToMerge, tokenRefId);
        }
        return tokenService.getNumberOfToken(sProcessInstance.getId());
    }

    /**
     * execute the implicit end of an element
     * 
     * @param numberOfTokenToMerge
     * @param tokenRefId
     * @param processInstanceId
     * @param processDefinition
     * @throws SBonitaException
     * @throws WorkRegisterException
     * @throws SGatewayModificationException
     */
    private void implcitEnd(final SProcessDefinition processDefinition, final long processInstanceId, final int numberOfTokenToMerge, final Long tokenRefId)
            throws SGatewayModificationException, WorkRegisterException, SBonitaException {
        final SToken token = tokenService.getToken(processInstanceId, tokenRefId);
        tokenService.deleteTokens(processInstanceId, tokenRefId, numberOfTokenToMerge);
        /*
         * if there is inclusive gateway we check if there is one that refers to this token
         * In this case the gateway should be notified in order to recalculate if it is now merged
         */
        if (processDefinition.getProcessContainer().containsInclusiveGateway()) {
            final SGatewayInstance gatewayInstance = gatewayInstanceService.getGatewayMergingToken(processInstanceId, tokenRefId);
            if (gatewayInstance != null && gatewayInstanceService.checkMergingCondition(processDefinition, gatewayInstance)) {
                gatewayInstanceService.setFinish(gatewayInstance);
                workService.registerWork(new ExecuteFlowNodeWork(this, gatewayInstance.getId(), null, null, processInstanceId));
            }
        }
        // consume on token parent if there is one and if there is no other token having the same refId
        if (token.getParentRefId() != null && tokenService.getNumberOfToken(processInstanceId, tokenRefId) == 0) {
            implcitEnd(processDefinition, processInstanceId, 1, token.getParentRefId());
        }

    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition sDefinition, final List<SOperation> operations, final Map<String, Object> context)
            throws SActivityReadException, SActivityExecutionFailedException, SActivityExecutionException, SActivityInterruptedException,
            SProcessInstanceCreationException {
        return start(userId, sDefinition, -1, null, operations, context, null, -1, -1);
    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition sDefinition, final SExpressionContext expressionContext,
            final List<SOperation> operations, final Map<String, Object> context) throws SActivityReadException, SActivityExecutionFailedException,
            SActivityExecutionException, SActivityInterruptedException, SProcessInstanceCreationException {
        return start(userId, sDefinition, -1, expressionContext, operations, context, null, -1, -1);
    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition sDefinition) throws SActivityReadException, SActivityExecutionFailedException,
            SActivityExecutionException, SActivityInterruptedException, SProcessInstanceCreationException {
        return start(userId, sDefinition, -41, null, null, null, null, -1, -1);
    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition sDefinition, final SExpressionContext expressionContext,
            final List<SOperation> operations, final Map<String, Object> context, final long callerId) throws SActivityReadException,
            SActivityExecutionFailedException, SActivityExecutionException, SActivityInterruptedException, SProcessInstanceCreationException {
        return start(userId, sDefinition, -1, expressionContext, operations, context, null, callerId, -1);
    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition processDefinition, final long targetSFlowNodeDefinitionId,
            final SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectors, final long callerId, final long subProcessDefinitionId)
            throws SProcessInstanceCreationException, SActivityReadException, SActivityExecutionException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            boolean txOpened;
            try {
                txOpened = transactionExecutor.openTransaction();
                final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("process", processDefinition.getId());
                Thread.currentThread().setContextClassLoader(localClassLoader);
                // initialize the process classloader by getting it one time
                try {
                    localClassLoader.loadClass(this.getClass().getName());
                } catch (final ClassNotFoundException e) {
                }
                try {
                    final SProcessInstance sProcessInstance = createProcessInstance(userId, processDefinition, callerId);

                    final SFlowElementContainerDefinition processContainer = getProcessContainer(processDefinition, subProcessDefinitionId);
                    final boolean isInitializing = initialize(userId, processDefinition, sProcessInstance, expressionContext,
                            operations != null ? new ArrayList<SOperation>(operations) : operations, context, processContainer, connectors);
                    handleEventSubProcess(processDefinition, sProcessInstance, subProcessDefinitionId);
                    if (isInitializing) {
                        // some connectors were trigger
                        processInstanceService.setState(sProcessInstance, ProcessInstanceState.INITIALIZING);
                        // we stop execution here
                        return sProcessInstance;
                    }
                    return startElements(processDefinition, sProcessInstance, subProcessDefinitionId, targetSFlowNodeDefinitionId);
                } catch (final SBonitaException e) {
                    transactionExecutor.setTransactionRollback();
                    throw new SProcessInstanceCreationException(e);
                } finally {
                    transactionExecutor.completeTransaction(txOpened);
                }
            } catch (final STransactionException e) {
                throw new SProcessInstanceCreationException(e);
            } catch (final ClassLoaderException e) {
                throw new SProcessInstanceCreationException(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public SProcessInstance start(final long userId, final SProcessDefinition processDefinition, final List<SOperation> operations,
            final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectorsWithInput) throws SActivityReadException,
            SActivityExecutionException, SActivityInterruptedException, SProcessInstanceCreationException {
        return start(userId, processDefinition, -1, null, operations, context, connectorsWithInput, -1, -1);
    }

    /*
     * Execute connectors then execute output operation and disconnect connectors
     */
    protected void executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sInstance,
            final List<ConnectorDefinitionWithInputValues> connectorsList) throws InvalidEvaluationConnectorConditionException, SBonitaException {
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinition.getId());
        expcontext.setProcessDefinition(processDefinition);
        expcontext.setContainerId(sInstance.getId());
        expcontext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
        for (final ConnectorDefinitionWithInputValues connectorWithInput : connectorsList) {
            final ConnectorDefinition connectorDefinition = connectorWithInput.getConnectorDefinition();
            final Map<String, Map<String, Serializable>> contextInputValues = connectorWithInput.getInputValues();
            final String connectorId = connectorDefinition.getConnectorId();
            final String version = connectorDefinition.getVersion();
            final Map<String, Expression> inputs = connectorDefinition.getInputs();
            if (contextInputValues.size() != inputs.size()) {
                throw new InvalidEvaluationConnectorConditionException(contextInputValues.size(), inputs.size());
            }
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(expressionBuilders, inputs);

            // we use the context classloader because the process classloader is already set
            final ConnectorResult result = connectorService.executeMutipleEvaluation(processDefinition.getId(), connectorId, version, connectorsExps,
                    contextInputValues, Thread.currentThread().getContextClassLoader(), expcontext);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            final List<SOperation> sOperations = new ArrayList<SOperation>(outputs.size());
            for (final Operation operation : outputs) {
                sOperations.add(ModelConvertor.constructSOperation(operation, operationBuilders, expressionBuilders));
            }
            connectorService.executeOutputOperation(sOperations, expcontext, result);
        }
    }

    protected SFlowElementContainerDefinition getProcessContainer(final SProcessDefinition processDefinition, final long subProcessDefinitionId) {
        SFlowElementContainerDefinition processContainer;
        if (subProcessDefinitionId == -1) {
            processContainer = processDefinition.getProcessContainer();
        } else {
            processContainer = ((SSubProcessDefinition) processDefinition.getProcessContainer().getFlowNode(subProcessDefinitionId)).getSubProcessContainer();
        }
        return processContainer;
    }

    protected void handleEventSubProcess(final SProcessDefinition processDefinition, final SProcessInstance sInstance, final long subProcessDefinitionId)
            throws SActivityExecutionException {
        if (subProcessDefinitionId == -1) {
            try {
                // modify that to support event sub-processes within sub-processes
                eventsHandler.handleEventSubProcess(processDefinition, sInstance);
            } catch (final SBonitaException e) {
                throw new SActivityExecutionException("Unable to register events for event sub process in process " + processDefinition.getName() + " "
                        + processDefinition.getVersion(), e);
            }
        }
    }

    @Override
    public SProcessInstance startElements(final SProcessDefinition sDefinition, final SProcessInstance sInstance) throws SActivityReadException,
            SProcessInstanceCreationException, SActivityExecutionException {
        return startElements(sDefinition, sInstance, -1, -1);
    }

    @Override
    public SProcessInstance startElements(final SProcessDefinition sDefinition, final SProcessInstance sInstance, final long subProcessDefinitionId,
            final long targetSFlowNodeDefinitionId) throws SActivityReadException, SProcessInstanceCreationException, SActivityExecutionException {
        List<SFlowNodeInstance> flowNodeInstances = initializeFirstExecutableElements(sInstance, sDefinition, subProcessDefinitionId,
                targetSFlowNodeDefinitionId);
        // process is initialized and now the engine trigger jobs to execute other activities, give the hand back
        ProcessInstanceState state;
        final int size = flowNodeInstances.size();
        if (size == 0) {
            state = ProcessInstanceState.COMPLETED;
        } else {
            state = ProcessInstanceState.STARTED;
        }
        try {
            tokenService.createTokens(sInstance.getId(), sInstance.getProcessDefinitionId(), null, size);
            processInstanceService.setState(sInstance, state);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to set the state on the process", e);
        }
        for (final SFlowNodeInstance flowNode : flowNodeInstances) {
            try {
                workService.registerWork(new ExecuteFlowNodeWork(flowNodeExecutor, flowNode.getId(), null, null, sInstance.getId()));
            } catch (final WorkRegisterException e) {
                throw new SActivityExecutionException("unable to trigger execution of flow node " + flowNode, e);
            }
        }
        return sInstance;
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.PROCESS.name();
    }

    private void createAndExecuteActivities(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance flowNodeInstance,
            final long parentProcessInstanceId, final List<SFlowNodeDefinition> choosenActivityDefinitions, final long rootProcessInstanceId,
            final Long tokenRefId) throws SBonitaException {
        final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        final SStateCategory stateCategory = parentProcessInstance.getStateCategory();

        // Create Activities
        final List<SFlowNodeInstance> sFlowNodeInstances = bpmInstancesCreator.createFlowNodeInstances(sProcessDefinition,
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId(), choosenActivityDefinitions, rootProcessInstanceId,
                parentProcessInstanceId, stateCategory, tokenRefId);

        // Execute Activities
        for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
            workService.registerWork(new ExecuteFlowNodeWork(this, sFlowNodeInstance.getId(), null, null, parentProcessInstanceId));
        }
    }
}
