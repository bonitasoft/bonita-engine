/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilderFactory;
import com.bonitasoft.engine.service.platform.PlatformInformationManager;
import com.bonitasoft.engine.service.platform.SynchronizationPlatformInfoManager;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SContractViolationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.ProcessExecutorImpl;
import org.bonitasoft.engine.execution.TransitionEvaluator;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.exception.SPlatformUpdateException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessExecutorExt extends ProcessExecutorImpl {

    private final SynchronizationPlatformInfoManager platformInformationManager;
    private final PlatformVerifier platformVerifier;

    public ProcessExecutorExt(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final TechnicalLoggerService logger, final FlowNodeExecutor flowNodeExecutor, final WorkService workService,
            final ProcessDefinitionService processDefinitionService, final GatewayInstanceService gatewayInstanceService,
            final EventInstanceService eventInstanceService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final OperationService operationService,
            final ExpressionResolverService expressionResolverService, final ExpressionService expressionService, final EventService eventService,
            final Map<String, SProcessInstanceHandler<SEvent>> handlers, final DocumentService documentService,
            final ReadSessionAccessor sessionAccessor, final ContainerRegistry containerRegistry, final BPMInstancesCreator bpmInstancesCreator,
            final EventsHandler eventsHandler, final FlowNodeStateManager flowNodeStateManager,
            final BusinessDataRepository businessDataRepository, final RefBusinessDataService refBusinessDataService, TransitionEvaluator transitionEvaluator,
            final ContractDataService contractDataService, SynchronizationPlatformInfoManager platformInformationManager, PlatformVerifier platformVerifier) {
        super(activityInstanceService, processInstanceService, logger, flowNodeExecutor, workService, processDefinitionService, gatewayInstanceService,
                eventInstanceService, connectorService, connectorInstanceService, classLoaderService, operationService,
                expressionResolverService, expressionService, eventService, handlers, documentService, sessionAccessor, containerRegistry, bpmInstancesCreator,
                eventsHandler, flowNodeStateManager, businessDataRepository, refBusinessDataService, transitionEvaluator, contractDataService);
        this.platformInformationManager = platformInformationManager;
        this.platformVerifier = platformVerifier;
    }

    protected void initializeData(final SProcessDefinition sDefinition, final SProcessInstance sInstance) throws SProcessInstanceCreationException {
        try {
            initializeStringIndexes(sInstance, sDefinition);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceCreationException("Unable to initialize string index on process instance.", e);
        }
    }

    private void initializeStringIndexes(final SProcessInstance sInstance, final SProcessDefinition sDefinition) throws SExpressionTypeUnknownException,
            SExpressionEvaluationException, SExpressionDependencyMissingException, SInvalidExpressionException, SProcessInstanceModificationException {
        final SExpressionContext contextDependency = new SExpressionContext(sInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name(),
                sDefinition.getId());
        boolean update = false;
        final SProcessInstanceUpdateBuilder updateBuilder = BuilderFactory.get(SProcessInstanceUpdateBuilderFactory.class).createNewInstance();
        for (int i = 1; i <= 5; i++) {
            final SExpression value = sDefinition.getStringIndexValue(i);
            if (value != null) {
                update = true;
                final Object evaluate = expressionResolverService.evaluate(value, contextDependency);
                switch (i) {
                    case 1:
                        updateBuilder.updateStringIndex1(String.valueOf(evaluate));
                        break;
                    case 2:
                        updateBuilder.updateStringIndex2(String.valueOf(evaluate));
                        break;
                    case 3:
                        updateBuilder.updateStringIndex3(String.valueOf(evaluate));
                        break;
                    case 4:
                        updateBuilder.updateStringIndex4(String.valueOf(evaluate));
                        break;
                    case 5:
                        updateBuilder.updateStringIndex5(String.valueOf(evaluate));
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
        if (update) {
            processInstanceService.updateProcess(sInstance, updateBuilder.done());
        }
    }

    protected SProcessInstance start(final long starterId, final long starterSubstituteId, final SExpressionContext expressionContextToEvaluateOperations, final List<SOperation> operations, final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectors, final long callerId, final FlowNodeSelector selector, final Map<String, Serializable> processInputs) throws SProcessInstanceCreationException, SContractViolationException {
        verifyPlatformInfo(callerId);
        SProcessInstance instance = startSuper(starterId, starterSubstituteId, expressionContextToEvaluateOperations, operations, context, connectors, callerId, selector, processInputs);
        updatePlatformInfo(instance);
        return instance;
    }

    private void verifyPlatformInfo(final long callerId) throws SProcessInstanceCreationException {
        if(callerId <= 0) {
            try {
                platformVerifier.check();
            } catch (SPlatformNotFoundException e) {
                throw new SProcessInstanceCreationException(e);
            }
        }
    }

    private void updatePlatformInfo(final SProcessInstance instance) throws SProcessInstanceCreationException {
        try {
            if(instance.isRootInstance()) {
                getPlatformInformationManager().update();
            }
        } catch (SPlatformNotFoundException | SPlatformUpdateException e) {
           throw  new SProcessInstanceCreationException(e);
        }
    }

    protected SProcessInstance startSuper(final long starterId, final long starterSubstituteId, final SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context, final List<ConnectorDefinitionWithInputValues> connectors, final long callerId, final FlowNodeSelector selector, final Map<String, Serializable> processInputs) throws SProcessInstanceCreationException, SContractViolationException {
        return super.start(starterId, starterSubstituteId, expressionContext, operations, context, connectors, callerId, selector, processInputs);
    }

    protected PlatformInformationManager getPlatformInformationManager() {
        return platformInformationManager;
    }

}
