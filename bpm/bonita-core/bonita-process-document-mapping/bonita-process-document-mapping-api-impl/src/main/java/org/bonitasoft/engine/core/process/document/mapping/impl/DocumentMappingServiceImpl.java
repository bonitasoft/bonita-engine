/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.mapping.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingCreationException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingDeletionException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingNotFoundException;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.builder.SADocumentMappingBuilderFactory;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingUpdateBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingUpdateBuilderFactory;
import org.bonitasoft.engine.core.process.document.mapping.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class DocumentMappingServiceImpl implements DocumentMappingService {

    private static final String SUPERVISED_BY = "SupervisedBy";

    private final TechnicalLoggerService technicalLogger;

    private final ReadPersistenceService persistenceService;

    private final Recorder recorder;

    private final EventService eventService;

    private final ArchiveService archiveService;

    public DocumentMappingServiceImpl(final TechnicalLoggerService technicalLogger, final ReadPersistenceService persistenceService, final Recorder recorder,
            final EventService eventService, final ArchiveService archiveService) {
        super();
        this.technicalLogger = technicalLogger;
        this.persistenceService = persistenceService;
        this.recorder = recorder;
        this.eventService = eventService;
        this.archiveService = archiveService;
    }

    @Override
    public SDocumentMapping create(final SDocumentMapping documentMapping) throws SDocumentMappingCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(documentMapping);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DOCUMENTMAPPING).setObject(documentMapping)
                        .done();
            }
            recorder.recordInsert(insertRecord, insertEvent);
            return documentMapping;
        } catch (final SRecorderException e) {
            throw new SDocumentMappingCreationException(e);
        }
    }

    @Override
    public void delete(final SDocumentMapping documentMapping) throws SDocumentMappingDeletionException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(documentMapping);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DOCUMENTMAPPING).setObject(documentMapping)
                        .done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw handleDeletionError("can't delete Document Mapping " + documentMapping, e);
        }
    }

    @Override
    public void delete(final long id) throws SDocumentMappingDeletionException {
        try {
            delete(get(id));
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentMappingDeletionException(e);
        }
    }

    @Override
    public SDocumentMapping get(final long documentMappingId) throws SDocumentMappingNotFoundException {
        try {
            final SDocumentMapping documentMapping = persistenceService.selectById(SelectDescriptorBuilder.getElementById(SDocumentMapping.class,
                    "DocumentMapping", documentMappingId));
            if (documentMapping == null) {
                throw new SDocumentMappingNotFoundException(documentMappingId);
            }
            return documentMapping;
        } catch (final SBonitaReadException e) {
            throw new SDocumentMappingNotFoundException(documentMappingId, e);
        }
    }

    @Override
    public List<SDocumentMapping> getDocumentMappingsForProcessInstance(final long processInstanceId, final int fromIndex, final int maxResults,
            final String sortFieldName, final OrderByType order) throws SDocumentMappingException {
        try {
            List<SDocumentMapping> documentMappingList = persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsforProcessInstance(
                    processInstanceId, fromIndex, maxResults, sortFieldName, order));
            if (documentMappingList == null) {
                documentMappingList = Collections.emptyList();
            }
            return documentMappingList;
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the document mappings", e);
        }
    }

    @Override
    public List<SDocumentMapping> getDocumentMappingsForProcessInstanceOrderedById(final long processInstanceId, final int fromIndex, final int maxResults)
            throws SDocumentMappingException {
        try {
            List<SDocumentMapping> documentMappingList = persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsforProcessInstance(
                    processInstanceId, fromIndex, maxResults, null, null));
            if (documentMappingList == null) {
                documentMappingList = Collections.emptyList();
            }
            return documentMappingList;
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the document mappings", e);
        }
    }

    private SDocumentMappingNotFoundException handleNotFoundError(final String message, final Exception e) {
        if (e != null && technicalLogger.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.ERROR, message, e);
        }
        return new SDocumentMappingNotFoundException(message, e);
    }

    private SDocumentMappingDeletionException handleDeletionError(final String message, final Exception e) {
        if (e != null && technicalLogger.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
            technicalLogger.log(this.getClass(), TechnicalLogSeverity.ERROR, message, e);
        }
        return new SDocumentMappingDeletionException(message, e);
    }

    @Override
    public SDocumentMapping get(final long processInstanceId, final String documentName) throws SDocumentMappingNotFoundException {
        try {
            final SDocumentMapping docMapping = persistenceService.selectOne(SelectDescriptorBuilder.getDocumentByName(processInstanceId, documentName));
            if (docMapping == null) {
                throw new SDocumentMappingNotFoundException("can't find the document named " + documentName + " on process instance with id "
                        + processInstanceId);
            }
            return docMapping;
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the document mappings", e);
        }
    }

    @Override
    public SADocumentMapping get(final long processInstanceId, final String documentName, final long time) throws SDocumentMappingNotFoundException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final List<SADocumentMapping> docMapping = persistenceService.selectList(SelectDescriptorBuilder.getArchivedDocumentByName(processInstanceId,
                    documentName, time));
            if (docMapping.isEmpty()) {
                throw new SDocumentMappingNotFoundException("Can't find the archived document named " + documentName + " on process instance with id "
                        + processInstanceId + " for the time " + time);
            }
            return docMapping.get(0);
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the document mappings", e);
        }
        // FIXME throws a SDocumentMappingReadException when we can't read on the persistence service instead of not found...
    }

    @Override
    public long getNumberOfDocumentMappingsForProcessInstance(final long processInstanceId) throws SDocumentMappingException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfDocumentMappingsforProcessInstance(processInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SDocumentMappingException("Unable to count number of documents for process instance : " + processInstanceId, e);
        }
    }

    @Override
    public SDocumentMapping update(final SDocumentMapping docMapping) throws SDocumentMappingException {
        final SDocumentMapping sDocumentMapping = get(docMapping.getProcessInstanceId(), docMapping.getDocumentName());
        archiveOldMappingIfNecessary(sDocumentMapping, System.currentTimeMillis());
        final EntityUpdateDescriptor descriptor = buildUpdateDescriptor(docMapping);
        return updateMapping(sDocumentMapping, descriptor);
    }

    @Override
    public void archive(final SDocumentMapping docMapping, final long archiveDate) throws SDocumentMappingException {
        archiveOldMappingIfNecessary(docMapping, archiveDate);
        delete(docMapping);
    }

    private void archiveOldMappingIfNecessary(final SDocumentMapping docMapping, final long archiveDate) throws SDocumentMappingException {
        if (archiveService.isArchivable(SDocumentMapping.class)) {
            final SADocumentMapping sArchivedDocumentMapping = buildMappingToArchive(docMapping);
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(sArchivedDocumentMapping);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (final Exception e) {
                if (technicalLogger.isLoggable(getClass(), TechnicalLogSeverity.ERROR)) {
                    technicalLogger.log(this.getClass(), TechnicalLogSeverity.ERROR, "The document mapping was not archived id = <" + docMapping.getId() + ">",
                            e);
                }
                throw new SDocumentMappingException(docMapping.getId(), e);
            }
        }
    }

    private SADocumentMapping buildMappingToArchive(final SDocumentMapping docMapping) {
        return BuilderFactory.get(SADocumentMappingBuilderFactory.class).createNewInstance(docMapping).done();
    }

    private EntityUpdateDescriptor buildUpdateDescriptor(final SDocumentMapping docMapping) {
        final SDocumentMappingUpdateBuilder updateBuilder = BuilderFactory.get(SDocumentMappingUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.setDocumentAuthor(docMapping.getDocumentAuthor());
        updateBuilder.setDocumentContentFileName(docMapping.getDocumentContentFileName());
        updateBuilder.setDocumentContentMimeType(docMapping.getDocumentContentMimeType());
        updateBuilder.setDocumentCreationDate(docMapping.getDocumentCreationDate());
        updateBuilder.setDocumentStorageId(docMapping.getContentStorageId());
        updateBuilder.setDocumentURL(docMapping.getDocumentURL());
        updateBuilder.setHasContent(docMapping.documentHasContent());
        return updateBuilder.done();
    }

    private SDocumentMapping updateMapping(final SDocumentMapping docMapping, final EntityUpdateDescriptor descriptor) throws SDocumentMappingException {
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(docMapping, descriptor);
        try {
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DOCUMENTMAPPING).setObject(docMapping).done();
            }
            recorder.recordUpdate(updateRecord, updateEvent);
            return docMapping;
        } catch (final SRecorderException e) {
            throw new SDocumentMappingException("Impossible to update document.", e);
        }
    }

    @Override
    public long getNumberOfDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SDocumentMapping.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SDocumentMapping> searchDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SDocumentMapping.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        try {
            return persistenceService.getNumberOfEntities(SDocumentMapping.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SDocumentMapping> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.searchEntity(SDocumentMapping.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfArchivedDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            return persistenceService.getNumberOfEntities(SADocumentMapping.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SADocumentMapping> searchArchivedDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            return persistenceService.searchEntity(SADocumentMapping.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        try {
            return persistenceService.getNumberOfEntities(SADocumentMapping.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SADocumentMapping> searchArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.searchEntity(SADocumentMapping.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public SADocumentMapping getArchivedDocument(final long archivedDocumentId) throws SDocumentMappingNotFoundException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final SADocumentMapping docMapping = persistenceService.selectById(SelectDescriptorBuilder.getArchivedDocumentById(archivedDocumentId));
            if (docMapping == null) {
                throw new SDocumentMappingNotFoundException(archivedDocumentId);
            }
            return docMapping;
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the archived document mappings", e);
        }
    }

    @Override
    public SADocumentMapping getArchivedVersionOfDocument(final long documentId) throws SDocumentMappingNotFoundException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final SADocumentMapping docMapping = persistenceService.selectOne(SelectDescriptorBuilder.getArchivedVersionOdDocument(documentId));
            if (docMapping == null) {
                throw new SDocumentMappingNotFoundException(documentId);
            }
            return docMapping;
        } catch (final SBonitaReadException e) {
            throw handleNotFoundError("Can't get the archived document mappings", e);
        }
    }

    @Override
    public void delete(final SADocumentMapping documentMapping) throws SDocumentMappingDeletionException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(documentMapping);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(DOCUMENTMAPPING).setObject(documentMapping)
                        .done();
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw handleDeletionError("can't delete archived Document Mapping " + documentMapping, e);
        }
    }

}
