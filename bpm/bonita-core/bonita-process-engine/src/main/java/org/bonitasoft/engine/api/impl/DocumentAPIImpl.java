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
package org.bonitasoft.engine.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.DocumentAPI;
import org.bonitasoft.engine.api.impl.transaction.document.GetDocumentByNameAtProcessInstantiation;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentAttachmentException;
import org.bonitasoft.engine.bpm.document.DocumentCriterion;
import org.bonitasoft.engine.bpm.document.DocumentException;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.persistence.OrderAndField;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.document.SearchArchivedDocuments;
import org.bonitasoft.engine.search.document.SearchArchivedDocumentsSupervisedBy;
import org.bonitasoft.engine.search.document.SearchDocuments;
import org.bonitasoft.engine.search.document.SearchDocumentsSupervisedBy;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 */
public class DocumentAPIImpl implements DocumentAPI {

    public DocumentAPIImpl() {
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType, final String url)
            throws DocumentAttachmentException {
        final DocumentValue documentValue = new DocumentValue(url);
        documentValue.setFileName(fileName);
        documentValue.setMimeType(mimeType);
        try {
            return addDocument(processInstanceId, documentName, null, documentValue);
        } catch (final BonitaException e) {
            throw new DocumentAttachmentException(e);
        }
    }

    /*
     * If the target document is a list of document then we append it to the list
     * If the target document is a list of document and the index is set on the document value then we insert the element in the list at the specified index
     * If the target single document or is non existent in the definition we create it
     * If the target single document and is already existent an exception is thrown
     */
    @Override
    public Document addDocument(final long processInstanceId, final String documentName, final String description, final DocumentValue documentValue)
            throws DocumentAttachmentException, AlreadyExistsException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();

        final DocumentHelper documentHelper = new DocumentHelper(documentService, tenantAccessor.getProcessDefinitionService(),
                tenantAccessor.getProcessInstanceService());
        final SDocument sDocument = buildSDocument(documentValue);
        int index = documentValue.getIndex();
        try {
            if (documentHelper.isListDefinedInDefinition(documentName, processInstanceId)) {
                final List<SMappedDocument> allDocumentOfTheList = documentHelper.getAllDocumentOfTheList(processInstanceId, documentName);
                if (index == -1) {
                    index = allDocumentOfTheList.size();
                } else {
                    if (index > allDocumentOfTheList.size()) {
                        throw new DocumentAttachmentException("Can't attach a document on the list " + documentName + " on process instance "
                                + processInstanceId + " the index is out of range, list size is " + allDocumentOfTheList.size());
                    }
                    for (int i = index; i < allDocumentOfTheList.size(); i++) {
                        documentService.updateDocumentIndex(allDocumentOfTheList.get(i), i + 1);
                    }
                }
            } else {
                if (index >= 0) {
                    throw new DocumentAttachmentException("Unable to add a document with an index if it is a single document");
                }
            }
            final SMappedDocument mappedDocument = documentService.attachDocumentToProcessInstance(sDocument, processInstanceId, documentName, description,
                    index);
            return ModelConvertor.toDocument(mappedDocument, documentService);

        } catch (final SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (final SBonitaException e) {
            throw new DocumentAttachmentException(e);
        }

    }

