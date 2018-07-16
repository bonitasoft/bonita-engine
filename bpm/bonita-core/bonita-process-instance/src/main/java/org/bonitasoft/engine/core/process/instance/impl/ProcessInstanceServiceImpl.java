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
package org.bonitasoft.engine.core.process.instance.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SAProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private static final String MANAGER_USER_ID = "managerUserId";

    private static final String USER_ID = "userId";

    private static final String SUPERVISED_BY = "SupervisedBy";

    private static final String FAILED_AND_SUPERVISED_BY = "FailedAndSupervisedBy";

    private static final String INVOLVING_USER = "InvolvingUser";

    private static final String FAILED = "Failed";

    private static final String MANAGED_BY = "ManagedBy";

    private static final int BATCH_SIZE = 100;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    private final EventService eventService;

    private final ActivityInstanceService activityService;

    private final SProcessInstanceBuilderFactory processInstanceKeyProvider;

    private final EventInstanceService bpmEventInstanceService;

    private final DataInstanceService dataInstanceService;

    private final ArchiveService archiveService;

    private final TechnicalLoggerService logger;

    private final ProcessDefinitionService processDefinitionService;

    private final ConnectorInstanceService connectorInstanceService;

    private final ClassLoaderService classLoaderService;

    private final DocumentService documentService;

    private final SCommentService commentService;

    private final RefBusinessDataService refBusinessDataService;

    public ProcessInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final ActivityInstanceService activityService, final TechnicalLoggerService logger, final EventInstanceService bpmEventInstanceService,
            final DataInstanceService dataInstanceService, final ArchiveService archiveService, final ProcessDefinitionService processDefinitionService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService, final DocumentService documentService,
            final SCommentService commentService, final RefBusinessDataService refBusinessDataService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.eventService = eventService;
        this.activityService = activityService;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.documentService = documentService;
        this.commentService = commentService;
        this.refBusinessDataService = refBusinessDataService;
        processInstanceKeyProvider = BuilderFactory.get(SProcessInstanceBuilderFactory.class);
        this.bpmEventInstanceService = bpmEventInstanceService;
        this.dataInstanceService = dataInstanceService;
        this.archiveService = archiveService;
        this.logger = logger;
    }

    @Override
    public void createProcessInstance(final SProcessInstance processInstance) throws SProcessInstanceCreationException {
        try {
            recorder.recordInsert(new InsertRecord(processInstance), PROCESSINSTANCE);
            setProcessState(processInstance, ProcessInstanceState.INITIALIZING);
        } catch (final SRecorderException | SProcessInstanceModificationException sre) {
            throw new SProcessInstanceCreationException(sre);
        }
    }

    @Override
    public SProcessInstance getProcessInstance(final long processInstanceId) throws SProcessInstanceReadException, SProcessInstanceNotFoundException {
        final SProcessInstance instance;
        try {
            instance = persistenceRead.selectById(SelectDescriptorBuilder.getElementById(SProcessInstance.class, "ProcessInstance", processInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
        if (instance == null) {
            throw new SProcessInstanceNotFoundException(processInstanceId);
        }
        return instance;
    }

    @Override
    public void deleteProcessInstance(final long processInstanceId) throws SProcessInstanceModificationException, SProcessInstanceReadException,
            SProcessInstanceNotFoundException {
        final SProcessInstance processInstance = getProcessInstance(processInstanceId);
        deleteProcessInstance(processInstance);
    }

    @Override
    public long deleteParentProcessInstanceAndElements(final List<SProcessInstance> sProcessInstances) throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        long nbDeleted = 0;
        for (final SProcessInstance sProcessInstance : sProcessInstances) {
            deleteParentProcessInstanceAndElements(sProcessInstance);
            nbDeleted++;
        }
        return nbDeleted;
    }

    @Override
    public void deleteParentProcessInstanceAndElements(final long processInstanceId) throws SProcessInstanceReadException, SProcessInstanceNotFoundException,
            SFlowNodeReadException, SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        final SProcessInstance sProcessInstance = getProcessInstance(processInstanceId);
        deleteParentProcessInstanceAndElements(sProcessInstance);
    }

    @Override
    public void deleteParentProcessInstanceAndElements(final SProcessInstance sProcessInstance) throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        checkIfCallerIsNotActive(sProcessInstance.getCallerId());

        try {
            deleteProcessInstance(sProcessInstance);
            deleteArchivedProcessInstanceElements(sProcessInstance.getId(), sProcessInstance.getProcessDefinitionId());
        } catch (final SProcessInstanceModificationException e) {
            getProcessInstanceAndLogException(sProcessInstance, e);
        }
    }

    private void getProcessInstanceAndLogException(final SProcessInstance sProcessInstance, final SProcessInstanceModificationException e)
            throws SProcessInstanceModificationException {
        try {
            getProcessInstance(sProcessInstance.getId());
            // process is still here, that's not normal. The problem must be raised:
            throw e;
        } catch (final SProcessInstanceReadException | SProcessInstanceNotFoundException e1) {
            logProcessInstanceNotFound(e);
        }
    }

    protected void logProcessInstanceNotFound(final SProcessInstanceModificationException e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, e.getMessage() + ". It has probably completed.");
        }
    }

    protected void logArchivedProcessInstanceNotFound(final SBonitaException e) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
            logger.log(this.getClass(), TechnicalLogSeverity.WARNING, e.getMessage());
        }
    }

    @Override
    public long deleteArchivedParentProcessInstancesAndElements(final List<SAProcessInstance> saProcessInstances) throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        long nbDeleted = 0;
        HashSet<Long> sourceProcessInstanceIds = new HashSet<>();
        for (final SAProcessInstance saProcessInstance : saProcessInstances) {
            sourceProcessInstanceIds.add(saProcessInstance.getSourceObjectId());
            deleteArchivedParentProcessInstanceAndElements(saProcessInstance);
            nbDeleted++;
        }
        for (Long sourceProcessInstanceId : sourceProcessInstanceIds) {
            try {
                refBusinessDataService.deleteArchivedRefBusinessDataInstance(sourceProcessInstanceId);
            } catch (SObjectModificationException e) {
                throw new SProcessInstanceModificationException(e);
            }
        }
        return nbDeleted;
    }

    @Override
    public void deleteArchivedParentProcessInstanceAndElements(final SAProcessInstance saProcessInstance) throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        checkIfCallerIsNotActive(saProcessInstance.getCallerId());
        try {
            deleteArchivedProcessInstanceElements(saProcessInstance.getSourceObjectId(), saProcessInstance.getProcessDefinitionId());
            deleteArchivedProcessInstance(saProcessInstance);
        } catch (final SProcessInstanceModificationException e) {
            getArchivedProcessInstanceAndLogWhenNotFound(saProcessInstance, e);
        }
    }

    private void getArchivedProcessInstanceAndLogWhenNotFound(final SAProcessInstance saProcessInstance, final SProcessInstanceModificationException e)
            throws SProcessInstanceModificationException {
        try {
            final SAProcessInstance saProcessInstance2 = getArchivedProcessInstance(saProcessInstance.getId());
            if (saProcessInstance2 != null) {
                // archived process is still here, that's not normal. The problem must be raised:
                throw e;
            }
            logArchivedProcessInstanceNotFound(new SAProcessInstanceNotFoundException(saProcessInstance.getId()));
        } catch (final SProcessInstanceReadException e1) {
            logArchivedProcessInstanceNotFound(e);
        }
    }

    @Override
    public void deleteArchivedProcessInstance(final SAProcessInstance archivedProcessInstance)
            throws SProcessInstanceModificationException {
        try {
            recorder.recordDelete(new DeleteRecord(archivedProcessInstance), PROCESSINSTANCE);
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    @Override
    public void deleteArchivedProcessInstanceElements(final long processInstanceId, final long processDefinitionId)
            throws SProcessInstanceModificationException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("PROCESS", processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);
            activityService.deleteArchivedFlowNodeInstances(processInstanceId);
            dataInstanceService.deleteLocalArchivedDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString());
            documentService.deleteArchivedDocuments(processInstanceId);
            connectorInstanceService.deleteArchivedConnectorInstances(processInstanceId, SConnectorInstance.PROCESS_TYPE);
            commentService.deleteArchivedComments(processInstanceId);
            deleteArchivedChildrenProcessInstanceAndElements(processInstanceId, processDefinitionId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void deleteArchivedChildrenProcessInstanceAndElements(final long processInstanceId, final long processDefinitionId) throws SBonitaException {
        List<Long> childrenProcessInstanceIds;
        do {
            // from index always will be zero because elements will be deleted
            childrenProcessInstanceIds = getArchivedChildrenSourceObjectIdsFromRootProcessInstance(processInstanceId, 0, BATCH_SIZE, OrderByType.ASC);
            deleteArchivedChildrenProcessInstancesAndElements(processDefinitionId, childrenProcessInstanceIds);
        } while (!childrenProcessInstanceIds.isEmpty());
    }

    private void deleteArchivedChildrenProcessInstancesAndElements(final long processDefinitionId, final List<Long> childrenProcessInstanceIds)
            throws SBonitaException {
        for (final Long childProcessInstanceId : childrenProcessInstanceIds) {
            deleteArchivedProcessInstanceElements(childProcessInstanceId, processDefinitionId);
            deleteArchivedProcessInstancesOfProcessInstance(childProcessInstanceId);
            refBusinessDataService.deleteArchivedRefBusinessDataInstance(childProcessInstanceId);
        }
    }

    @Override
    public List<Long> getSourceProcessInstanceIdsOfArchProcessInstancesFromDefinition(final long processDefinitionId, final int fromIndex, final int maxResults,
            final OrderByType sortingOrder) throws SProcessInstanceReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final String saCommentSourceObjectId = BuilderFactory.get(SACommentBuilderFactory.class).getSourceObjectId();
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SAProcessInstance.class, saCommentSourceObjectId, sortingOrder);
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(processDefinitionId,
                    queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public void deleteProcessInstance(final SProcessInstance sProcessInstance) throws SProcessInstanceModificationException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final long processDefinitionId = sProcessInstance.getProcessDefinitionId();
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("PROCESS", processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);
            deleteProcessInstanceElements(sProcessInstance);
            final DeleteRecord deleteRecord = new DeleteRecord(sProcessInstance);
            recorder.recordDelete(deleteRecord, PROCESSINSTANCE);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    protected void checkIfCallerIsNotActive(final long callerId) throws SFlowNodeReadException, SProcessInstanceHierarchicalDeletionException {
        try {
            if (callerId > 0) {
                final SFlowNodeInstance flowNodeInstance = activityService.getFlowNodeInstance(callerId);
                final SProcessInstanceHierarchicalDeletionException exception = new SProcessInstanceHierarchicalDeletionException(
                        "Unable to delete the process instance, because the parent (call activity) is still active.",
                        flowNodeInstance.getRootProcessInstanceId());
                setExceptionContext(flowNodeInstance, exception);
                throw exception;
            }
        } catch (final SFlowNodeNotFoundException e) {
            // ok the activity that called this process do not exists anymore
        }
    }

    protected void deleteProcessInstanceElements(final SProcessInstance processInstance) throws SBonitaException {
        SProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
        } catch (final SProcessDefinitionNotFoundException e) {
            // delete anyway
        }
        deleteFlowNodeInstances(processInstance.getId(), processDefinition);
        deleteDataInstancesIfNecessary(processInstance, processDefinition);
        documentService.deleteDocumentsFromProcessInstance(processInstance.getId());
        deleteConnectorInstancesIfNecessary(processInstance, processDefinition);
        commentService.deleteComments(processInstance.getId());
    }

    private void deleteConnectorInstancesIfNecessary(final SProcessInstance processInstance, final SProcessDefinition processDefinition)
            throws SConnectorInstanceReadException, SConnectorInstanceDeletionException {
        if (processDefinition != null && processDefinition.hasConnectors()) {
            connectorInstanceService.deleteConnectors(processInstance.getId(), SConnectorInstance.PROCESS_TYPE);
        }
    }

    protected void deleteConnectorInstancesIfNecessary(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition)
            throws SConnectorInstanceReadException, SConnectorInstanceDeletionException {
        if (hasConnectors(flowNodeInstance, processDefinition)) {
            connectorInstanceService.deleteConnectors(flowNodeInstance.getId(), SConnectorInstance.FLOWNODE_TYPE);
        }
    }

    private boolean hasConnectors(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) {
        if (processDefinition != null) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) getFlowNode(flowNodeInstance, processDefinition);
            if (activityDefinition != null) {
                return activityDefinition.hasConnectors();
            }
        }
        return false;
    }

    private void deleteDataInstancesIfNecessary(final SProcessInstance processInstance, final SProcessDefinition processDefinition)
            throws SDataInstanceException {
        boolean dataPresent = true;
        if (processDefinition != null) {
            dataPresent = processDefinition.getProcessContainer().getDataDefinitions().size() > 0;
        }
        dataInstanceService.deleteLocalDataInstances(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.toString(), dataPresent);
    }

    private void deleteFlowNodeInstances(final long processInstanceId, final SProcessDefinition processDefinition) throws SFlowNodeReadException,
            SProcessInstanceModificationException {
        List<SFlowNodeInstance> activityInstances;
        do {
            activityInstances = activityService.getFlowNodeInstances(processInstanceId, 0, BATCH_SIZE);
            for (final SFlowNodeInstance activityInstance : activityInstances) {
                deleteFlowNodeInstance(activityInstance, processDefinition);
            }
        } while (activityInstances.size() == BATCH_SIZE);
    }

    @Override
    public void deleteArchivedProcessInstancesOfProcessInstance(final long processInstanceId) throws SBonitaException {
        // fromIndex variable is not updated because the elements will be deleted, so we always need to start from zero;
        final SAProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);
        final FilterOption filterOption = new FilterOption(SAProcessInstance.class, processInstanceBuilderFact.getSourceObjectIdKey(), processInstanceId);
        final OrderByOption orderBy = new OrderByOption(SAProcessInstance.class, processInstanceBuilderFact.getIdKey(), OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(orderBy), Collections.singletonList(filterOption), null);

        List<SAProcessInstance> archProcessInstances;
        do {
            archProcessInstances = searchArchivedProcessInstances(queryOptions);
            deleteArchivedProcessInstances(archProcessInstances);
        } while (!archProcessInstances.isEmpty()); // never will be null as the persistence service sends an empty list if there are no results
    }

    private void deleteArchivedProcessInstances(final List<SAProcessInstance> saProcessInstances) throws SProcessInstanceModificationException {
        for (final SAProcessInstance saProcessInstance : saProcessInstances) {
            deleteArchivedProcessInstance(saProcessInstance);
        }
    }

    @Override
    public void deleteFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition)
            throws SProcessInstanceModificationException {
        try {
            deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);
            activityService.deleteFlowNodeInstance(flowNodeInstance);
        } catch (final SBonitaException e) {
            setExceptionContext(processDefinition, flowNodeInstance, e);
            throw new SProcessInstanceModificationException(e);
        }
    }

    void deleteFlowNodeInstanceElements(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) throws SBonitaException {
        final SFlowNodeType type = flowNodeInstance.getType();
        if (type.equals(SFlowNodeType.INTERMEDIATE_CATCH_EVENT) || type.equals(SFlowNodeType.BOUNDARY_EVENT) || type.equals(SFlowNodeType.RECEIVE_TASK)
                || type.equals(SFlowNodeType.START_EVENT)) {
            bpmEventInstanceService.deleteWaitingEvents(flowNodeInstance);
        }
        if (flowNodeInstance instanceof SActivityInstance) {
            deleteActivityInstanceElements((SActivityInstance) flowNodeInstance, processDefinition);
        }
    }

    private void deleteActivityInstanceElements(final SActivityInstance sActivityInstance, final SProcessDefinition processDefinition) throws SBonitaException {
        deleteDataInstancesIfNecessary(sActivityInstance, processDefinition);
        deleteConnectorInstancesIfNecessary(sActivityInstance, processDefinition);
        if (SFlowNodeType.USER_TASK.equals(sActivityInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(sActivityInstance.getType())) {
            activityService.deletePendingMappings(sActivityInstance.getId());
        } else if (SFlowNodeType.CALL_ACTIVITY.equals(sActivityInstance.getType()) || SFlowNodeType.SUB_PROCESS.equals(sActivityInstance.getType())) {
            // in the case of a call activity or subprocess activity delete the child process instance
            deleteSubProcess(sActivityInstance, processDefinition);
        }
    }

    void deleteSubProcess(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) throws SBonitaException {
        try {
            deleteProcessInstance(getChildOfActivity(flowNodeInstance.getId()));
        } catch (final SProcessInstanceNotFoundException e) {
            setExceptionContext(processDefinition, flowNodeInstance, e);

            // if the child process is not found, it's because it has already finished and archived or it was not created
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(getClass(), TechnicalLogSeverity.DEBUG,
                        "Can't find the process instance called by the activity. This process may be already finished.");
                logger.log(getClass(), TechnicalLogSeverity.DEBUG, e);
            }
        }
    }

    private void setExceptionContext(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final SBonitaException e) {
        if (processDefinition != null) {
            e.setProcessDefinitionIdOnContext(processDefinition.getId());
            e.setProcessDefinitionNameOnContext(processDefinition.getName());
            e.setProcessDefinitionVersionOnContext(processDefinition.getVersion());
        }
        setExceptionContext(flowNodeInstance, e);
    }

    private void setExceptionContext(final SFlowNodeInstance flowNodeInstance, final SBonitaException e) {
        e.setProcessInstanceIdOnContext(flowNodeInstance.getParentProcessInstanceId());
        e.setRootProcessInstanceIdOnContext(flowNodeInstance.getRootProcessInstanceId());
        e.setFlowNodeDefinitionIdOnContext(flowNodeInstance.getFlowNodeDefinitionId());
        e.setFlowNodeInstanceIdOnContext(flowNodeInstance.getId());
        e.setFlowNodeNameOnContext(flowNodeInstance.getName());
    }

    void deleteDataInstancesIfNecessary(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition)
            throws SDataInstanceException {
        boolean hasData = true;
        if (processDefinition != null) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) getFlowNode(flowNodeInstance, processDefinition);
            if (activityDefinition != null) {
                hasData = activityDefinition.getSDataDefinitions().size() > 0;
            }
        }
        dataInstanceService.deleteLocalDataInstances(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString(), hasData);
    }

    private SFlowNodeDefinition getFlowNode(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) {
        if (processDefinition == null) {
            return null;
        }
        return processDefinition.getProcessContainer().getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
    }

    @Override
    public void setState(final SProcessInstance processInstance, final ProcessInstanceState state) throws SProcessInstanceModificationException {
        // Let's archive the process instance before changing the state (to keep a track of state change):
        archiveProcessInstance(processInstance);
        final int previousStateId = processInstance.getStateId();
        setProcessState(processInstance, state);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, MessageFormat.format("[{0} with id {1}]{2}->{3}(new={4})", processInstance.getClass()
                    .getSimpleName(), processInstance.getId(), previousStateId, state.getId(), state.name()));
        }
    }

    private void setProcessState(final SProcessInstance processInstance, final ProcessInstanceState state) throws SProcessInstanceModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(processInstanceKeyProvider.getStateIdKey(), state.getId());
        final long now = System.currentTimeMillis();
        switch (state) {
            case COMPLETED:
                descriptor.addField(processInstanceKeyProvider.getEndDateKey(), now);
                break;
            case ABORTED:
                descriptor.addField(processInstanceKeyProvider.getEndDateKey(), now);
                break;
            case CANCELLED:
                descriptor.addField(processInstanceKeyProvider.getEndDateKey(), now);
                break;
            case STARTED:
                descriptor.addField(processInstanceKeyProvider.getStartDateKey(), now);
                break;
            default:
                break;
        }
        descriptor.addField(processInstanceKeyProvider.getLastUpdateKey(), now);
        updateProcessInstance(processInstance, descriptor, PROCESSINSTANCE_STATE);
    }

    private void updateProcessInstance(final SProcessInstance processInstance, final EntityUpdateDescriptor descriptor, final String eventType)
            throws SProcessInstanceModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(processInstance, descriptor), eventType);
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void archiveProcessInstance(final SProcessInstance processInstance) throws SProcessInstanceModificationException {
        final SAProcessInstance saProcessInstance = BuilderFactory.get(SAProcessInstanceBuilderFactory.class).createNewInstance(processInstance).done();
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saProcessInstance);
        try {
            archiveService.recordInsert(System.currentTimeMillis(), insertRecord);
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    @Override
    public void setStateCategory(final SProcessInstance processInstance, final SStateCategory stateCatetory) throws SProcessInstanceModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(processInstanceKeyProvider.getStateCategoryKey(), stateCatetory);
        updateProcessInstance(processInstance, descriptor, PROCESS_INSTANCE_CATEGORY_STATE);
    }

    @Override
    public List<Long> getChildInstanceIdsOfProcessInstance(final long processInstanceId, final int fromIndex, final int maxResults, final String sortingField,
            final OrderByType sortingOrder) throws SProcessInstanceReadException {
        try {
            final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SProcessInstance.class, sortingField, sortingOrder);
            final SelectListDescriptor<Long> elements = SelectDescriptorBuilder.getChildInstanceIdsOfProcessInstance(SProcessInstance.class, processInstanceId,
                    queryOptions);
            return persistenceRead.selectList(elements);
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public SProcessInstance getChildOfActivity(final long activityInstId) throws SProcessInstanceNotFoundException, SBonitaReadException {
        final SProcessInstance sProcessInstance = persistenceRead.selectOne(new SelectOneDescriptor<SProcessInstance>("getChildOfActivity", Collections
                .<String, Object> singletonMap("activityInstanceId", activityInstId), SProcessInstance.class));
        if (sProcessInstance == null) {
            throw new SProcessInstanceNotFoundException("No process instance was found as child of the activity with id = <" + activityInstId + ">");
        }
        return sProcessInstance;

    }

    @Override
    public long getNumberOfChildInstancesOfProcessInstance(final long processInstanceId) throws SProcessInstanceReadException {
        try {
            return persistenceRead.selectOne(SelectDescriptorBuilder.getNumberOfChildInstancesOfProcessInstance(processInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public long getNumberOfProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.getNumberOfEntities(SProcessInstance.class, queryOptions, Collections.emptyMap());
    }

    @Override
    public List<SProcessInstance> searchProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.searchEntity(SProcessInstance.class, queryOptions, Collections.emptyMap());
    }

    @Override
    public long getNumberOfOpenProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.searchEntity(SProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public long getNumberOfFailedProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, FAILED_AND_SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchFailedProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.searchEntity(SProcessInstance.class, FAILED_AND_SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public long getNumberOfOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = new HashMap<>(1);
            parameters.put(USER_ID, userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(USER_ID, userId);
        return persistenceRead.searchEntity(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
    }

    @Override
    public long getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(MANAGER_USER_ID, managerUserId);
        return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER + "s" + MANAGED_BY, queryOptions, parameters);
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(MANAGER_USER_ID, managerUserId);
        return persistenceRead.searchEntity(SProcessInstance.class, INVOLVING_USER + "s" + MANAGED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfArchivedProcessInstancesWithoutSubProcess(final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.getNumberOfEntities(SAProcessInstance.class, "WithoutSubProcess", queryOptions, null);
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesWithoutSubProcess(final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.searchEntity(SAProcessInstance.class, "WithoutSubProcess", queryOptions, null);
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        return persistenceService.searchEntity(SAProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfArchivedProcessInstancesSupervisedBy(final long userId, final QueryOptions countOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        return persistenceService.getNumberOfEntities(SAProcessInstance.class, SUPERVISED_BY, countOptions, parameters);
    }

    @Override
    public long getNumberOfArchivedProcessInstancesInvolvingUser(final long userId, final QueryOptions countOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = new HashMap<>(2);
            parameters.put(USER_ID, userId);
            return persistenceService.getNumberOfEntities(SAProcessInstance.class, INVOLVING_USER, countOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.getNumberOfEntities(SAProcessInstance.class, queryOptions, null);
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.searchEntity(SAProcessInstance.class, queryOptions, null);
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = new HashMap<>(1);
            parameters.put(USER_ID, userId);
            return persistenceService.searchEntity(SAProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void updateProcess(final SProcessInstance processInstance, final EntityUpdateDescriptor descriptor) throws SProcessInstanceModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(processInstance, descriptor), PROCESSINSTANCE);
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    @Override
    public SAProcessInstance getArchivedProcessInstance(final long archivedProcessInstanceId) throws SProcessInstanceReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("id", (Object) archivedProcessInstanceId);
            return persistenceService.selectOne(new SelectOneDescriptor<SAProcessInstance>("getArchivedProcessInstance",
                    parameters, SAProcessInstance.class));
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public List<SAProcessInstance> getArchivedProcessInstancesInAllStates(final List<Long> processInstanceIds) throws SProcessInstanceReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds", (Object) processInstanceIds);
            final SelectListDescriptor<SAProcessInstance> saProcessInstances = new SelectListDescriptor<>(
                    "getArchivedProcessInstancesInAllStates", parameters, SAProcessInstance.class, QueryOptions.countQueryOptions());
            return persistenceService.selectList(saProcessInstances);
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public List<SProcessInstance> getProcessInstancesInStates(final QueryOptions queryOptions, final ProcessInstanceState... states)
            throws SProcessInstanceReadException {
        final Set<Integer> stateIds = getStateIdsFromStates(states);
        final Map<String, Object> inputParameters = new HashMap<>(1);
        inputParameters.put("stateIds", stateIds);
        final SelectListDescriptor<SProcessInstance> selectProcessInstancesInStates = new SelectListDescriptor<>("getProcessInstancesInStates",
                inputParameters, SProcessInstance.class, queryOptions);
        try {
            return persistenceRead.selectList(selectProcessInstancesInStates);
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    protected Set<Integer> getStateIdsFromStates(final ProcessInstanceState... states) {
        if (states.length < 1) {
            throw new IllegalArgumentException("ProcessInstanceServiceImpl.getProcessInstancesInStates() must have at least one state as parameter");
        }
        final Set<Integer> stateIds = new HashSet<>(states.length);
        for (ProcessInstanceState state : states) {
            stateIds.add(state.getId());
        }
        return stateIds;
    }

    @Override
    public List<SProcessInstance> getProcessInstancesInState(final QueryOptions queryOptions, final ProcessInstanceState state)
            throws SProcessInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<>(1);
        inputParameters.put("state", state.getId());
        final SelectListDescriptor<SProcessInstance> selectListDescriptor = new SelectListDescriptor<>("getProcessInstancesInState",
                inputParameters, SProcessInstance.class, queryOptions);
        try {
            return persistenceRead.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public List<Long> getArchivedChildrenSourceObjectIdsFromRootProcessInstance(final long rootProcessInstanceId, final int fromIndex, final int maxResults,
            final OrderByType sortingOrder) throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>(1);
        inputParameters.put("rootProcessInstanceId", rootProcessInstanceId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SAProcessInstance.class, "sourceObjectId", sortingOrder);
        final SelectListDescriptor<Long> selectListDescriptor = new SelectListDescriptor<>("getChildrenSourceProcessInstanceIdsFromRootProcessInstance",
                inputParameters, SAProcessInstance.class, queryOptions);
        return persistenceRead.selectList(selectListDescriptor);
    }

    @Override
    public SAProcessInstance getLastArchivedProcessInstance(final long processInstanceId) throws SBonitaReadException {
        final SAProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);
        final FilterOption filterOption = new FilterOption(SAProcessInstance.class, processInstanceBuilderFact.getSourceObjectIdKey(), processInstanceId);
        final List<OrderByOption> orderByOptions = new ArrayList<>();
        orderByOptions.add(new OrderByOption(SAProcessInstance.class, processInstanceBuilderFact.getArchiveDateKey(), OrderByType.DESC));
        orderByOptions.add(new OrderByOption(SAProcessInstance.class, processInstanceBuilderFact.getEndDateKey(), OrderByType.DESC));
        final QueryOptions queryOptions = new QueryOptions(0, 1, orderByOptions, Collections.singletonList(filterOption), null);
        final List<SAProcessInstance> processInstances = searchArchivedProcessInstances(queryOptions);
        if (!processInstances.isEmpty()) {
            return processInstances.get(0);
        }
        return null;
    }

    @Override
    public long getNumberOfFailedProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.getNumberOfEntities(SProcessInstance.class, FAILED, queryOptions, null);
    }

    @Override
    public List<SProcessInstance> searchFailedProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.searchEntity(SProcessInstance.class, FAILED, queryOptions, null);
    }

    @Override
    public long getNumberOfProcessInstances(final long processDefinitionId) throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("processDefinitionId", processDefinitionId);
        final SelectOneDescriptor<Long> countDescriptor = new SelectOneDescriptor<>("countProcessInstancesOfProcessDefinition", inputParameters,
                SProcessInstance.class);
        return persistenceRead.selectOne(countDescriptor);
    }

}
