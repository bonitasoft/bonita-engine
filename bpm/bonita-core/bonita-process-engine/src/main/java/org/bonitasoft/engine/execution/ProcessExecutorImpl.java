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
 */
package org.bonitasoft.engine.execution;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.bpm.contract.validation.ContractValidator;
import org.bonitasoft.engine.bpm.contract.validation.ContractValidatorFactory;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataCreationException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentDefinition;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayDefinition;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.work.SWorkRegisterException;
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

    protected final ActivityInstanceService activityInstanceService;
    protected final ProcessInstanceService processInstanceService;
    protected final ClassLoaderService classLoaderService;
    protected final ExpressionResolverService expressionResolverService;
    protected final ExpressionService expressionService;
    protected final ConnectorService connectorService;
    protected final BPMInstancesCreator bpmInstancesCreator;
    protected final EventsHandler eventsHandler;
    private final FlowNodeExecutor flowNodeExecutor;
    private final TechnicalLoggerService logger;
    private final WorkService workService;
    private final ProcessDefinitionService processDefinitionService;
    private final GatewayInstanceService gatewayInstanceService;
    private final OperationService operationService;
    private ProcessResourcesService processResourcesService;
    private final ConnectorInstanceService connectorInstanceService;
    private final TransitionEvaluator transitionEvaluator;
    private final ContractDataService contractDataService;
    private final BusinessDataRepository businessDataRepository;
    private final RefBusinessDataService refBusinessDataService;
    private final DocumentHelper documentHelper;
    private final BPMWorkFactory workFactory;

    public ProcessExecutorImpl(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
                               final TechnicalLoggerService logger, final FlowNodeExecutor flowNodeExecutor, final WorkService workService,
                               final ProcessDefinitionService processDefinitionService, final GatewayInstanceService gatewayInstanceService,
                               final ProcessResourcesService processResourcesService, final ConnectorService connectorService,
                               final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final OperationService operationService,
                               final ExpressionResolverService expressionResolverService, final ExpressionService expressionService, final EventService eventService,
                               final Map<String, SProcessInstanceHandler<SEvent>> handlers, final DocumentService documentService,
                               final ContainerRegistry containerRegistry, final BPMInstancesCreator bpmInstancesCreator,
                               final EventsHandler eventsHandler, final FlowNodeStateManager flowNodeStateManager, final BusinessDataRepository businessDataRepository,
                               final RefBusinessDataService refBusinessDataService, final TransitionEvaluator transitionEvaluator, final ContractDataService contractDataService, BPMWorkFactory workFactory) {
        super();
        this.activityInstanceService = activityInstanceService;
        this.processInstanceService = processInstanceService;
        this.processResourcesService = processResourcesService;
        this.connectorInstanceService = connectorInstanceService;
        this.logger = logger;
        this.flowNodeExecutor = flowNodeExecutor;
        this.workService = workService;
        this.processDefinitionService = processDefinitionService;
        this.gatewayInstanceService = gatewayInstanceService;
        this.connectorService = connectorService;
        this.classLoaderService = classLoaderService;
        this.operationService = operationService;
        this.expressionResolverService = expressionResolverService;
        this.expressionService = expressionService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.eventsHandler = eventsHandler;
        this.transitionEvaluator = transitionEvaluator;
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
        this.contractDataService = contractDataService;
        this.workFactory = workFactory;
        documentHelper = new DocumentHelper(documentService, processDefinitionService, processInstanceService);
        // dependency injection because of circular references...
        flowNodeStateManager.setProcessExecutor(this);
        eventsHandler.setProcessExecutor(this);
        for (final Entry<String, SProcessInstanceHandler<SEvent>> handler : handlers.entrySet()) {
            try {
                eventService.addHandler(handler.getKey(), handler.getValue());
            } catch (final HandlerRegistrationException e) {
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
                }
                if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            }
        }
        containerRegistry.addContainerExecutor(this);
    }

    @Override
    public FlowNodeState executeFlowNode(final long flowNodeInstanceId,
                                         final Long executerId, final Long executerSubstituteId) throws SFlowNodeExecutionException {
        return flowNodeExecutor.stepForward(flowNodeInstanceId, executerId, executerSubstituteId);
    }

    private SConnectorInstance getNextConnectorInstance(final SProcessInstance processInstance, final ConnectorEvent event)
            throws SConnectorInstanceReadException {
        final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(processInstance.getId(),
                SConnectorInstance.PROCESS_TYPE, event, 0, 1, ConnectorService.TO_BE_EXECUTED);
        return connectorInstances.size() == 1 ? connectorInstances.get(0) : null;
    }

    @Override
    public boolean executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sProcessInstance, final ConnectorEvent activationEvent,
                                     final FlowNodeSelector selectorForConnectorOnEnter) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        final long processDefinitionId = processDefinition.getId();
        final List<SConnectorDefinition> connectors = processContainer.getConnectors(activationEvent);
        if (connectors.size() > 0) {
            SConnectorInstance nextConnectorInstance;
            nextConnectorInstance = getNextConnectorInstance(sProcessInstance, activationEvent);
            if (nextConnectorInstance != null) {
                for (final SConnectorDefinition sConnectorDefinition : connectors) {
                    if (sConnectorDefinition.getName().equals(nextConnectorInstance.getName())) {
                        workService.registerWork(workFactory.createExecuteConnectorOfProcessDescriptor(processDefinitionId, sProcessInstance.getId(),
                                sProcessInstance.getRootProcessInstanceId(), nextConnectorInstance.getId(), sConnectorDefinition.getName(), activationEvent,
                                selectorForConnectorOnEnter));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<SFlowNodeInstance> initializeFirstExecutableElements(final SProcessInstance sProcessInstance, final FlowNodeSelector selector) {
        try {
            final List<SFlowNodeDefinition> flownNodeDefinitions = selector.getFilteredElements();
            long rootProcessInstanceId = sProcessInstance.getRootProcessInstanceId();
            if (rootProcessInstanceId <= 0) {
                rootProcessInstanceId = sProcessInstance.getId();
            }
            return bpmInstancesCreator.createFlowNodeInstances(selector.getProcessDefinition().getId(), rootProcessInstanceId, sProcessInstance.getId(),
                    flownNodeDefinitions, rootProcessInstanceId, sProcessInstance.getId(), SStateCategory.NORMAL);
        } catch (final SBonitaException e) {
            setExceptionContext(selector.getProcessDefinition(), sProcessInstance, e);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw new BonitaRuntimeException(e);
        }
    }

    private SProcessInstance createProcessInstance(final SProcessDefinition sDefinition, final long starterId, final long starterSubstituteId,
                                                   final long callerId, final SFlowNodeType callerType, final long rootProcessInstanceId) throws SProcessInstanceCreationException {
        final SProcessInstanceBuilder processInstanceBuilder = BuilderFactory.get(SProcessInstanceBuilderFactory.class).createNewInstance(sDefinition)
                .setStartedBy(starterId).setStartedBySubstitute(starterSubstituteId).setCallerId(callerId, callerType)
                .setRootProcessInstanceId(rootProcessInstanceId);
        final SProcessInstance sProcessInstance = processInstanceBuilder.done();
        processInstanceService.createProcessInstance(sProcessInstance);
        return sProcessInstance;
    }

    protected SProcessInstance createProcessInstance(final SProcessDefinition processDefinition, final long starterId, final long starterSubstituteId,
            final long callerId) throws SProcessInstanceCreationException {
        SActivityInstance callerInstance;
        try {
            callerInstance = getCaller(callerId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to get caller.", e);
        }

        if (callerInstance != null) {
            return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId, callerInstance.getType(),
                    callerInstance.getRootContainerId());
        }
        return createProcessInstance(processDefinition, starterId, starterSubstituteId, callerId, null, -1);
    }

    private SActivityInstance getCaller(final long callerId) throws SActivityReadException, SActivityInstanceNotFoundException {
        if (callerId > 0) {
            return activityInstanceService.getActivityInstance(callerId);
        }
        return null;
    }

    /*
     * this method is called when a flow node having a transition that goes to a gateway is finished
     * it get the active gateway pointed by this transition, update the tokens of this gateway and execute it if merged
     */
    private void executeGateway(final SProcessDefinition sProcessDefinition, final STransitionDefinition sTransitionDefinition,
            final SFlowNodeInstance flowNodeThatTriggeredTheTransition) throws SBonitaException {
        final long parentProcessInstanceId = flowNodeThatTriggeredTheTransition.getParentProcessInstanceId();
        final long rootProcessInstanceId = flowNodeThatTriggeredTheTransition.getRootProcessInstanceId();
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinitionService.getNextFlowNode(sProcessDefinition, String.valueOf(sTransitionDefinition.getId()));
        try {
            List<SGatewayInstance> gatewaysToExecute = new ArrayList<>(1);
            final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
            final SStateCategory stateCategory = parentProcessInstance.getStateCategory();
            final SGatewayInstance gatewayInstance = getActiveGatewayOrCreateIt(sProcessDefinition, sFlowNodeDefinition, stateCategory,
                    parentProcessInstanceId,
                    rootProcessInstanceId);
            gatewayInstanceService.hitTransition(gatewayInstance, sFlowNodeDefinition.getTransitionIndex(sTransitionDefinition.getId()));
            if (gatewayInstanceService.checkMergingCondition(sProcessDefinition, gatewayInstance)) {
                gatewaysToExecute.add(gatewayInstance);
                gatewaysToExecute.addAll(gatewayInstanceService.setFinishAndCreateNewGatewayForRemainingToken(sProcessDefinition, gatewayInstance));
            }
            for (final SGatewayInstance gatewayToExecute : gatewaysToExecute) {
                executeFlowNode(gatewayToExecute.getId(), null, null);
            }
        } catch (final SBonitaException e) {
            setExceptionContext(sProcessDefinition, flowNodeThatTriggeredTheTransition, e);
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            throw e;
        }
    }

    /*
     * try to gate active gateway.
     * if the gateway is already hit by this transition or by the same token, we create a new gateway
     */
    SGatewayInstance getActiveGatewayOrCreateIt(final SProcessDefinition sProcessDefinition, final SFlowNodeDefinition flowNodeDefinition,
            final SStateCategory stateCategory, final long parentProcessInstanceId, final long rootProcessInstanceId) throws SBonitaException {
        SGatewayInstance gatewayInstance = gatewayInstanceService.getActiveGatewayInstanceOfTheProcess(parentProcessInstanceId, flowNodeDefinition.getName());
        if (gatewayInstance == null) {
            // no gateway found we create one
            gatewayInstance = createGateway(sProcessDefinition.getId(), flowNodeDefinition, stateCategory, parentProcessInstanceId, rootProcessInstanceId);
        }
        return gatewayInstance;
    }

    private SGatewayInstance createGateway(final Long processDefinitionId, final SFlowNodeDefinition flowNodeDefinition, final SStateCategory stateCategory,
            final long parentProcessInstanceId, final long rootProcessInstanceId) throws SBonitaException {
        return (SGatewayInstance) bpmInstancesCreator
                .createFlowNodeInstance(processDefinitionId, rootProcessInstanceId, parentProcessInstanceId, SFlowElementsContainerType.PROCESS,
                        flowNodeDefinition, rootProcessInstanceId, parentProcessInstanceId, false, 0, stateCategory, -1);
    }

    protected void executeOperations(final List<SOperation> operations, final Map<String, Object> context, SExpressionContext expressionContext,
            final SExpressionContext expressionContextToEvaluateOperations,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        if (operations != null && !operations.isEmpty()) {
            SExpressionContext currentExpressionContext = expressionContextToEvaluateOperations != null ? expressionContextToEvaluateOperations
                    : expressionContext;
            currentExpressionContext.setInputValues(context);
            if (currentExpressionContext.getContainerId() == null) {
                currentExpressionContext.setContainerId(sProcessInstance.getId());
                currentExpressionContext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
            }
            operationService.execute(new ArrayList<>(operations), sProcessInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                    currentExpressionContext);
        }
    }

    protected boolean initialize(final long userId, final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            SExpressionContext expressionContextToEvaluateOperations, List<SOperation> operations, final Map<String, Object> context,
            final SFlowElementContainerDefinition processContainer, final List<ConnectorDefinitionWithInputValues> connectors,
            final FlowNodeSelector selectorForConnectorOnEnter, final Map<String, Serializable> processInputs) throws BonitaHomeNotSetException, IOException,
            InvalidEvaluationConnectorConditionException, SBonitaException {

        SExpressionContext expressionContext = createExpressionsContextForProcessInstance(sProcessDefinition, sProcessInstance);
        operations = operations != null ? new ArrayList<>(operations) : Collections.<SOperation> emptyList();

        storeProcessInstantiationInputs(sProcessInstance.getId(), processInputs);

        // Create SDataInstances
        bpmInstancesCreator.createDataInstances(sProcessInstance, processContainer, sProcessDefinition, expressionContext, operations, context,
                expressionContextToEvaluateOperations);

        initializeBusinessData(processContainer, sProcessInstance, expressionContext);
        initializeData(processContainer, sProcessDefinition, sProcessInstance);

        createDocuments(sProcessDefinition, processContainer, sProcessInstance, userId, expressionContext, context);
        createDocumentLists(processContainer, sProcessInstance, userId, expressionContext, context);
        if (connectors != null) {
            //these are set only when start process through the command ExecuteActionsAndStartInstanceExt
            executeConnectors(sProcessDefinition, sProcessInstance, connectors);
        }
        // oepration given to the startProcess method of the API or by command, not operations of the process definition
        executeOperations(operations, context, expressionContext, expressionContextToEvaluateOperations, sProcessInstance);

        // Create connectors
        bpmInstancesCreator.createConnectorInstances(sProcessInstance, processContainer.getConnectors(), SConnectorInstance.PROCESS_TYPE);

        return executeConnectors(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_ENTER, selectorForConnectorOnEnter);
    }

    private SExpressionContext createExpressionsContextForProcessInstance(SProcessDefinition sProcessDefinition, SProcessInstance sProcessInstance) {
        SExpressionContext expressionContext = new SExpressionContext();
        expressionContext.setProcessDefinitionId(sProcessDefinition.getId());
        expressionContext.setContainerId(sProcessInstance.getId());
        expressionContext.setContainerType(DataInstanceContainer.PROCESS_INSTANCE.name());
        return expressionContext;
    }

    private void storeProcessInstantiationInputs(final long processInstanceId, final Map<String, Serializable> processInputs)
            throws SContractDataCreationException {
        contractDataService.addProcessData(processInstanceId, processInputs);
    }

    protected void initializeData(final SFlowElementContainerDefinition processContainer, SProcessDefinition sProcessDefinition, final SProcessInstance sInstance) throws SProcessInstanceCreationException {
        // nothing to do
    }

    private void initializeBusinessData(SFlowElementContainerDefinition processContainer, final SProcessInstance sInstance,
            final SExpressionContext expressionContext)
            throws SBonitaException {
        final List<SBusinessDataDefinition> businessDataDefinitions = processContainer.getBusinessDataDefinitions();
        for (final SBusinessDataDefinition bdd : businessDataDefinitions) {
            final SExpression expression = bdd.getDefaultValueExpression();
            if (bdd.isMultiple()) {
                final List<Long> dataIds = initializeMultipleBusinessDataIds(expressionContext, expression);
                final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory.get(SRefBusinessDataInstanceBuilderFactory.class);
                final SRefBusinessDataInstance instance = instanceFactory.createNewInstance(bdd.getName(), sInstance.getId(), dataIds, bdd.getClassName())
                        .done();
                refBusinessDataService.addRefBusinessDataInstance(instance);
            } else {
                final Long primaryKey = initializeSingleBusinessData(expressionContext, expression);
                final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory.get(SRefBusinessDataInstanceBuilderFactory.class);
                final SRefBusinessDataInstance instance = instanceFactory.createNewInstance(bdd.getName(), sInstance.getId(), primaryKey,
                        bdd.getClassName())
                        .done();
                refBusinessDataService.addRefBusinessDataInstance(instance);
            }
        }
    }

    private Long initializeSingleBusinessData(final SExpressionContext expressionContext, final SExpression expression) throws SBonitaException {
        Long primaryKey = null;
        if (expression != null) {
            final Entity businessData = (Entity) expressionResolverService.evaluate(expression, expressionContext);
            primaryKey = saveBusinessData(businessData);
        }
        return primaryKey;
    }

    private List<Long> initializeMultipleBusinessDataIds(final SExpressionContext expressionContext, final SExpression expression) throws SBonitaException {
        final List<Long> dataIds = new ArrayList<>();
        if (expression != null) {
            final List<Entity> businessData = (List<Entity>) expressionResolverService.evaluate(expression, expressionContext);
            for (final Entity entity : businessData) {
                dataIds.add(saveBusinessData(entity));
            }
        }
        return dataIds;
    }

    private Long saveBusinessData(final Entity entity) throws SObjectCreationException {
        try {
            final Entity mergedBusinessData = businessDataRepository.merge(ServerProxyfier.unProxifyIfNeeded(entity));
            if (mergedBusinessData == null) {
                return null;
            }
            return mergedBusinessData.getPersistenceId();
        } catch (IllegalArgumentException e) {
            throw new SObjectCreationException("Unable to save the business data", e);
        }
    }

    private void createDocuments(final SProcessDefinition sDefinition, SFlowElementContainerDefinition processContainer,
            final SProcessInstance sProcessInstance, final long authorId,
            final SExpressionContext expressionContext, final Map<String, Object> context)
            throws SObjectCreationException, BonitaHomeNotSetException, STenantIdNotSetException, IOException, SObjectAlreadyExistsException,
            SBonitaReadException, SObjectModificationException, SExpressionTypeUnknownException, SExpressionDependencyMissingException,
            SExpressionEvaluationException, SInvalidExpressionException, SOperationExecutionException {
        final List<SDocumentDefinition> documentDefinitions = processContainer.getDocumentDefinitions();
        final Map<SExpression, DocumentValue> evaluatedDocumentValues = evaluateInitialExpressionsOfDocument(sProcessInstance, expressionContext, context,
                documentDefinitions);
        if (!documentDefinitions.isEmpty()) {
            for (final SDocumentDefinition document : documentDefinitions) {
                final DocumentValue documentValue = getInitialDocumentValue(sDefinition, evaluatedDocumentValues, document);
                if (documentValue != null) {
                    documentHelper.createOrUpdateDocument(documentValue,
                            document.getName(), sProcessInstance.getId(), authorId, document.getDescription());
                }
            }
        }
    }

    protected DocumentValue getInitialDocumentValue(final SProcessDefinition sDefinition, final Map<SExpression, DocumentValue> evaluatedDocumentValues,
            final SDocumentDefinition document) throws BonitaHomeNotSetException, IOException, STenantIdNotSetException, SBonitaReadException {
        DocumentValue documentValue = null;
        if (document.getInitialValue() != null) {
            documentValue = evaluatedDocumentValues.get(document.getInitialValue());
        } else if (document.getFile() != null) {
            final byte[] content = getProcessDocumentContent(sDefinition, document);
            documentValue = new DocumentValue(content, document.getMimeType(), document.getFileName());
        } else if (document.getUrl() != null) {
            documentValue = new DocumentValue(document.getUrl());
            documentValue.setFileName(document.getFileName());
            documentValue.setMimeType(document.getMimeType());
        }
        return documentValue;
    }

    byte[] getProcessDocumentContent(final SProcessDefinition sDefinition, final SDocumentDefinition document) throws BonitaHomeNotSetException, IOException,
            STenantIdNotSetException, SBonitaReadException {
        final String file = document.getFile();// should always exists...validation on BusinessArchive
        return processResourcesService.get(sDefinition.getId(), BARResourceType.DOCUMENT, file).getContent();
    }

    private Map<SExpression, DocumentValue> evaluateInitialExpressionsOfDocument(final SProcessInstance processInstance,
            final SExpressionContext expressionContext,
            final Map<String, Object> context, final List<SDocumentDefinition> documentDefinitions) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException, SOperationExecutionException {
        final List<SExpression> initialValuesExpressions = new ArrayList<>(documentDefinitions.size());
        final Map<SExpression, DocumentValue> evaluatedDocumentValue = new HashMap<>();
        for (final SDocumentDefinition documentDefinition : documentDefinitions) {
            if (documentDefinition.getInitialValue() != null) {
                initialValuesExpressions.add(documentDefinition.getInitialValue());
            }
        }
        final List<Object> evaluate = expressionResolverService.evaluate(initialValuesExpressions,
                getsExpressionContext(processInstance, expressionContext, context));
        for (int i = 0; i < initialValuesExpressions.size(); i++) {
            evaluatedDocumentValue.put(initialValuesExpressions.get(i), documentHelper.toCheckedDocumentValue(evaluate.get(i)));
        }
        return evaluatedDocumentValue;
    }

    private void createDocumentLists(SFlowElementContainerDefinition processContainer, final SProcessInstance processInstance, final long authorId,
            final SExpressionContext expressionContext, final Map<String, Object> context)
            throws SBonitaException {
        final List<SDocumentListDefinition> documentListDefinitions = processContainer.getDocumentListDefinitions();
        if (!documentListDefinitions.isEmpty()) {
            final List<Object> initialValues = evaluateInitialExpressionsOfDocumentLists(processInstance, expressionContext, context, documentListDefinitions);
            for (int i = 0; i < documentListDefinitions.size(); i++) {
                final Object newValue = initialValues.get(i);
                if (newValue == null) {
                    continue;
                }
                documentHelper.setDocumentList(
                        documentHelper.toCheckedList(newValue), documentListDefinitions.get(i).getName(), processInstance.getId(), authorId);
            }
        }
    }

    private List<Object> evaluateInitialExpressionsOfDocumentLists(final SProcessInstance processInstance, final SExpressionContext expressionContext,
            final Map<String, Object> context, final List<SDocumentListDefinition> documentListDefinitions) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException {
        final List<SExpression> initialValuesExpressions = new ArrayList<>(documentListDefinitions.size());
        for (final SDocumentListDefinition documentList : documentListDefinitions) {
            initialValuesExpressions.add(documentList.getExpression());
        }
        final SExpressionContext currentExpressionContext = getsExpressionContext(processInstance, expressionContext, context);
        return expressionResolverService.evaluate(initialValuesExpressions, currentExpressionContext);
    }

    private SExpressionContext getsExpressionContext(final SProcessInstance processInstance, final SExpressionContext expressionContext,
            final Map<String, Object> context) {
        SExpressionContext currentExpressionContext;
        if (expressionContext != null) {
            expressionContext.setInputValues(context);
            currentExpressionContext = expressionContext;
        } else {
            currentExpressionContext = new SExpressionContext(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                    processInstance.getProcessDefinitionId());
            currentExpressionContext.setInputValues(context);
        }
        return currentExpressionContext;
    }

    @Override
    public void childFinished(long processDefinitionId, long parentId, SFlowNodeInstance sFlowNodeInstanceChild) throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SUserTaskInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);
        final long processInstanceId = sFlowNodeInstanceChild.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex());

        SProcessInstance sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
        final boolean isEnd = executeValidOutgoingTransitionsAndUpdateTokens(sProcessDefinition, sFlowNodeInstanceChild, sProcessInstance);
        logger.log(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG, "The flow node <" + sFlowNodeInstanceChild.getName() + "> with id<"
                + sFlowNodeInstanceChild.getId() + "> of process instance <" + processInstanceId + "> finished");
        if (isEnd) {
            int numberOfFlowNode = activityInstanceService.getNumberOfFlowNodes(sProcessInstance.getId());
            if (sProcessInstance.getInterruptingEventId() > 0) {
                ///the event is deleted by latter...
                numberOfFlowNode -= 1;
            }
            if (numberOfFlowNode > 0) {
                if (logger.isLoggable(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG)) {

                    logger.log(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG, "The process instance <" + processInstanceId + "> from definition <"
                            + sProcessDefinition.getName() + ":" + sProcessDefinition.getVersion()
                            + "> executed a branch that is finished but there is still <"
                            + numberOfFlowNode + "> to execute");
                    logger.log(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG,
                            activityInstanceService.getFlowNodeInstances(processInstanceId, 0, numberOfFlowNode).toString());
                }
                return;
            }
            logger.log(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG, "The process instance <" + processInstanceId + "> from definition <"
                    + sProcessDefinition.getName() + ":" + sProcessDefinition.getVersion() + "> finished");
            boolean hasActionsToExecute = false;
            if (ProcessInstanceState.ABORTING.getId() != sProcessInstance.getStateId()) {
                hasActionsToExecute = executePostThrowEventHandlers(sProcessDefinition, sProcessInstance, sFlowNodeInstanceChild);
                // the process instance has maybe changed
                logger.log(ProcessExecutorImpl.class, TechnicalLogSeverity.DEBUG, "has action to execute");
                if (hasActionsToExecute) {
                    sProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
                }
                eventsHandler.unregisterEventSubProcess(sProcessDefinition, sProcessInstance);
            }
            handleProcessCompletion(sProcessDefinition, sProcessInstance, hasActionsToExecute);
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
                    if (executeConnectors(sProcessDefinition, sProcessInstance, ConnectorEvent.ON_FINISH, null)) {
                        // some connectors were trigger
                        processInstanceState = ProcessInstanceState.COMPLETING;
                    } else {
                        processInstanceState = ProcessInstanceState.COMPLETED;
                    }
                }
                break;
        }
        processInstanceService.setState(sProcessInstance, processInstanceState);
        flowNodeExecutor.childReachedState(sProcessInstance, processInstanceState, hasActionsToExecute);

    }

    private boolean executePostThrowEventHandlers(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            final SFlowNodeInstance child) throws SBonitaException {
        boolean hasActionsToExecute = false;
        if (sProcessInstance.hasBeenInterruptedByEvent()) {
            final SFlowNodeInstance endEventInstance = activityInstanceService.getFlowNodeInstance(sProcessInstance.getInterruptingEventId());
            final SEndEventDefinition endEventDefinition = (SEndEventDefinition) sProcessDefinition.getProcessContainer().getFlowNode(
                    endEventInstance.getFlowNodeDefinitionId());
            hasActionsToExecute = eventsHandler.handlePostThrowEvent(sProcessDefinition, endEventDefinition, (SThrowEventInstance) endEventInstance, child);
            flowNodeExecutor.archiveFlowNodeInstance(endEventInstance, true, sProcessDefinition.getId());
        }
        return hasActionsToExecute;
    }

    /**
     * Evaluate the split of the element
     * The element contains the current token it received
     *
     * @return number of token of the process
     */
    private boolean executeValidOutgoingTransitionsAndUpdateTokens(final SProcessDefinition processDefinition, final SFlowNodeInstance child,
            final SProcessInstance sProcessInstance) throws SBonitaException {
        // token we merged
        final SFlowNodeDefinition sFlowNodeDefinition = processDefinition.getProcessContainer().getFlowNode(child.getFlowNodeDefinitionId());
        final FlowNodeTransitionsWrapper transitionsDescriptor = transitionEvaluator.buildTransitionsWrapper(sFlowNodeDefinition, processDefinition, child);

        List<STransitionDefinition> chosenGatewaysTransitions = new ArrayList<>(transitionsDescriptor
                .getValidOutgoingTransitionDefinitions().size());
        final List<SFlowNodeDefinition> chosenFlowNode = new ArrayList<>(transitionsDescriptor.getValidOutgoingTransitionDefinitions()
                .size());
        for (final STransitionDefinition sTransitionDefinition : transitionsDescriptor.getValidOutgoingTransitionDefinitions()) {
            final SFlowNodeDefinition flowNodeDefinition = processDefinitionService.getNextFlowNode(processDefinition, String.valueOf(sTransitionDefinition.getId()));
            if (flowNodeDefinition instanceof SGatewayDefinition) {
                chosenGatewaysTransitions.add(sTransitionDefinition);
            } else {
                // Shortcut: event or activity, we execute them directly
                chosenFlowNode.add(flowNodeDefinition);
            }
        }

        archiveFlowNodeInstance(processDefinition, child, sProcessInstance);

        final long processInstanceId = sProcessInstance.getId();
        createAndExecuteActivities(processDefinition.getId(), child, processInstanceId, chosenFlowNode, child.getRootProcessInstanceId());

        //test to check the particular case of an inclusive gateway receiving transitions from the same flownode.
        //if that's the case only one should be executed.
        removeDuplicatedInclusiveGatewayTransitions(processDefinition, chosenGatewaysTransitions);

        // execute transition/activities
        for (final STransitionDefinition sTransitionDefinition : chosenGatewaysTransitions) {
            executeGateway(processDefinition, sTransitionDefinition, child);
        }

        if (processDefinition.getProcessContainer().containsInclusiveGateway() && needToReevaluateInclusiveGateways(transitionsDescriptor)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "some branches died, will check again all inclusive gateways");
            final List<SGatewayInstance> inclusiveGatewaysOfProcessInstance = gatewayInstanceService.getInclusiveGatewaysOfProcessInstanceThatShouldFire(
                    processDefinition, processInstanceId);
            List<SGatewayInstance> gatewaysToExecute = new ArrayList<>(inclusiveGatewaysOfProcessInstance);
            for (final SGatewayInstance gatewayInstance : inclusiveGatewaysOfProcessInstance) {
                gatewaysToExecute.addAll(gatewayInstanceService.setFinishAndCreateNewGatewayForRemainingToken(processDefinition,
                        gatewayInstance));
            }
            for (final SGatewayInstance gatewayToExecute : gatewaysToExecute) {
                executeFlowNode(gatewayToExecute.getId(), null, null);
            }

        }
        return transitionsDescriptor.isLastFlowNode();
    }

    protected void removeDuplicatedInclusiveGatewayTransitions(SProcessDefinition processDefinition, List<STransitionDefinition> chosenGatewaysTransitions) {
        List<STransitionDefinition> transitionToRemove = new ArrayList<>();
        Set<SGatewayDefinition> gateways = new HashSet<>();
        for (STransitionDefinition gatewaysTransition : chosenGatewaysTransitions) {
            SGatewayDefinition gateway = getGateway(gatewaysTransition, processDefinition);
            if(isInclusiveGateway(gateway)){
                boolean alreadyExists = !gateways.add(gateway);
                if(alreadyExists){
                    transitionToRemove.add(gatewaysTransition);
                }
            }
        }
        chosenGatewaysTransitions.removeAll(transitionToRemove);
    }
    
    private boolean isInclusiveGateway(SGatewayDefinition gateway) {
        return gateway.getGatewayType() == SGatewayType.INCLUSIVE;
    }
    
    private SGatewayDefinition getGateway(STransitionDefinition gatewaysTransition, SProcessDefinition processDefinition) {
        return (SGatewayDefinition) processDefinition.getProcessContainer().getFlowNode(gatewaysTransition.getTarget());
    }

    private void archiveFlowNodeInstance(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance child, final SProcessInstance sProcessInstance)
            throws SArchivingException {
        //FIXME we archive the flow node instance here because it was not archived before because the flow node was interrupting the parent.. we should change that because it's not very easy to see how it works
        if (child.getId() != sProcessInstance.getInterruptingEventId() || SFlowNodeType.SUB_PROCESS.equals(sProcessInstance.getCallerType())) {
            // Let's archive the final state of the child:
            flowNodeExecutor.archiveFlowNodeInstance(child, true, sProcessDefinition.getId());
        }
    }

    private boolean needToReevaluateInclusiveGateways(final FlowNodeTransitionsWrapper transitionsDescriptor) {
        final int allOutgoingTransitions = transitionsDescriptor.getNonDefaultOutgoingTransitionDefinitions().size()
                + (transitionsDescriptor.getDefaultTransition() != null ? 1 : 0);
        final int takenTransition = transitionsDescriptor.getValidOutgoingTransitionDefinitions().size();
        /*
         * Why this condition?
         * If a gateway was blocked because it was waiting for a token to come it will not be unblock when all transitions
         * are taken but only if some transitions are not.
         * In conclusion if all declared transition are not taken it means that a 'branch died' and that some inclusive gateways might be triggered so the
         * reevaluation is needed
         */
        return takenTransition < allOutgoingTransitions;
    }

    @Override
    public SProcessInstance start(final long starterId, final long starterSubstituteId, final List<SOperation> operations, final Map<String, Object> context,
            final List<ConnectorDefinitionWithInputValues> connectorsWithInput, final FlowNodeSelector selector, final Map<String, Serializable> processInputs)
            throws SProcessInstanceCreationException, SContractViolationException {
        return start(starterId, starterSubstituteId, null, operations, context, connectorsWithInput, -1, selector, processInputs);
    }

    @Override
    /* Started by call activity and events, operations must be evaluated using the given context */
    public SProcessInstance start(final long processDefinitionId, final long targetSFlowNodeDefinitionId, final long starterId, final long starterSubstituteId,
            final SExpressionContext expressionContextToEvaluateOperations, final List<SOperation> operations,
            final long callerId, final long subProcessDefinitionId,
            final Map<String, Serializable> processInputs) throws SProcessInstanceCreationException, SContractViolationException {
        try {
            final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final FlowNodeSelector selector = new FlowNodeSelector(sProcessDefinition, getFilter(targetSFlowNodeDefinitionId), subProcessDefinitionId);
            return start(starterId, starterSubstituteId, expressionContextToEvaluateOperations, operations, null, null, callerId, selector, processInputs);
        } catch (final SProcessDefinitionNotFoundException | SBonitaReadException e) {
            throw new SProcessInstanceCreationException(e);
        }
    }

    private Filter<SFlowNodeDefinition> getFilter(final long targetSFlowNodeDefinitionId) {
        if (targetSFlowNodeDefinitionId == -1) {
            return new StartFlowNodeFilter();
        }
        return new FlowNodeIdFilter(targetSFlowNodeDefinitionId);
    }

    protected SProcessInstance start(final long starterId, final long starterSubstituteId, final SExpressionContext expressionContextToEvaluateOperations,
            final List<SOperation> operations, final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectors,
            final long callerId, final FlowNodeSelector selector, final Map<String, Serializable> processInputs) throws SProcessInstanceCreationException,
            SContractViolationException {

        final SProcessDefinition sProcessDefinition = selector.getProcessDefinition();

        // Validate start process contract inputs:
        if (selector.getSubProcessDefinitionId() <= 0) {
            validateContractInputs(processInputs, sProcessDefinition);
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ensureProcessIsEnabled(sProcessDefinition);
            setProcessClassloader(sProcessDefinition);
            final SProcessInstance sProcessInstance = createProcessInstance(sProcessDefinition, starterId, starterSubstituteId, callerId);
            final boolean isInitializing = initialize(starterId, sProcessDefinition, sProcessInstance, expressionContextToEvaluateOperations,
                    operations, context, selector.getContainer(), connectors,
                    selector, processInputs);
            handleEventSubProcess(sProcessDefinition, sProcessInstance, selector.getSubProcessDefinitionId());

            if (isInitializing) {
                // some connectors were trigger
                processInstanceService.setState(sProcessInstance, ProcessInstanceState.INITIALIZING);
                // we stop execution here
                return sProcessInstance;
            }
            return startElements(sProcessInstance, selector);
        } catch (final SProcessInstanceCreationException e) {
            throw e;
        } catch (final SBonitaException | IOException | BonitaException e) {
            throw new SProcessInstanceCreationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void ensureProcessIsEnabled(final SProcessDefinition sProcessDefinition)
            throws SProcessDefinitionNotFoundException, SBonitaReadException, SProcessDefinitionException {
        final SProcessDefinitionDeployInfo deployInfo = processDefinitionService.getProcessDeploymentInfo(sProcessDefinition.getId());
        if (ActivationState.DISABLED.name().equals(deployInfo.getActivationState())) {
            throw new SProcessDefinitionException("The process " + deployInfo.getName() + " " + deployInfo.getVersion() + " is not enabled.",
                    deployInfo.getProcessId(), deployInfo.getName(), deployInfo.getVersion());
        }
    }

    private void setProcessClassloader(SProcessDefinition sProcessDefinition) throws SClassLoaderException {
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), sProcessDefinition.getId());
        Thread.currentThread().setContextClassLoader(localClassLoader);
        // initialize the process classloader by getting it one time
        try {
            localClassLoader.loadClass(this.getClass().getName());
        } catch (final ClassNotFoundException e) {
            // ignore, just to load
        }
    }

    protected void validateContractInputs(final Map<String, Serializable> processInputs, final SProcessDefinition sProcessDefinition)
            throws SContractViolationException {
        final SContractDefinition contractDefinition = sProcessDefinition.getContract();
        if (contractDefinition != null) {
            final ContractValidator validator = new ContractValidatorFactory().createContractValidator(logger, expressionService);
            validator.validate(sProcessDefinition.getId(), contractDefinition, processInputs);
        }
    }

    /*
     * Execute connectors then execute output operation and disconnect connectors
     */
    protected void executeConnectors(final SProcessDefinition processDefinition, final SProcessInstance sProcessInstance,
            final List<ConnectorDefinitionWithInputValues> connectorsList) throws InvalidEvaluationConnectorConditionException, SConnectorException {
        final SExpressionContext expcontext = new SExpressionContext();
        expcontext.setProcessDefinitionId(processDefinition.getId());
        expcontext.setProcessDefinition(processDefinition);
        expcontext.setContainerId(sProcessInstance.getId());
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
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(inputs);

            // we use the context classloader because the process classloader is already set
            final ConnectorResult result = connectorService.executeMultipleEvaluation(processDefinition.getId(), connectorId, version, connectorsExps,
                    contextInputValues, Thread.currentThread().getContextClassLoader(), expcontext);
            final List<Operation> outputs = connectorDefinition.getOutputs();
            connectorService.executeOutputOperation(ModelConvertor.convertOperations(outputs), expcontext, result);
        }
    }

    protected void handleEventSubProcess(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance,
            final long subProcessDefinitionId)
            throws SBonitaException {
        if (subProcessDefinitionId == -1) {
            // modify that to support event sub-processes within sub-processes
            try {
                eventsHandler.handleEventSubProcess(sProcessDefinition, sProcessInstance);
            } catch (final SProcessInstanceCreationException e) {
                throw e;
            } catch (final SBonitaException e) {
                setExceptionContext(sProcessDefinition, sProcessInstance, e);
                throw new SProcessInstanceCreationException("Unable to register events for event sub process in process.", e);
            }
        }
    }

    @Override
    public SProcessInstance startElements(final SProcessInstance sProcessInstance, final FlowNodeSelector selector) throws SProcessInstanceCreationException,
            SFlowNodeExecutionException {
        try {
            contractDataService.archiveAndDeleteProcessData(sProcessInstance.getId(), System.currentTimeMillis());
        } catch (final SObjectModificationException e) {
            throw new SProcessInstanceCreationException(e);
        }
        final List<SFlowNodeInstance> flowNodeInstances = initializeFirstExecutableElements(sProcessInstance, selector);
        // process is initialized and now the engine trigger jobs to execute other activities, give the hand back
        ProcessInstanceState state;
        final int size = flowNodeInstances.size();
        if (size == 0) {
            state = ProcessInstanceState.COMPLETED;
        } else {
            state = ProcessInstanceState.STARTED;
        }
        try {
            processInstanceService.setState(sProcessInstance, state);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to set the state on the process.", e);
        }
        for (final SFlowNodeInstance sFlowNodeInstance : flowNodeInstances) {
            try {
                workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(sProcessInstance.getProcessDefinitionId(), sProcessInstance.getId(),
                        sFlowNodeInstance.getId()));
            } catch (final SWorkRegisterException e) {
                setExceptionContext(sProcessInstance, sFlowNodeInstance, e);
                throw new SFlowNodeExecutionException("Unable to trigger execution of the flow node.", e);
            }
        }
        return sProcessInstance;
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.PROCESS.name();
    }

    private void createAndExecuteActivities(final Long processDefinitionId, final SFlowNodeInstance flowNodeInstance, final long parentProcessInstanceId,
            final List<SFlowNodeDefinition> choosenActivityDefinitions, final long rootProcessInstanceId) throws SBonitaException {
        final SProcessInstance parentProcessInstance = processInstanceService.getProcessInstance(parentProcessInstanceId);
        final SStateCategory stateCategory = parentProcessInstance.getStateCategory();

        // Create Activities
        final List<SFlowNodeInstance> sFlowNodeInstances = bpmInstancesCreator.createFlowNodeInstances(processDefinitionId,
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId(), choosenActivityDefinitions, rootProcessInstanceId,
                parentProcessInstanceId, stateCategory);

        // Execute Activities
        for (final SFlowNodeInstance sFlowNodeInstance : sFlowNodeInstances) {
            workService
                    .registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(processDefinitionId, parentProcessInstanceId, sFlowNodeInstance.getId()));
        }
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        e.setProcessInstanceIdOnContext(sFlowNodeInstance.getParentProcessInstanceId());
        e.setRootProcessInstanceIdOnContext(sFlowNodeInstance.getRootProcessInstanceId());
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SProcessInstance sProcessInstance, final SBonitaException e) {
        setExceptionContext(sProcessDefinition, e);
        setExceptionContext(sProcessInstance, e);
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessInstance.getProcessDefinitionId());
        e.setProcessDefinitionNameOnContext(sProcessInstance.getName());
        setExceptionContext(sProcessInstance, e);
        setExceptionContext(sFlowNodeInstance, e);
    }

    private void setExceptionContext(final SProcessDefinition sProcessDefinition, final SBonitaException e) {
        e.setProcessDefinitionIdOnContext(sProcessDefinition.getId());
        e.setProcessDefinitionNameOnContext(sProcessDefinition.getName());
        e.setProcessDefinitionVersionOnContext(sProcessDefinition.getVersion());
    }

    private void setExceptionContext(final SProcessInstance sProcessInstance, final SBonitaException e) {
        e.setProcessInstanceIdOnContext(sProcessInstance.getId());
        e.setRootProcessInstanceIdOnContext(sProcessInstance.getRootProcessInstanceId());
    }

    private void setExceptionContext(final SFlowNodeInstance sFlowNodeInstance, final SBonitaException e) {
        e.setFlowNodeDefinitionIdOnContext(sFlowNodeInstance.getFlowNodeDefinitionId());
        e.setFlowNodeInstanceIdOnContext(sFlowNodeInstance.getId());
        e.setFlowNodeNameOnContext(sFlowNodeInstance.getName());
    }
}
