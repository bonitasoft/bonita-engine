/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentContentNotFoundException;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentCreationException;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentDeletionException;
import org.bonitasoft.engine.core.process.document.mapping.DocumentMappingService;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingDeletionException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingException;
import org.bonitasoft.engine.core.process.document.mapping.exception.SDocumentMappingNotFoundException;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SAProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.builder.SAProcessDocumentBuilderFactory;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilderFactory;
import org.bonitasoft.engine.document.DocumentService;
import org.bonitasoft.engine.document.SDocumentException;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.document.model.SDocument;
import org.bonitasoft.engine.document.model.SDocumentBuilder;
import org.bonitasoft.engine.document.model.SDocumentBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ProcessDocumentServiceImpl implements ProcessDocumentService {

    private final DocumentService documentService;

    private final DocumentMappingService documentMappingService;

    private final SDocumentDownloadURLProvider urlProvider;

    public ProcessDocumentServiceImpl(final DocumentService documentService,
            final DocumentMappingService documentMappingService,
            final SDocumentDownloadURLProvider urlProvider) {
        this.documentService = documentService;
        this.documentMappingService = documentMappingService;
        this.urlProvider = urlProvider;
    }

    @Override
    public SProcessDocument attachDocumentToProcessInstance(final SProcessDocument document) throws SProcessDocumentCreationException {
        try {
            SDocumentMapping docMapping = toDocumentMapping(document);
            docMapping = documentMappingService.create(docMapping);
            return toProcessDocument(docMapping);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SProcessDocument attachDocumentToProcessInstance(final SProcessDocument document, final byte[] documentContent)
            throws SProcessDocumentCreationException {
        try {
            SDocument sDocument = toSDocument(document);
            sDocument = documentService.storeDocumentContent(sDocument, documentContent);
            SDocumentMapping docMapping = buildDocumentMapping(document, sDocument);
            docMapping = documentMappingService.create(docMapping);
            return toProcessDocument(docMapping);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    private SDocumentMapping buildDocumentMapping(final SProcessDocument document, final SDocument sDocument) {
        final SDocumentMappingBuilder builder = initDocumentMappingBuilder(document);
        builder.setDocumentStorageId(sDocument.getStorageId());
        builder.setHasContent(true);
        return builder.done();
    }

    @Override
    public void deleteArchivedDocuments(final long instanceId) throws SDocumentMappingDeletionException {
        final FilterOption filterOption = new FilterOption(SADocumentMapping.class, "processInstanceId", instanceId);
        final List<FilterOption> filters = new ArrayList<FilterOption>();
        filters.add(filterOption);
        final QueryOptions queryOptions = new QueryOptions(0, 100, null, filters, null);

        try {
            List<SADocumentMapping> documents = documentMappingService.searchArchivedDocuments(queryOptions);
            while (!documents.isEmpty()) {
                for (final SADocumentMapping document : documents) {
                    documentMappingService.delete(document);
                }
                documents = documentMappingService.searchArchivedDocuments(queryOptions);
            }
        } catch (final SBonitaSearchException e) {
            throw new SDocumentMappingDeletionException(e);
        }
    }

    @Override
    public void deleteDocumentsFromProcessInstance(final Long processInstanceId) throws SDocumentException, SProcessDocumentDeletionException {
        List<SProcessDocument> sProcessDocuments;
        do {
            sProcessDocuments = getDocumentsOfProcessInstanceOrderedById(processInstanceId, 0, 100);
            removeDocuments(sProcessDocuments);
        } while (!sProcessDocuments.isEmpty());
    }

    private String generateDocumentURL(final String name, final String contentStorageId) {
        return urlProvider.generateURL(name, contentStorageId);
    }

    @Override
    public SAProcessDocument getArchivedDocument(final long archivedProcessDocumentId) throws SDocumentNotFoundException {
        SADocumentMapping aDocMapping;
        try {
            aDocMapping = documentMappingService.getArchivedDocument(archivedProcessDocumentId);
            return toAProcessDocument(aDocMapping);
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found with identifer: " + archivedProcessDocumentId, e);
        }
    }

    @Override
    public SAProcessDocument getArchivedVersionOfProcessDocument(final long documentId) throws SDocumentNotFoundException {
        SADocumentMapping aDocMapping;
        try {
            aDocMapping = documentMappingService.getArchivedVersionOfDocument(documentId);
            return toAProcessDocument(aDocMapping);
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found with identifer: " + documentId, e);
        }
    }

    @Override
    public SProcessDocument getDocument(final long documentId) throws SDocumentNotFoundException {
        SDocumentMapping docMapping;
        try {
            docMapping = documentMappingService.get(documentId);
            return toProcessDocument(docMapping);
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentId, e);
        }
    }

    @Override
    public SProcessDocument getDocument(final long processInstanceId, final String documentName) throws SDocumentNotFoundException {
        SDocumentMapping docMapping;
        try {
            docMapping = documentMappingService.get(processInstanceId, documentName);
            return toProcessDocument(docMapping);
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId, e);
        }
    }

    @Override
    public SProcessDocument getDocument(final long processInstanceId, final String documentName, final long time) throws SDocumentNotFoundException {
        SADocumentMapping archDocMapping;
        try {
            try {
                archDocMapping = documentMappingService.get(processInstanceId, documentName, time);
            } catch (final SDocumentMappingNotFoundException e) {
                archDocMapping = null;
            }
            final SProcessDocument processDocument;
            if (archDocMapping == null) {// no archive = still the current element
                final SDocumentMapping docMapping = documentMappingService.get(processInstanceId, documentName);
                processDocument = toProcessDocument(docMapping);
            } else {
                processDocument = toProcessDocument(archDocMapping);
            }
            return processDocument;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId, e);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentStorageId) throws SProcessDocumentContentNotFoundException {
        try {
            return documentService.getContent(documentStorageId);
        } catch (final Exception e) {
            throw new SProcessDocumentContentNotFoundException(e);
        }
    }

    @Override
    public List<SProcessDocument> getDocumentsOfProcessInstance(final long processInstanceId, final int fromIndex, final int numberPerPage, final String field,
            final OrderByType order) throws SDocumentException {
        try {
            final List<SDocumentMapping> docMappings = documentMappingService.getDocumentMappingsForProcessInstance(processInstanceId, fromIndex,
                    numberPerPage, field, order);
            if ((docMappings != null) && !docMappings.isEmpty()) {
                final List<SProcessDocument> result = new ArrayList<SProcessDocument>(docMappings.size());
                for (final SDocumentMapping docMapping : docMappings) {
                    result.add(toProcessDocument(docMapping));
                }
                return result;
            }
            return Collections.emptyList();
        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to list documents of process instance: " + processInstanceId, e);
        }
    }

    private List<SProcessDocument> getDocumentsOfProcessInstanceOrderedById(final long processInstanceId, final int fromIndex, final int numberPerPage)
            throws SDocumentException {
        try {
            final List<SDocumentMapping> docMappings = documentMappingService.getDocumentMappingsForProcessInstanceOrderedById(processInstanceId, fromIndex,
                    numberPerPage);
            if (docMappings != null && !docMappings.isEmpty()) {
                final List<SProcessDocument> result = new ArrayList<SProcessDocument>(docMappings.size());
                for (final SDocumentMapping docMapping : docMappings) {
                    result.add(toProcessDocument(docMapping));
                }
                return result;
            } else {
                return Collections.emptyList();
            }

        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to list documents of process instance: " + processInstanceId, e);
        }
    }

    private String getDocumentUrl(final SADocumentMapping docMapping) {
        if (docMapping.documentHasContent()) {
            return generateDocumentURL(docMapping.getDocumentContentFileName(), docMapping.getContentStorageId());
        }
        return docMapping.getDocumentURL();
    }

    private String getDocumentUrl(final SDocumentMapping docMapping) {
        if (docMapping.documentHasContent()) {
            return generateDocumentURL(docMapping.getDocumentContentFileName(), docMapping.getContentStorageId());
        }
        return docMapping.getDocumentURL();
    }

    @Override
    public long getNumberOfArchivedDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        return documentMappingService.getNumberOfArchivedDocuments(queryOptions);
    }

    @Override
    public long getNumberOfArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        return documentMappingService.getNumberOfArchivedDocumentsSupervisedBy(userId, queryOptions);
    }

    @Override
    public long getNumberOfDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        return documentMappingService.getNumberOfDocuments(queryOptions);
    }

    @Override
    public long getNumberOfDocumentsOfProcessInstance(final long processInstanceId) throws SDocumentException {
        try {
            return documentMappingService.getNumberOfDocumentMappingsForProcessInstance(processInstanceId);
        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to count documents of process instance: " + processInstanceId, e);
        }
    }

    @Override
    public long getNumberOfDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        return documentMappingService.getNumberOfDocumentsSupervisedBy(userId, queryOptions);
    }

    private SDocumentMappingBuilder initDocumentMappingBuilder(final SProcessDocument document) {
        final SDocumentMappingBuilder builder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        builder.setProcessInstanceId(document.getProcessInstanceId());
        builder.setDocumentName(document.getName());
        builder.setDocumentAuthor(document.getAuthor());
        builder.setDocumentCreationDate(document.getCreationDate());
        builder.setHasContent(document.hasContent());
        builder.setDocumentContentFileName(document.getContentFileName());
        builder.setDocumentContentMimeType(document.getContentMimeType());
        builder.setDocumentStorageId(document.getContentStorageId());
        return builder;
    }

    @Override
    public void removeCurrentVersion(final long processInstanceId, final String documentName) throws SDocumentNotFoundException, SObjectModificationException {

        try {
            final SDocumentMapping docMapping = documentMappingService.get(processInstanceId, documentName);
            documentMappingService.archive(docMapping, System.currentTimeMillis());
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException(e);
        } catch (final SDocumentMappingException e) {
            throw new SObjectModificationException(e);
        }
    }

    @Override
    public void removeDocument(final SProcessDocument sProcessDocument) throws SProcessDocumentDeletionException {
        try {
            // Delete document content, if has
            if (sProcessDocument.hasContent()) {
                documentService.deleteDocumentContent(sProcessDocument.getContentStorageId());
            }

            // Remove Mapping between process instance and document
            documentMappingService.delete(sProcessDocument.getId());
        } catch (final SBonitaException e) {
            throw new SProcessDocumentDeletionException(e.getMessage(), e);
        }
    }

    @Override
    public void removeDocuments(final List<SProcessDocument> sProcessDocuments) throws SProcessDocumentDeletionException {
        try {
            for (final SProcessDocument sProcessDocument : sProcessDocuments) {
                removeDocument(sProcessDocument);
            }
        } catch (final SBonitaException e) {
            throw new SProcessDocumentDeletionException(e.getMessage(), e);
        }
    }

    @Override
    public List<SAProcessDocument> searchArchivedDocuments(final QueryOptions queryOptions)
            throws SBonitaSearchException {
        final List<SADocumentMapping> docMappings = documentMappingService.searchArchivedDocuments(queryOptions);
        final List<SAProcessDocument> result = new ArrayList<SAProcessDocument>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SADocumentMapping docMapping : docMappings) {
                result.add(toAProcessDocument(docMapping));
            }
        }
        return result;
    }

    @Override
    public List<SAProcessDocument> searchArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SADocumentMapping> docMappings = documentMappingService.searchArchivedDocumentsSupervisedBy(userId, queryOptions);
        final List<SAProcessDocument> result = new ArrayList<SAProcessDocument>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SADocumentMapping docMapping : docMappings) {
                result.add(toAProcessDocument(docMapping));
            }
        }
        return result;
    }

    @Override
    public List<SProcessDocument> searchDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SDocumentMapping> docMappings = documentMappingService.searchDocuments(queryOptions);
        final List<SProcessDocument> result = new ArrayList<SProcessDocument>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SDocumentMapping docMapping : docMappings) {
                result.add(toProcessDocument(docMapping));
            }
        }
        return result;
    }

    @Override
    public List<SProcessDocument> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SDocumentMapping> docMappings = documentMappingService.searchDocumentsSupervisedBy(userId, queryOptions);
        final List<SProcessDocument> result = new ArrayList<SProcessDocument>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SDocumentMapping docMapping : docMappings) {
                result.add(toProcessDocument(docMapping));
            }
        }
        return result;
    }

    private SAProcessDocument toAProcessDocument(final SADocumentMapping docMapping) {
        final SAProcessDocumentBuilder builder = BuilderFactory.get(SAProcessDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(docMapping.getDocumentAuthor());
        builder.setContentMimeType(docMapping.getDocumentContentMimeType());
        builder.setCreationDate(docMapping.getDocumentCreationDate());
        builder.setFileName(docMapping.getDocumentContentFileName());
        builder.setHasContent(docMapping.documentHasContent());
        builder.setId(docMapping.getId());
        builder.setName(docMapping.getDocumentName());
        builder.setProcessInstanceId(docMapping.getProcessInstanceId());
        builder.setURL(getDocumentUrl(docMapping));
        builder.setContentStorageId(docMapping.getContentStorageId());
        builder.setArchiveDate(docMapping.getArchiveDate());
        builder.setSourceObjectId(docMapping.getSourceObjectId());
        return builder.done();
    }

    private SDocumentMapping toDocumentMapping(final SProcessDocument document) {
        final SDocumentMappingBuilder builder = initDocumentMappingBuilder(document);
        builder.setDocumentURL(document.getURL());
        return builder.done();
    }

    private SProcessDocument toProcessDocument(final SADocumentMapping docMapping) {
        final SProcessDocumentBuilder builder = BuilderFactory.get(SProcessDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(docMapping.getDocumentAuthor());
        builder.setContentMimeType(docMapping.getDocumentContentMimeType());
        builder.setCreationDate(docMapping.getDocumentCreationDate());
        builder.setFileName(docMapping.getDocumentContentFileName());
        builder.setHasContent(docMapping.documentHasContent());
        builder.setId(docMapping.getSourceObjectId());
        builder.setName(docMapping.getDocumentName());
        builder.setProcessInstanceId(docMapping.getProcessInstanceId());
        builder.setURL(getDocumentUrl(docMapping));
        builder.setContentStorageId(docMapping.getContentStorageId());
        return builder.done();
    }

    private SProcessDocument toProcessDocument(final SDocumentMapping docMapping) {
        final SProcessDocumentBuilder builder = BuilderFactory.get(SProcessDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(docMapping.getDocumentAuthor());
        builder.setContentMimeType(docMapping.getDocumentContentMimeType());
        builder.setCreationDate(docMapping.getDocumentCreationDate());
        builder.setFileName(docMapping.getDocumentContentFileName());
        builder.setHasContent(docMapping.documentHasContent());
        builder.setId(docMapping.getId());
        builder.setName(docMapping.getDocumentName());
        builder.setProcessInstanceId(docMapping.getProcessInstanceId());
        builder.setURL(getDocumentUrl(docMapping));
        builder.setContentStorageId(docMapping.getContentStorageId());
        return builder.done();
    }

    private SDocument toSDocument(final SProcessDocument document) {
        final SDocumentBuilder builder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(document.getAuthor());
        builder.setContentFileName(document.getContentFileName());
        builder.setContentMimeType(document.getContentMimeType());
        builder.setContentSize(document.getContentSize());
        builder.setCreationDate(document.getCreationDate());
        builder.setDocumentId(document.getContentStorageId());
        return builder.done();
    }

    @Override
    public SProcessDocument updateDocumentOfProcessInstance(final SProcessDocument document) throws SProcessDocumentCreationException {
        try {
            SDocumentMapping docMapping = toDocumentMapping(document);
            docMapping = documentMappingService.update(docMapping);
            return toProcessDocument(docMapping);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SProcessDocument updateDocumentOfProcessInstance(final SProcessDocument document, final byte[] documentContent)
            throws SProcessDocumentCreationException {
        try {
            // will update documentBuilder, contentFileName, contentMimeType, author, documentContent, hasContent
            // based on processInstanceId, documentName
            SDocument sDocument = toSDocument(document);
            sDocument = documentService.storeDocumentContent(sDocument, documentContent);
            // we have the new document id (in the storage)
            SDocumentMapping docMapping = buildDocumentMapping(document, sDocument);
            docMapping = documentMappingService.update(docMapping);
            return toProcessDocument(docMapping);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

}
