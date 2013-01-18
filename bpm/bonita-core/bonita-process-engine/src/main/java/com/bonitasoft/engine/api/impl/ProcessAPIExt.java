/*
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.actor.ActorMappingExportException;
import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.privilege.api.ActorPrivilegeService;
import org.bonitasoft.engine.api.impl.PageIndexCheckingUtil;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.resolver.ActorProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.ConnectorProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.ProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.resolver.UserFilterProcessDependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.CheckActorMapping;
import org.bonitasoft.engine.api.impl.transaction.CreateManualUserTask;
import org.bonitasoft.engine.api.impl.transaction.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.GetActivityInstance;
import org.bonitasoft.engine.api.impl.transaction.GetProcessDefinition;
import org.bonitasoft.engine.api.impl.transaction.GetProcessDeploymentInfo;
import org.bonitasoft.engine.api.impl.transaction.GetSUser;
import org.bonitasoft.engine.api.impl.transaction.RemoveActorPrivilegeById;
import org.bonitasoft.engine.api.impl.transaction.ResolveProcessAndCreateDependencies;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.model.ConfigurationState;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorState;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.Problem;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.bpm.model.privilege.ActorPrivilege;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceModificationException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.operation.model.builder.SOperationBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SDeletingEnabledProcessException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilders;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.builder.DependencyBuilderAccessor;
import org.bonitasoft.engine.exception.ActivityCreationException;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.DeletingEnabledProcessException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ObjectModificationException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.ObjectReadException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDeletionException;
import org.bonitasoft.engine.exception.ProcessDeployException;
import org.bonitasoft.engine.exception.ProcessResourceException;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.transaction.AddActivityInstanceTokenCount;
import org.bonitasoft.engine.expression.model.builder.SExpressionBuilders;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SOutOfBoundException;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.search.SearchActorPrivileges;
import org.bonitasoft.engine.search.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.util.FileUtil;

import com.bonitasoft.engine.api.ParameterSorting;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.impl.resolver.ParameterProcessDependencyResolver;
import com.bonitasoft.engine.api.impl.transaction.CheckParameterProblems;
import com.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.bpm.model.impl.ParameterImpl;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessAPIExt extends ProcessAPIImpl implements ProcessAPI {

    private static TenantServiceAccessor getTenantAccessor() throws InvalidSessionException {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public void deleteProcess(final long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDeletionException,
            DeletingEnabledProcessException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            deleteProcessInstancesFromProcessDefinition(processDefinitionUUID, tenantAccessor);
            final SProcessDefinition serverProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionUUID, processDefinitionService);
            final DeleteProcess deleteProcess = new DeleteProcess(processDefinitionService, serverProcessDefinition, processInstanceService,
                    tenantAccessor.getArchiveService(), tenantAccessor.getCommentService(), tenantAccessor.getBPMInstanceBuilders());
            transactionExecutor.execute(deleteProcess);
            final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantAccessor.getTenantId());
            final File file = new File(processesFolder);
            if (!file.exists()) {
                file.mkdir();
            }
            tenantAccessor.getParameterService().deleteAll(serverProcessDefinition.getId());
            final File processeFolder = new File(file, String.valueOf(serverProcessDefinition.getId()));
            FileUtil.deleteDir(processeFolder);

            // delete actorPrivileges
            final ActorPrivilegeService actorPrivilegeService = tenantAccessor.getActorPrivilegeService();
            final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
            final SearchOptions searchOptions = new SearchOptionsImpl(0, 10);
            final SearchActorPrivileges searchActorPrivileges = new SearchActorPrivileges(actorPrivilegeService,
                    searchEntitiesDescriptor.getActorPrivilegeDescriptor(), searchOptions);
            transactionExecutor.execute(searchActorPrivileges);
            final SearchResult<ActorPrivilege> actorPrisRes = searchActorPrivileges.getResult();
            if (actorPrisRes.getCount() > 0) {
                for (final ActorPrivilege actorPrivilege : actorPrisRes.getResult()) {
                    final RemoveActorPrivilegeById removeActorPrivilegeById = new RemoveActorPrivilegeById(actorPrivilege.getId(), actorPrivilegeService);
                    transactionExecutor.execute(removeActorPrivilegeById);
                }
            }
        } catch (final SProcessDefinitionNotFoundException e) {
            log(tenantAccessor, e);
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SDeletingEnabledProcessException e) {
            log(tenantAccessor, e);
            throw new DeletingEnabledProcessException(e);
        } catch (final SProcessDefinitionReadException e) {
            log(tenantAccessor, e);
            throw new ProcessDeletionException(e);
        } catch (final BonitaHomeNotSetException e) {
            log(tenantAccessor, e);
            throw new BonitaRuntimeException(e);
        } catch (final SBonitaException e) {
            log(tenantAccessor, e);
            throw new ProcessDeletionException(e);
        }
    }

    @Override
    public ProcessDefinition deploy(final BusinessArchive businessArchive) throws InvalidSessionException, ProcessDeployException,
            ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final BPMDefinitionBuilders bpmDefinitionBuilders = tenantAccessor.getBPMDefinitionBuilders();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final SExpressionBuilders sExpressionBuilders = tenantAccessor.getSExpressionBuilders();
        final SDataDefinitionBuilders sDataDefinitionBuilders = tenantAccessor.getSDataDefinitionBuilders();
        final SOperationBuilders sOperationBuilders = tenantAccessor.getSOperationBuilders();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final DependencyBuilderAccessor dependencyBuilderAccessor = tenantAccessor.getDependencyBuilderAccessor();
        final DesignProcessDefinition processDefinition = businessArchive.getProcessDefinition();
        // create the runtime process definition
        final SProcessDefinition sDefinition = bpmDefinitionBuilders.getProcessDefinitionBuilder()
                .createNewInstance(processDefinition, sExpressionBuilders, sDataDefinitionBuilders, sOperationBuilders).done();
        try {
            transactionExecutor.openTransaction();
            try {
                processDefinitionService.store(sDefinition, processDefinition.getDisplayName(), processDefinition.getDisplayDescription());
                unzipBar(businessArchive, sDefinition, transactionExecutor, tenantAccessor.getTenantId());// TODO first unzip in temp folder
                // TODO refactor this to avoid using transaction executor inside
                final boolean isResolved = resolveDependencies(businessArchive, tenantAccessor, sDefinition);
                if (isResolved) {
                    transactionExecutor.execute(new ResolveProcessAndCreateDependencies(processDefinitionService, sDefinition.getId(), dependencyService,
                            dependencyBuilderAccessor, businessArchive));
                }
            } catch (final BonitaHomeNotSetException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } catch (final IOException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } catch (final SBonitaException e) {
                transactionExecutor.setTransactionRollback();
                throw new ProcessDeployException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ProcessDeployException(e);
        }
        return ModelConvertor.toProcessDefinition(sDefinition);
    }

    private boolean resolveDependencies(final BusinessArchive businessArchive, final TenantServiceAccessor tenantAccessor, final SProcessDefinition sDefinition)
            throws InvalidSessionException, ProcessDeployException {
        final List<ProcessDependencyResolver> resolvers = Arrays.asList(new ActorProcessDependencyResolver(), new ParameterProcessDependencyResolver(),
                new ConnectorProcessDependencyResolver(), new UserFilterProcessDependencyResolver());
        ProcessDeployException pde = null;
        boolean resolved = true;
        for (final ProcessDependencyResolver resolver : resolvers) {
            try {
                resolved &= resolver.resolve(this, tenantAccessor, businessArchive, sDefinition);
            } catch (final BonitaException e) {
                if (pde == null) {
                    pde = new ProcessDeployException("Some dependencies are not resolved");
                    pde.setProcessDefinitionId(sDefinition.getId());
                }
                resolved = false;
                pde.addException(e);
            }
        }
        if (pde != null) {
            final TechnicalLoggerService technicalLoggerService = tenantAccessor.getTechnicalLoggerService();
            for (final BonitaException e : pde.getExceptions()) {
                technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw pde;
        }
        return resolved;
    }

    @Override
    public void importParameters(final long pDefinitionId, final byte[] parametersXML) throws InvalidSessionException, InvalidParameterValueException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        SProcessDefinition sDefinition = null;
        if (pDefinitionId > 0) {
            final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
            final GetProcessDefinition getProcessDefinition = new GetProcessDefinition(pDefinitionId, processDefinitionService);
            try {
                transactionExecutor.execute(getProcessDefinition);
            } catch (final SBonitaException e) {
                throw new InvalidParameterValueException(e);
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
                throw new InvalidParameterValueException(e1);
            }
            final Enumeration<String> names = (Enumeration<String>) property.propertyNames();
            while (names.hasMoreElements()) {
                final String name = names.nextElement();
                final String value = property.getProperty(name);
                defaultParamterValues.put(name, value);
            }
            /*
             * for(Entry entry : property.entrySet()){
             * defaultParamterValues.put((String)entry.getKey(), (String)entry.getValue());
             * }
             */
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
            throw new InvalidParameterValueException(e);
        }
    }

    private void unzipBar(final BusinessArchive businessArchive, final SProcessDefinition sDefinition, final TransactionExecutor transactionExecutor,
            final long tenantId) throws BonitaHomeNotSetException, InvalidSessionException, ProcessDeployException, IOException {
        final String processesFolder = BonitaHomeServer.getInstance().getProcessesFolder(tenantId);
        final File file = new File(processesFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
        final File processFolder = new File(file, String.valueOf(sDefinition.getId()));
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, processFolder);
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
    public int getNumberOfParameterInstances(final long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(transactionExecutor, processDefinitionUUID, processDefinitionService);
            return sProcessDefinition.getParameters().size();
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public ParameterInstance getParameterInstance(final long processDefinitionId, final String parameterName) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException {
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
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int pageIndex, final int numberPerPage,
            final ParameterSorting sort) throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException {
        final int totalNumber = getNumberOfParameterInstances(processDefinitionId);
        PageIndexCheckingUtil.checkIfPageIsOutOfRange(totalNumber, pageIndex, numberPerPage);
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
            final List<SParameter> parameters = parameterService.get(processDefinitionId, pageIndex * numberPerPage, numberPerPage, order);
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
        } catch (final SParameterProcessNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SOutOfBoundException e) {
            throw new PageOutOfRangeException(e);
        }
    }

    @Override
    public void updateParameterInstanceValue(final long processDefinitionId, final String parameterName, final String parameterValue)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, ParameterNotFoundException, InvalidParameterValueException {
        if (parameterValue == null) {
            throw new InvalidParameterValueException("The parameter value cannot be null");
        }
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
            resolvedDependencies(sProcessDefinition, tenantAccessor);
        } catch (final SParameterProcessNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SProcessDefinitionReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    private void resolvedDependencies(final SProcessDefinition definition, final TenantServiceAccessor tenantAccessor) throws SBonitaException {
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final DependencyBuilderAccessor dependencyBuilderAccessor = tenantAccessor.getDependencyBuilderAccessor();
        final GetProcessDeploymentInfo getProcessDeploymentInfo = new GetProcessDeploymentInfo(definition.getId(), processDefinitionService);
        transactionExecutor.execute(getProcessDeploymentInfo);
        final SProcessDefinitionDeployInfo processDefinitionDeployInfo = getProcessDeploymentInfo.getResult();
        final boolean containsNullParameterValues = parameterService.containsNullValues(definition.getId());
        final ActorMappingService actorMappingService = tenantAccessor.getActorMappingService();
        final CheckActorMapping checkActorMapping = new CheckActorMapping(actorMappingService, definition.getId());
        transactionExecutor.execute(checkActorMapping);
        final Boolean actorMappingResolved = checkActorMapping.getResult();
        if (!containsNullParameterValues && actorMappingResolved
                && ConfigurationState.UNRESOLVED.name().equals(processDefinitionDeployInfo.getConfigurationState())) {
            try {
                transactionExecutor.execute(new ResolveProcessAndCreateDependencies(processDefinitionService, definition.getId(), dependencyService,
                        dependencyBuilderAccessor, tenantAccessor.getTenantId()));
            } catch (final BonitaHomeNotSetException e) {
                throw new BonitaRuntimeException(e);
            }
        }
    }

    @Override
    public ManualTaskInstance addManualUserTask(final long humanTaskId, final String taskName, final String displayName, final long assignTo,
            final String description, final Date dueDate, final TaskPriority priority) throws InvalidSessionException, ActivityInterruptedException,
            ActivityExecutionErrorException, ActivityCreationException, ActivityNotFoundException {
        if (!Manager.isFeatureActive(Features.CREATE_MANUAL_TASK)) {
            throw new IllegalStateException("The add of a manual task is not an active feature");
        }
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
                throw new ActivityCreationException("Unable to create a child task from this task, it's not assigned to you!");
            }
            final TransactionContentWithResult<SManualTaskInstance> createManualUserTask = new CreateManualUserTask(activityInstanceService, taskName,
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
            throw new ActivityCreationException(e.getMessage());
        } catch (final InvalidSessionException e) {
            throw e;
        } catch (final ActivityInterruptedException e) {
            throw e;
        } catch (final ActivityExecutionErrorException e) {
            throw e;
        } catch (final Exception e) {
            log(tenantAccessor, e);
            throw new ActivityExecutionErrorException(e.getMessage());
        }
    }

    private String getUserNameFromSession() throws InvalidSessionException {
        SessionAccessor sessionAccessor = null;
        try {
            sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long sessionId = sessionAccessor.getSessionId();
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            return platformServiceAccessor.getSessionService().getSession(sessionId).getUserName();
        } catch (final Exception e) {
            throw new InvalidSessionException(e);
        }
    }

    @Override
    public List<Problem> getProcessResolutionProblems(final long processId) throws InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessResourceException {
        final List<Problem> problems = super.getProcessResolutionProblems(processId);
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        try {
            final ParameterService parameterService = tenantAccessor.getParameterService();
            final CheckParameterProblems checkParameterProblems = new CheckParameterProblems(parameterService, processId);
            transactionExecutor.execute(checkParameterProblems);
            problems.addAll(checkParameterProblems.getProblems());
            return problems;
        } catch (final SBonitaException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    @Override
    public List<ConnectorInstance> getConnectorInstancesOfActivity(final long activityInstanceId, final int pageNumber, final int numberPerPage,
            final ConnectorInstanceCriterion order) throws InvalidSessionException, ObjectReadException, PageOutOfRangeException {
        return getConnectorInstancesFor(activityInstanceId, pageNumber, numberPerPage, SConnectorInstance.FLOWNODE_TYPE, order);
    }

    private List<ConnectorInstance> getConnectorInstancesFor(final long instanceId, final int pageNumber, final int numberPerPage, final String flownodeType,
            final ConnectorInstanceCriterion order) throws InvalidSessionException, PageOutOfRangeException, ObjectReadException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
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
            transactionExecutor.openTransaction();
            long numberOfConnectorInstances;
            try {
                numberOfConnectorInstances = connectorService.getNumberOfConnectorInstances(instanceId, flownodeType);
                PageIndexCheckingUtil.checkIfPageIsOutOfRange(numberOfConnectorInstances, pageNumber, numberPerPage);
                final List<SConnectorInstance> connectorInstances = connectorService.getConnectorInstances(instanceId, flownodeType,
                        pageNumber * numberPerPage, numberPerPage, fieldName, orderByType);
                return ModelConvertor.toConnectorInstances(connectorInstances);
            } catch (final SConnectorInstanceReadException e) {
                throw new ObjectReadException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectReadException(e);
        }
    }

    @Override
    public List<ConnectorInstance> getConnectorInstancesOfProcess(final long processInstanceId, final int pageNumber, final int numberPerPage,
            final ConnectorInstanceCriterion order) throws InvalidSessionException, ObjectReadException, PageOutOfRangeException {
        return getConnectorInstancesFor(processInstanceId, pageNumber, numberPerPage, SConnectorInstance.PROCESS_TYPE, order);
    }

    @Override
    public void setConnectorInstanceState(final long connectorInstanceId, final ConnectorState state) throws InvalidSessionException, ObjectReadException,
            ObjectNotFoundException, ObjectModificationException {
        if (!Manager.isFeatureActive(Features.SET_CONNECTOR_STATE)) {
            throw new IllegalStateException("Set a connector state is not an active feature");
        }
        if (ConnectorState.TO_BE_EXECUTED.equals(state)) {
            throw new ObjectModificationException("You can't put the connector as TO_BE_EXECUTED, use TO_RE_EXECUTE intead");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        try {
            transactionExecutor.openTransaction();
            try {
                final SConnectorInstance connectorInstance = connectorService.getConnectorInstance(connectorInstanceId);
                if (connectorInstance == null) {
                    throw new ObjectNotFoundException("connector instance with id " + connectorInstanceId);
                }
                connectorService.setState(connectorInstance, state.name());
            } catch (final SConnectorInstanceReadException e) {
                throw new ObjectReadException(e);
            } catch (final SConnectorInstanceModificationException e) {
                throw new ObjectModificationException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectReadException(e);
        }
    }

    @Override
    public void replayActivity(final long activityInstanceId) throws InvalidSessionException, ObjectNotFoundException, ObjectReadException,
            ObjectModificationException, ActivityExecutionFailedException {
        if (!Manager.isFeatureActive("REPLAY_ACTIVITY")) {
            throw new IllegalStateException("The replay an activity is not an active feature");
        }
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionExecutor transactionExecutor = tenantAccessor.getTransactionExecutor();
        final ConnectorService connectorService = tenantAccessor.getConnectorService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final ContainerRegistry containerRegistry = tenantAccessor.getContainerRegistry();
        String containerType;
        try {
            transactionExecutor.openTransaction();
            try {
                final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(activityInstanceId);
                List<SConnectorInstance> connectorInstances = connectorService.getConnectorInstances(activityInstanceId, SConnectorInstance.FLOWNODE_TYPE,
                        ConnectorEvent.ON_ENTER, 0, 1, ConnectorState.FAILED.name());
                if (!connectorInstances.isEmpty()) {
                    throw new ActivityExecutionFailedException("There is at least on connector in failed on onEnter of the activity: "
                            + connectorInstances.get(0).getName());
                }
                connectorInstances = connectorService.getConnectorInstances(activityInstanceId, SConnectorInstance.FLOWNODE_TYPE, ConnectorEvent.ON_FINISH, 0,
                        1, ConnectorState.FAILED.name());
                if (!connectorInstances.isEmpty()) {
                    throw new ActivityExecutionFailedException("There is at least on connector in failed on onFinish of the activity: "
                            + connectorInstances.get(0).getName());
                }
                // can change state and call execute
                activityInstanceService.setState(activityInstance, flowNodeStateManager.getState(activityInstance.getPreviousStateId()));
                activityInstanceService.setExecuting(activityInstance);
                containerType = SFlowElementsContainerType.PROCESS.name();
                if (activityInstance.getLogicalGroup(2) > 0) {
                    containerType = SFlowElementsContainerType.FLOWNODE.name();
                }
            } catch (final SConnectorInstanceReadException e) {
                throw new ObjectReadException(e);
            } catch (final SActivityReadException e) {
                throw new ObjectReadException(e);
            } catch (final SActivityInstanceNotFoundException e) {
                throw new ObjectNotFoundException(e);
            } catch (final SFlowNodeModificationException e) {
                throw new ObjectModificationException(e);
            } finally {
                transactionExecutor.completeTransaction();
            }
        } catch (final STransactionException e) {
            throw new ObjectReadException(e);
        }
        try {
            containerRegistry.executeFlowNodeInSameThread(activityInstanceId, null, null, containerType);
        } catch (final SActivityReadException e) {
            throw new ObjectReadException(e);
        } catch (final SBonitaException e) {
            throw new ActivityExecutionFailedException(e);
        }
    }

    @Override
    // TODO delete files after use/if an exception occurs
    public byte[] exportBarProcessContentUnderHome(final long processDefinitionId) throws BonitaRuntimeException, IOException, InvalidSessionException {
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
        final File currentParasF = new File(processFolder.getPath(), "current-parameters.properties");
        if (currentParasF.exists()) {
            final File parasF = new File(processFolder.getPath(), "parameters.properties");
            if (!parasF.exists()) {
                parasF.createNewFile();
            }
            final String content = FileUtil.read(currentParasF);
            FileUtil.write(parasF, content);
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
            throw new BonitaRuntimeException(e);
        }
        FileUtil.write(actormappF, xmlcontent);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            FileUtil.zipDir(processFolder.getPath(), zos, processFolder.getPath());
            return baos.toByteArray();
        } finally {
            zos.close();
        }
    }

}
