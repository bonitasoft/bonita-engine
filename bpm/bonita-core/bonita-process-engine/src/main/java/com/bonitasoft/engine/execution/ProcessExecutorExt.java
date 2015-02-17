/*******************************************************************************
 * Copyright (C) 2009, 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.execution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorDefinitionWithInputValues;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.ProcessExecutorImpl;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.handler.SProcessInstanceHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilderFactory;
import com.bonitasoft.engine.core.process.instance.model.builder.SRefBusinessDataInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessExecutorExt extends ProcessExecutorImpl {

    private final BusinessDataRepository businessDataRepository;

    private final RefBusinessDataService refBusinessDataService;

    public ProcessExecutorExt(final ActivityInstanceService activityInstanceService, final ProcessInstanceService processInstanceService,
            final TechnicalLoggerService logger, final FlowNodeExecutor flowNodeExecutor, final WorkService workService,
            final ProcessDefinitionService processDefinitionService, final GatewayInstanceService gatewayInstanceService,
            final TransitionService transitionService, final EventInstanceService eventInstanceService, final ConnectorService connectorService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final OperationService operationService,
            final ExpressionResolverService expressionResolverService, final EventService eventService,
            final Map<String, SProcessInstanceHandler<SEvent>> handlers, final DocumentService documentService,
            final ReadSessionAccessor sessionAccessor, final ContainerRegistry containerRegistry, final BPMInstancesCreator bpmInstancesCreator,
            final TokenService tokenService, final EventsHandler eventsHandler, final FlowNodeStateManager flowNodeStateManager,
            final BusinessDataRepository businessDataRepository, final RefBusinessDataService refBusinessDataService) {
        super(activityInstanceService, processInstanceService, logger, flowNodeExecutor, workService, processDefinitionService, gatewayInstanceService,
                transitionService, eventInstanceService, connectorService, connectorInstanceService, classLoaderService, operationService,
                expressionResolverService, eventService, handlers, documentService, sessionAccessor, containerRegistry, bpmInstancesCreator,
                tokenService, eventsHandler, flowNodeStateManager);
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
    }

    @Override
    protected boolean initialize(final long userId, final SProcessDefinition sDefinition, final SProcessInstance sInstance,
            SExpressionContext expressionContext, final List<SOperation> operations, final Map<String, Object> context,
            final SFlowElementContainerDefinition processContainer, final List<ConnectorDefinitionWithInputValues> connectors,
            final FlowNodeSelector selectorForConnectorOnEnter)
            throws SProcessInstanceCreationException {
        if (expressionContext == null) {
            expressionContext = new SExpressionContext();
        }
        expressionContext.setProcessDefinitionId(sDefinition.getId());
        try {
            // Create SDataInstances
            bpmInstancesCreator.createDataInstances(sInstance, processContainer, sDefinition, expressionContext, operations, context);
            try {
                initializeStringIndexes(sInstance, sDefinition);
            } catch (final SBonitaException e) {
                throw new SProcessInstanceCreationException("Unable to initialize string index on process instance.", e);
            }

            final List<SBusinessDataDefinition> businessDataDefinitions = sDefinition.getProcessContainer().getBusinessDataDefinitions();
            for (final SBusinessDataDefinition bdd : businessDataDefinitions) {
                final SExpression expression = bdd.getDefaultValueExpression();
                if (bdd.isMultiple()) {
                    final List<Long> dataIds = new ArrayList<Long>();
                    if (expression != null) {
                        final List<Entity> businessData = (List<Entity>) expressionResolverService.evaluate(expression, expressionContext);
                        for (final Entity entity : businessData) {
                            final Entity tmp = businessDataRepository.merge(entity);
                            dataIds.add(tmp.getPersistenceId());
                        }
                    }
                    final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory.get(SRefBusinessDataInstanceBuilderFactory.class);
                    final SRefBusinessDataInstance instance = instanceFactory.createNewInstance(bdd.getName(), sInstance.getId(), dataIds, bdd.getClassName())
                            .done();
                    refBusinessDataService.addRefBusinessDataInstance(instance);
                } else {
                    Long primaryKey = null;
                    if (expression != null) {
                        Entity businessData = (Entity) expressionResolverService.evaluate(expression, expressionContext);
                        businessData = businessDataRepository.merge(businessData);
                        primaryKey = businessData.getPersistenceId();
                    }
                    final SRefBusinessDataInstanceBuilderFactory instanceFactory = BuilderFactory.get(SRefBusinessDataInstanceBuilderFactory.class);
                    final SRefBusinessDataInstance instance = instanceFactory.createNewInstance(bdd.getName(), sInstance.getId(), primaryKey,
                            bdd.getClassName())
                            .done();
                    refBusinessDataService.addRefBusinessDataInstance(instance);
                }
            }
            createDocuments(sDefinition, sInstance, userId);
            createDocumentLists(sDefinition, sInstance, userId, expressionContext, context);
            if (connectors != null) {
                executeConnectors(sDefinition, sInstance, connectors);
            }
            executeOperations(operations, context, expressionContext, sInstance);

            // Create connectors
            bpmInstancesCreator.createConnectorInstances(sInstance, processContainer.getConnectors(), SConnectorInstance.PROCESS_TYPE);

            return executeConnectors(sDefinition, sInstance, ConnectorEvent.ON_ENTER, selectorForConnectorOnEnter);
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

}
