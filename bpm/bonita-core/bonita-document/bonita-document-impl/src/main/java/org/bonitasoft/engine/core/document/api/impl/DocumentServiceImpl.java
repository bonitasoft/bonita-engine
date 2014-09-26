/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.document.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SDocumentException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingDeletionException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingException;
import org.bonitasoft.engine.core.document.exception.SDocumentMappingNotFoundException;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SProcessDocumentContentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SProcessDocumentCreationException;
import org.bonitasoft.engine.core.document.exception.SProcessDocumentDeletionException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.impl.SADocumentMappingImpl;
import org.bonitasoft.engine.core.document.model.impl.SDocumentMappingImpl;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.core.document.model.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Baptiste Mesta
 */
public class DocumentServiceImpl implements DocumentService {

    private final SDocumentDownloadURLProvider urlProvider;
    private final EventService eventService;
    private final ArchiveService archiveService;
    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private ReadPersistenceService definitiveArchiveReadPersistenceService;

    public DocumentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final SDocumentDownloadURLProvider urlProvider, EventService eventService, TechnicalLoggerService technicalLogger, ArchiveService archiveService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.urlProvider = urlProvider;
        this.eventService = eventService;
        this.archiveService = archiveService;
        definitiveArchiveReadPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();

    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, long processInstanceId, String name, String description)
            throws SProcessDocumentCreationException {
        try {
            insertDocument(document);
            SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description, -1);
            return new SMappedDocumentImpl(documentMapping, document);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, long processInstanceId, String name, String description, int index)
            throws SProcessDocumentCreationException {
        try {
            insertDocument(document);
            SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description, index);
            return new SMappedDocumentImpl(documentMapping, document);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SMappedDocument updateDocumentOfProcessInstance(final SDocument document, final long processInstanceId, String name, String description)
            throws SProcessDocumentCreationException {
        try {
            SDocumentMapping sDocumentMapping = getMappedDocument(processInstanceId, name);
            return updateMappedDocument(document, description, -1, sDocumentMapping);

        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    private SMappedDocument updateMappedDocument(SDocument document, String description, int index, SDocumentMapping sDocumentMapping) throws SRecorderException, SDocumentMappingException {
        //insert new document
        insertDocument(document);
        //update mapping
        archive(sDocumentMapping, System.currentTimeMillis());
        updateMapping(document.getId(), sDocumentMapping, description,index);
        return new SMappedDocumentImpl(sDocumentMapping, document);
    }

    @Override
    public void updateDocumentOfList(final SMappedDocument mappedDocument, final SDocument document, int index) throws SProcessDocumentCreationException {
        try {
            updateMappedDocument(document,null,index,mappedDocument);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public void updateDocumentIndex(final SMappedDocument mappedDocument, int index) throws SProcessDocumentCreationException {
        try {
            updateMapping(mappedDocument.getDocumentId(), mappedDocument, mappedDocument.getDescription(), index);
        } catch (SRecorderException e) {
            throw new SProcessDocumentCreationException(e);
        }
    }

    private void updateMapping(long documentId, SDocumentMapping sDocumentMapping, String description, int index) throws SRecorderException {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("documentId", documentId);
        params.put("description", description);
        params.put("version", incrementVersion(sDocumentMapping.getVersion()));
        params.put("index", index);
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sDocumentMapping,
                params);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DOCUMENTMAPPING).setObject(sDocumentMapping)
                    .done();
        }
        recorder.recordUpdate(updateRecord, updateEvent);
    }

    private String incrementVersion(String version) {
        Integer intVersion = Integer.valueOf(version);
        return String.valueOf(intVersion + 1);
    }

    private SDocument insertDocument(SDocument document) throws SRecorderException {
        final InsertRecord insertRecord = new InsertRecord(document);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(DOCUMENT, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DOCUMENT).setObject(document)
                    .done();
        }
        recorder.recordInsert(insertRecord, insertEvent);
        return document;
    }

    @Override
    public void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SBonitaReadException, SProcessDocumentDeletionException {
        List<SMappedDocument> mappedDocuments;
        do {
            mappedDocuments = persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
                    processInstanceId, 0, 100, null, null));
            for (SMappedDocument mappedDocument : mappedDocuments) {
                removeDocument(mappedDocument);
            }
        } while (!mappedDocuments.isEmpty());
    }

    @Override
    public String generateDocumentURL(final String name, final String contentStorageId) {
        return urlProvider.generateURL(name, contentStorageId);
    }

    @Override
    public SAMappedDocument getArchivedDocument(final long archivedProcessDocumentId) throws SDocumentNotFoundException {
        try {
            final SAMappedDocument docMapping = definitiveArchiveReadPersistenceService.selectById(SelectDescriptorBuilder
                    .getArchivedDocumentById(archivedProcessDocumentId));
            if (docMapping == null) {
                throw new SDocumentMappingNotFoundException(archivedProcessDocumentId);
            }
            return docMapping;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found with identifier: " + archivedProcessDocumentId, e);
        } catch (SBonitaReadException e) {
            throw new SDocumentNotFoundException(e);
        }
    }

    @Override
    public SAMappedDocument getArchivedVersionOfProcessDocument(final long documentId) throws SDocumentNotFoundException {
        try {
            SAMappedDocument aDocMapping = definitiveArchiveReadPersistenceService.selectOne(SelectDescriptorBuilder.getArchivedVersionOdDocument(documentId));
            if (aDocMapping == null) {
                throw new SDocumentNotFoundException(documentId);
            }
            return aDocMapping;
        } catch (final SBonitaReadException e) {
            throw new SDocumentNotFoundException("Document not found with identifier: " + documentId, e);
        }
    }

    @Override
    public SLightDocument getDocument(final long documentId) throws SDocumentNotFoundException, SBonitaReadException {
        final SLightDocument document = persistenceService.selectById(new SelectByIdDescriptor<SLightDocument>("getLightDocumentById", SLightDocument.class,
                documentId));
        if (document == null) {
            throw new SDocumentNotFoundException("Document with id " + documentId + " not found");
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(long processInstanceId, String documentName) throws SDocumentNotFoundException, SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("name", documentName);
        SelectOneDescriptor<SMappedDocument> selectOneDescriptor = new SelectOneDescriptor<SMappedDocument>("getSMappedDocumentOfProcessWithName", parameters,
                SDocument.class);
        final SMappedDocument document = persistenceService.selectOne(selectOneDescriptor);
        if (document == null) {
            throw new SDocumentNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId);
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(final long processInstanceId, final String documentName, final long time) throws SDocumentNotFoundException,
            SBonitaReadException {
        final List<SAMappedDocument> docMapping = definitiveArchiveReadPersistenceService.selectList(SelectDescriptorBuilder
                .getSAMappedDocumentOfProcessWithName(
                        processInstanceId,
                        documentName, time));
        if (docMapping.isEmpty()) {
            return getMappedDocument(processInstanceId, documentName);
        } else {
            return docMapping.get(0);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentId) throws SProcessDocumentContentNotFoundException {
        try {
            final SDocument document = persistenceService.selectById(new SelectByIdDescriptor<SDocument>("geDocumentById", SDocument.class, Long
                    .valueOf(documentId)));
            if (document == null) {
                throw new SProcessDocumentContentNotFoundException("Document with id " + documentId + " not found");
            }
            return document.getContent();
        } catch (NumberFormatException e) {
            throw new SProcessDocumentContentNotFoundException("Identifier " + documentId + " is not valid, it must be a long");
        } catch (final SBonitaReadException e) {
            throw new SProcessDocumentContentNotFoundException(e);
        }
    }

    @Override
    public SMappedDocument getMappedDocument(long mappingId) throws SDocumentNotFoundException, SBonitaReadException {
        final SMappedDocument document = persistenceService.selectById(new SelectByIdDescriptor<SMappedDocument>("getSMappedDocumentById",
                SMappedDocument.class,
                mappingId));
        if (document == null) {
            throw new SDocumentNotFoundException("SMappedDocument with id " + mappingId + " not found");
        }
        return document;
    }

    @Override
    public List<SMappedDocument> getDocumentsOfProcessInstance(final long processInstanceId, final int fromIndex, final int numberPerPage, final String field,
            final OrderByType order) throws SDocumentException {
        try {
            return persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
                    processInstanceId, fromIndex, numberPerPage, field, order));
        } catch (final SBonitaReadException e) {
            throw new SDocumentException("Unable to list documents of process instance: " + processInstanceId, e);
        }
    }

    @Override
    public long getNumberOfArchivedDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        try {
            return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SMappedDocument.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfDocumentsOfProcessInstance(final long processInstanceId) throws SDocumentException {
        try {
            return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfSMappedDocumentOfProcess(processInstanceId));
        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to count documents of process instance: " + processInstanceId, e);
        }
    }

    @Override
    public long getNumberOfDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        try {
            return persistenceService.getNumberOfEntities(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    public void removeCurrentVersion(SMappedDocument document) throws SDocumentNotFoundException, SObjectModificationException {
        try {
            archive(document, System.currentTimeMillis());
            removeDocument(document);
        } catch (final SDocumentMappingException e) {
            throw new SObjectModificationException(e);
        } catch (SProcessDocumentDeletionException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void removeCurrentVersion(final long processInstanceId, final String documentName) throws SDocumentNotFoundException, SObjectModificationException {
        try {
            removeCurrentVersion(getMappedDocument(processInstanceId, documentName));
        } catch (SBonitaReadException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void removeArchivedDocument(final SAMappedDocument mappedDocument) throws SRecorderException, SBonitaReadException, SDocumentNotFoundException {
        // Delete document itself and the mapping
        delete((SADocumentMapping) mappedDocument);
        delete(getDocument(mappedDocument.getDocumentId()));
    }

    @Override
    public void deleteDocument(SLightDocument document) throws SProcessDocumentDeletionException {
        try {
            delete(document);
        } catch (SRecorderException e) {
            throw new SProcessDocumentDeletionException(e);
        }
    }

    private void delete(SLightDocument document) throws SRecorderException {
        final DeleteRecord deleteDocRecord = new DeleteRecord(document);
        final SDeleteEvent deleteDocEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SDocument")
                .setObject(document)
                .done();
        recorder.recordDelete(deleteDocRecord, deleteDocEvent);
    }

    private void delete(SADocumentMapping mappedDocument) throws SRecorderException {
        final DeleteRecord deleteRecord = new DeleteRecord(mappedDocument);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SADocumentMapping")
                .setObject(mappedDocument)
                .done();
        recorder.recordDelete(deleteRecord, deleteEvent);
    }

    @Override
    public void removeDocument(final SMappedDocument mappedDocument) throws SProcessDocumentDeletionException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(mappedDocument);
            final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SDocumentMapping")
                    .setObject(mappedDocument)
                    .done();
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentDeletionException(e.getMessage(), e);
        }
    }

    @Override
    public List<SAMappedDocument> searchArchivedDocuments(final QueryOptions queryOptions)
            throws SBonitaSearchException {
        final ReadPersistenceService persistenceService1 = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            return persistenceService1.searchEntity(SAMappedDocument.class, queryOptions, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SAMappedDocument> searchArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService1 = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService1.searchEntity(SAMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SMappedDocument> searchDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SMappedDocument.class, queryOptions, null);
        } catch (SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SMappedDocument> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.searchEntity(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void deleteArchivedDocuments(final long instanceId) throws SDocumentMappingDeletionException {
        final FilterOption filterOption = new FilterOption(SAMappedDocument.class, "processInstanceId", instanceId);
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(filterOption);
        final QueryOptions queryOptions = new QueryOptions(0, 100, null, filters, null);
        try {
            List<SAMappedDocument> documentMappings;
            do {
                documentMappings = definitiveArchiveReadPersistenceService.searchEntity(SAMappedDocument.class, queryOptions, null);
                for (final SAMappedDocument documentMapping : documentMappings) {
                    removeArchivedDocument(documentMapping);
                }
            } while (!documentMappings.isEmpty());
        } catch (final SBonitaException e) {
            throw new SDocumentMappingDeletionException(e);
        }
    }

    private SDocumentMapping create(long documentId, long processInstanceId, String name, String description, int index) throws SRecorderException {
        SDocumentMappingImpl documentMapping = new SDocumentMappingImpl(documentId, processInstanceId, name);
        documentMapping.setDescription(description);
        documentMapping.setVersion("1");
        documentMapping.setIndex(index);
        final InsertRecord insertRecord = new InsertRecord(documentMapping);
        SInsertEvent insertEvent = null;
        if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(DOCUMENTMAPPING).setObject(documentMapping)
                    .done();
        }
        recorder.recordInsert(insertRecord, insertEvent);
        return documentMapping;
    }

    @Override
    public void archive(final SDocumentMapping docMapping, final long archiveDate) throws SDocumentMappingException {
        if (archiveService.isArchivable(SDocumentMapping.class)) {
            final SADocumentMapping saDocumentMapping = new SADocumentMappingImpl(docMapping.getDocumentId(), docMapping.getProcessInstanceId(), archiveDate,
                    docMapping.getId(), docMapping.getName(), docMapping.getDescription(), docMapping.getVersion());
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saDocumentMapping);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (SBonitaException e) {
                throw new SDocumentMappingException(docMapping.getId(), e);
            }
        }
    }

    @Override
    public List<SMappedDocument> getDocumentList(String documentName, long processInstanceId) throws SBonitaReadException {
        List<SMappedDocument> mappedDocuments;
        List<SMappedDocument> result = new ArrayList<SMappedDocument>();
        QueryOptions queryOptions = new QueryOptions(0, 100);
        do {
            mappedDocuments = persistenceService.selectList(SelectDescriptorBuilder.getDocumentList(documentName, processInstanceId, queryOptions));
            result.addAll(mappedDocuments);
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (mappedDocuments.size() == 100);
        return result;
    }


}
