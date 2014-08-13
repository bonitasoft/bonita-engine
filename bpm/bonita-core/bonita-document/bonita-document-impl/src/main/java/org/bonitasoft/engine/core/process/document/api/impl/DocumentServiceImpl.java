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
import org.bonitasoft.engine.core.process.document.api.DocumentService;
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
import org.bonitasoft.engine.document.DocumentContentService;
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
public class DocumentServiceImpl implements DocumentService {

    private final DocumentContentService documentContentService;

    private final DocumentMappingService documentMappingService;

    private final SDocumentDownloadURLProvider urlProvider;

    public DocumentServiceImpl(final DocumentContentService documentContentService,
                               final DocumentMappingService documentMappingService,
                               final SDocumentDownloadURLProvider urlProvider) {
        this.documentContentService = documentContentService;
        this.documentMappingService = documentMappingService;
        this.urlProvider = urlProvider;
    }

    @Override
    public SDocumentMapping attachDocumentToProcessInstance(final SDocumentMapping document) throws SProcessDocumentCreationException {
        try {
            return documentMappingService.create(document);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SDocumentMapping attachDocumentToProcessInstance(final SDocumentMapping document, final byte[] documentContent)
            throws SProcessDocumentCreationException {
        try {
            SDocument sDocument = toSDocument(document);
            sDocument = documentContentService.storeDocumentContent(sDocument, documentContent);
            SDocumentMapping docMapping = setStorageId(document, sDocument);
            return documentMappingService.create(docMapping);
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    SDocumentMapping setStorageId(final SDocumentMapping document, final SDocument sDocument) {
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
        List<SDocumentMapping> sProcessDocuments;
        do {
            sProcessDocuments = getDocumentsOfProcessInstanceOrderedById(processInstanceId, 0, 100);
            removeDocuments(sProcessDocuments);
        } while (!sProcessDocuments.isEmpty());
    }

    @Override
    public String generateDocumentURL(final String name, final String contentStorageId) {
        return urlProvider.generateURL(name, contentStorageId);
    }

    @Override
    public SADocumentMapping getArchivedDocument(final long archivedProcessDocumentId) throws SDocumentNotFoundException {
        SADocumentMapping aDocMapping;
        try {
            aDocMapping = documentMappingService.getArchivedDocument(archivedProcessDocumentId);
            return aDocMapping;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found with identifer: " + archivedProcessDocumentId, e);
        }
    }

    @Override
    public SADocumentMapping getArchivedVersionOfProcessDocument(final long documentId) throws SDocumentNotFoundException {
        SADocumentMapping aDocMapping;
        try {
            aDocMapping = documentMappingService.getArchivedVersionOfDocument(documentId);
            return aDocMapping;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found with identifer: " + documentId, e);
        }
    }

    @Override
    public SDocumentMapping getDocument(final long documentId) throws SDocumentNotFoundException {
        SDocumentMapping docMapping;
        try {
            docMapping = documentMappingService.get(documentId);
            return docMapping;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentId, e);
        }
    }

    @Override
    public SDocumentMapping getDocument(final long processInstanceId, final String documentName) throws SDocumentNotFoundException {
        SDocumentMapping docMapping;
        try {
            docMapping = documentMappingService.get(processInstanceId, documentName);
            return docMapping;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId, e);
        }
    }

    @Override
    public SDocumentMapping getDocument(final long processInstanceId, final String documentName, final long time) throws SDocumentNotFoundException {
        SADocumentMapping archDocMapping;
        try {
            try {
                archDocMapping = documentMappingService.get(processInstanceId, documentName, time);
            } catch (final SDocumentMappingNotFoundException e) {
                archDocMapping = null;
            }
            final SDocumentMapping processDocument;
            if (archDocMapping == null) {// no archive = still the current element
                processDocument = documentMappingService.get(processInstanceId, documentName);
            } else {
                processDocument = archDocMapping;
            }
            return processDocument;
        } catch (final SDocumentMappingNotFoundException e) {
            throw new SDocumentNotFoundException("Document not found: " + documentName + " for process instance: " + processInstanceId, e);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentStorageId) throws SProcessDocumentContentNotFoundException {
        try {
            return documentContentService.getContent(documentStorageId);
        } catch (final SDocumentException e) {
            throw new SProcessDocumentContentNotFoundException(e);
        }
    }

    @Override
    public List<SDocumentMapping> getDocumentsOfProcessInstance(final long processInstanceId, final int fromIndex, final int numberPerPage, final String field,
            final OrderByType order) throws SDocumentException {
        try {
            final List<SDocumentMapping> docMappings = documentMappingService.getDocumentMappingsForProcessInstance(processInstanceId, fromIndex,
                    numberPerPage, field, order);
            if ((docMappings != null) && !docMappings.isEmpty()) {
                final List<SDocumentMapping> result = new ArrayList<SDocumentMapping>(docMappings.size());
                for (final SDocumentMapping docMapping : docMappings) {
                    result.add(docMapping);
                }
                return result;
            }
            return Collections.emptyList();
        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to list documents of process instance: " + processInstanceId, e);
        }
    }

    private List<SDocumentMapping> getDocumentsOfProcessInstanceOrderedById(final long processInstanceId, final int fromIndex, final int numberPerPage)
            throws SDocumentException {
        try {
            final List<SDocumentMapping> docMappings = documentMappingService.getDocumentMappingsForProcessInstanceOrderedById(processInstanceId, fromIndex,
                    numberPerPage);
            if (docMappings != null && !docMappings.isEmpty()) {
                final List<SDocumentMapping> result = new ArrayList<SDocumentMapping>(docMappings.size());
                for (final SDocumentMapping docMapping : docMappings) {
                    result.add(docMapping);
                }
                return result;
            } else {
                return Collections.emptyList();
            }

        } catch (final SBonitaException e) {
            throw new SDocumentException("Unable to list documents of process instance: " + processInstanceId, e);
        }
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

    private SDocumentMappingBuilder initDocumentMappingBuilder(final SDocumentMapping document) {
        final SDocumentMappingBuilder builder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        builder.setProcessInstanceId(document.getProcessInstanceId());
        builder.setDocumentName(document.getDocumentName());
        builder.setDocumentAuthor(document.getDocumentAuthor());
        builder.setDocumentCreationDate(document.getDocumentCreationDate());
        builder.setHasContent(document.documentHasContent());
        builder.setDocumentContentFileName(document.getDocumentContentFileName());
        builder.setDocumentContentMimeType(document.getDocumentContentMimeType());
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
    public void removeDocument(final SDocumentMapping sProcessDocument) throws SProcessDocumentDeletionException {
        try {
            // Delete document content, if has
            if (sProcessDocument.documentHasContent()) {
                documentContentService.deleteDocumentContent(sProcessDocument.getContentStorageId());
            }

            // Remove Mapping between process instance and document
            documentMappingService.delete(sProcessDocument.getId());
        } catch (final SBonitaException e) {
            throw new SProcessDocumentDeletionException(e.getMessage(), e);
        }
    }

    @Override
    public void removeDocuments(final List<SDocumentMapping> sProcessDocuments) throws SProcessDocumentDeletionException {
        try {
            for (final SDocumentMapping sDocumentMapping : sProcessDocuments) {
                removeDocument(sDocumentMapping);
            }
        } catch (final SBonitaException e) {
            throw new SProcessDocumentDeletionException(e.getMessage(), e);
        }
    }

    @Override
    public List<SADocumentMapping> searchArchivedDocuments(final QueryOptions queryOptions)
            throws SBonitaSearchException {
        final List<SADocumentMapping> docMappings = documentMappingService.searchArchivedDocuments(queryOptions);
        final List<SADocumentMapping> result = new ArrayList<SADocumentMapping>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SADocumentMapping docMapping : docMappings) {
                result.add(docMapping);
            }
        }
        return result;
    }

    @Override
    public List<SADocumentMapping> searchArchivedDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SADocumentMapping> docMappings = documentMappingService.searchArchivedDocumentsSupervisedBy(userId, queryOptions);
        final List<SADocumentMapping> result = new ArrayList<SADocumentMapping>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SADocumentMapping docMapping : docMappings) {
                result.add(docMapping);
            }
        }
        return result;
    }

