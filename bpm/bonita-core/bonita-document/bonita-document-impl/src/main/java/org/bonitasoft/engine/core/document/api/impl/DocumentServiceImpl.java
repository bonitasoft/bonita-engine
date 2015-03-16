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
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
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
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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
    private final ReadPersistenceService definitiveArchiveReadPersistenceService;

    public DocumentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final SDocumentDownloadURLProvider urlProvider, final EventService eventService, final ArchiveService archiveService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.urlProvider = urlProvider;
        this.eventService = eventService;
        this.archiveService = archiveService;
        definitiveArchiveReadPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();

    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, final long processInstanceId, final String name, final String description)
            throws SObjectCreationException {
        try {
            insertDocument(document);
            final SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description, -1);
            return new SMappedDocumentImpl(documentMapping, document);
        } catch (final SBonitaException e) {
            throw new SObjectCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, final long processInstanceId, final String name, final String description,
            final int index)
            throws SObjectCreationException, SObjectAlreadyExistsException {
        try {
            if (index == -1) {
                final SMappedDocument mappedDocumentInternal = getMappedDocumentInternal(processInstanceId, name);
                if (mappedDocumentInternal != null) {
                    throw new SObjectAlreadyExistsException("A document already exists with name " + name + " and process instance id " + processInstanceId);
                }
            }
            insertDocument(document);
            final SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description, index);
            return new SMappedDocumentImpl(documentMapping, document);
        } catch (final SObjectAlreadyExistsException e) {
            throw new SObjectAlreadyExistsException(e);
        } catch (final SBonitaException e) {
            throw new SObjectCreationException(e);
        }
    }

    @Override
    public void updateDocumentOfList(final SMappedDocument mappedDocument, final SDocument document, final int index) throws SObjectModificationException {
        updateDocument(mappedDocument, document, index);
    }

    @Override
    public void updateDocumentIndex(final SMappedDocument mappedDocument, final int index) throws SObjectModificationException {
        final Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("index", index);
        updateFields(mappedDocument, params);
    }

    private void updateFields(final SDocumentMapping mappedDocument, final Map<String, Object> params) throws SObjectModificationException {
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(mappedDocument,
                params);
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(DOCUMENTMAPPING, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DOCUMENTMAPPING).setObject(mappedDocument)
                    .done();
        }
        try {
            recorder.recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void updateMapping(final long documentId, final SDocumentMapping sDocumentMapping, final String description, final int index)
            throws SObjectModificationException {
        final Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("documentId", documentId);
        params.put("description", description);
        params.put("version", incrementVersion(sDocumentMapping.getVersion()));
        params.put("index", index);
        updateFields(sDocumentMapping, params);
    }

    private String incrementVersion(final String version) {
        final Integer intVersion = Integer.valueOf(version);
        return String.valueOf(intVersion + 1);
    }

    private SDocument insertDocument(final SDocument document) throws SRecorderException {
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
    public void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SBonitaReadException, SObjectModificationException {
        List<SMappedDocument> mappedDocuments;
        do {
            mappedDocuments = persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
                    processInstanceId, 0, 100, null, null));
            for (final SMappedDocument mappedDocument : mappedDocuments) {
                removeDocument(mappedDocument);
            }
        } while (!mappedDocuments.isEmpty());
    }

    @Override
    public String generateDocumentURL(final String name, final String contentStorageId) {
        return urlProvider.generateURL(name, contentStorageId);
    }

    @Override
    public SAMappedDocument getArchivedDocument(final long archivedProcessDocumentId) throws SObjectNotFoundException {
        try {
            final SAMappedDocument docMapping = definitiveArchiveReadPersistenceService.selectById(SelectDescriptorBuilder
                    .getArchivedDocumentById(archivedProcessDocumentId));
            if (docMapping == null) {
                throw new SObjectNotFoundException("Document not found with identifier: " + archivedProcessDocumentId);
            }
            return docMapping;
        } catch (final SBonitaReadException e) {
            throw new SObjectNotFoundException(e);
        }
    }

    @Override
    public SAMappedDocument getArchivedVersionOfProcessDocument(final long documentId) throws SObjectNotFoundException {
        try {
            final SAMappedDocument aDocMapping = definitiveArchiveReadPersistenceService.selectOne(SelectDescriptorBuilder
                    .getArchivedVersionOdDocument(documentId));
            if (aDocMapping == null) {
                throw new SObjectNotFoundException(documentId);
            }
            return aDocMapping;
        } catch (final SBonitaReadException e) {
            throw new SObjectNotFoundException("Document not found with identifier: " + documentId, e);
        }
    }

    @Override
    public SLightDocument getDocument(final long documentId) throws SObjectNotFoundException, SBonitaReadException {
        final SLightDocument document = persistenceService.selectById(new SelectByIdDescriptor<SLightDocument>("getLightDocumentById", SLightDocument.class,
                documentId));
        if (document == null) {
            throw new SObjectNotFoundException("Document with id " + documentId + " not found");
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(final long processInstanceId, final String documentName) throws SObjectNotFoundException, SBonitaReadException {
        final SMappedDocument document = getMappedDocumentInternal(processInstanceId, documentName);
        if (document == null) {
            throw new SObjectNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId);
        }
        return document;
    }

    private SMappedDocument getMappedDocumentInternal(final long processInstanceId, final String documentName) throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("name", documentName);
        final SelectOneDescriptor<SMappedDocument> selectOneDescriptor = new SelectOneDescriptor<SMappedDocument>("getSMappedDocumentOfProcessWithName",
                parameters,
                SDocument.class);
        return persistenceService.selectOne(selectOneDescriptor);
    }

    @Override
    public SMappedDocument getMappedDocument(final long processInstanceId, final String documentName, final long time) throws SObjectNotFoundException,
            SBonitaReadException {
        final List<SAMappedDocument> docMapping = definitiveArchiveReadPersistenceService.selectList(SelectDescriptorBuilder
                .getSAMappedDocumentOfProcessWithName(
                        processInstanceId,
                        documentName, time));
        if (docMapping.isEmpty()) {
            return getMappedDocument(processInstanceId, documentName);
        }
        return docMapping.get(0);
    }

    @Override
    public byte[] getDocumentContent(final String documentId) throws SObjectNotFoundException {
        try {
            final Long id = Long
                    .valueOf(documentId);
            return getDocumentWithContent(id).getContent();
        } catch (final NumberFormatException e) {
            throw new SObjectNotFoundException("Identifier " + documentId + " is not valid, it must be a long");
        } catch (final SBonitaReadException e) {
            throw new SObjectNotFoundException(e);
        }
    }

    private SDocument getDocumentWithContent(final Long id) throws SBonitaReadException, SObjectNotFoundException {
        final SDocument document = persistenceService.selectById(new SelectByIdDescriptor<SDocument>("geDocumentById", SDocument.class, id));
        if (document == null) {
            throw new SObjectNotFoundException("Document with id " + id + " not found");
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(final long mappingId) throws SObjectNotFoundException, SBonitaReadException {
        final SMappedDocument document = persistenceService.selectById(new SelectByIdDescriptor<SMappedDocument>("getSMappedDocumentById",
                SMappedDocument.class,
                mappingId));
        if (document == null) {
            throw new SObjectNotFoundException("SMappedDocument with id " + mappingId + " not found");
        }
        return document;
    }

    @Override
    public List<SMappedDocument> getDocumentsOfProcessInstance(final long processInstanceId, final int fromIndex, final int numberPerPage, final String field,
            final OrderByType order) throws SBonitaReadException {
        return persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
                processInstanceId, fromIndex, numberPerPage, field, order));
    }

    @Override
    public long getNumberOfArchivedDocuments(final QueryOptions queryOptions) throws SBonitaReadException {
        return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, queryOptions, null);
    }

    @Override
    public long getNumberOfArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfDocuments(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SMappedDocument.class, queryOptions, null);
    }

    @Override
    public long getNumberOfDocumentsOfProcessInstance(final long processInstanceId) throws SBonitaReadException {
        return persistenceService.selectOne(SelectDescriptorBuilder.getNumberOfSMappedDocumentOfProcess(processInstanceId));
    }

    @Override
    public long getNumberOfDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return persistenceService.getNumberOfEntities(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public void removeCurrentVersion(final SMappedDocument document) throws SObjectModificationException {
        archive(document, System.currentTimeMillis());
        removeDocument(document);

    }

    @Override
    public void removeCurrentVersion(final long processInstanceId, final String documentName) throws SObjectNotFoundException, SObjectModificationException {
        try {
            removeCurrentVersion(getMappedDocument(processInstanceId, documentName));
        } catch (final SBonitaReadException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void removeArchivedDocument(final SAMappedDocument mappedDocument) throws SRecorderException, SBonitaReadException, SObjectNotFoundException {
        // Delete document itself and the mapping
        delete((SADocumentMapping) mappedDocument);
        delete(getDocument(mappedDocument.getDocumentId()));
    }

    @Override
    public void deleteDocument(final SLightDocument document) throws SObjectModificationException {
        try {
            delete(document);
        } catch (final SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void delete(final SLightDocument document) throws SRecorderException {
        final DeleteRecord deleteDocRecord = new DeleteRecord(document);
        final SDeleteEvent deleteDocEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SDocument")
                .setObject(document)
                .done();
        recorder.recordDelete(deleteDocRecord, deleteDocEvent);
    }

    private void delete(final SADocumentMapping mappedDocument) throws SRecorderException {
        final DeleteRecord deleteRecord = new DeleteRecord(mappedDocument);
        final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SADocumentMapping")
                .setObject(mappedDocument)
                .done();
        recorder.recordDelete(deleteRecord, deleteEvent);
    }

    @Override
    public void removeDocument(final SMappedDocument mappedDocument) throws SObjectModificationException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(mappedDocument);
            final SDeleteEvent deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent("SDocumentMapping")
                    .setObject(mappedDocument)
                    .done();
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SBonitaException e) {
            throw new SObjectModificationException(e.getMessage(), e);
        }
    }

    @Override
    public List<SAMappedDocument> searchArchivedDocuments(final QueryOptions queryOptions)
            throws SBonitaReadException {
        final ReadPersistenceService persistenceService1 = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService1.searchEntity(SAMappedDocument.class, queryOptions, null);
    }

    @Override
    public List<SAMappedDocument> searchArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService1 = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return persistenceService1.searchEntity(SAMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public List<SMappedDocument> searchDocuments(final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            return persistenceService.searchEntity(SMappedDocument.class, queryOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public List<SMappedDocument> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.searchEntity(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void deleteArchivedDocuments(final long instanceId) throws SObjectModificationException {
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
            throw new SObjectModificationException(e);
        }
    }

    private SDocumentMapping create(final long documentId, final long processInstanceId, final String name, final String description, final int index)
            throws SRecorderException {
        final SDocumentMappingImpl documentMapping = new SDocumentMappingImpl(documentId, processInstanceId, name);
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
    public void archive(final SDocumentMapping docMapping, final long archiveDate) throws SObjectModificationException {
        if (archiveService.isArchivable(SDocumentMapping.class)) {
            final SADocumentMappingImpl saDocumentMapping = new SADocumentMappingImpl(docMapping.getDocumentId(), docMapping.getProcessInstanceId(),
                    archiveDate, docMapping.getId(), docMapping.getName(), docMapping.getDescription(), docMapping.getVersion());
            saDocumentMapping.setIndex(docMapping.getIndex());
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saDocumentMapping);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (final SBonitaException e) {
                throw new SObjectModificationException("Unable to archive the document with id = <" + docMapping.getId() + ">", e);
            }
        }
    }

    @Override
    public List<SMappedDocument> getDocumentList(final String documentName, final long processInstanceId, final int fromIndex, final int numberOfResult)
            throws SBonitaReadException {
        return persistenceService.selectList(SelectDescriptorBuilder.getDocumentList(documentName, processInstanceId, new QueryOptions(fromIndex,
                numberOfResult)));
    }

    @Override
    public void deleteContentOfArchivedDocument(final long archivedDocumentId) throws SObjectNotFoundException, SBonitaReadException, SRecorderException {
        final SAMappedDocument archivedDocument = getArchivedDocument(archivedDocumentId);
        final SDocument document = getDocumentWithContent(archivedDocument.getDocumentId());
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(document, Collections.singletonMap("content", null));
        SUpdateEvent updateEvent = null;
        if (eventService.hasHandlers(DOCUMENT, EventActionType.UPDATED)) {
            updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(DOCUMENT).setObject(document)
                    .done();
        }
        recorder.recordUpdate(updateRecord, updateEvent);
    }

    @Override
    public SMappedDocument updateDocument(final long documentId, final SDocument sDocument) throws SBonitaReadException, SObjectNotFoundException,
            SObjectModificationException {
        final SDocumentMapping sDocumentMapping = getMappedDocument(documentId);
        return updateDocument(sDocumentMapping, sDocument);
    }

    @Override
    public SMappedDocument updateDocument(final SDocumentMapping documentToUpdate, final SDocument sDocument) throws SObjectModificationException {
        return updateDocument(documentToUpdate, sDocument, documentToUpdate.getIndex());
    }

    private SMappedDocument updateDocument(final SDocumentMapping documentToUpdate, final SDocument sDocument, final int index)
            throws SObjectModificationException {
        //insert new document
        try {
            insertDocument(sDocument);
        } catch (final SRecorderException e) {
            throw new SObjectModificationException(e);
        }
        //update mapping
        archive(documentToUpdate, System.currentTimeMillis());
        updateMapping(sDocument.getId(), documentToUpdate, documentToUpdate.getDescription(), index);
        return new SMappedDocumentImpl(documentToUpdate, sDocument);
    }

    @Override
    public List<SMappedDocument> getDocumentList(final String documentName, final long processInstanceId, final long time) throws SBonitaReadException {
        final List<SAMappedDocument> archivedList = persistenceService.selectList(SelectDescriptorBuilder.getArchivedDocumentList(documentName,
                processInstanceId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS), time));
        final List<SMappedDocument> elementsInJournal = persistenceService.selectList(SelectDescriptorBuilder.getDocumentListCreatedBefore(documentName,
                processInstanceId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS), time));

        final List<SMappedDocument> result = new ArrayList<SMappedDocument>(archivedList.size() + elementsInJournal.size());
        for (final SAMappedDocument mappedDocument : archivedList) {
            result.add(mappedDocument);
        }
        for (final SMappedDocument mappedDocument : elementsInJournal) {
            result.add(mappedDocument);
        }
        return result;
    }
}
