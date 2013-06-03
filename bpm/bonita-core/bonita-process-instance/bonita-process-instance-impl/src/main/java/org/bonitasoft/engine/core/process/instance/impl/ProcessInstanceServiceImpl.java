/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentDeletionException;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TokenService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STaskVisibilityException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.ProcessInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.document.SDocumentException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

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

    private static final String MANAGED_BY = "ManagedBy";

    private static final int BATCH_SIZE = 100;

    private final Recorder recorder;

    private final ReadPersistenceService persistenceRead;

    private final EventService eventService;

    private final ActivityInstanceService activityService;

    private final SProcessInstanceBuilder processInstanceKeyProvider;

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final EventInstanceService bpmEventInstanceService;

    private final DataInstanceService dataInstanceService;

    private final ArchiveService archiveService;

    private final TechnicalLoggerService logger;

    private final QueriableLoggerService queriableLoggerService;

    private final TransitionService transitionService;

    private final ProcessDefinitionService processDefinitionService;

    private final ConnectorInstanceService connectorInstanceService;

    private final ClassLoaderService classLoaderService;

    private final ProcessDocumentService processDocumentService;

    private final SDocumentMappingBuilderAccessor documentMappingBuilderAccessor;

    private final SCommentService commentService;

    private final SCommentBuilders commentBuilders;

    private final TokenService tokenService;

    public ProcessInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final ActivityInstanceService activityService, final TechnicalLoggerService logger, final BPMInstanceBuilders bpmInstanceBuilders,
            final EventInstanceService bpmEventInstanceService, final DataInstanceService dataInstanceService, final ArchiveService archiveService,
            final QueriableLoggerService queriableLoggerService, final TransitionService transitionService,
            final ProcessDefinitionService processDefinitionService, final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService, final ProcessDocumentService processDocumentService,
            final SDocumentMappingBuilderAccessor documentMappingBuilderAccessor, final SCommentService commentService, final SCommentBuilders commentBuilders,
            final TokenService tokenService) {
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.eventService = eventService;
        this.activityService = activityService;
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.transitionService = transitionService;
        this.processDefinitionService = processDefinitionService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.processDocumentService = processDocumentService;
        this.documentMappingBuilderAccessor = documentMappingBuilderAccessor;
        this.commentService = commentService;
        this.commentBuilders = commentBuilders;
        this.tokenService = tokenService;
        processInstanceKeyProvider = bpmInstanceBuilders.getSProcessInstanceBuilder();
        this.bpmEventInstanceService = bpmEventInstanceService;
        this.dataInstanceService = dataInstanceService;
        this.archiveService = archiveService;
        this.logger = logger;
        this.queriableLoggerService = queriableLoggerService;
    }

    private ProcessInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final ProcessInstanceLogBuilder logBuilder = bpmInstanceBuilders.getProcessInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public void createProcessInstance(final SProcessInstance processInstance) throws SProcessInstanceCreationException {
        final ProcessInstanceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new Process Instance");
        final InsertRecord insertRecord = new InsertRecord(processInstance);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.CREATED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            insertEvent = (SInsertEvent) eventBuilder.createInsertEvent(PROCESSINSTANCE).setObject(processInstance).done();
        }
        try {
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "createProcessInstance");
            setProcessState(processInstance, ProcessInstanceState.INITIALIZING);
        } catch (final SRecorderException sre) {
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createProcessInstance");
            throw new SProcessInstanceCreationException(sre);
        } catch (final SProcessInstanceModificationException spicme) {
            throw new SProcessInstanceCreationException(spicme);
        }
    }

    @Override
    public SProcessInstance getProcessInstance(final long processInstanceId) throws SProcessInstanceReadException, SProcessInstanceNotFoundException {
        SProcessInstance instance;
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
    public void deleteProcessInstance(final long processInstanceId) throws SFlowNodeReadException, SProcessInstanceModificationException,
            SProcessInstanceReadException, SProcessInstanceNotFoundException {
        final SProcessInstance processInstance = getProcessInstance(processInstanceId);
        deleteProcessInstance(processInstance);
    }

    @Override
    public void deleteArchivedProcessInstance(final SAProcessInstance archivedProcessInstance) throws SProcessInstanceModificationException,
            SFlowNodeReadException {
        final DeleteRecord deleteRecord = new DeleteRecord(archivedProcessInstance);
        final ProcessInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting process instance");
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.DELETED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(PROCESSINSTANCE).setObject(archivedProcessInstance).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(archivedProcessInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteArchivedProcessInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(archivedProcessInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteArchivedProcessInstance");
            throw new SProcessInstanceModificationException(e);
        }
    }

    @Override
    public void deleteArchivedProcessInstanceElements(final long processInstanceId, final long processDefinitionId) throws SFlowNodeReadException,
            SProcessInstanceModificationException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);
            deleteArchivedFlowNodeInstances(processInstanceId);
            deleteArchivedDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString());
            processDocumentService.deleteArchivedDocuments(processInstanceId, persistenceRead);
            deleteArchivedConnectorInstances(processInstanceId, SConnectorInstance.PROCESS_TYPE);
            deleteArchivedTransitionsOfProcessInstance(processInstanceId);
            deleteArchivedComments(processInstanceId);
            deleteArchivedProcessInstancesOfProcessInstance(processInstanceId, persistenceRead);
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
            }
        } while (!childrenProcessInstanceIds.isEmpty());
    }

    private void deleteArchivedConnectorInstances(final long containerId, final String containerType) throws SBonitaSearchException,
            SConnectorInstanceDeletionException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final List<FilterOption> filters = getFiltersForConnectors(containerId, containerType, true);
        final SConnectorInstanceBuilder connectorKeyProvider = bpmInstanceBuilders.getSConnectorInstanceBuilder();
        final OrderByOption orderBy = new OrderByOption(SAConnectorInstance.class, connectorKeyProvider.getIdKey(), OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, BATCH_SIZE, Collections.singletonList(orderBy), filters, null);
        List<SAConnectorInstance> connectorInstances = null;
        do {
            connectorInstances = connectorInstanceService.searchArchivedConnectorInstance(queryOptions, persistenceService);
            for (final SAConnectorInstance sConnectorInstance : connectorInstances) {
                connectorInstanceService.deleteArchivedConnectorInstance(sConnectorInstance);
            }
        } while (connectorInstances != null && !connectorInstances.isEmpty());

    }

    private void deleteArchivedTransitionsOfProcessInstance(final long processInstanceId) throws STransitionDeletionException, STransitionReadException {
        List<SATransitionInstance> transitionInstances;
        do {
            transitionInstances = transitionService.getArchivedTransitionOfProcessInstance(processInstanceId, 0, BATCH_SIZE);
            for (final SATransitionInstance saTransitionInstance : transitionInstances) {
                transitionService.delete(saTransitionInstance);
            }
        } while (!transitionInstances.isEmpty());
    }

    private void deleteArchivedFlowNodeInstances(final long processInstanceId) throws SFlowNodeReadException, SProcessInstanceModificationException,
            SBonitaSearchException, SConnectorInstanceDeletionException {
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
                deleteArchivedFlowNodeInstance(activityInstance);
            }
            for (final SAFlowNodeInstance orgActivity : orgActivities) {
                deleteArchivedFlowNodeInstanceElements(orgActivity);
            }
        } while (!activityInstances.isEmpty());
    }

    public void deleteArchivedFlowNodeInstance(final SAFlowNodeInstance activityInstance) throws SFlowNodeReadException, SProcessInstanceModificationException {
        final SFlowNodeInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting archived flow node instance", activityInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(activityInstance);
        SDeleteEvent deleteEvent = null;
        if (eventService.hasHandlers(ARCHIVED_ACTIVITYINSTANCE, EventActionType.DELETED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(ARCHIVED_ACTIVITYINSTANCE).setObject(activityInstance).done();
        }
        try {
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(activityInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteArchivedFlowNodeInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(activityInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteArchivedFlowNodeInstance");

            throw new SProcessInstanceModificationException(e);
        }
    }

    private void deleteArchivedFlowNodeInstanceElements(final SAFlowNodeInstance activityInstance) throws SFlowNodeReadException,
            SProcessInstanceModificationException, SBonitaSearchException, SConnectorInstanceDeletionException {
        if (activityInstance instanceof SAActivityInstance) {
            deleteArchivedDataInstances(activityInstance.getSourceObjectId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString());
            deleteArchivedConnectorInstances(activityInstance.getSourceObjectId(), SConnectorInstance.FLOWNODE_TYPE);
            if (SFlowNodeType.USER_TASK.equals(activityInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(activityInstance.getType())) {
                try {
                    activityService.deleteArchivedPendingMappings(activityInstance.getSourceObjectId());
                } catch (final SActivityModificationException e) {
                    throw new SFlowNodeReadException(e);
                }
            }
        }
    }

    private void deleteArchivedDataInstances(final long containerId, final String dataInstanceContainerType) throws SProcessInstanceModificationException {
        try {
            List<SADataInstance> sDataInstances;
            do {
                sDataInstances = dataInstanceService.getLocalSADataInstances(containerId, dataInstanceContainerType, 0, BATCH_SIZE);
                for (final SADataInstance sDataInstance : sDataInstances) {
                    deleteArchivedProcessDataInstance(sDataInstance);
                }
            } while (!sDataInstances.isEmpty());
        } catch (final SDataInstanceException sdie) {
            throw new SProcessInstanceModificationException(sdie);
        }
    }

    private void deleteArchivedProcessDataInstance(final SADataInstance sDataInstance) throws SProcessInstanceModificationException {
        try {
            dataInstanceService.deleteSADataInstance(sDataInstance);
        } catch (final SDataInstanceException sdie) {
            throw new SProcessInstanceModificationException(sdie);
        }
    }

    private void deleteArchivedComments(final long processInstanceId) throws SBonitaException {
        final SACommentBuilder archCommentKeyProvider = commentBuilders.getSACommentBuilder();
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(SAComment.class, "processInstanceId", processInstanceId));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SAComment.class, archCommentKeyProvider.getIdKey(),
                OrderByType.ASC));
        List<SAComment> searchArchivedComments = null;
        // fromIndex always will be zero because the elements will be deleted
        final QueryOptions queryOptions = new QueryOptions(0, BATCH_SIZE, orderByOptions, filters, null);
        do {
            searchArchivedComments = commentService.searchArchivedComments(queryOptions, persistenceRead);
            for (final SAComment saComment : searchArchivedComments) {
                archiveService.recordDelete(new DeleteRecord(saComment), null);
            }
        } while (!searchArchivedComments.isEmpty());
    }

    @Override
    public List<Long> getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(final ReadPersistenceService persistenceService,
            final long processDefinitionId, final int fromIndex, final int maxResults, final OrderByType sortingOrder) throws SProcessInstanceReadException {
        final SACommentBuilder archCommentKeyProvider = commentBuilders.getSACommentBuilder();
        final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, SAProcessInstance.class, archCommentKeyProvider.getSourceObjectId(),
                sortingOrder);
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getSourceProcesInstanceIdsOfArchProcessInstancesFromDefinition(processDefinitionId,
                    queryOptions));
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
    }

    @Override
    public void deleteProcessInstance(final SProcessInstance processInstance) throws SFlowNodeReadException, SProcessInstanceModificationException {
        final ProcessInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting process instance");
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final long processDefinitionId = processInstance.getProcessDefinitionId();
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);
            deleteProcessInstanceElements(processInstance);
            final DeleteRecord deleteRecord = new DeleteRecord(processInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.DELETED)) {
                final SEventBuilder eventBuilder = eventService.getEventBuilder();
                deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(PROCESSINSTANCE).setObject(processInstance).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteProcessInstance");
        } catch (final SBonitaException e) {
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteProcessInstance");
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void deleteProcessInstanceElements(final SProcessInstance processInstance) throws SBonitaException {
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
        deleteDocumentsFromProcessInstance(processInstance.getId());
        deleteConnectorInstancesIfNecessary(processInstance, processDefinition);
        deleteProcessComments(processInstance.getId());
    }

    private void deleteConnectorInstancesIfNecessary(final SProcessInstance processInstance, final SProcessDefinition processDefinition)
            throws SBonitaSearchException, SConnectorInstanceDeletionException {
        if (hasConnectors(processDefinition)) {
            deleteConnectors(processInstance.getId(), SConnectorInstance.PROCESS_TYPE);
        }
    }

    private boolean hasConnectors(final SProcessDefinition processDefinition) {
        boolean hasConnectors = false;
        if (processDefinition != null) {
            hasConnectors = processDefinition.getProcessContainer().getConnectors().size() > 0;
        }
        return hasConnectors;
    }

    private void deleteDataInstancesIfNecessary(final SProcessInstance processInstance, final SProcessDefinition processDefinition)
            throws SProcessInstanceModificationException {
        boolean dataPresent = true;
        if (processDefinition != null) {
            dataPresent = processDefinition.getProcessContainer().getDataDefinitions().size() > 0;
        }
        deleteDataInstances(processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.toString(), dataPresent);
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

    private void deleteProcessComments(final long processInstanceId) throws SBonitaException {
        final List<SComment> sComments = commentService.getComments(processInstanceId);
        for (final SComment sComment : sComments) {
            commentService.delete(sComment);
        }
    }

    private void deleteArchivedProcessInstancesOfProcessInstance(final long processInstanceId, final ReadPersistenceService archivePersistenceService)
            throws SBonitaException {
        final int fromIndex = 0;
        final int maxResults = 100;
        final SAProcessInstanceBuilder archProcInstKeyProvider = bpmInstanceBuilders.getSAProcessInstanceBuilder();

        List<SAProcessInstance> archProcInstances = null;
        do {
            // fromIndex variable is not updated because the elements will be deleted, so we always need to start from zero;
            final SAProcessInstanceBuilder processInstanceBuilder = bpmInstanceBuilders.getSAProcessInstanceBuilder();
            final FilterOption filterOption = new FilterOption(SAProcessInstance.class, processInstanceBuilder.getSourceObjectIdKey(), processInstanceId);
            final OrderByOption orderBy = new OrderByOption(SAProcessInstance.class, archProcInstKeyProvider.getIdKey(), OrderByType.ASC);
            final QueryOptions queryOptions = new QueryOptions(fromIndex, maxResults, Collections.singletonList(orderBy),
                    Collections.singletonList(filterOption), null);
            archProcInstances = searchArchivedProcessInstances(queryOptions, archivePersistenceService);
            for (final SAProcessInstance archProcInstance : archProcInstances) {
                deleteArchivedProcessInstance(archProcInstance);
            }
        } while (!archProcInstances.isEmpty()); // never will be null as the persistence service sends an empty list if there are no results
    }

    private void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SProcessDocumentDeletionException, SDocumentException {
        final SDocumentMappingBuilder documentMappingKeyProvider = documentMappingBuilderAccessor.getSDocumentMappingBuilder();
        List<SProcessDocument> sProcessDocuments;
        do {
            sProcessDocuments = processDocumentService.getDocumentsOfProcessInstance(processInstanceId, 0, 100,
                    documentMappingKeyProvider.getDocumentNameKey(), OrderByType.ASC);
            processDocumentService.removeDocuments(sProcessDocuments);
        } while (!sProcessDocuments.isEmpty());
    }

    @Override
    public void deleteFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) throws SFlowNodeReadException,
            SProcessInstanceModificationException {
        final SFlowNodeInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting flow node instance", flowNodeInstance);
        try {
            deleteFlowNodeInstanceElements(flowNodeInstance, processDefinition);
            final DeleteRecord deleteRecord = new DeleteRecord(flowNodeInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(ACTIVITYINSTANCE, EventActionType.DELETED)) {
                final SEventBuilder eventBuilder = eventService.getEventBuilder();
                deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(ACTIVITYINSTANCE).setObject(flowNodeInstance).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(flowNodeInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteFlowNodeInstance");
        } catch (final SBonitaException e) {
            initiateLogBuilder(flowNodeInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteFlowNodeInstance");
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void deleteFlowNodeInstanceElements(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) throws SBonitaException {
        if (flowNodeInstance.getType().equals(SFlowNodeType.INTERMEDIATE_CATCH_EVENT)) {
            deleteWaitingEvents(flowNodeInstance);
        }
        if (flowNodeInstance instanceof SEventInstance) {
            deleteEventTriggerInstances(flowNodeInstance.getId());
        } else if (flowNodeInstance instanceof SActivityInstance) {
            deleteDataInstancesIfNecessary(flowNodeInstance, processDefinition);
            deleteConnectorInstancesIfNecessary(flowNodeInstance, processDefinition);
            if (SFlowNodeType.USER_TASK.equals(flowNodeInstance.getType()) || SFlowNodeType.MANUAL_TASK.equals(flowNodeInstance.getType())) {
                deleteHiddenTasks(flowNodeInstance.getId());
                try {
                    activityService.deletePendingMappings(flowNodeInstance.getId());
                } catch (final SActivityModificationException e) {
                    throw new SFlowNodeReadException(e);
                }
            } else if (SFlowNodeType.CALL_ACTIVITY.equals(flowNodeInstance.getType()) || SFlowNodeType.SUB_PROCESS.equals(flowNodeInstance.getType())) {
                // in the case of a call activity or subprocess activity delete the child process instance
                try {
                    deleteProcessInstance(getChildOfActivity(flowNodeInstance.getId()));
                } catch (final SProcessInstanceNotFoundException e) {
                    final StringBuilder stb = new StringBuilder();
                    stb.append("Can't find the process instance called by the activity [id: ");
                    stb.append(flowNodeInstance.getId());
                    stb.append(", name: ");
                    stb.append(flowNodeInstance.getName());
                    stb.append("]. This process may be already finished");
                    // if the child process is not found, it's because it has already finished and archived or it was not created
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, stb.toString());
                    logger.log(getClass(), TechnicalLogSeverity.DEBUG, e);
                }
            }
        }
    }

    private void deleteDataInstancesIfNecessary(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition)
            throws SProcessInstanceModificationException {
        boolean dataPresent = true;
        if (processDefinition != null) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {
                dataPresent = activityDefinition.getSDataDefinitions().size() > 0;
            }
        }
        deleteDataInstances(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString(), dataPresent);
        if (dataPresent) {
            deleteDataInstances(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.toString(), dataPresent);
        }
    }

    private void deleteConnectorInstancesIfNecessary(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition)
            throws SBonitaSearchException, SConnectorInstanceDeletionException {
        if (hasConnectors(flowNodeInstance, processDefinition)) {
            deleteConnectors(flowNodeInstance.getId(), SConnectorInstance.FLOWNODE_TYPE);
        }
    }

    private boolean hasConnectors(final SFlowNodeInstance flowNodeInstance, final SProcessDefinition processDefinition) {
        boolean hasConnectors = false;
        if (processDefinition != null) {
            final SActivityDefinition activityDefinition = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(
                    flowNodeInstance.getFlowNodeDefinitionId());
            if (activityDefinition != null) {
                hasConnectors = activityDefinition.getConnectors().size() > 0;
            }
        }
        return hasConnectors;
    }

    private void deleteConnectors(final long containerId, final String containerType) throws SBonitaSearchException, SConnectorInstanceDeletionException {
        final List<FilterOption> filters = getFiltersForConnectors(containerId, containerType, false);
        final SConnectorInstanceBuilder connectorKeyProvider = bpmInstanceBuilders.getSConnectorInstanceBuilder();
        final OrderByOption orderBy = new OrderByOption(SConnectorInstance.class, connectorKeyProvider.getIdKey(), OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, BATCH_SIZE, Collections.singletonList(orderBy), filters, null);
        List<SConnectorInstance> connetorInstances;
        do {
            // the QueryOptions always will use 0 as start index because the retrieved results will be deleted
            connetorInstances = connectorInstanceService.searchConnetorInstances(queryOptions);
            for (final SConnectorInstance sConnectorInstance : connetorInstances) {
                connectorInstanceService.deleteConnectorInstance(sConnectorInstance);
            }
        } while (!connetorInstances.isEmpty());
    }

    private List<FilterOption> getFiltersForConnectors(final long containerId, final String containerType, final boolean archived) {
        final SConnectorInstanceBuilder connectorKeyProvider = bpmInstanceBuilders.getSConnectorInstanceBuilder();
        final List<FilterOption> filters = new ArrayList<FilterOption>(2);
        Class<? extends PersistentObject> persistentClass;
        if (archived) {
            persistentClass = SAConnectorInstance.class;
        } else {
            persistentClass = SConnectorInstance.class;
        }
        filters.add(new FilterOption(persistentClass, connectorKeyProvider.getContainerIdKey(), containerId));
        filters.add(new FilterOption(persistentClass, connectorKeyProvider.getContainerTypeKey(), containerType));
        return filters;
    }

    private void deleteWaitingEvents(final SFlowNodeInstance flowNodeInstance) throws SFlowNodeReadException, SProcessInstanceModificationException {
        final OrderByOption orderByOption = new OrderByOption(SWaitingEvent.class, bpmInstanceBuilders.getSWaitingMessageEventBuilder().getFlowNodeNameKey(),
                OrderByType.ASC);

        final FilterOption filterOption = new FilterOption(SWaitingEvent.class,
                bpmInstanceBuilders.getSWaitingMessageEventBuilder().getFlowNodeInstanceIdKey(), flowNodeInstance.getId());
        final List<FilterOption> filters = Collections.singletonList(filterOption);
        try {
            QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(orderByOption), filters, null);
            List<SWaitingEvent> waitingEvents = bpmEventInstanceService.searchWaitingEvents(SWaitingEvent.class, queryOptions);

            do {
                for (final SWaitingEvent sWaitingEvent : waitingEvents) {
                    bpmEventInstanceService.deleteWaitingEvent(sWaitingEvent);
                }
                queryOptions = new QueryOptions(0, 10, Collections.singletonList(orderByOption), filters, null);
                waitingEvents = bpmEventInstanceService.searchWaitingEvents(SWaitingEvent.class, queryOptions);
            } while (waitingEvents.size() > 0);
        } catch (final SBonitaSearchException e) {
            throw new SFlowNodeReadException(e); // To change body of catch statement use File | Settings | File Templates.
        } catch (final SWaitingEventModificationException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void deleteHiddenTasks(final long activityInstanceId) throws SProcessInstanceModificationException {
        try {
            activityService.deleteHiddenTasksForActivity(activityInstanceId);
        } catch (final STaskVisibilityException e) {
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void deleteDataInstances(final long containerId, final String dataInstanceContainerType, final boolean dataPresent)
            throws SProcessInstanceModificationException {
        try {
            if (dataPresent) {
                final int deleteBatchSize = 80;
                List<SDataInstance> sDataInstances = dataInstanceService.getLocalDataInstances(containerId, dataInstanceContainerType, 0, deleteBatchSize);
                while (sDataInstances.size() > 0) {
                    for (final SDataInstance sDataInstance : sDataInstances) {
                        dataInstanceService.deleteDataInstance(sDataInstance);
                    }
                    sDataInstances = dataInstanceService.getLocalDataInstances(containerId, dataInstanceContainerType, 0, deleteBatchSize);
                }
            }
            dataInstanceService.removeContainer(containerId, dataInstanceContainerType);
        } catch (final SDataInstanceException sdie) {
            throw new SProcessInstanceModificationException(sdie);
        }
    }

    private void deleteEventTriggerInstances(final long eventInstanceId) throws SFlowNodeReadException, SProcessInstanceModificationException {
        try {
            final List<SEventTriggerInstance> triggerInstances = bpmEventInstanceService.getEventTriggerInstances(eventInstanceId);
            for (final SEventTriggerInstance eventTriggerInstance : triggerInstances) {
                deleteEventTriggerInstance(eventTriggerInstance);
            }
        } catch (final SEventTriggerInstanceReadException e) {
            throw new SFlowNodeReadException(e);
        }
    }

    private void deleteEventTriggerInstance(final SEventTriggerInstance eventTriggerInstance) throws SProcessInstanceModificationException {
        final SEventTriggerInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting eventTrigger instance", eventTriggerInstance);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(eventTriggerInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.DELETED)) {
                final SEventBuilder eventBuilder = eventService.getEventBuilder();
                deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(EVENT_TRIGGER_INSTANCE).setObject(eventTriggerInstance).done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteEventTriggerInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteEventTriggerInstance");
            throw new SProcessInstanceModificationException(e);
        }
    }

    private SFlowNodeInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SFlowNodeInstance activityInstance) {
        final SFlowNodeInstanceLogBuilder logBuilder = bpmInstanceBuilders.getActivityInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.processInstanceId(activityInstance.getRootContainerId());
        return logBuilder;
    }

    private SFlowNodeInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SAFlowNodeInstance activityInstance) {
        final SFlowNodeInstanceLogBuilder logBuilder = bpmInstanceBuilders.getActivityInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.processInstanceId(activityInstance.getRootContainerId());
        return logBuilder;
    }

    private SEventTriggerInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SEventTriggerInstance eventTriggerInstance) {
        final SEventTriggerInstanceLogBuilder logBuilder = bpmInstanceBuilders.getSEventTriggerInstanceLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        logBuilder.eventInstanceId(eventTriggerInstance.getEventInstanceId());
        return logBuilder;
    }

    @Override
    public void setState(final SProcessInstance processInstance, final ProcessInstanceState state) throws SProcessInstanceModificationException {
        // Let's archive the process instance before changing the state (to keep a track of state change):
        archiveProcessInstance(processInstance);
        setProcessState(processInstance, state);
        if (logger.isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, MessageFormat.format("[{0} with id {1}]{2}->{3}(new={4})", processInstance.getClass()
                    .getSimpleName(), processInstance.getId(), processInstance.getStateId(), state.getId(), state.getClass().getSimpleName()));
        }
    }

    @Override
    public void setMigrationPlanId(final SProcessInstance processInstance, final long migrationPlanId) throws SProcessInstanceModificationException {

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(processInstanceKeyProvider.getMigrationPlanIdKey(), migrationPlanId);
        final long now = System.currentTimeMillis();
        descriptor.addField(processInstanceKeyProvider.getLastUpdateKey(), now);

        updateProcessInstance(processInstance, "set migration plan", descriptor, MIGRATION_PLAN);
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

        updateProcessInstance(processInstance, "updating process instance state", descriptor, PROCESSINSTANCE_STATE);
    }

    private void updateProcessInstance(final SProcessInstance processInstance, final String message, final EntityUpdateDescriptor descriptor,
            final String eventType) throws SProcessInstanceModificationException {
        final ProcessInstanceLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, message);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(processInstance, descriptor);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(eventType, EventActionType.UPDATED)) {
            final SEventBuilder eventBuilder = eventService.getEventBuilder();
            updateEvent = (SUpdateEvent) eventBuilder.createUpdateEvent(eventType).setObject(processInstance).done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProcessInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProcessInstance");
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void archiveProcessInstance(final SProcessInstance processInstance) throws SProcessInstanceModificationException {
        final SAProcessInstance saProcessInstance = bpmInstanceBuilders.getSAProcessInstanceBuilder().createNewInstance(processInstance).done();
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saProcessInstance);
        try {
            archiveService.recordInsert(System.currentTimeMillis(), insertRecord, getQueriableLog(ActionType.CREATED, "archive the process instance").done());
        } catch (final SRecorderException e) {
            throw new SProcessInstanceModificationException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "The process instance was not archived. Id:" + processInstance.getId(), e);
            }
        }
    }

    @Override
    public void setStateCategory(final SProcessInstance processInstance, final SStateCategory stateCatetory) throws SProcessInstanceNotFoundException,
            SProcessInstanceModificationException {
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(processInstanceKeyProvider.getStateCategoryKey(), stateCatetory);
        updateProcessInstance(processInstance, "update process instance state category", descriptor, PROCESS_INSTANCE_CATEGORY_STATE);
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
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
    public SProcessInstance getChildOfActivity(final long activityInstId) throws SProcessInstanceNotFoundException, SBonitaSearchException {
        try {
            final SProcessInstanceBuilder processInstanceBuilder = bpmInstanceBuilders.getSProcessInstanceBuilder();
            final FilterOption filterOption = new FilterOption(SProcessInstance.class, processInstanceBuilder.getCallerIdKey(), activityInstId);
            final QueryOptions queryOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, null, Collections.singletonList(filterOption), null);
            return searchProcessInstances(queryOptions).get(0);
        } catch (final IndexOutOfBoundsException e) {
            throw new SProcessInstanceNotFoundException("No process instance was found as child of activity: " + activityInstId);
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
    public long getNumberOfProcessInstances(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchProcessInstances(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceRead.searchEntity(SProcessInstance.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfOpenProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
            return persistenceRead.searchEntity(SProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(USER_ID, userId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(USER_ID, userId);
            return persistenceRead.searchEntity(SProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(MANAGER_USER_ID, managerUserId);
            return persistenceRead.getNumberOfEntities(SProcessInstance.class, INVOLVING_USER + "s" + MANAGED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(final long managerUserId, final QueryOptions queryOptions)
            throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(MANAGER_USER_ID, managerUserId);
            return persistenceRead.searchEntity(SProcessInstance.class, INVOLVING_USER + "s" + MANAGED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstancesWithoutSubProcess(final QueryOptions queryOptions, final ReadPersistenceService persistenceService)
            throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SAProcessInstance.class, "WithoutSubProcess", queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesWithoutSubProcess(final QueryOptions queryOptions,
            final ReadPersistenceService persistenceService) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SAProcessInstance.class, "WithoutSubProcess", queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesSupervisedBy(final long userId, final QueryOptions queryOptions,
            final ReadPersistenceService persistenceService) throws SBonitaSearchException {
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        try {
            return persistenceService.searchEntity(SAProcessInstance.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstancesSupervisedBy(final long userId, final QueryOptions countOptions,
            final ReadPersistenceService persistenceService) throws SBonitaSearchException {
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        try {
            return persistenceService.getNumberOfEntities(SAProcessInstance.class, SUPERVISED_BY, countOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstancesInvolvingUser(final long userId, final QueryOptions countOptions,
            final ReadPersistenceService persistenceService) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(2);
            parameters.put(USER_ID, userId);
            return persistenceService.getNumberOfEntities(SAProcessInstance.class, INVOLVING_USER, countOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedProcessInstances(final QueryOptions queryOptions, final ReadPersistenceService persistenceService)
            throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SAProcessInstance.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstances(final QueryOptions queryOptions, final ReadPersistenceService persistenceService)
            throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SAProcessInstance.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SAProcessInstance> searchArchivedProcessInstancesInvolvingUser(final long userId, final QueryOptions queryOptions,
            final ReadPersistenceService persistenceService) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>(1);
            parameters.put(USER_ID, userId);
            return persistenceService.searchEntity(SAProcessInstance.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void updateProcess(final SProcessInstance processInstance, final EntityUpdateDescriptor descriptor) throws SProcessInstanceModificationException {
        final ProcessInstanceLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "process instance is updated");
        try {
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(processInstance, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(PROCESSINSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) eventService.getEventBuilder().createUpdateEvent(PROCESSINSTANCE).setObject(processInstance).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateProcess");
        } catch (final SRecorderException e) {
            initiateLogBuilder(processInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateProcess");
            throw new SProcessInstanceModificationException(e);
        }
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public SAProcessInstance getArchivedProcessInstance(final long archivedProcessInstanceId, final ReadPersistenceService persistenceService)
            throws SProcessInstanceReadException, SProcessInstanceNotFoundException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("id", (Object) archivedProcessInstanceId);
            final SAProcessInstance saProcessInstance = persistenceService.selectOne(new SelectOneDescriptor<SAProcessInstance>("getArchivedProcessInstance",
                    parameters, SAProcessInstance.class));
            if (saProcessInstance == null) {
                throw new SProcessInstanceNotFoundException(archivedProcessInstanceId);
            }
            return saProcessInstance;
        } catch (final SBonitaReadException e) {
            throw new SProcessInstanceReadException(e);
        }
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
}
