/**
 * Copyright (C) 2011-2012, 2014 BonitaSoft S.A.
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
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SAProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
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
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
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

    private final TransitionService transitionService;

    private final ProcessDefinitionService processDefinitionService;

    private final ConnectorInstanceService connectorInstanceService;

    private final ClassLoaderService classLoaderService;

    private final DocumentService documentService;

    private final SCommentService commentService;

    private final TokenService tokenService;

    public ProcessInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final ActivityInstanceService activityService, final TechnicalLoggerService logger, final EventInstanceService bpmEventInstanceService,
            final DataInstanceService dataInstanceService, final ArchiveService archiveService, final TransitionService transitionService,
            final ProcessDefinitionService processDefinitionService, final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService, final DocumentService documentService, final SCommentService commentService,
            final TokenService tokenService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.eventService = eventService;
        this.activityService = activityService;
        this.transitionService = transitionService;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.documentService = documentService;
        this.commentService = commentService;
        this.tokenService = tokenService;
        processInstanceKeyProvider = BuilderFactory.get(SProcessInstanceBuilderFactory.class);
        this.bpmEventInstanceService = bpmEventInstanceService;
        this.dataInstanceService = dataInstanceService;
        this.archiveService = archiveService;
        this.logger = logger;
    }

    @Override
    public void createProcessInstance(final SProcessInstance processInstance) throws SProcessInstanceCreationException {
        final InsertRecord insertRecord = new InsertRecord(processInstance);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(PROCESSINSTANCE).setObject(processInstance).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            setProcessState(processInstance, ProcessInstanceState.INITIALIZING);
        } catch (final SRecorderException sre) {
            throw new SProcessInstanceCreationException(sre);
        } catch (final SProcessInstanceModificationException spicme) {
            throw new SProcessInstanceCreationException(spicme);
        }
    }

    @Override
    public SProcessInstance getProcessInstance(final long processInstanceId) throws SProcessInstanceReadException, SProcessInstanceNotFoundException {
        final SProcessInstance instance;
        try {
            instance = persistenceRead.selectById(SelectDescriptorBuilder.getElementById(SProcessInstance.class, "ProcessInstance", processInstanceId));
        } catch (final SBonitaReadException sbre) {
            throw new SProcessInstanceReadException(sbre);
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
            try {
                getProcessInstance(sProcessInstance.getId());
                // process is still here, that's not normal. The problem must be raised:
                throw e;
            } catch (SProcessInstanceReadException e1) {
                logProcessInstanceNotFound(e);
            } catch (SProcessInstanceNotFoundException e1) {
                logProcessInstanceNotFound(e);
            }
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
    public long deleteParentArchivedProcessInstancesAndElements(final List<SAProcessInstance> saProcessInstances) throws SFlowNodeReadException,
    SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        long nbDeleted = 0;
        for (final SAProcessInstance saProcessInstance : saProcessInstances) {
            deleteParentArchivedProcessInstanceAndElements(saProcessInstance);
            nbDeleted++;
        }
        return nbDeleted;
    }

    @Override
    public void deleteParentArchivedProcessInstanceAndElements(final SAProcessInstance saProcessInstance) throws SFlowNodeReadException,
    SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        checkIfCallerIsNotActive(saProcessInstance.getCallerId());
        try {
            deleteArchivedProcessInstanceElements(saProcessInstance.getSourceObjectId(), saProcessInstance.getProcessDefinitionId());
            deleteArchivedProcessInstance(saProcessInstance);
        } catch (final SProcessInstanceModificationException e) {
            try {
                final SAProcessInstance saProcessInstance2 = getArchivedProcessInstance(saProcessInstance.getId());
                if (saProcessInstance2 != null) {
                    // archived process is still here, that's not normal. The problem must be raised:
                    throw e;
                }
                logArchivedProcessInstanceNotFound(new SAProcessInstanceNotFoundException(saProcessInstance.getId()));
            } catch (final SProcessInstanceModificationException e1) {
                throw e;
            } catch (SProcessInstanceReadException e1) {
                logArchivedProcessInstanceNotFound(e);
            }
        }
    }

    @Override
    public void deleteArchivedProcessInstance(final SAProcessInstance archivedProcessInstance) throws SProcessInstanceModificationException {
        final DeleteRecord deleteRecord = new DeleteRecord(archivedProcessInstance);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROCESSINSTANCE).setObject(archivedProcessInstance)
                    .done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
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
            deleteArchivedFlowNodeInstances(processInstanceId);
            dataInstanceService.deleteLocalArchivedDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString());
            documentService.deleteArchivedDocuments(processInstanceId);
            connectorInstanceService.deleteArchivedConnectorInstances(processInstanceId, SConnectorInstance.PROCESS_TYPE);
            transitionService.deleteArchivedTransitionsOfProcessInstance(processInstanceId);
            commentService.deleteArchivedComments(processInstanceId);
            deleteArchivedChidrenProcessInstanceElements(processInstanceId, processDefinitionId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void deleteArchivedChidrenProcessInstanceElements(final long processInstanceId, final long processDefinitionId) throws SBonitaException {
        List<Long> childrenProcessInstanceIds = null;
        do {
            // from index always will be zero because elements will be deleted
            childrenProcessInstanceIds = getArchivedChildrenSourceObjectIdsFromRootProcessInstance(processInstanceId, 0, BATCH_SIZE, OrderByType.ASC);
            for (final Long childProcessInstanceId : childrenProcessInstanceIds) {
                deleteArchivedProcessInstanceElements(childProcessInstanceId, processDefinitionId);
                deleteArchivedProcessInstancesOfProcessInstance(childProcessInstanceId);
            }
        } while (!childrenProcessInstanceIds.isEmpty());
    }

    protected void deleteArchivedFlowNodeInstances(final long processInstanceId) throws SFlowNodeReadException, SBonitaReadException,
    SConnectorInstanceDeletionException, SFlowNodeDeletionException, SDataInstanceException {
        List<SAFlowNodeInstance> activityInstances;
        do {
            activityInstances = activityService.getArchivedFlowNodeInstances(processInstanceId, 0, BATCH_SIZE);
            final HashSet<Long> orgActivityIds = new HashSet<Long>();
            final ArrayList<SAFlowNodeInstance> orgActivities = new ArrayList<SAFlowNodeInstance>();
            for (final SAFlowNodeInstance activityInstance : activityInstances) {
                if (!orgActivityIds.contains(activityInstance.getSourceObjectId())) {
                    orgActivityIds.add(activityInstance.getSourceObjectId());
                    orgActivities.add(activityInstance);
                }
                activityService.deleteArchivedFlowNodeInstance(activityInstance);
            }
            for (final SAFlowNodeInstance orgActivity : orgActivities) {
                deleteArchivedFlowNodeInstanceElements(orgActivity);
            }
        } while (!activityInstances.isEmpty());
    }

    private void deleteArchivedFlowNodeInstanceElements(final SAFlowNodeInstance activityInstance) throws SFlowNodeReadException, SBonitaReadException,
    SConnectorInstanceDeletionException, SDataInstanceException {
        if (activityInstance instanceof SAActivityInstance) {
            dataInstanceService.deleteLocalArchivedDataInstances(activityInstance.getSourceObjectId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString());
            connectorInstanceService.deleteArchivedConnectorInstances(activityInstance.getSourceObjectId(), SConnectorInstance.FLOWNODE_TYPE);
            if (SFlowNodeType.USER_TASK.equals(activityInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(activityInstance.getType())) {
                try {
                    activityService.deleteArchivedPendingMappings(activityInstance.getSourceObjectId());
                } catch (final SActivityModificationException e) {
                    throw new SFlowNodeReadException(e);
                }
            }
        }
    }

    @Override
    public List<Long> getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(final long processDefinitionId, final int fromIndex, final int maxResults,
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
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(PROCESSINSTANCE).setObject(sProcessInstance)
                        .done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    protected void checkIfCallerIsNotActive(final long callerId) throws SFlowNodeReadException, SProcessInstanceHierarchicalDeletionException {
        if (callerId > 0) {
            try {
                final SFlowNodeInstance flowNodeInstance = activityService.getFlowNodeInstance(callerId);
                final SProcessInstanceHierarchicalDeletionException exception = new SProcessInstanceHierarchicalDeletionException(
                        "Unable to delete the process instance, because the parent (call activity) is still active.",
                        flowNodeInstance.getRootProcessInstanceId());
                setExceptionContext(flowNodeInstance, exception);
                throw exception;
            } catch (final SFlowNodeNotFoundException e) {
                // ok the activity that called this process do not exists anymore
            }
        }
    }

    protected void deleteProcessInstanceElements(final SProcessInstance processInstance) throws SBonitaException {
        SProcessDefinition processDefinition = null;
        try {
            processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
        } catch (final SProcessDefinitionNotFoundException e) {
            // delete anyway
        }
        try {
            tokenService.deleteTokens(processInstance.getId());
        } catch (final SObjectReadException e) {
            throw new SProcessInstanceModificationException(e);
        } catch (final SObjectModificationException e) {
            throw new SProcessInstanceModificationException(e);
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
            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    flowNodeInstance.getFlowNodeDefinitionId());
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
        } while (!activityInstances.isEmpty());
    }

    @Override
    public void deleteArchivedProcessInstancesOfProcessInstance(final long processInstanceId) throws SBonitaException {
        final int fromIndex = 0;
        final int maxResults = 100;
        final SAProcessInstanceBuilderFactory archProcInstKeyProvider = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);

        List<SAProcessInstance> archProcInstances = null;
        do {
            // fromIndex variable is not updated because the elements will be deleted, so we always need to start from zero;
            final SAProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);
            final FilterOption filterOption = new FilterOption(SAProcessInstance.class, processInstanceBuilderFact.getSourceObjectIdKey(), processInstanceId);
            final OrderByOption orderBy = new OrderByOption(SAProcessInstance.class, archProcInstKeyProvider.getIdKey(), OrderByType.ASC);
            final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, Collections.singletonList(orderBy),
                    Collections.singletonList(filterOption), null);
            archProcInstances = searchArchivedProcessInstances(queryOptions);
            for (final SAProcessInstance archProcInstance : archProcInstances) {
                deleteArchivedProcessInstance(archProcInstance);
            }
        } while (!archProcInstances.isEmpty()); // never will be null as the persistence service sends an empty list if there are no results
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
        if (flowNodeInstance instanceof SEventInstance) {
            bpmEventInstanceService.deleteEventTriggerInstances(flowNodeInstance.getId());
        } else if (flowNodeInstance instanceof SActivityInstance) {
            deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
            deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
            if (SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType())) {
                activityService.deleteHiddenTasksForActivity(flowNodeInstance.getId());
                activityService.deletePendingMappings(flowNodeInstance.getId());
            } else if (SFlowNodeType.CALL_ACTIVITY.equals(flowNodeInstance.getType()) || SFlowNodeType.SUB_PROCESS.equals(flowNodeInstance.getType())) {
                // in the case of a call activity or subprocess activity delete the child process instance
                deleteSubProcess(flowNodeInstance, processDefinition);
            }
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
        e.setProcessDefinitionIdOnContext(processDefinition.getId());
        e.setProcessDefinitionNameOnContext(processDefinition.getName());
        e.setProcessDefinitionVersionOnContext(processDefinition.getVersion());
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
        boolean dataPresent = true;
        if (processDefinition != null) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {
                dataPresent = activityDefinition.getSDataDefinitions().size() > 0;
            }
        }
        dataInstanceService.deleteLocalDataInstances(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString(), dataPresent);
    }

    @Override
    public void setState(final SProcessInstance processInstance, final ProcessInstanceState state) throws SProcessInstanceModificationException {
        // Let's archive the process instance before changing the state (to keep a track of state change):
        archiveProcessInstance(processInstance);
        int previousStateId = processInstance.getStateId();
        setProcessState(processInstance, state);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, MessageFormat.format("[{0} with id {1}]{2}->{3}(new={4})", processInstance.getClass()
                    .getSimpleName(), processInstance.getId(), previousStateId, state.getId(), state.name()));
        }
    }

    @Override
    public void setMigrationPlanId(final SProcessInstance processInstance, final long migrationPlanId) throws SProcessInstanceModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(processInstanceKeyProvider.getMigrationPlanIdKey(), migrationPlanId);
        final long now = System.currentTimeMillis();
        descriptor.addField(processInstanceKeyProvider.getLastUpdateKey(), now);
        updateProcessInstance(processInstance, descriptor, MIGRATION_PLAN);
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
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(processInstance, descriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(eventType, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(eventType).setObject(processInstance).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
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
        try {
            final SProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory.get(SProcessInstanceBuilderFactory.class);
            final FilterOption filterOption = new FilterOption(SProcessInstance.class, processInstanceBuilderFact.getCallerIdKey(), activityInstId);
            final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, null, Collections.singletonList(filterOption), null);
            return searchProcessInstances(queryOptions).get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new SProcessInstanceNotFoundException("No process instance was found as child of the activity with id = <" + activityInstId + ">");
        }
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
        return persistenceRead.getNumberOfEntities(SProcessInstance.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    @Override
    public List<SProcessInstance> searchProcessInstances(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceRead.searchEntity(SProcessInstance.class, queryOptions, Collections.<String, Object> emptyMap());
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
    public long getNumberOfOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(USER_ID, userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put(USER_ID, userId);
        return persistenceRead.searchEntity(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
    }

    @Override
    public long getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put(MANAGER_USER_ID, managerUserId);
        return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER + "s" + MANAGED_BY, queryOptions, parameters);
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
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
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
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
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(USER_ID, userId);
            return persistenceService.searchEntity(SAProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void updateProcess(final SProcessInstance processInstance, final EntityUpdateDescriptor descriptor) throws SProcessInstanceModificationException {
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(processInstance, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(PROCESSINSTANCE).setObject(processInstance)
                        .done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    @Override
    public SAProcessInstance getArchivedProcessInstance(final long archivedProcessInstanceId) throws SProcessInstanceReadException{
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("id", (Object) archivedProcessInstanceId);
            final SAProcessInstance saProcessInstance = persistenceService.selectOne(new SelectOneDescriptor<SAProcessInstance>("getArchivedProcessInstance",
                    parameters, SAProcessInstance.class));
            return saProcessInstance;
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public List<SAProcessInstance> getArchivedProcessInstancesInAllStates(final List<Long> processInstanceIds) throws SProcessInstanceReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("sourceObjectIds", (Object) processInstanceIds);
            final SelectListDescriptor<SAProcessInstance> saProcessInstances = new SelectListDescriptor<SAProcessInstance>(
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
        final Map<String, Object> inputParameters = new HashMap<String, Object>(1);
        inputParameters.put("stateIds", stateIds);
        final SelectListDescriptor<SProcessInstance> selectProcessInstancesInStates = new SelectListDescriptor<SProcessInstance>("getProcessInstancesInStates",
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
        final Set<Integer> stateIds = new HashSet<Integer>(states.length);
        for (int i = 0; i < states.length; i++) {
            stateIds.add(states[i].getId());
        }
        return stateIds;
    }

    @Override
    public List<SProcessInstance> getProcessInstancesInState(final QueryOptions queryOptions, final ProcessInstanceState state)
            throws SProcessInstanceReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(1);
        inputParameters.put("state", state.getId());
        final SelectListDescriptor<SProcessInstance> selectListDescriptor = new SelectListDescriptor<SProcessInstance>("getProcessInstancesInState",
                inputParameters, SProcessInstance.class, queryOptions);
        try {
            return persistenceRead.selectList(selectListDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public List<Long> getArchivedChildrenSourceObjectIdsFromRootProcessInstance(final long rootProcessIntanceId, final int fromIndex, final int maxResults,
            final OrderByType sortingOrder) throws SBonitaReadException {
        final Map<String, Object> inputParameters = new HashMap<String, Object>(1);
        inputParameters.put("rootProcessInstanceId", rootProcessIntanceId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SAProcessInstance.class, "sourceObjectId", sortingOrder);
        final SelectListDescriptor<Long> selectListDescriptor = new SelectListDescriptor<Long>("getChildrenSourceProcessInstanceIdsFromRootProcessInstance",
                inputParameters, SAProcessInstance.class, queryOptions);
        return persistenceRead.selectList(selectListDescriptor);
    }

    @Override
    public SAProcessInstance getLastArchivedProcessInstance(long processInstanceId) throws SBonitaReadException {
        final SAProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);
        final FilterOption filterOption = new FilterOption(SAProcessInstance.class, processInstanceBuilderFact.getSourceObjectIdKey(), processInstanceId);
        final List<OrderByOption> orderByOptions = new ArrayList<OrderByOption>();
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
        final Map<String, Object> inputParameters = new HashMap<String, Object>();
        inputParameters.put("processDefinitionId", processDefinitionId);
        final SelectOneDescriptor<Long> countDescriptor = new SelectOneDescriptor<Long>("countProcessInstancesOfProcessDefinition", inputParameters,
                SProcessInstance.class);
        return persistenceRead.selectOne(countDescriptor);
    }

}
