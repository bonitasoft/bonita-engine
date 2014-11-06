/*******************************************************************************
 * Copyright (C) 2009, 2014 BonitaSoft S.A.
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

import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessManagementAPIImplDelegate;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsDefinitionLevel;
import org.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsInstanceLevel;
import org.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsInstanceLevelAndArchived;
import org.bonitasoft.engine.api.impl.transaction.process.GetArchivedProcessInstanceList;
import org.bonitasoft.engine.api.impl.transaction.process.GetLastArchivedProcessInstance;
import org.bonitasoft.engine.api.impl.transaction.process.GetProcessDefinition;
import org.bonitasoft.engine.bpm.actor.ActorMappingExportException;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceNotFoundException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.connector.InvalidConnectorImplementationException;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessExportException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceNotFoundException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.process.SearchProcessInstances;
import org.bonitasoft.engine.search.supervisor.SearchArchivedActivityInstanceSupervisedBy;
import org.bonitasoft.engine.search.supervisor.SearchArchivedFlowNodeInstanceSupervisedBy;
import org.bonitasoft.engine.search.supervisor.SearchFlowNodeInstanceSupervisedBy;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.impl.transaction.UpdateProcessInstance;
import com.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsDefinitionLevelExt;
import com.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsInstanceLevelAndArchivedExt;
import com.bonitasoft.engine.api.impl.transaction.expression.EvaluateExpressionsInstanceLevelExt;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator.ManualTaskField;
import com.bonitasoft.engine.bpm.parameter.ImportParameterException;
import com.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import com.bonitasoft.engine.bpm.parameter.ParameterInstance;
import com.bonitasoft.engine.bpm.parameter.ParameterNotFoundException;
import com.bonitasoft.engine.bpm.parameter.impl.ParameterImpl;
import com.bonitasoft.engine.bpm.process.Index;
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceUpdater;
import com.bonitasoft.engine.businessdata.BusinessDataReference;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;
import com.bonitasoft.engine.parameter.OrderBy;
import com.bonitasoft.engine.parameter.ParameterService;
import com.bonitasoft.engine.parameter.SParameter;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import com.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.LicenseChecker;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ProcessAPIExt extends ProcessAPIImpl implements ProcessAPI {

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    protected ProcessManagementAPIImplDelegate instantiateProcessManagementAPIDelegate() {
        return new ProcessManagementAPIExtDelegate();
    }

    @Override
    public void importParameters(final long processDefinitionId, final byte[] parameters) throws ImportParameterException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_PARAMETER);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        SProcessDefinition sDefinition = null;
        if (processDefinitionId > 0) {
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(processDefinitionId, processDefinitionService);
            try {
                getProcessDefinition.execute();
            } catch (final SBonitaException e) {
                throw new ImportParameterException(e);
            }
            sDefinition = getProcessDefinition.getResult();
        } else {
            throw new ImportParameterException("The identifier of the process definition have to be a positif number.");
        }

        final ParameterService parameterService = tenantAccessor.getParameterService();
        final Set<SParameterDefinition> params = sDefinition.getParameters();
        final Map<String, String> defaultParameterValues = new HashMap<String, String>();

        if (parameters != null) {
            final Properties property = new Properties();
            try {
                property.load(new ByteArrayInputStream(parameters));
            } catch (final IOException e1) {
                throw new ImportParameterException(e1);
            }

            for (final Entry<Object, Object> entry : property.entrySet()) {
                defaultParameterValues.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }

        final Map<String, String> storedParameters = new HashMap<String, String>();
        for (final SParameterDefinition sParameterDefinition : params) {
            final String name = sParameterDefinition.getName();
            final String value = defaultParameterValues.get(name);
            if (value != null) {
                storedParameters.put(name, value);
            }
        }

        try {
            parameterService.addAll(sDefinition.getId(), storedParameters);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ImportParameterException(e);
        }

        // update process resolution:
        tenantAccessor.getDependencyResolver().resolveDependencies(processDefinitionId, tenantAccessor);
    }

    private void log(final TenantServiceAccessor tenantAccessor, final Exception e) {
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
    }

    private SProcessDefinition getServerProcessDefinition(final long processDefinitionId, final ProcessDefinitionService processDefinitionService)
            throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        final TransactionContentWithResult<SProcessDefinition> transactionContentWithResult = new GetProcessDefinition(processDefinitionId,
                processDefinitionService);
        try {
            transactionContentWithResult.execute();
            return transactionContentWithResult.getResult();
        } catch (final SProcessDefinitionNotFoundException e) {
            throw e;
        } catch (final SProcessDefinitionReadException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SProcessDefinitionNotFoundException(e, processDefinitionId);
        }
    }

    @Override
    public int getNumberOfParameterInstances(final long processDefinitionId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
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
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
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
    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int startIndex, final int maxResults,
            final ParameterCriterion sort) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
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

            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
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
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
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
    public ManualTaskInstance addManualUserTask(final ManualTaskCreator creator) throws CreationException, AlreadyExistsException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_MANUAL_TASK);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();

        final Map<ManualTaskField, Serializable> fields = creator.getFields();

        final long parentHumanTaskId = (Long) fields.get(ManualTaskField.PARENT_TASK_ID);
        final long loggedUserId = SessionInfos.getUserIdFromSession();
        try {
            final SActivityInstance parentActivityInstance = getSActivityInstance(parentHumanTaskId);
            if (!(parentActivityInstance instanceof SHumanTaskInstance)) {
                throw new CreationException("The parent activity is not a Human task");
            }
            if (((SHumanTaskInstance) parentActivityInstance).getAssigneeId() != loggedUserId) {
                throw new CreationException("Unable to create a child task from this task, it's not assigned to you!");
            }

            final SManualTaskInstance createManualUserTask = createManualUserTask(tenantAccessor, fields, parentHumanTaskId);
            executeFlowNode(loggedUserId, createManualUserTask.getId(), false /* wrapInTransaction */);// put it in ready
            addActivityInstanceTokenCount(activityInstanceService, parentActivityInstance);

            return ModelConvertor.toManualTask(createManualUserTask, flowNodeStateManager);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new CreationException(e.getMessage());
        }
    }

    private void addActivityInstanceTokenCount(final ActivityInstanceService activityInstanceService, final SActivityInstance activityInstance)
            throws SFlowNodeModificationException {
        final int tokenCount = activityInstance.getTokenCount() + 1;
        activityInstanceService.setTokenCount(activityInstance, tokenCount);
    }

    private SManualTaskInstance createManualUserTask(final TenantServiceAccessor tenantAccessor, final Map<ManualTaskField, Serializable> fields,
            final long parentHumanTaskId) throws SFlowNodeNotFoundException, SFlowNodeReadException, SActivityCreationException {
        final BPMInstancesCreator bpmInstancesCreator = tenantAccessor.getBPMInstancesCreator();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final Date dueDate = (Date) fields.get(ManualTaskField.DUE_DATE);
        long dueTime = 0L;
        if (dueDate != null) {
            dueTime = dueDate.getTime();
        }

        final TaskPriority taskPriority = fields.get(ManualTaskField.PRIORITY) != null ? (TaskPriority) fields.get(ManualTaskField.PRIORITY)
                : TaskPriority.NORMAL;

        final SManualTaskInstance sManualTaskInstance = bpmInstancesCreator.createManualTaskInstance(parentHumanTaskId,
                (String) fields.get(ManualTaskField.TASK_NAME), -1L,
                (String) fields.get(ManualTaskField.DISPLAY_NAME), (Long) fields.get(ManualTaskField.ASSIGN_TO),
                (String) fields.get(ManualTaskField.DESCRIPTION), dueTime, STaskPriority.valueOf(taskPriority.name()));
        activityInstanceService.createActivityInstance(sManualTaskInstance);
        return sManualTaskInstance;
    }

    @Override
    public void deleteManualUserTask(final long manualTaskId) throws DeletionException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.CREATE_MANUAL_TASK);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(manualTaskId);
            if (activityInstance instanceof SManualTaskInstance) {// should check in the definition that it does not exist
                processInstanceService.deleteFlowNodeInstance(activityInstance, null);
            } else {
                throw new DeletionException("Can't delete a task that is not a manual one");
            }
        } catch (final SBonitaException e) {
            throw new DeletionException(e);
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
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final SConnectorInstanceBuilderFactory keyProvider = BuilderFactory.get(SConnectorInstanceBuilderFactory.class);
        OrderByType orderByType;
        String fieldName;
        switch (order) {
            case ACTIVATION_EVENT_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getActivationEventKey();
                break;
            case ACTIVATION_EVENT_DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getActivationEventKey();
                break;
            case CONNECTOR_ID_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getConnectorIdKey();
                break;
            case CONNECTOR_ID__DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getConnectorIdKey();
                break;
            case CONTAINER_ID_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getContainerIdKey();
                break;
            case CONTAINER_ID__DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getContainerIdKey();
                break;
            case DEFAULT:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getNameKey();
                break;
            case NAME_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getNameKey();
                break;
            case NAME_DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getNameKey();
                break;
            case STATE_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getStateKey();
                break;
            case STATE_DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getStateKey();
                break;
            case VERSION_ASC:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getVersionKey();
                break;
            case VERSION_DESC:
                orderByType = OrderByType.DESC;
                fieldName = keyProvider.getVersionKey();
                break;
            default:
                orderByType = OrderByType.ASC;
                fieldName = keyProvider.getNameKey();
                break;
        }
        try {
            final List<SConnectorInstance> connectorInstances = connectorInstanceService.getConnectorInstances(instanceId, flownodeType, startIndex,
                    maxResults, fieldName, orderByType);
            return ModelConvertor.toConnectorInstances(connectorInstances);
        } catch (final SConnectorInstanceReadException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public List<ConnectorInstance> getConnectorInstancesOfProcess(final long processInstanceId, final int startIndex, final int maxResults,
            final ConnectorInstanceCriterion sortingCriterion) {
        return getConnectorInstancesFor(processInstanceId, startIndex, maxResults, SConnectorInstance.PROCESS_TYPE, sortingCriterion);
    }

    @Override
    public void setConnectorInstanceState(final long connectorInstanceId, final ConnectorStateReset state) throws UpdateException {
        final Map<Long, ConnectorStateReset> connectorsToReset = new HashMap<Long, ConnectorStateReset>(1);
        connectorsToReset.put(connectorInstanceId, state);
        setConnectorInstanceState(connectorsToReset);
    }

    @Override
    public void setConnectorInstanceState(final Map<Long, ConnectorStateReset> connectorsToReset) throws UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.SET_CONNECTOR_STATE);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        try {
            setConnectorInstancesState(connectorsToReset, connectorInstanceService);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    private void setConnectorInstancesState(final Map<Long, ConnectorStateReset> connectorsToReset, final ConnectorInstanceService connectorInstanceService)
            throws SBonitaException {
        for (final Entry<Long, ConnectorStateReset> connEntry : connectorsToReset.entrySet()) {
            final Long connectorInstanceId = connEntry.getKey();
            final SConnectorInstance connectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
            if (connectorInstance == null) {
                throw new SConnectorException("Connector instance not found with id " + connectorInstanceId);
            }
            final ConnectorStateReset state = connEntry.getValue();
            connectorInstanceService.setState(connectorInstance, state.name());
        }
    }

    @Override
    public void setConnectorImplementation(final long processDefinitionId, final String connectorId, final String connectorVersion,
            final byte[] connectorImplementationArchive) throws InvalidConnectorImplementationException, UpdateException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.POST_DEPLOY_CONFIG);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final long tenantId = tenantAccessor.getTenantId();
        try {
            final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            connectorService.setConnectorImplementation(sProcessDefinition, tenantId, connectorId, connectorVersion, connectorImplementationArchive);
        } catch (final SInvalidConnectorImplementationException e) {
            throw new InvalidConnectorImplementationException(e);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
        // refresh classloader in an other transaction.
        final DependencyService dependencyService = getTenantAccessor().getDependencyService();
        try {
            dependencyService.refreshClassLoader(ScopeType.PROCESS, processDefinitionId);
        } catch (final SBonitaException e) {
            throw new UpdateException(e);
        }
    }

    @Override
    public void replayActivity(final long activityInstanceId) throws ActivityExecutionException, ActivityInstanceNotFoundException {
        replayActivity(activityInstanceId, null);
    }

    @Override
    public void replayActivity(final long activityInstanceId, final Map<Long, ConnectorStateReset> connectorsToReset) throws ActivityExecutionException,
    ActivityInstanceNotFoundException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.REPLAY_ACTIVITY);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final ContainerRegistry containerRegistry = tenantAccessor.getContainerRegistry();

        try {
            // Reset connectors first:
            if (connectorsToReset != null) {
                for (final Entry<Long, ConnectorStateReset> connEntry : connectorsToReset.entrySet()) {
                    final SConnectorInstanceWithFailureInfo connectorInstanceWithFailure = connectorInstanceService
                            .getConnectorInstanceWithFailureInfo(connEntry.getKey());
                    // set state
                    final ConnectorStateReset state = connEntry.getValue();
                    connectorInstanceService.setState(connectorInstanceWithFailure, state.name());
                    // clean stack trace
                    if (connectorInstanceWithFailure.getStackTrace() != null) {
                        connectorInstanceService.setConnectorInstanceFailureException(connectorInstanceWithFailure, null);
                    }
                }
            }

            // Check if no connector remains in FAILED state:
            ensureNoMoreConnectoFailed(activityInstanceId, connectorInstanceService);

            // Then replay activity:
            // can change state and call execute
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
            activityInstanceService.setState(activityInstance, flowNodeStateManager.getState(activityInstance.getPreviousStateId()));
            activityInstanceService.setExecuting(activityInstance);

            containerRegistry.executeFlowNode(activityInstance.getProcessDefinitionId(), activityInstance.getParentProcessInstanceId(), activityInstanceId,
                    null, null);
        } catch (final SActivityInstanceNotFoundException e) {
            throw new ActivityInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ActivityExecutionException(e);
        } catch (final Exception e) {
            throw new ActivityExecutionException(e);
        }
    }

    /**
     * @param activityInstanceId
     * @param connectorInstanceService
     * @throws SConnectorInstanceReadException
     * @throws ActivityExecutionException
     */
    private void ensureNoMoreConnectoFailed(final long activityInstanceId, final ConnectorInstanceService connectorInstanceService)
            throws SConnectorInstanceReadException, ActivityExecutionException {
        for (final ConnectorEvent connectorEvent : ConnectorEvent.values()) {
            List<SConnectorInstance> connectorInstances;
            connectorInstances = connectorInstanceService.getConnectorInstances(activityInstanceId, SConnectorInstance.FLOWNODE_TYPE, connectorEvent, 0, 1,
                    ConnectorState.FAILED.name());
            if (!connectorInstances.isEmpty()) {
                throw new ActivityExecutionException("There is one connector in failed on " + connectorEvent.name() + " of the activity: "
                        + connectorInstances.get(0).getName());
            }
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
                IOUtil.writeContentToFile(content, parasF);
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
            IOUtil.writeContentToFile(xmlcontent, actormappF);

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
                    throws ConnectorExecutionException {
        return executeConnectorAtProcessInstantiationWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorAtProcessInstantiation(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ConnectorExecutionException {
        return executeConnectorAtProcessInstantiationWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    private Map<String, Serializable> executeConnectorAtProcessInstantiationWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        try {
            final GetArchivedProcessInstanceList getArchivedProcessInstanceList = new GetArchivedProcessInstanceList(processInstanceService,
                    tenantAccessor.getSearchEntitiesDescriptor(), processInstanceId, 0, 1,
                    BuilderFactory.get(SAProcessInstanceBuilderFactory.class).getIdKey(), OrderByType.ASC);
            getArchivedProcessInstanceList.execute();
            final ArchivedProcessInstance saprocessInstance = getArchivedProcessInstanceList.getResult().get(0);
            final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
            final ClassLoader classLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);

            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType("PROCESS_INSTANCE");
            expcontext.setContainerState(ContainerState.ACTIVE);
            expcontext.setProcessDefinitionId(processDefinitionId);
            expcontext.setTime(saprocessInstance.getArchiveDate().getTime());

            final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);

            if (operations != null) {
                // execute operations
                return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
            }
            return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
        } catch (final SBonitaException e) {
            throw new ConnectorExecutionException(e);
        } catch (final NotSerializableException e) {
            throw new ConnectorExecutionException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
                    throws ConnectorExecutionException {
        return executeConnectorOnActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, activityInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long activityInstanceId) throws ConnectorExecutionException {
        return executeConnectorOnActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, activityInstanceId);
    }

    /**
     * execute the connector and return connector output if there is no operation or operation output if there is operation
     *
     * @param operationsInputValues
     * @throws ConnectorExecutionException
     */
    private Map<String, Serializable> executeConnectorOnActivityInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long activityInstanceId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SUserTaskInstanceBuilderFactory sUserTaskInstanceBuilderFactory = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);

        try {
            final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
            final SProcessInstance processInstance = processInstanceService.getProcessInstance(activityInstance.getLogicalGroup(sUserTaskInstanceBuilderFactory
                    .getParentProcessInstanceIndex()));
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader classLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(activityInstanceId);
            expcontext.setContainerType("ACTIVITY_INSTANCE");
            expcontext.setContainerState(ContainerState.ACTIVE);
            expcontext.setProcessDefinitionId(processDefinitionId);
            final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
            if (operations != null) {
                // execute operations
                return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
            }
            return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
        } catch (final NotSerializableException e) {
            throw new ConnectorExecutionException(e);
        } catch (final SBonitaException e) {
            throw new ConnectorExecutionException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long activityInstanceId)
                    throws ConnectorExecutionException {
        return executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, activityInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long activityInstanceId) throws ConnectorExecutionException {
        return executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, activityInstanceId);
    }

    private Map<String, Serializable> executeConnectorOnCompletedActivityInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long activityInstanceId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        try {
            final SAActivityInstance aactivityInstance = activityInstanceService.getMostRecentArchivedActivityInstance(activityInstanceId);

            final GetLastArchivedProcessInstance getLastArchivedProcessInstance = new GetLastArchivedProcessInstance(processInstanceService,
                    aactivityInstance.getRootContainerId(), tenantAccessor.getSearchEntitiesDescriptor());
            getLastArchivedProcessInstance.execute();

            final long processDefinitionId = getLastArchivedProcessInstance.getResult().getProcessDefinitionId();
            final ClassLoader classLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);

            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(activityInstanceId);
            expcontext.setContainerType("ACTIVITY_INSTANCE");
            expcontext.setContainerState(ContainerState.ARCHIVED);
            expcontext.setProcessDefinitionId(processDefinitionId);
            expcontext.setTime(aactivityInstance.getArchiveDate() + 500);
            final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
            if (operations != null) {
                // execute operations
                return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
            }
            return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
        } catch (final NotSerializableException e) {
            throw new ConnectorExecutionException(e);
        } catch (final SBonitaException e) {
            throw new ConnectorExecutionException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
                    throws ConnectorExecutionException {
        return executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ConnectorExecutionException {
        return executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    private Map<String, Serializable> executeConnectorOnCompletedProcessInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();

        try {
            final GetLastArchivedProcessInstance getLastArchivedProcessInstance = new GetLastArchivedProcessInstance(processInstanceService, processInstanceId,
                    tenantAccessor.getSearchEntitiesDescriptor());
            getLastArchivedProcessInstance.execute();
            final ArchivedProcessInstance saprocessInstance = getLastArchivedProcessInstance.getResult();
            final long processDefinitionId = saprocessInstance.getProcessDefinitionId();
            final ClassLoader classLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);

            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType("PROCESS_INSTANCE");
            expcontext.setContainerState(ContainerState.ARCHIVED);
            expcontext.setProcessDefinitionId(processDefinitionId);
            expcontext.setTime(saprocessInstance.getArchiveDate().getTime() + 500);
            final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
            if (operations != null) {
                // execute operations
                return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
            }
            return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
        } catch (final NotSerializableException e) {
            throw new ConnectorExecutionException(e);
        } catch (final SBonitaException e) {
            throw new ConnectorExecutionException(e);
        }
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final long processInstanceId)
                    throws ConnectorExecutionException {
        return executeConnectorOnProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, null, null, processInstanceId);
    }

    @Override
    public Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            final Map<String, Expression> connectorInputParameters, final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations,
            final Map<String, Serializable> operationsInputValues, final long processInstanceId) throws ConnectorExecutionException {
        return executeConnectorOnProcessInstanceWithOrWithoutOperations(connectorDefinitionId, connectorDefinitionVersion, connectorInputParameters,
                inputValues, operations, operationsInputValues, processInstanceId);
    }

    /**
     * execute the connector and return connector output if there is no operation or operation output if there is operation
     *
     * @param operationsInputValues
     * @throws ConnectorExecutionException
     */
    private Map<String, Serializable> executeConnectorOnProcessInstanceWithOrWithoutOperations(final String connectorDefinitionId,
            final String connectorDefinitionVersion, final Map<String, Expression> connectorInputParameters,
            final Map<String, Map<String, Serializable>> inputValues, final List<Operation> operations, final Map<String, Serializable> operationsInputValues,
            final long processInstanceId) throws ConnectorExecutionException {
        checkConnectorParameters(connectorInputParameters, inputValues);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ClassLoaderService classLoaderService = tenantAccessor.getClassLoaderService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();

        try {

            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader classLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
            final Map<String, SExpression> connectorsExps = ModelConvertor.constructExpressions(connectorInputParameters);
            final SExpressionContext expcontext = new SExpressionContext();
            expcontext.setContainerId(processInstanceId);
            expcontext.setContainerType("PROCESS_INSTANCE");
            expcontext.setContainerState(ContainerState.ACTIVE);
            expcontext.setProcessDefinitionId(processDefinitionId);
            final ConnectorResult connectorResult = connectorService.executeMutipleEvaluation(processDefinitionId, connectorDefinitionId,
                    connectorDefinitionVersion, connectorsExps, inputValues, classLoader, expcontext);
            if (operations != null) {
                // execute operations
                return executeOperations(connectorResult, operations, operationsInputValues, expcontext, classLoader, tenantAccessor);
            }
            return getSerializableResultOfConnector(connectorDefinitionVersion, connectorResult, connectorService);
        } catch (final NotSerializableException e) {
            throw new ConnectorExecutionException(e);
        } catch (final SBonitaException e) {
            throw new ConnectorExecutionException(e);
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
            throws ProcessInstanceNotFoundException, UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, processInstanceId, index, value);
            updateProcessInstance.execute();
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
    public ProcessInstance updateProcessInstance(final long processInstanceId, final ProcessInstanceUpdater updater) throws ProcessInstanceNotFoundException,
    UpdateException {
        if (updater == null || updater.getFields().isEmpty()) {
            throw new UpdateException("The update descriptor does not contain field updates");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            final UpdateProcessInstance updateProcessInstance = new UpdateProcessInstance(processInstanceService, updater, processInstanceId);
            updateProcessInstance.execute();
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
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final SearchProcessInstances searchProcessInstances = new SearchProcessInstances(processInstanceService,
                    searchEntitiesDescriptor.getSearchProcessInstanceDescriptor(), searchOptions, processDefinitionService);
            searchProcessInstances.execute();
            return searchProcessInstances.getResult();
        } catch (final SBonitaException sbe) {
            throw new BonitaRuntimeException(sbe);
        }
    }

    @Override
    public ConnectorInstanceWithFailureInfo getConnectorInstanceWithFailureInformation(final long connectorInstanceId)
            throws ConnectorInstanceNotFoundException {
        LicenseChecker.getInstance().checkLicenceAndFeature(Features.REPLAY_ACTIVITY);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        SConnectorInstanceWithFailureInfo serverObject;
        try {
            serverObject = connectorInstanceService.getConnectorInstanceWithFailureInfo(connectorInstanceId);
        } catch (final SConnectorInstanceNotFoundException e) {
            throw new ConnectorInstanceNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
        return ModelConvertor.toConnectorInstanceWithFailureInfo(serverObject);
    }

    @Override
    public long getNumberOfProcessSupervisorsForUser(final long processDefinitionId) {
        final SProcessSupervisorBuilderFactory sProcessSupervisorBuilderFactory = BuilderFactory.get(SProcessSupervisorBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getProcessDefIdKey(),
                processDefinitionId));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getUserIdKey(), 0, FilterOperationType.GREATER));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getGroupIdKey(), -1));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getRoleIdKey(), -1));

        return getNumberOfProcessSupervisors(filterOptions);
    }

    @Override
    public long getNumberOfProcessSupervisorsForGroup(final long processDefinitionId) {
        final SProcessSupervisorBuilderFactory sProcessSupervisorBuilderFactory = BuilderFactory.get(SProcessSupervisorBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getProcessDefIdKey(),
                processDefinitionId));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getUserIdKey(), -1));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getGroupIdKey(), 0, FilterOperationType.GREATER));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getRoleIdKey(), -1));

        return getNumberOfProcessSupervisors(filterOptions);
    }

    @Override
    public long getNumberOfProcessSupervisorsForRole(final long processDefinitionId) {
        final SProcessSupervisorBuilderFactory sProcessSupervisorBuilderFactory = BuilderFactory.get(SProcessSupervisorBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getProcessDefIdKey(),
                processDefinitionId));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getUserIdKey(), -1));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getGroupIdKey(), -1));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getRoleIdKey(), 0, FilterOperationType.GREATER));

        return getNumberOfProcessSupervisors(filterOptions);
    }

    @Override
    public long getNumberOfProcessSupervisorsForMembership(final long processDefinitionId) {
        final SProcessSupervisorBuilderFactory sProcessSupervisorBuilderFactory = BuilderFactory.get(SProcessSupervisorBuilderFactory.class);
        final List<FilterOption> filterOptions = new ArrayList<FilterOption>();
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getProcessDefIdKey(),
                processDefinitionId));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getUserIdKey(), -1));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getGroupIdKey(), 0, FilterOperationType.GREATER));
        filterOptions.add(new FilterOption(SProcessSupervisor.class, sProcessSupervisorBuilderFactory.getRoleIdKey(), 0, FilterOperationType.GREATER));

        return getNumberOfProcessSupervisors(filterOptions);
    }

    private long getNumberOfProcessSupervisors(final List<FilterOption> filterOptions) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SupervisorMappingService supervisorService = tenantAccessor.getSupervisorService();
        final QueryOptions countOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, null, filterOptions, null);
        try {
            return supervisorService.getNumberOfProcessSupervisors(countOptions);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public SearchResult<ArchivedActivityInstance> searchArchivedActivityInstancesSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedActivityInstanceSupervisedBy searchedTasksTransaction = new SearchArchivedActivityInstanceSupervisedBy(supervisorId,
                activityInstanceService, flowNodeStateManager, searchEntitiesDescriptor.getSearchArchivedFlowNodeInstanceDescriptor(), searchOptions);

        try {
            searchedTasksTransaction.execute();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();
    }

    @Override
    public SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchArchivedFlowNodeInstanceSupervisedBy searchedTasksTransaction = new SearchArchivedFlowNodeInstanceSupervisedBy(supervisorId,
                activityInstanceService, flowNodeStateManager, searchEntitiesDescriptor.getSearchArchivedFlowNodeInstanceDescriptor(), searchOptions);

        try {
            searchedTasksTransaction.execute();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();
    }

    @Override
    public SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy(final long supervisorId, final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor serviceAccessor = getTenantAccessor();
        final FlowNodeStateManager flowNodeStateManager = serviceAccessor.getFlowNodeStateManager();
        final ActivityInstanceService activityInstanceService = serviceAccessor.getActivityInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = serviceAccessor.getSearchEntitiesDescriptor();
        final SearchFlowNodeInstanceSupervisedBy searchedTasksTransaction = new SearchFlowNodeInstanceSupervisedBy(supervisorId,
                activityInstanceService, flowNodeStateManager, searchEntitiesDescriptor.getSearchFlowNodeInstanceDescriptor(), searchOptions);

        try {
            searchedTasksTransaction.execute();
        } catch (final SBonitaException sbe) {
            throw new SearchException(sbe);
        }
        return searchedTasksTransaction.getResult();
    }

    @Override
    protected EvaluateExpressionsInstanceLevel createInstanceLevelExpressionEvaluator(final Map<Expression, Map<String, Serializable>> expressions,
            final long containerId,
            final String containerType, final long processDefinitionId, final ExpressionResolverService expressionService) {
        return new EvaluateExpressionsInstanceLevelExt(expressions, containerId, containerType, processDefinitionId, expressionService, getTenantAccessor()
                .getBusinessDataRepository());
    }

    @Override
    protected EvaluateExpressionsDefinitionLevel createDefinitionLevelExpressionEvaluator(
            final Map<Expression, Map<String, Serializable>> expressionsAndTheirPartialContext, final long processDefinitionId,
            final ExpressionResolverService expressionResolverService, final ProcessDefinitionService processDefinitionService) {
        return new EvaluateExpressionsDefinitionLevelExt(expressionsAndTheirPartialContext, processDefinitionId, expressionResolverService,
                processDefinitionService, getTenantAccessor()
                .getBusinessDataRepository());
    }

    @Override
    protected EvaluateExpressionsInstanceLevelAndArchived createInstanceAndArchivedLevelExpressionEvaluator(
            final Map<Expression, Map<String, Serializable>> expressions, final long containerId, final String containerType, final long processDefinitionId,
            final long time,
            final ExpressionResolverService expressionService) {
        return new EvaluateExpressionsInstanceLevelAndArchivedExt(expressions, containerId, containerType, processDefinitionId, time, expressionService,
                getTenantAccessor()
                .getBusinessDataRepository());
    }

    @Override
    public BusinessDataReference getProcessBusinessDataReference(final String businessDataName, final long processInstanceId) throws DataNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final RefBusinessDataService refBusinessDataService = tenantAccessor.getRefBusinessDataService();
            final SRefBusinessDataInstance sReference = refBusinessDataService.getRefBusinessDataInstance(businessDataName,
                    processInstanceId);
            if (sReference instanceof SSimpleRefBusinessDataInstance) {
                return SPModelConvertor.toSimpleBusinessDataReference((SSimpleRefBusinessDataInstance) sReference);
            } else {
                return SPModelConvertor.toMultipleBusinessDataReference((SMultiRefBusinessDataInstance) sReference);
            }
        } catch (final SRefBusinessDataInstanceNotFoundException srbdnfe) {
            throw new DataNotFoundException(srbdnfe);
        } catch (final SBonitaReadException sbre) {
            throw new RetrieveException(sbre);
        }
    }

    @Override
    public List<BusinessDataReference> getProcessBusinessDataReferences(final long processInstanceId, final int startIndex, final int maxResults) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final RefBusinessDataService refBusinessDataService = tenantAccessor.getRefBusinessDataService();
            final List<SRefBusinessDataInstance> sReferences = refBusinessDataService.getRefBusinessDataInstances(processInstanceId, startIndex, maxResults);
            final List<BusinessDataReference> references = new ArrayList<BusinessDataReference>();
            for (final SRefBusinessDataInstance sReference : sReferences) {
                if (sReference instanceof SSimpleRefBusinessDataInstance) {
                    references.add(SPModelConvertor.toSimpleBusinessDataReference((SSimpleRefBusinessDataInstance) sReference));
                } else {
                    references.add(SPModelConvertor.toMultipleBusinessDataReference((SMultiRefBusinessDataInstance) sReference));
                }
            }
            return references;
        } catch (final SBonitaReadException sbre) {
            throw new RetrieveException(sbre);
        }
    }

}