    @Override
    public List<SDocumentMapping> searchDocuments(final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SDocumentMapping> docMappings = documentMappingService.searchDocuments(queryOptions);
        final List<SDocumentMapping> result = new ArrayList<SDocumentMapping>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SDocumentMapping docMapping : docMappings) {
                result.add(docMapping);
            }
        }
        return result;
    }

    @Override
    public List<SDocumentMapping> searchDocumentsSupervisedBy(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final List<SDocumentMapping> docMappings = documentMappingService.searchDocumentsSupervisedBy(userId, queryOptions);
        final List<SDocumentMapping> result = new ArrayList<SDocumentMapping>(docMappings.size());
        if (!docMappings.isEmpty()) {
            for (final SDocumentMapping docMapping : docMappings) {
                result.add(docMapping);
            }
        }
        return result;
    }

    private SDocument toSDocument(final SDocumentMapping document) {
        final SDocumentBuilder builder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        builder.setAuthor(document.getDocumentAuthor());
        builder.setContentFileName(document.getDocumentContentFileName());
        builder.setContentMimeType(document.getDocumentContentMimeType());
        builder.setCreationDate(document.getDocumentCreationDate());
        builder.setDocumentId(document.getContentStorageId());
        return builder.done();
    }

    @Override
    public SDocumentMapping updateDocumentOfProcessInstance(final SDocumentMapping document) throws SProcessDocumentCreationException {
        try {
            return  documentMappingService.update(document);
        } catch (final SDocumentMappingException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

    @Override
    public SDocumentMapping updateDocumentOfProcessInstance(final SDocumentMapping document, final byte[] documentContent)
            throws SProcessDocumentCreationException {
        try {
            // will update documentBuilder, contentFileName, contentMimeType, author, documentContent, hasContent
            // based on processInstanceId, documentName
            SDocument sDocument = toSDocument(document);
            sDocument = documentContentService.storeDocumentContent(sDocument, documentContent);
            // we have the new document id (in the storage)
            SDocumentMapping docMapping = setStorageId(document, sDocument);
            docMapping = documentMappingService.update(docMapping);
            return docMapping;
        } catch (final SBonitaException e) {
            throw new SProcessDocumentCreationException(e.getMessage(), e);
        }
    }

}
