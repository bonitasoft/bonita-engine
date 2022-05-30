/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceDeletionException;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SAProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDeleteDataInstanceException;
import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;

/**
 * This implementation only override the delete of archive process instances behavior.
 * This behavior is the same as 7.7.4 and before
 * It is not used by default but is a fallback in case the trigger of events on archived elements deletion is important
 * It will most likely be deleted in 7.9.0
 */
@Slf4j
@Deprecated
public class ProcessInstanceService7_7_4 extends ProcessInstanceServiceImpl {

    private static final int BATCH_SIZE = 100;

    private Recorder recorder;
    private ReadPersistenceService persistenceRead;
    private ActivityInstanceService activityService;
    private DataInstanceService dataInstanceService;
    private ArchiveService archiveService;
    private ConnectorInstanceService connectorInstanceService;
    private ClassLoaderService classLoaderService;
    private DocumentService documentService;
    private SCommentService commentService;
    private RefBusinessDataService refBusinessDataService;

    public ProcessInstanceService7_7_4(final Recorder recorder, final ReadPersistenceService persistenceRead,
            final ActivityInstanceService activityService, final EventInstanceService bpmEventInstanceService,
            final DataInstanceService dataInstanceService, final ArchiveService archiveService,
            final ProcessDefinitionService processDefinitionService,
            final ConnectorInstanceService connectorInstanceService, final ClassLoaderService classLoaderService,
            final DocumentService documentService,
            final SCommentService commentService, final RefBusinessDataService refBusinessDataService,
            ContractDataService contractDataService) {
        super(recorder, persistenceRead, activityService, bpmEventInstanceService, dataInstanceService,
                archiveService, processDefinitionService,
                connectorInstanceService, classLoaderService, documentService, commentService, refBusinessDataService,
                contractDataService);
        this.recorder = recorder;
        this.persistenceRead = persistenceRead;
        this.activityService = activityService;
        this.dataInstanceService = dataInstanceService;
        this.archiveService = archiveService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.documentService = documentService;
        this.commentService = commentService;
        this.refBusinessDataService = refBusinessDataService;
        log.warn(
                "You are using a deprecated implementation of the ProcessInstanceService, This implementation will most likely be deleted in 7.9.");
    }

    @Override
    public int deleteArchivedProcessInstances(List<Long> sourceProcessInstanceIds) throws SBonitaException {
        final List<SAProcessInstance> saProcessInstances = getArchivedProcessInstancesInAllStates(
                sourceProcessInstanceIds);
        return deleteArchivedParentProcessInstancesAndElements(saProcessInstances);

    }

    private int deleteArchivedParentProcessInstancesAndElements(final List<SAProcessInstance> saProcessInstances)
            throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        int nbDeleted = 0;
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

    private void deleteArchivedParentProcessInstanceAndElements(final SAProcessInstance saProcessInstance)
            throws SFlowNodeReadException,
            SProcessInstanceHierarchicalDeletionException, SProcessInstanceModificationException {
        checkIfCallerIsNotActive(saProcessInstance.getCallerId());
        try {
            deleteArchivedProcessInstanceElements(saProcessInstance.getSourceObjectId(),
                    saProcessInstance.getProcessDefinitionId());
            deleteArchivedProcessInstance(saProcessInstance);
        } catch (final SProcessInstanceModificationException e) {
            getArchivedProcessInstanceAndLogWhenNotFound(saProcessInstance, e);
        }
    }

    private void getArchivedProcessInstanceAndLogWhenNotFound(final SAProcessInstance saProcessInstance,
            final SProcessInstanceModificationException e)
            throws SProcessInstanceModificationException {
        try {
            final SAProcessInstance saProcessInstance2 = getArchivedProcessInstance(saProcessInstance.getId());
            if (saProcessInstance2 != null) {
                // archived process is still here, that's not normal. The problem must be raised:
                throw e;
            }
            log.warn(new SAProcessInstanceNotFoundException(saProcessInstance.getId()).getMessage());
        } catch (final SProcessInstanceReadException e1) {
            log.warn(e.getMessage());
        }
    }

