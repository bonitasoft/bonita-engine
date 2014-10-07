/*
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
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SDocumentCreationException;
import org.bonitasoft.engine.core.document.exception.SDocumentException;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.operation.DocumentListLeftOperandHandler;
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
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final long author = getUserId();
        try {
            final SDocument attachment = buildExternalProcessDocumentReference(fileName, mimeType, author, url);
            final SMappedDocument document = documentService.attachDocumentToProcessInstance(attachment, processInstanceId, documentName, null);
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }


    /*
     * If the target document is a list of document then we append it to the list
     * If the target document is a list of document and the index is set on the document value then we insert the element in the list at the specified index
     * If the target single document or is non existent in the definition we create it
     * If the target single document and is already existent an exception is thrown
     */
    @Override
    public Document addDocument(long processInstanceId, String documentName, String description, DocumentValue documentValue) throws ProcessInstanceNotFoundException, DocumentAttachmentException, AlreadyExistsException {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        DocumentService documentService = tenantAccessor.getDocumentService();

        SDocument sDocument = buildSDocument(documentValue);
        int index = documentValue.getIndex();
        try {
            if(isList(processInstanceId,documentName, tenantAccessor)){
                List<SMappedDocument> allDocumentOfTheList = DocumentListLeftOperandHandler.getAllDocumentOfTheList(processInstanceId, documentName, documentService);
                if(index == -1){
                    index = allDocumentOfTheList.size();
                }else{
                    if(index > allDocumentOfTheList.size()){
                        throw new DocumentAttachmentException("Can't attach a document on the list "+documentName+ " on process instance "+processInstanceId +" the index is out of range, list size is "+allDocumentOfTheList.size());
                    }
                    for (int i = index; i < allDocumentOfTheList.size() ; i++) {
                        documentService.updateDocumentIndex(allDocumentOfTheList.get(i),i+1);
                    }
                }
            }else{
                if(index>=0){
                    throw  new DocumentAttachmentException("Unable to add a document with an index if it is a single document");
                }
            }
            SMappedDocument mappedDocument = documentService.attachDocumentToProcessInstance(sDocument, processInstanceId, documentName, description, index);
            return ModelConvertor.toDocument(mappedDocument,documentService);

        } catch (SDocumentCreationException e) {
            throw new DocumentAttachmentException(e);
        } catch (SObjectAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        } catch (SObjectNotFoundException e) {
            throw new DocumentAttachmentException(e);
        } catch (SBonitaReadException e) {
            throw new DocumentAttachmentException(e);
        }

    }

    private boolean isList(long processInstanceId, String documentName, TenantServiceAccessor tenantServiceAccessor) throws SObjectNotFoundException, SBonitaReadException {
        return DocumentListLeftOperandHandler.isListDefinedInDefinition(documentName,processInstanceId,tenantServiceAccessor.getProcessDefinitionService(),tenantServiceAccessor.getProcessInstanceService());
    }

    @Override
    public Document updateDocument(long documentId, DocumentValue documentValue) throws DocumentAttachmentException {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        DocumentService documentService = tenantAccessor.getDocumentService();
        try{
            SMappedDocument document = documentService.updateDocument(documentId,buildSDocument(documentValue));
            return ModelConvertor.toDocument(document,documentService);
        }catch(SBonitaException e){
            throw new DocumentAttachmentException(e);
        }
    }

    private SDocument buildSDocument(DocumentValue documentValue) {
        if(documentValue.hasContent()){
            return buildProcessDocument(documentValue.getFileName(),documentValue.getMimeType(), getUserId(),documentValue.getContent());
        }else{
            return buildExternalProcessDocumentReference(documentValue.getFileName(), documentValue.getMimeType(), getUserId(), documentValue.getUrl());
        }
    }

    private long getUserId() {
        return APIUtils.getUserId();
    }

    private SDocument buildExternalProcessDocumentReference(final String fileName,
            final String mimeType, final long authorId, final String url) {
        final SDocumentBuilder documentBuilder = initDocumentBuilder(fileName, mimeType, authorId);
        documentBuilder.setURL(url);
        documentBuilder.setHasContent(false);
        return documentBuilder.done();
    }

    private SDocument buildProcessDocument(final String fileName, final String mimetype,
            final long authorId, byte[] content) {
        final SDocumentBuilder documentBuilder = initDocumentBuilder(fileName, mimetype, authorId);
        documentBuilder.setHasContent(true);
        documentBuilder.setContent(content);
        return documentBuilder.done();
    }

    private SDocumentBuilder initDocumentBuilder(final String fileName, final String mimetype,
            final long authorId) {
        final SDocumentBuilder documentBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        documentBuilder.setFileName(fileName);
        documentBuilder.setMimeType(mimetype);
        documentBuilder.setAuthor(authorId);
        documentBuilder.setCreationDate(System.currentTimeMillis());
        return documentBuilder;
    }

    @Override
    public Document attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent) throws DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final long authorId = getUserId();
        try {
            final SMappedDocument mappedDocument = attachDocument(processInstanceId, documentName, fileName, mimeType, documentContent, documentService,
                    authorId, null);
            return ModelConvertor.toDocument(mappedDocument, documentService);
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    TenantServiceAccessor getTenantAccessor() {
        return APIUtils.getTenantAccessor();
    }

    protected SMappedDocument attachDocument(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final byte[] documentContent, final DocumentService documentService, final long authorId, String description) throws SBonitaException {
        final SDocument attachment = buildProcessDocument(fileName, mimeType, authorId, documentContent);
        return documentService.attachDocumentToProcessInstance(attachment, processInstanceId, documentName, description);
    }

    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String fileName, final String mimeType,
            final String url) throws DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final long authorId = getUserId();
        try {
            final SDocument attachment = buildExternalProcessDocumentReference(fileName, mimeType, authorId, url);

            return ModelConvertor.toDocument(documentService.updateDocumentOfProcessInstance(attachment, processInstanceId, documentName, null),
                    documentService);
        } catch (final SBonitaException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }


    @Override
    public Document attachNewDocumentVersion(final long processInstanceId, final String documentName, final String contentFileName,
            final String contentMimeType, final byte[] documentContent) throws DocumentAttachmentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        final long authorId = getUserId();
        try {
            final SDocument attachment = buildProcessDocument(contentFileName, contentMimeType, authorId, documentContent);
            return ModelConvertor.toDocument(documentService.updateDocumentOfProcessInstance(attachment, processInstanceId, documentName, null),
                    documentService);
        } catch (final SDocumentCreationException sbe) {
            throw new DocumentAttachmentException(sbe);
        }
    }

    @Override
    public Document getDocument(final long documentId) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toDocument(documentService.getMappedDocument(documentId), documentService);
        } catch (SDocumentNotFoundException e) {
            throw new DocumentNotFoundException(e);
        } catch (SBonitaReadException e) {
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
        } catch (final SDocumentException sbe) {
            throw new DocumentException(sbe);
        }
    }

    @Override
    public byte[] getDocumentContent(final String documentStorageId) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return documentService.getDocumentContent(documentStorageId);
        } catch (final SDocumentNotFoundException sbe) {
            throw new DocumentNotFoundException(sbe);
        }
    }

    @Override
    public Document getLastDocument(final long processInstanceId, final String documentName) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toDocument(documentService.getMappedDocument(processInstanceId, documentName), documentService);
        } catch (final SDocumentNotFoundException sbe) {
            throw new DocumentNotFoundException(sbe);
        } catch (SBonitaReadException e) {
            throw new DocumentNotFoundException(e);
        }
    }

    @Override
    public long getNumberOfDocuments(final long processInstanceId) throws DocumentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return documentService.getNumberOfDocumentsOfProcessInstance(processInstanceId);

        } catch (final SDocumentException sbe) {
            throw new DocumentException(sbe);
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
                    processInstanceService, searchEntitiesDescriptor, processInstanceId, documentName);
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
            SMappedDocument document = documentService.getMappedDocument(instance.getRootContainerId(), documentName, instance.getArchiveDate());
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
        } catch (SDocumentNotFoundException e) {
            throw new ArchivedDocumentNotFoundException(e);
        }
    }

    @Override
    public ArchivedDocument getArchivedProcessDocument(final long archivedProcessDocumentId) throws ArchivedDocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            return ModelConvertor.toArchivedDocument(documentService.getArchivedDocument(archivedProcessDocumentId), documentService);
        } catch (final SDocumentNotFoundException e) {
            throw new ArchivedDocumentNotFoundException(e);
        }
    }

    @Override
    public Document removeDocument(long documentId) throws DocumentNotFoundException, DeletionException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        try {
            SMappedDocument document = documentService.getMappedDocument(documentId);
            int index = document.getIndex();
            if(index != -1){
                //document is in list
                List<SMappedDocument> allDocumentOfTheList = DocumentListLeftOperandHandler.getAllDocumentOfTheList(document.getProcessInstanceId(), document.getName(), documentService);

                    for (int i = index+1; i < allDocumentOfTheList.size() ; i++) {
                        documentService.updateDocumentIndex(allDocumentOfTheList.get(i),i-1);

                }
            }
            documentService.removeCurrentVersion(document);
            return ModelConvertor.toDocument(document, documentService);
        } catch (final SDocumentNotFoundException e) {
            throw new DocumentNotFoundException("Unable to delete the document " + documentId + " because it does not exists", e);
        } catch (SBonitaReadException e) {
            throw new DeletionException("Unable to delete the document " + documentId, e);
        } catch (SObjectModificationException e) {
            throw new DeletionException("Unable to delete the document " + documentId, e);
        } catch (SDocumentCreationException e) {
            throw new DeletionException("Unable to delete the document " + documentId, e);
        }
    }

    @Override
    public List<Document> getDocumentList(long processInstanceId, String name, int fromIndex, int numberOfResult) throws DocumentNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DocumentService documentService = tenantAccessor.getDocumentService();
        ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        try {
            List<SMappedDocument> documentList = documentService.getDocumentList(name, processInstanceId,fromIndex,numberOfResult);
            if (documentList.isEmpty()
                    && !DocumentListLeftOperandHandler.isListDefinedInDefinition(name, processInstanceId, processDefinitionService, processInstanceService)) {
                throw new DocumentNotFoundException("doc not found");
            }
            return ModelConvertor.toDocuments(documentList, documentService);
        } catch (SObjectNotFoundException e) {
            throw new DocumentNotFoundException(e);
        } catch (SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public void setDocumentList(long processInstanceId, String name, List<DocumentValue> documentsValues) throws DocumentException {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        DocumentListLeftOperandHandler documentListLeftOperandHandler = new DocumentListLeftOperandHandler(tenantAccessor.getDocumentService(), tenantAccessor.getActivityInstanceService(), tenantAccessor.getSessionAccessor(), tenantAccessor.getSessionService(), tenantAccessor.getProcessDefinitionService(), tenantAccessor.getProcessInstanceService());
        try {
            documentListLeftOperandHandler.setDocumentList(documentsValues,name,processInstanceId);
        } catch (SBonitaException e) {
            throw new DocumentException("Unable to set the list "+name+" on process instance "+processInstanceId,e);
        }
    }

    @Override
    public void deleteContentOfArchivedDocument(long documentId) throws DocumentException, DocumentNotFoundException {
        TenantServiceAccessor tenantAccessor = APIUtils.getTenantAccessor();
        DocumentService documentService = tenantAccessor.getDocumentService();
        try{
            documentService.deleteContentOfArchivedDocument(documentId);
        }catch (SDocumentNotFoundException e){
            throw new DocumentNotFoundException("The document with id "+documentId+" could not be found",e);
        }catch (SBonitaException e ){
            throw new DocumentException("Unable to delete content of all version of the document "+documentId,e);
        }
    }


}
