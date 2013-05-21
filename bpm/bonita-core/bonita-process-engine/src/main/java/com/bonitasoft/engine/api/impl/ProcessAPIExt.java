/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.actor.ActorMappingExportException;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.transaction.activity.GetActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.identity.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.api.impl.transaction.process.GetLastArchivedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinition;
import org.bonitasoft.engine.api.impl.transaction.task.CreateManualUserTask;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorState;
import org.bonitasoft.engine.bpm.model.ConnectorStateReset;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.Index;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.ProcessExportException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.activity.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.activity.ActivityExecutionException;
import org.bonitasoft.engine.exception.activity.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.activity.ActivityInterruptedException;
import org.bonitasoft.engine.exception.activity.ActivityNotFoundException;
import org.bonitasoft.engine.exception.activity.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.connector.ConnectorException;
import org.bonitasoft.engine.exception.connector.ConnectorExecutionException;
import org.bonitasoft.engine.exception.connector.ConnectorInstanceNotFoundException;
import org.bonitasoft.engine.exception.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.exception.connector.InvalidConnectorImplementationException;
import org.bonitasoft.engine.exception.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.exception.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.process.ProcessDeployException;
import org.bonitasoft.engine.exception.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.transaction.AddActivityInstanceTokenCount;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.process.SearchProcessInstances;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.util.IOUtil;