    private void deleteArchivedProcessInstanceElements(final long processInstanceId, final long processDefinitionId)
            throws SProcessInstanceModificationException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader localClassLoader = classLoaderService
                    .getClassLoader(identifier(ScopeType.valueOf("PROCESS"), processDefinitionId));
            Thread.currentThread().setContextClassLoader(localClassLoader);
            deleteArchivedFlowNodeInstances(processInstanceId);
            deleteLocalArchivedDataInstances(processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.toString());
            deleteArchivedDocuments(processInstanceId);
            deleteArchivedConnectorInstances(processInstanceId, SConnectorInstance.PROCESS_TYPE);
            deleteArchivedComments(processInstanceId);
            deleteArchivedChildrenProcessInstanceAndElements(processInstanceId, processDefinitionId);
        } catch (final SBonitaException e) {
            throw new SProcessInstanceModificationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void deleteArchivedComments(final long processInstanceId) throws SBonitaException {
        final List<FilterOption> filters = Collections
                .singletonList(new FilterOption(SAComment.class, SAComment.PROCESSINSTANCEID_KEY, processInstanceId));
        final List<OrderByOption> orderByOptions = Collections
                .singletonList(new OrderByOption(SAComment.class, SAComment.ID_KEY, OrderByType.ASC));
        List<SAComment> searchArchivedComments;
        // fromIndex always will be zero because the elements will be deleted
        final QueryOptions queryOptions = new QueryOptions(0, 100, orderByOptions, filters, null);
        do {
            searchArchivedComments = commentService.searchArchivedComments(queryOptions);
            for (final SAComment saComment : searchArchivedComments) {
                archiveService.recordDelete(new DeleteRecord(saComment));
            }
        } while (!searchArchivedComments.isEmpty());
    }

    private void deleteArchivedDocuments(final long instanceId) throws SObjectModificationException {
        final FilterOption filterOption = new FilterOption(SAMappedDocument.class, "processInstanceId", instanceId);
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(filterOption);
        final QueryOptions queryOptions = new QueryOptions(0, 100, null, filters, null);
        try {
            List<SAMappedDocument> documentMappings;
            do {
                documentMappings = persistenceRead.searchEntity(SAMappedDocument.class, queryOptions, null);
                for (final SAMappedDocument documentMapping : documentMappings) {
                    removeArchivedDocument(documentMapping);
                }
            } while (!documentMappings.isEmpty());
        } catch (final SBonitaException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void deleteArchivedChildrenProcessInstanceAndElements(final long processInstanceId,
            final long processDefinitionId) throws SBonitaException {
        List<Long> childrenProcessInstanceIds;
        do {
            // from index always will be zero because elements will be deleted
            childrenProcessInstanceIds = getArchivedChildrenSourceObjectIdsFromRootProcessInstance(processInstanceId, 0,
                    BATCH_SIZE, OrderByType.ASC);
            deleteArchivedChildrenProcessInstancesAndElements(processDefinitionId, childrenProcessInstanceIds);
        } while (!childrenProcessInstanceIds.isEmpty());
    }

    private void deleteArchivedChildrenProcessInstancesAndElements(final long processDefinitionId,
            final List<Long> childrenProcessInstanceIds)
            throws SBonitaException {
        for (final Long childProcessInstanceId : childrenProcessInstanceIds) {
            deleteArchivedProcessInstanceElements(childProcessInstanceId, processDefinitionId);
            deleteArchivedProcessInstancesOfProcessInstance(childProcessInstanceId);
            refBusinessDataService.deleteArchivedRefBusinessDataInstance(childProcessInstanceId);
        }
    }

    private void deleteArchivedProcessInstancesOfProcessInstance(final long processInstanceId) throws SBonitaException {
        // fromIndex variable is not updated because the elements will be deleted, so we always need to start from zero;
        final SAProcessInstanceBuilderFactory processInstanceBuilderFact = BuilderFactory
                .get(SAProcessInstanceBuilderFactory.class);
        final FilterOption filterOption = new FilterOption(SAProcessInstance.class,
                processInstanceBuilderFact.getSourceObjectIdKey(), processInstanceId);
        final OrderByOption orderBy = new OrderByOption(SAProcessInstance.class, processInstanceBuilderFact.getIdKey(),
                OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(orderBy),
                Collections.singletonList(filterOption), null);

        List<SAProcessInstance> archProcessInstances;
        do {
            archProcessInstances = searchArchivedProcessInstances(queryOptions);
            for (final SAProcessInstance saProcessInstance : archProcessInstances) {
                deleteArchivedProcessInstance(saProcessInstance);
            }
        } while (!archProcessInstances.isEmpty()); // never will be null as the persistence service sends an empty list if there are no results
    }

    private void deleteArchivedFlowNodeInstances(final long processInstanceId) throws SFlowNodeDeletionException {
        try {
            deleteArchivedFlowNodeInstancesAndElements(processInstanceId);
        } catch (final SFlowNodeDeletionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    private void deleteArchivedFlowNodeInstancesAndElements(final long processInstanceId) throws SBonitaException {
        Set<Long> sourceActivityIds = new HashSet<Long>();
        List<SAFlowNodeInstance> saFlowNodeInstances;
        do {
            saFlowNodeInstances = activityService.getArchivedFlowNodeInstances(processInstanceId, 0, BATCH_SIZE);
            sourceActivityIds = deleteArchivedFlowNodeInstancesAndElements(sourceActivityIds, saFlowNodeInstances);
        } while (!saFlowNodeInstances.isEmpty());
    }

    private Set<Long> deleteArchivedFlowNodeInstancesAndElements(final Set<Long> sourceActivityIds,
            final List<SAFlowNodeInstance> saFlowNodeInstances)
            throws SBonitaException {
        Set<Long> newSourceActivityIds = new HashSet<Long>(sourceActivityIds);
        for (final SAFlowNodeInstance saFlowNodeInstance : saFlowNodeInstances) {
            newSourceActivityIds = deleteArchivedFlowNodeInstanceAndElements(newSourceActivityIds, saFlowNodeInstance);
        }
        return newSourceActivityIds;
    }

    private Set<Long> deleteArchivedFlowNodeInstanceAndElements(final Set<Long> sourceActivityIds,
            final SAFlowNodeInstance saFlowNodeInstance)
            throws SBonitaException {
        final Set<Long> newSourceActivityIds = new HashSet<Long>(sourceActivityIds);
        if (saFlowNodeInstance instanceof SAActivityInstance
                && !sourceActivityIds.contains(saFlowNodeInstance.getSourceObjectId())) {
            newSourceActivityIds.add(saFlowNodeInstance.getSourceObjectId());
            deleteArchivedFlowNodeInstanceElements((SAActivityInstance) saFlowNodeInstance);
        }
        deleteArchivedFlowNodeInstance(saFlowNodeInstance);
        return newSourceActivityIds;
    }

    public void deleteArchivedFlowNodeInstance(final SAFlowNodeInstance saFlowNodeInstance)
            throws SFlowNodeDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(saFlowNodeInstance), "ARCHIVED_FLOWNODE_INSTANCE");
        } catch (final SRecorderException e) {
            throw new SFlowNodeDeletionException(e);
        }
    }

    private void deleteArchivedFlowNodeInstanceElements(final SAActivityInstance saActivityInstance)
            throws SBonitaException {
        deleteLocalArchivedDataInstances(saActivityInstance.getSourceObjectId(),
                DataInstanceContainer.ACTIVITY_INSTANCE.toString());
        deleteArchivedConnectorInstances(saActivityInstance.getSourceObjectId(), SConnectorInstance.FLOWNODE_TYPE);
    }

    private void deleteSADataInstance(final SADataInstance dataInstance) throws SDeleteDataInstanceException {
        try {
            recorder.recordDelete(new DeleteRecord(dataInstance), "DATA_INSTANCE");
        } catch (final SRecorderException e) {
            throw new SDeleteDataInstanceException("Impossible to delete data instance", e);
        }
    }

    private void deleteLocalArchivedDataInstances(final long containerId, final String containerType)
            throws SDataInstanceException {
        List<SADataInstance> sDataInstances;
        do {
            sDataInstances = dataInstanceService.getLocalSADataInstances(containerId, containerType, 0, 100);
            for (final SADataInstance sDataInstance : sDataInstances) {
                deleteSADataInstance(sDataInstance);
            }
        } while (!sDataInstances.isEmpty());
    }

    private void deleteArchivedConnectorInstances(final long containerId, final String containerType)
            throws SBonitaException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final List<FilterOption> filters = buildFiltersForConnectors(containerId, containerType);
        final OrderByOption orderBy = new OrderByOption(SAConnectorInstance.class, SConnectorInstance.ID_KEY,
                OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(0, 100, Collections.singletonList(orderBy), filters, null);
        List<SAConnectorInstance> connectorInstances;
        do {
            connectorInstances = connectorInstanceService.searchArchivedConnectorInstance(queryOptions,
                    persistenceService);
            for (final SAConnectorInstance sConnectorInstance : connectorInstances) {
                deleteArchivedConnectorInstance(sConnectorInstance);
            }
        } while (!connectorInstances.isEmpty());
    }

    private void deleteArchivedConnectorInstance(final SAConnectorInstance sConnectorInstance)
            throws SConnectorInstanceDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(sConnectorInstance), "CONNECTOR_INSTANCE");
        } catch (final SRecorderException e) {
            throw new SConnectorInstanceDeletionException(e);
        }
    }

    private List<FilterOption> buildFiltersForConnectors(final long containerId, final String containerType) {
        final List<FilterOption> filters = new ArrayList<>(2);
        filters.add(new FilterOption(SAConnectorInstance.class, SConnectorInstance.CONTAINER_ID_KEY, containerId));
        filters.add(new FilterOption(SAConnectorInstance.class, SConnectorInstance.CONTAINER_TYPE_KEY, containerType));
        return filters;
    }

    private void removeArchivedDocument(final SAMappedDocument mappedDocument)
            throws SRecorderException, SBonitaReadException, SObjectNotFoundException {
        // Delete document itself and the mapping
        delete(mappedDocument);
        delete(documentService.getDocument(mappedDocument.getDocumentId()));
    }

    private void delete(final SLightDocument document) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(document), "SDocument");
    }

    private void delete(final SAMappedDocument mappedDocument) throws SRecorderException {
        recorder.recordDelete(new DeleteRecord(mappedDocument), "SADocumentMapping");
    }

}