    @Override
    public Document updateDocument(final long documentId, final DocumentValue documentValue) throws DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            final SMappedDocument document = documentService.updateDocument(documentId, buildSDocument(documentValue));
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SBonitaException e) {
            throw new DocumentAttachmentException(e);
        }
    }

    private SDocument buildSDocument(final DocumentValue documentValue) {
        if (documentValue.hasContent()) {
            return buildProcessDocument(documentValue.getFileName(), documentValue.getMimeType(), getUserId(), documentValue.getContent());
        }
        return buildExternalProcessDocumentReference(documentValue.getFileName(), documentValue.getMimeType(), getUserId(), documentValue.getUrl());
    }

    private long getUserId() {
        return APIUtils.getUserId();
    }

    private SDocument buildExternalProcessDocumentReference(final String fileName, final String mimeType, final long authorId, final String url) {
        return BuilderFactory.get(SDocumentBuilderFactory.class).createNewExternalProcessDocumentReference(fileName, mimeType, authorId, url).done();
    }

    private SDocument buildProcessDocument(final String fileName, final String mimeType, final long authorId, final byte[] content) {
        return BuilderFactory.get(SDocumentBuilderFactory.class).createNewProcessDocument(fileName, mimeType, authorId, content).done();
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent) throws DocumentAttachmentException {
        final DocumentValue documentValue = new DocumentValue(documentContent, mimeType, fileName);
        try {
            return addDocument(processInstanceId, documentName, null, documentValue);
        } catch (final BonitaException e) {
            throw new DocumentAttachmentException(e);
        }
    }

    TenantServiceAccessor getTenantAccessor() {
        return APIUtils.getTenantAccessor();
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url) throws DocumentAttachmentException {
        final DocumentService documentService = getTenantAccessor().getDocumentService();
        try {
            return ModelConvertor.toDocument(
                    documentService.updateDocument(documentService.getMappedDocument(processInstanceId, documentName),
                            buildExternalProcessDocumentReference(fileName, mimeType, getUserId(), url)), documentService);
        } catch (final Exception e) {
            throw new DocumentAttachmentException(e);
        }
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String contentFileName,
            final String contentMimeType, final byte[] documentContent) throws DocumentAttachmentException {
        final DocumentService documentService = getTenantAccessor().getDocumentService();
        try {
            return ModelConvertor.toDocument(
                    documentService.updateDocument(documentService.getMappedDocument(processInstanceId, documentName),
                            buildProcessDocument(contentFileName, contentMimeType, getUserId(), documentContent)), documentService);
        } catch (final Exception e) {
            throw new DocumentAttachmentException(e);
        }
    }

    @Override
    public Document getDocument(final long documentId) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toDocument(documentService.getMappedDocument(documentId), documentService);
        } catch (final SObjectNotFoundException e) {
            throw new DocumentNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new DocumentNotFoundException(e);
        }
    }

    @Override
    public List<Document> getLastVersionOfDocuments(final long processInstanceId, final int pageIndex, final int numberPerPage,
            final DocumentCriterion pagingCriterion) throws DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();

        final OrderAndField orderAndField = OrderAndFields.getOrderAndFieldForDocument(pagingCriterion);
        try {
            final List<SMappedDocument> mappedDocuments = documentService.getDocumentsOfProcessInstance(processInstanceId, pageIndex, numberPerPage,
                    orderAndField.getField(), orderAndField.getOrder());
            if (mappedDocuments != null && !mappedDocuments.isEmpty()) {
                final List<Document> result = new ArrayList<Document>(mappedDocuments.size());
                for (final SMappedDocument mappedDocument : mappedDocuments) {
                    result.add(ModelConvertor.toDocument(mappedDocument, documentService));
                }
                return result;
            }
            return Collections.emptyList();
        } catch (final SBonitaReadException e) {
            throw new DocumentException(e);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentStorageId) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return documentService.getDocumentContent(documentStorageId);
        } catch (final SObjectNotFoundException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getLastDocument(final long processInstanceId, final String documentName) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toDocument(documentService.getMappedDocument(processInstanceId, documentName), documentService);
        } catch (final SObjectNotFoundException sbe) {
            throw new DocumentNotFoundException(sbe);
        } catch (final SBonitaReadException e) {
            throw new DocumentNotFoundException(e);
        }
    }

    @Override
    public long getNumberOfDocuments(final long processInstanceId) throws DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return documentService.getNumberOfDocumentsOfProcessInstance(processInstanceId);
        } catch (final SBonitaReadException e) {
            throw new DocumentException(e);
        }
    }

    @Override
    public Document getDocumentAtProcessInstantiation(final long processInstanceId, final String documentName) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();

        try {
            final GetDocumentByNameAtProcessInstantiation transactionContent = new GetDocumentByNameAtProcessInstantiation(documentService,
                    processInstanceService, tenantAccessor.getProcessDefinitionService(), searchEntitiesDescriptor, processInstanceId, documentName);
            transactionContent.execute();
            final SMappedDocument attachment = transactionContent.getResult();
            return ModelConvertor.toDocument(attachment, documentService);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getDocumentAtActivityInstanceCompletion(final long activityInstanceId, final String documentName) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        try {
            final SAActivityInstance instance = activityInstanceService.getMostRecentArchivedActivityInstance(activityInstanceId);
            final SMappedDocument document = documentService.getMappedDocument(instance.getRootContainerId(), documentName, instance.getArchiveDate());
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SBonitaException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public SearchResult<Document> searchDocuments(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final DocumentService documentService = tenantAccessor.getDocumentService();

        final SearchDocuments searchDocuments = new SearchDocuments(documentService, searchEntitiesDescriptor.getSearchDocumentDescriptor(),
                searchOptions);
        try {
            searchDocuments.execute();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<Document> searchDocumentsSupervisedBy(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final DocumentService documentService = tenantAccessor.getDocumentService();

        final SearchDocumentsSupervisedBy searchDocuments = new SearchDocumentsSupervisedBy(documentService,
                searchEntitiesDescriptor.getSearchDocumentDescriptor(), searchOptions, userId);
        try {
            searchDocuments.execute();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<ArchivedDocument> searchArchivedDocuments(final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final SearchArchivedDocuments searchDocuments = new SearchArchivedDocuments(documentService,
                searchEntitiesDescriptor.getSearchArchivedDocumentDescriptor(), searchOptions);
        try {
            searchDocuments.execute();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public SearchResult<ArchivedDocument> searchArchivedDocumentsSupervisedBy(final long userId, final SearchOptions searchOptions) throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final SearchArchivedDocumentsSupervisedBy searchDocuments = new SearchArchivedDocumentsSupervisedBy(userId, documentService,
                searchEntitiesDescriptor.getSearchArchivedDocumentDescriptor(), searchOptions);
        try {
            searchDocuments.execute();
        } catch (final SBonitaException e) {
            throw new SearchException(e);
        }
        return searchDocuments.getResult();
    }

    @Override
    public ArchivedDocument getArchivedVersionOfProcessDocument(final long sourceObjectId) throws ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();

        try {
            return ModelConvertor.toArchivedDocument(documentService.getArchivedVersionOfProcessDocument(sourceObjectId), documentService);
        } catch (final SObjectNotFoundException e) {
            throw new ArchivedDocumentNotFoundException(e);
        }
    }

    @Override
    public ArchivedDocument getArchivedProcessDocument(final long archivedProcessDocumentId) throws ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toArchivedDocument(documentService.getArchivedDocument(archivedProcessDocumentId), documentService);
        } catch (final SObjectNotFoundException e) {
            throw new ArchivedDocumentNotFoundException(e);
        }
    }

    @Override
    public Document removeDocument(final long documentId) throws DocumentNotFoundException, DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final DocumentHelper documentHelper = new DocumentHelper(documentService, tenantAccessor.getProcessDefinitionService(),
                tenantAccessor.getProcessInstanceService());
        try {
            final SMappedDocument document = documentService.getMappedDocument(documentId);
            final int index = document.getIndex();
            if (index != -1) {
                //document is in list
                final List<SMappedDocument> allDocumentOfTheList = documentHelper.getAllDocumentOfTheList(document.getProcessInstanceId(), document.getName());

                for (int i = index + 1; i < allDocumentOfTheList.size(); i++) {
                    documentService.updateDocumentIndex(allDocumentOfTheList.get(i), i - 1);

                }
            }
            documentService.removeCurrentVersion(document);
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SObjectNotFoundException e) {
            throw new DocumentNotFoundException("Unable to delete the document " + documentId + " because it does not exists", e);
        } catch (final SBonitaException e) {
            throw new DeletionException("Unable to delete the document " + documentId, e);
        }
    }

    @Override
    public List<Document> getDocumentList(final long processInstanceId, final String name, final int fromIndex, final int numberOfResult)
            throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final DocumentHelper documentHelper = new DocumentHelper(documentService, processDefinitionService, processInstanceService);
        try {
            final List<SMappedDocument> documentList = documentService.getDocumentList(name, processInstanceId, fromIndex, numberOfResult);
            if (documentList.isEmpty()
                    && !documentHelper.isListDefinedInDefinition(name, processInstanceId)) {
                throw new DocumentNotFoundException("doc not found");
            }
            return ModelConvertor.toDocuments(documentList, documentService);
        } catch (final org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException e) {
            throw new DocumentNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void setDocumentList(final long processInstanceId, final String name, final List<DocumentValue> documentsValues) throws DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentHelper documentHelper = new DocumentHelper(tenantAccessor.getDocumentService(), tenantAccessor.getProcessDefinitionService(),
                tenantAccessor.getProcessInstanceService());
        try {
            documentHelper.setDocumentList(documentsValues, name, processInstanceId, getUserId());
        } catch (final SBonitaException e) {
            throw new DocumentException("Unable to set the list " + name + " on process instance " + processInstanceId, e);
        }
    }

    @Override
    public void deleteContentOfArchivedDocument(final long archivedDocumentId) throws DocumentException, DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = APIUtils.getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            documentService.deleteContentOfArchivedDocument(archivedDocumentId);
        } catch (final SObjectNotFoundException e) {
            throw new DocumentNotFoundException("The document with id " + archivedDocumentId + " could not be found", e);
        } catch (final SBonitaException e) {
            throw new DocumentException("Unable to delete content of all version of the document " + archivedDocumentId, e);
        }
    }

}