import com.bonitasoft.engine.api.ParameterSorting;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.impl.transaction.UpdateProcessInstance;
import com.bonitasoft.engine.api.impl.transaction.connector.SetConnectorInstancesState;
import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.bpm.model.ProcessInstanceUpdateDescriptor;
import com.bonitasoft.engine.bpm.model.impl.ParameterImpl;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import com.bonitasoft.engine.exception.ImportParameterException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessAPIExt extends ProcessAPIImpl implements ProcessAPI {

    private static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public void deleteProcess(final long processDefinitionId) throws DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            deleteProcessInstancesFromProcessDefinition(processDefinitionId, tenantAccessor);
            final SProcessDefinition serverProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
            final DeleteProcess deleteProcess = new DeleteProcess(processDefinitionService, serverProcessDefinition, processInstanceService,
                    tenantAccessor.getArchiveService(), actorMappingService);
            transactionExecutor.execute(deleteProcess);
            final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
            final File file = new File(processesFolder);
            if (!file.exists()) {
                file.mkdir();
            }
            if (!serverProcessDefinition.getParameters().isEmpty()) {
                tenantAccessor.getParameterService().deleteAll(serverProcessDefinition.getId());
            }
            final File processeFolder = new File(file, String.valueOf(serverProcessDefinition.getId()));
            IOUtil.deleteDir(processeFolder);
        } catch (final BonitaException e) {
            log(tenantAccessor, e);
            throw new BonitaRuntimeException(e);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new DeletionException(e);
        } catch (final IOException e) {
            log(tenantAccessor, e);
            throw new DeletionException(e);
        }
    }

    @Override
    public void importParameters(final long pDefinitionId, final byte[] parametersXML) throws ImportParameterException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_PARAMETER);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        SProcessDefinition sDefinition = null;
        if (pDefinitionId > 0) {
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(pDefinitionId, processDefinitionService);
            try {
                transactionExecutor.execute(getProcessDefinition);
            } catch (final SBonitaException e) {
                throw new ImportParameterException(e);
            }
            sDefinition = getProcessDefinition.getResult();
        }

        final ParameterService parameterService = tenantAccessor.getParameterService();
        final Set<SParameterDefinition> parameters = sDefinition.getParameters();
        final Map<String, String> defaultParamterValues = new HashMap<String, String>();

        if (parametersXML != null) {
            final Properties property = new Properties();
            try {
                property.load(new ByteArrayInputStream(parametersXML));
            } catch (final IOException e1) {
                throw new ImportParameterException(e1);
            }

            for (final Entry<Object, Object> entry : property.entrySet()) {
                defaultParamterValues.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        final Map<String, String> storedParameters = new HashMap<String, String>();
        for (final SParameterDefinition sParameterDefinition : parameters) {
            final String name = sParameterDefinition.getName();
            final String value = defaultParamterValues.get(name);
            if (value != null) {
                storedParameters.put(name, value);
            }
        }

        try {
            parameterService.addAll(sDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ImportParameterException(e);
        }
    }

    private void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
    }

    private SProcessDefinition getServerProcessDefinition(final TransactionExecutor transactionExecutor, final long processDefinitionUUID,
            final ProcessDefinitionService processDefinitionService) throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        final TransactionContentWithResult<SProcessDefinition> transactionContentWithResult = new GetProcessDefinition(processDefinitionUUID,
                processDefinitionService);
        try {
            transactionExecutor.execute(transactionContentWithResult);
            return transactionContentWithResult.getResult();
        } catch (final SProcessDefinitionNotFoundException e) {
            throw e;
        } catch (final SProcessDefinitionReadException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public int getNumberOfParameterInstances(final long processDefinitionId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            return sProcessDefinition.getParameters().size();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public ParameterInstance getParameterInstance(final long processDefinitionId, final String parameterName) throws ParameterNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final SParameter parameter = parameterService.get(processDefinitionId, parameterName);
            final String name = parameter.getName();
            final String value = parameter.getValue();
            final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
            final String description = parameterDefinition.getDescription();
            final String type = parameterDefinition.getType();
            return new ParameterImpl(name, description, value, type);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ParameterNotFoundException(processDefinitionId, parameterName);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int startIndex, final int maxResults, final ParameterSorting sort) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            OrderBy order = null;
            switch (sort) {
                case NAME_DESC:
                    order = OrderBy.NAME_DESC;
                    break;
                default:
                    order = OrderBy.NAME_ASC;
                    break;
            }

            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            if (sProcessDefinition.getParameters().isEmpty()) {
                return Collections.emptyList();
            }
            final List<SParameter> parameters = parameterService.get(processDefinitionId, startIndex, maxResults, order);
            final List<ParameterInstance> paramterInstances = new ArrayList<ParameterInstance>();
            for (int i = 0; i < parameters.size(); i++) {
                final SParameter parameter = parameters.get(i);
                final String name = parameter.getName();
                final String value = parameter.getValue();
                final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
                final String description = parameterDefinition.getDescription();
                final String type = parameterDefinition.getType();
                paramterInstances.add(new ParameterImpl(name, description, value, type));
            }
            return paramterInstances;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void updateParameterInstanceValue(final long processDefinitionId, final String parameterName, final String parameterValue)
            throws ParameterNotFoundException, UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionId, processDefinitionService);
            final SParameterDefinition parameter = sProcessDefinition.getParameter(parameterName);
            if (parameter == null) {
                throw new ParameterNotFoundException(processDefinitionId, parameterName);
            }
            parameterService.update(processDefinitionId, parameterName, parameterValue);
            tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ParameterNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public ManualTaskInstance addManualUserTask(final long humanTaskId, final String taskName, final String displayName, final long assignTo,
            final String description, final Date dueDate, final TaskPriority priority) throws ActivityInterruptedException, ActivityExecutionErrorException,
            CreationException, ActivityNotFoundException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_MANUAL_TASK);
        TenantServiceAccessor tenantAccessor = null;
        final TaskPriority prio = priority != null ? priority : TaskPriority.NORMAL;
        try {
            final String userName = getUserNameFromSession();
            tenantAccessor = getTenantAccessor();
            final IdentityService identityService = tenantAccessor.getIdentityService();
            final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
            final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
            final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
            final GetSUser getSUser = new GetSUser(identityService, userName);
            transactionExecutor.execute(getSUser);
            final long userId = getSUser.getResult().getId();

            final GetActivityInstance getActivityInstance = new GetActivityInstance(activityInstanceService, humanTaskId);
            transactionExecutor.execute(getActivityInstance);
            final SActivityInstance activityInstance = getActivityInstance.getResult();
            if (!(activityInstance instanceof SHumanTaskInstance)) {
                throw new ActivityNotFoundException("The parent activity is not a Human task", humanTaskId);
            }
            if (((SHumanTaskInstance) activityInstance).getAssigneeId() != userId) {
                throw new CreationException("Unable to create a child task from this task, it's not assigned to you!");
            }
            final TransactionContentWithResult<SManualTaskInstance> createManualUserTask = new CreateManualUserTask(activityInstanceService, taskName, -1,
                    displayName, humanTaskId, assignTo, description, dueDate, STaskPriority.valueOf(prio.name()));
            transactionExecutor.execute(createManualUserTask);
            final long id = createManualUserTask.getResult().getId();
            executeActivity(id);// put it in ready
            final AddActivityInstanceTokenCount addActivityInstanceTokenCount = new AddActivityInstanceTokenCount(activityInstanceService, humanTaskId, 1);
            transactionExecutor.execute(addActivityInstanceTokenCount);
            return ModelConvertor.toManualTask(createManualUserTask.getResult(), flowNodeStateManager);
        } catch (final SActivityInstanceNotFoundException e) {
            log(tenantAccessor, e);
            throw new ActivityNotFoundException(e.getMessage(), humanTaskId);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new CreationException(e.getMessage());
        } catch (final Exception e) {
            log(tenantAccessor, e);
            throw new ActivityExecutionErrorException(e.getMessage());
        }
    }

    @Override
    public void deleteManualUserTask(final long manualTaskId) throws DeletionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_MANUAL_TASK);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = getTenantAccessor().getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(manualTaskId);
                if (activityInstance instanceof SManualTaskInstance) {// should check in the definition that it does not exists
                    processInstanceService.deleteFlowNodeInstance(activityInstance, null);
                } else {
                    throw new DeletionException("Can't delete a task that is not a manual task");
                }
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new DeletionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new DeletionException(e);
        }
    }

    private String getUserNameFromSession() {
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            return platformServiceAccessor.getSessionService().getSession(sessionId).getUserName();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public List<ConnectorInstance> getConnectorInstancesOfActivity(final long activityInstanceId, final int startIndex, final int maxResults,
            final ConnectorInstanceCriterion sortingCriterion) {
        return getConnectorInstancesFor(activityInstanceId, startIndex, maxResults, SConnectorInstance.FLOWNODE_TYPE, sortingCriterion);
    }

    private List<ConnectorInstance> getConnectorInstancesFor(final long instanceId, final int startIndex, final int maxResults, final String flownodeType,
            final ConnectorInstanceCriterion order) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final SConnectorInstanceBuilder connectorInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSConnectorInstanceBuilder();
        OrderByType orderByType;
        String fieldName;
        switch (order) {
            case ACTIVATION_EVENT_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getActivationEventKey();
                break;
            case ACTIVATION_EVENT_DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getActivationEventKey();
                break;
            case CONNECTOR_ID_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getConnectorIdKey();
                break;
            case CONNECTOR_ID__DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getConnectorIdKey();
                break;
            case CONTAINER_ID_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getContainerIdKey();
                break;
            case CONTAINER_ID__DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getContainerIdKey();
                break;
            case DEFAULT:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getNameKey();
                break;
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getNameKey();
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getNameKey();
                break;
            case STATE_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getStateKey();
                break;
            case STATE_DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getStateKey();
                break;
            case VERSION_ASC:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getVersionKey();
                break;
            case VERSION_DESC:
                orderByType = OrderByType.DESC;
                fieldName = connectorInstanceBuilder.getVersionKey();
                break;
            default:
                orderByType = OrderByType.ASC;
                fieldName = connectorInstanceBuilder.getNameKey();
                break;
        }
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(instanceId, flownodeType, startIndex,
                        maxResults, fieldName, orderByType);
                return ModelConvertor.toConnectorInstances(connectorInstances);
            } catch (final SConnectorInstanceReadException e) {
                throw new RetrieveException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ConnectorInstance> getConnectorInstancesOfProcess(final long processInstanceId, final int startIndex, final int maxResults,
            final ConnectorInstanceCriterion sortingCriterion) {
        return getConnectorInstancesFor(processInstanceId, startIndex, maxResults, SConnectorInstance.PROCESS_TYPE, sortingCriterion);
    }

    @Override
    public void setConnectorInstanceState(final long connectorInstanceId, final ConnectorStateReset state) throws UpdateException,
            ConnectorInstanceNotFoundException {
        final Map<Long, ConnectorStateReset> connectorsToReset = new HashMap<Long, ConnectorStateReset>(1);
        connectorsToReset.put(connectorInstanceId, state);
        setConnectorInstanceState(connectorsToReset);
    }

    @Override
    public void setConnectorInstanceState(final Map<Long, ConnectorStateReset> connectorsToReset) throws ConnectorInstanceNotFoundException, UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.SET_CONNECTOR_STATE);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final SetConnectorInstancesState txContent = new SetConnectorInstancesState(connectorsToReset, connectorInstanceService);
        try {
            transactionExecutor.execute(txContent);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    /**
     * byte[] is a zip file exported from studio
     * clear: remove the old .impl file; put the new .impl file in the connector directory
     * reload the cache, connectorId and connectorVersion are used here.
     * Warning filesystem operation are not rolledback
     * 
     * @throws InvalidConnectorImplementationException
     */
    @Override
    public void setConnectorImplementation(final long processDefinitionId, final String connectorId, final String connectorVersion,
            final byte[] connectorImplementationArchive) throws InvalidConnectorImplementationException, UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.POST_DEPLOY_CONFIG);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final long tenantId = tenantAccessor.getTenantId();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                connectorService.setConnectorImplementation(sProcessDefinition, tenantId, connectorId, connectorVersion, connectorImplementationArchive);
            } catch (final SInvalidConnectorImplementationException e) {
                throw new InvalidConnectorImplementationException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new UpdateException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void replayActivity(final long activityInstanceId) throws ActivityExecutionException, ActivityNotFoundException {
        replayActivity(activityInstanceId, null);
    }

    @Override
    public void replayActivity(final long activityInstanceId, final Map<Long, ConnectorStateReset> connectorsToReset) throws ActivityExecutionException,
            ActivityNotFoundException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.REPLAY_ACTIVITY);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final ContainerRegistry containerRegistry = tenantAccessor.getContainerRegistry();
        String containerType;
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                // Reset connectors first:
                if (connectorsToReset != null) {
                    for (final Entry<Long, ConnectorStateReset> connEntry : connectorsToReset.entrySet()) {
                        final SConnectorInstance connectorInstance = connectorInstanceService.getConnectorInstance(connEntry.getKey());
                        final ConnectorStateReset state = connEntry.getValue();
                        connectorInstanceService.setState(connectorInstance, state.name());
                    }
                }

                // Then replay activity:
                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
                List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(activityInstanceId,
                        SConnectorInstance.FLOWNODE_TYPE, ConnectorEvent.ON_ENTER, 0, 1, ConnectorState.FAILED.name());
                if (!connectorInstances.isEmpty()) {
                    throw new ActivityExecutionException("There is at least one connector in failed on ON_ENTER of the activity: "
                            + connectorInstances.get(0).getName());
                }
                connectorInstances = connectorInstanceService.getConnectorInstances(activityInstanceId, SConnectorInstance.FLOWNODE_TYPE,
                        ConnectorEvent.ON_FINISH, 0, 1, ConnectorState.FAILED.name());
                if (!connectorInstances.isEmpty()) {
                    throw new ActivityExecutionException("There is at least one connector in failed on ON_FINISH of the activity: "
                            + connectorInstances.get(0).getName());
                }
                // can change state and call execute
                activityInstanceService.setState(activityInstance, flowNodeStateManager.getState(activityInstance.getPreviousStateId()));
                activityInstanceService.setExecuting(activityInstance);
                containerType = SFlowElementsContainerType.PROCESS.name();
                if (activityInstance.getLogicalGroup(2) > 0) {
                    containerType = SFlowElementsContainerType.FLOWNODE.name();
                }
            } catch (final SActivityInstanceNotFoundException e) {
                throw new ActivityNotFoundException(e);
            } catch (final SBonitaException e) {
                throw new ActivityExecutionException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ActivityExecutionException(e);
        }
        try {
            containerRegistry.executeFlowNodeInSameThread(activityInstanceId, null, null, containerType, null);
        } catch (final SBonitaException e) {
            throw new ActivityExecutionException(e);
        }
    }

    @Override
    // TODO delete files after use/if an exception occurs
    public byte[] exportBarProcessContentUnderHome(final long processDefinitionId) throws ProcessExportException {
        String processesFolder;
        try {
            final long tenantId = getTenantAccessor().getTenantId();
            processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        } catch (final BonitaHomeNotSetException e) {
            throw new BonitaRuntimeException(e);
        }
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File processFolder = new File(file, String.valueOf(processDefinitionId));

        // copy current parameter to parameter file
        try {
            final File currentParasF = new File(processFolder.getPath(), "current-parameters.properties");
            if (currentParasF.exists()) {
                final File parasF = new File(processFolder.getPath(), "parameters.properties");
                if (!parasF.exists()) {
                    parasF.createNewFile();
                }
                final String content = IOUtil.read(currentParasF);
                IOUtil.write(parasF, content);
            }

            // export actormapping
            final File actormappF = new File(processFolder.getPath(), "actorMapping.xml");
            if (!actormappF.exists()) {
                actormappF.createNewFile();
            }
            String xmlcontent = "";
            try {
                xmlcontent = exportActorMapping(processDefinitionId);
            } catch (final ActorMappingExportException e) {
                throw new ProcessExportException(e);
            }
            IOUtil.write(actormappF, xmlcontent);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            try {
                IOUtil.zipDir(processFolder.getPath(), zos, processFolder.getPath());
                return baos.toByteArray();
            } finally {
                zos.close();
            }
        } catch (final IOException e) {
            throw new ProcessExportException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorAtProcessInstantiation(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws NotSerializableException, ConnectorExecutionException, ConnectorNotFoundException {
        return executeConnectorAtProcessInstantiationWithOtWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorAtProcessInstantiation(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ArchivedProcessInstanceNotFoundException,
            ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorAtProcessInstantiationWithOtWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    private Map<String, Serializable> executeConnectorAtProcessInstantiationWithOtWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws InvalidEvaluationConnectorConditionException, ConnectorException, ArchivedProcessInstanceNotFoundException,
            ClassLoaderException, NotSerializableException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SAProcessInstanceBuilder saProcessInstanceBuilder = tenantAccessor.getBPMInstanceBuilders().getSAProcessInstanceBuilder();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                        persistenceService, tenantAccessor.getSearchEntitiesDescriptor(), processInstanceId, 0, 1, saProcessInstanceBuilder.getIdKey(),
                        OrderByType.ASC);
                getArchivedProcessInstanceList.execute();
                final ArchivedProcessInstance saprocessInstance = getArchivedProcessInstanceList.getResult().get(0);
                final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);

                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setContainerId(processInstanceId);
                expcontext.setContainerType("PROCESS_INSTANCE");
                expcontext.setProcessDefinitionId(processDefinitionId);
                expcontext.setTime(saprocessInstance.getArchiveDate().getTime());
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SConnectorException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } catch (final SProcessInstanceReadException e) {
                transactionExecutor.setTransactionRollback();
                throw new ArchivedProcessInstanceNotFoundException(e);
            } catch (final org.bonitasoft.engine.classloader.ClassLoaderException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorOnActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, activityInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long activityInstanceId) throws ConnectorException, NotSerializableException,
            ClassLoaderException, InvalidEvaluationConnectorConditionException {
        return executeConnectorOnActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, activityInstanceId);
    }

    /**
     * execute the connector and return connector output if there is no operation or operation output if there is operation
     * 
     * @param operationsInputValues
     */
    private Map<String, Serializable> executeConnectorOnActivityInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long activityInstanceId) throws ConnectorException, NotSerializableException, ClassLoaderException,
            InvalidEvaluationConnectorConditionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(activityInstance.getRootContainerId());
                final long processDefinitionId = processInstance.getProcessDefinitionId();
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setContainerId(activityInstanceId);
                expcontext.setContainerType("ACTIVITY_INSTANCE");
                expcontext.setProcessDefinitionId(processDefinitionId);
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SConnectorException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
            throws ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, activityInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long activityInstanceId) throws ArchivedActivityInstanceNotFoundException,
            ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, activityInstanceId);
    }

    private Map<String, Serializable> executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long activityInstanceId) throws InvalidEvaluationConnectorConditionException, ArchivedActivityInstanceNotFoundException,
            ClassLoaderException, NotSerializableException, ConnectorException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SAActivityInstance aactivityInstance = activityInstanceService.getArchivedActivityInstance(activityInstanceId, persistenceService);

                final GetLastArchivedProcessInstance getLastArchivedProcessInstance = new GetLastArchivedProcessInstance(processInstanceService,
                        aactivityInstance.getRootContainerId(), persistenceService, tenantAccessor.getSearchEntitiesDescriptor());
                getLastArchivedProcessInstance.execute();

                final long processDefinitionId = getLastArchivedProcessInstance.getResult().getProcessDefinitionId();
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);

                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setContainerId(activityInstanceId);
                expcontext.setContainerType("ACTIVITY_INSTANCE");
                expcontext.setProcessDefinitionId(processDefinitionId);
                expcontext.setTime(aactivityInstance.getArchiveDate() + 500);
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SProcessInstanceReadException e) {
                transactionExecutor.setTransactionRollback();
                throw new ArchivedActivityInstanceNotFoundException(e);
            } catch (final org.bonitasoft.engine.classloader.ClassLoaderException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException,
            NotSerializableException {
        return executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ArchivedProcessInstanceNotFoundException,
            ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    private Map<String, Serializable> executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws InvalidEvaluationConnectorConditionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException,
            NotSerializableException, ConnectorException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final GetLastArchivedProcessInstance getLastArchivedProcessInstance = new GetLastArchivedProcessInstance(processInstanceService,
                        processInstanceId, persistenceService, tenantAccessor.getSearchEntitiesDescriptor());
                getLastArchivedProcessInstance.execute();
                final ArchivedProcessInstance saprocessInstance = getLastArchivedProcessInstance.getResult();
                final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);

                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setContainerId(processInstanceId);
                expcontext.setContainerType("PROCESS_INSTANCE");
                expcontext.setProcessDefinitionId(processDefinitionId);
                expcontext.setTime(saprocessInstance.getArchiveDate().getTime() + 500);
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SProcessInstanceReadException e) {
                transactionExecutor.setTransactionRollback();
                throw new ArchivedProcessInstanceNotFoundException(e);
            } catch (final org.bonitasoft.engine.classloader.ClassLoaderException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } catch (final SConnectorException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
            throws ClassLoaderException, ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorConditionException,
            NotSerializableException {
        return executeConnectorOnProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ClassLoaderException, ProcessInstanceNotFoundException,
            ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException {
        return executeConnectorOnProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    /**
     * execute the connector and return connector output if there is no operation or operation output if there is operation
     * 
     * @param operationsInputValues
     */
    private Map<String, Serializable> executeConnectorOnProcessInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws InvalidEvaluationConnectorConditionException, NotSerializableException, ClassLoaderException,
            ConnectorException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final boolean txOpened = transactionExecutor.openTransaction();
            try {
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
                final long processDefinitionId = processInstance.getProcessDefinitionId();
                final ClassLoader classLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(sExpressionBuilders, connectorInputParameters);
                final SExpressionContext expcontext = new SExpressionContext();
                expcontext.setContainerId(processInstanceId);
                expcontext.setContainerType("PROCESS_INSTANCE");
                expcontext.setProcessDefinitionId(processDefinitionId);
                final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                        connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
                if (operations != null) {
                    // execute operations
                    return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
                } else {
                    return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
                }
            } catch (final SConnectorException e) {
                transactionExecutor.setTransactionRollback();
                throw new ConnectorException(e);
            } catch (final NotSerializableException e) {
                transactionExecutor.setTransactionRollback();
                throw e;
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ClassLoaderException(e);
            } finally {
                transactionExecutor.completeTransaction(txOpened);
            }
        } catch (final STransactionException e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public ProcessDefinition deploy(final BusinessArchive businessArchive) throws ProcessDeployException, AlreadyExistsException {
        final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();

        if (processDefinition.getStringIndexValue(1) != null || processDefinition.getStringIndexLabel(1) != null
                || processDefinition.getStringIndexValue(2) != null || processDefinition.getStringIndexLabel(2) != null
                || processDefinition.getStringIndexValue(3) != null || processDefinition.getStringIndexLabel(3) != null
                || processDefinition.getStringIndexValue(4) != null || processDefinition.getStringIndexLabel(4) != null
                || processDefinition.getStringIndexValue(5) != null || processDefinition.getStringIndexLabel(5) != null) {
            LicenseChecker.getInstance().checkLicenceAndFeature(Features.SEARCH_INDEX);
        }

        return super.deploy(businessArchive);
    }

    @Override
    public ProcessInstance updateProcessInstanceIndex(final long processInstanceId, final Index index, final String value)
            throws ProcessInstanceNotFoundException, UpdateException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final SProcessInstanceUpdateBuilder updateBuilder = bpmInstanceBuilders.getProcessInstanceUpdateBuilder();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, updateBuilder, processInstanceId, index,
                    value);
            transactionExecutor.execute(updateProcessInstance);
            return getProcessInstance(processInstanceId);
        } catch (final SProcessInstanceNotFoundException spinfe) {
            throw new ProcessInstanceNotFoundException(spinfe);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        } catch (final RetrieveException re) {
            throw new UpdateException(re);
        }
    }

    @Override
    public ProcessInstance updateProcessInstance(final long processInstanceId, final ProcessInstanceUpdateDescriptor updateDescriptor)
            throws ProcessInstanceNotFoundException, UpdateException, ProcessDefinitionNotFoundException {
        if (updateDescriptor == null || updateDescriptor.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final SProcessInstanceUpdateBuilder updateBuilder = bpmInstanceBuilders.getProcessInstanceUpdateBuilder();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, updateDescriptor, updateBuilder,
                    processInstanceId);
            transactionExecutor.execute(updateProcessInstance);
            return getProcessInstance(processInstanceId);
        } catch (final SProcessInstanceNotFoundException spinfe) {
            throw new ProcessInstanceNotFoundException(spinfe);
        } catch (final SBonitaException sbe) {
            throw new UpdateException(sbe);
        } catch (final RetrieveException re) {
            throw new UpdateException(re);
        }
    }

    protected SearchResult<ProcessInstance> searchProcessInstances(final TenantServiceAccessor tenantAccessor, final SearchOptions searchOptions) {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService,
                    searchEntitiesDescriptor.getProcessInstanceDescriptor(), searchOptions, processDefinitionService);
            transactionExecutor.execute(searchProcessInstances);
            return searchProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

}
