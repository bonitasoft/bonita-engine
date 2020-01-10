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
package org.bonitasoft.engine.core.document.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.AbstractSDocumentMapping;
import org.bonitasoft.engine.core.document.model.AbstractSMappedDocument;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.SLightDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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
    private final ArchiveService archiveService;
    private final Recorder recorder;
    private final ReadPersistenceService persistenceService;
    private final ReadPersistenceService definitiveArchiveReadPersistenceService;

    public DocumentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final SDocumentDownloadURLProvider urlProvider, final ArchiveService archiveService) {
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.urlProvider = urlProvider;
        this.archiveService = archiveService;
        definitiveArchiveReadPersistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();

    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, final long processInstanceId,
            final String name, final String description)
            throws SObjectCreationException {
        try {
            insertDocument(document);
            final SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description, -1);
            return new SMappedDocument(documentMapping, document);
        } catch (final SBonitaException e) {
            throw new SObjectCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SMappedDocument attachDocumentToProcessInstance(final SDocument document, final long processInstanceId,
            final String name, final String description,
            final int index)
            throws SObjectCreationException, SObjectAlreadyExistsException {
        try {
            if (index == -1) {
                final SMappedDocument mappedDocumentInternal = getMappedDocumentInternal(processInstanceId, name);
                if (mappedDocumentInternal != null) {
                    throw new SObjectAlreadyExistsException("A document already exists with name " + name
                            + " and process instance id " + processInstanceId);
                }
            }
            insertDocument(document);
            final SDocumentMapping documentMapping = create(document.getId(), processInstanceId, name, description,
                    index);
            return new SMappedDocument(documentMapping, document);
        } catch (final SObjectAlreadyExistsException e) {
            throw new SObjectAlreadyExistsException(e);
        } catch (final SBonitaException e) {
            throw new SObjectCreationException(e);
        }
    }

    @Override
    public void updateDocumentOfList(final AbstractSDocumentMapping mappedDocument, final SDocument document,
            final int index)
            throws SObjectModificationException {
        updateDocument(mappedDocument, document, index);
    }

    @Override
    public void updateDocumentIndex(final AbstractSDocumentMapping mappedDocument, final int index)
            throws SObjectModificationException {
        final Map<String, Object> params = new HashMap<>(2);
        params.put("index", index);
        updateFields(mappedDocument, params);
    }

    private void updateFields(final AbstractSDocumentMapping mappedDocument, final Map<String, Object> params)
            throws SObjectModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(mappedDocument,
                    params), DOCUMENTMAPPING);
        } catch (final SRecorderException e) {
            throw new SObjectModificationException(e);
        }
    }

    private void updateMapping(final long documentId, final AbstractSDocumentMapping sDocumentMapping,
            final String description,
            final int index)
            throws SObjectModificationException {
        final Map<String, Object> params = new HashMap<>(2);
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

    private void insertDocument(final SDocument document) throws SRecorderException {
        recorder.recordInsert(new InsertRecord(document), DOCUMENT);
    }

    @Override
    public void deleteDocumentsFromProcessInstance(final Long processInstanceId)
            throws SBonitaReadException, SObjectModificationException {
        List<SMappedDocument> mappedDocuments;
        do {
            mappedDocuments = persistenceService
                    .selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
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
            final SAMappedDocument docMapping = definitiveArchiveReadPersistenceService
                    .selectById(SelectDescriptorBuilder
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
            final SAMappedDocument aDocMapping = definitiveArchiveReadPersistenceService
                    .selectOne(SelectDescriptorBuilder
                            .getArchivedVersionOfDocument(documentId));
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
        final SLightDocument document = persistenceService
                .selectById(new SelectByIdDescriptor<>(SLightDocument.class,
                        documentId));
        if (document == null) {
            throw new SObjectNotFoundException("Document with id " + documentId + " not found");
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(final long processInstanceId, final String documentName)
            throws SObjectNotFoundException, SBonitaReadException {
        final SMappedDocument document = getMappedDocumentInternal(processInstanceId, documentName);
        if (document == null) {
            throw new SObjectNotFoundException(
                    "Document not found: " + documentName + " for process instance: " + processInstanceId);
        }
        return document;
    }

    private SMappedDocument getMappedDocumentInternal(final long processInstanceId, final String documentName)
            throws SBonitaReadException {
        final Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("processInstanceId", processInstanceId);
        parameters.put("name", documentName);
        final SelectOneDescriptor<SMappedDocument> selectOneDescriptor = new SelectOneDescriptor<>(
                "getSMappedDocumentOfProcessWithName",
                parameters,
                SDocument.class);
        return persistenceService.selectOne(selectOneDescriptor);
    }

    @Override
    public AbstractSMappedDocument getMappedDocument(final long processInstanceId, final String documentName,
            final long time)
            throws SObjectNotFoundException,
            SBonitaReadException {
        final List<SAMappedDocument> docMapping = definitiveArchiveReadPersistenceService
                .selectList(SelectDescriptorBuilder
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
        final SDocument document = persistenceService
                .selectById(new SelectByIdDescriptor<>(SDocument.class, id));
        if (document == null) {
            throw new SObjectNotFoundException("Document with id " + id + " not found");
        }
        return document;
    }

    @Override
    public SMappedDocument getMappedDocument(final long mappingId)
            throws SObjectNotFoundException, SBonitaReadException {
        final SMappedDocument document = persistenceService.selectById(new SelectByIdDescriptor<>(
                SMappedDocument.class,
                mappingId));
        if (document == null) {
            throw new SObjectNotFoundException("SMappedDocument with id " + mappingId + " not found");
        }
        return document;
    }

    @Override
    public List<SMappedDocument> getDocumentsOfProcessInstance(final long processInstanceId, final int fromIndex,
            final int numberPerPage, final String field,
            final OrderByType order) throws SBonitaReadException {
        return persistenceService.selectList(SelectDescriptorBuilder.getDocumentMappingsForProcessInstance(
                processInstanceId, fromIndex, numberPerPage, field, order));
    }

    @Override
    public long getNumberOfArchivedDocuments(final QueryOptions queryOptions) throws SBonitaReadException {
        return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, queryOptions, null);
    }

    @Override
    public long getNumberOfArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
        return definitiveArchiveReadPersistenceService.getNumberOfEntities(SAMappedDocument.class, SUPERVISED_BY,
                queryOptions, parameters);
    }

    @Override
    public long getNumberOfDocuments(final QueryOptions queryOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SMappedDocument.class, queryOptions, null);
    }

    @Override
    public long getNumberOfDocumentsOfProcessInstance(final long processInstanceId) throws SBonitaReadException {
        return persistenceService
                .selectOne(SelectDescriptorBuilder.getNumberOfSMappedDocumentOfProcess(processInstanceId));
    }

    @Override
    public long getNumberOfDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
        return persistenceService.getNumberOfEntities(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public void removeCurrentVersion(final AbstractSMappedDocument document) throws SObjectModificationException {
        archive(document, System.currentTimeMillis());
        removeDocument(document);

    }

    @Override
    public void removeCurrentVersion(final long processInstanceId, final String documentName)
            throws SObjectNotFoundException, SObjectModificationException {
        try {
            removeCurrentVersion(getMappedDocument(processInstanceId, documentName));
        } catch (final SBonitaReadException e) {
            throw new SObjectModificationException(e);
        }
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
        recorder.recordDelete(new DeleteRecord(document), "SDocument");
    }

    @Override
    public void removeDocument(final AbstractSMappedDocument mappedDocument) throws SObjectModificationException {
        try {
            recorder.recordDelete(new DeleteRecord(mappedDocument), "SDocumentMapping");
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
    public List<SAMappedDocument> searchArchivedDocumentsSupervisedBy(final long userId,
            final QueryOptions queryOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService1 = archiveService.getDefinitiveArchiveReadPersistenceService();
        final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
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
    public List<SMappedDocument> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
            return persistenceService.searchEntity(SMappedDocument.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void deleteArchivedDocuments(List<Long> processInstanceIds) throws SBonitaReadException, SRecorderException {
        List<SAMappedDocument> archivedMappedDocuments = persistenceService
                .selectList(new SelectListDescriptor<>("getArchiveMappingsOfProcessInstances",
                        Collections.singletonMap("processInstanceIds", processInstanceIds), SAMappedDocument.class,
                        QueryOptions.countQueryOptions()));
        if (archivedMappedDocuments.isEmpty()) {
            //no documents to delete
            return;
        }
        List<Long> documentIds = new ArrayList<>();
        List<Long> documentMappingIds = new ArrayList<>();
        for (SAMappedDocument mappedDocument : archivedMappedDocuments) {
            documentIds.add(mappedDocument.getDocumentId());
            documentMappingIds.add(mappedDocument.getId());
        }

        archiveService.deleteFromQuery("deleteArchiveDocumentsByIds", Collections.singletonMap("ids", documentIds));
        archiveService.deleteFromQuery("deleteArchiveMappingsByIds",
                Collections.singletonMap("ids", documentMappingIds));
    }

    private SDocumentMapping create(final long documentId, final long processInstanceId, final String name,
            final String description, final int index)
            throws SRecorderException {
        final SDocumentMapping documentMapping = new SDocumentMapping(documentId, processInstanceId, name);
        documentMapping.setDescription(description);
        documentMapping.setVersion("1");
        documentMapping.setIndex(index);
        recorder.recordInsert(new InsertRecord(documentMapping), DOCUMENTMAPPING);
        return documentMapping;
    }

    @Override
    public void archive(final AbstractSDocumentMapping docMapping, final long archiveDate)
            throws SObjectModificationException {
        if (archiveService.isArchivable(SDocumentMapping.class)) {
            final SADocumentMapping saDocumentMapping = new SADocumentMapping(docMapping.getDocumentId(),
                    docMapping.getProcessInstanceId(),
                    archiveDate, docMapping.getId(), docMapping.getName(), docMapping.getDescription(),
                    docMapping.getVersion());
            saDocumentMapping.setIndex(docMapping.getIndex());
            final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saDocumentMapping);
            try {
                archiveService.recordInsert(archiveDate, insertRecord);
            } catch (final SBonitaException e) {
                throw new SObjectModificationException(
                        "Unable to archive the document with id = <" + docMapping.getId() + ">", e);
            }
        }
    }

    @Override
    public List<SMappedDocument> getDocumentList(final String documentName, final long processInstanceId,
            final int fromIndex, final int numberOfResult)
            throws SBonitaReadException {
        return persistenceService.selectList(
                SelectDescriptorBuilder.getDocumentList(documentName, processInstanceId, new QueryOptions(fromIndex,
                        numberOfResult)));
    }

    @Override
    public void deleteContentOfArchivedDocument(final long archivedDocumentId)
            throws SObjectNotFoundException, SBonitaReadException, SRecorderException {
        final SAMappedDocument archivedDocument = getArchivedDocument(archivedDocumentId);
        final SDocument document = getDocumentWithContent(archivedDocument.getDocumentId());
        recorder.recordUpdate(UpdateRecord.buildSetFields(document, Collections.singletonMap("content", null)),
                DOCUMENT);
    }

    @Override
    public SMappedDocument updateDocument(final long documentId, final SDocument sDocument)
            throws SBonitaReadException, SObjectNotFoundException,
            SObjectModificationException {
        final AbstractSDocumentMapping sDocumentMapping = getMappedDocument(documentId);
        return updateDocument(sDocumentMapping, sDocument);
    }

    @Override
    public SMappedDocument updateDocument(final AbstractSDocumentMapping documentToUpdate, final SDocument sDocument)
            throws SObjectModificationException {
        return updateDocument(documentToUpdate, sDocument, documentToUpdate.getIndex());
    }

    private SMappedDocument updateDocument(final AbstractSDocumentMapping documentToUpdate, final SDocument sDocument,
            final int index)
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
        return new SMappedDocument(documentToUpdate, sDocument);
    }

    @Override
    public List<AbstractSMappedDocument> getDocumentList(final String documentName, final long processInstanceId,
            final long time) throws SBonitaReadException {
        final List<SAMappedDocument> archivedList = persistenceService
                .selectList(SelectDescriptorBuilder.getArchivedDocumentList(documentName,
                        processInstanceId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS), time));
        final List<SMappedDocument> elementsInJournal = persistenceService
                .selectList(SelectDescriptorBuilder.getDocumentListCreatedBefore(documentName,
                        processInstanceId, new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS), time));

        final List<AbstractSMappedDocument> result = new ArrayList<>(
                archivedList.size() + elementsInJournal.size());
        result.addAll(archivedList);
        result.addAll(elementsInJournal);
        return result;
    }
}
