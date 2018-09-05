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
 */
package org.bonitasoft.engine.core.document.api.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * Helper class that set/get/update document on API level (it uses the process definition)
 *
 * @author Baptiste Mesta
 */
public class DocumentHelper {

    private final DocumentService documentService;
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;

    public DocumentHelper(final DocumentService documentService, final ProcessDefinitionService processDefinitionService,
                          final ProcessInstanceService processInstanceService) {
        this.documentService = documentService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
    }

    public List<SMappedDocument> getAllDocumentOfTheList(final long processInstanceId, final String name) throws SBonitaReadException {
        QueryOptions queryOptions = new QueryOptions(0, 100);
        List<SMappedDocument> mappedDocuments;
        final List<SMappedDocument> result = new ArrayList<SMappedDocument>();
        do {
            mappedDocuments = documentService.getDocumentList(name, processInstanceId, queryOptions.getFromIndex(), queryOptions.getNumberOfResults());
            result.addAll(mappedDocuments);
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (mappedDocuments.size() == 100);
        return result;
    }

    public boolean isListDefinedInDefinition(final String documentName, final long processInstanceId)
            throws org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException, SBonitaReadException {
        try {
            final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
            final List<SDocumentListDefinition> documentDefinitions = processDefinition.getProcessContainer().getDocumentListDefinitions();
            for (final SDocumentListDefinition documentDefinition : documentDefinitions) {
                if (documentName.equals(documentDefinition.getName())) {
                    return true;
                }
            }
        } catch (final SProcessInstanceNotFoundException e) {
            throw new SObjectNotFoundException("Unable to find the list " + documentName + ", nothing in database and the process instance "
                    + processInstanceId + " is not found", e);
        } catch (final SProcessInstanceReadException e) {
            throw new SBonitaReadException(e);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new SObjectNotFoundException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                    + ", nothing in database and the process definition is not found", e);
        }
        return false;
    }

    public SDocument createDocumentObject(final DocumentValue documentValue, final long authorId) {
        final SDocumentBuilder processDocumentBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance(documentValue.getFileName(),
                getMimeTypeOrGuessIt(documentValue), authorId);
        processDocumentBuilder.setHasContent(documentValue.hasContent());
        processDocumentBuilder.setURL(documentValue.getUrl());
        processDocumentBuilder.setContent(documentValue.getContent());
        return processDocumentBuilder.done();
    }

    String getMimeTypeOrGuessIt(DocumentValue documentValue) {
        final String mimeType = documentValue.getMimeType();
        final byte[] content = documentValue.getContent();
        final String fileName = documentValue.getFileName();
        if (mimeType != null && !mimeType.isEmpty() || content == null || fileName == null || fileName.isEmpty()) {
            return mimeType;
        }
        try {
            final File tempFile = File.createTempFile("tmp", fileName);
            IOUtil.write(tempFile, content);
            final String s = Files.probeContentType(tempFile.toPath());
            tempFile.delete();
            return s;
        } catch (Throwable e) {
            return mimeType;
        }
    }

    public void deleteDocument(final String documentName, final long processInstanceId) throws SObjectModificationException {
        try {
            documentService.removeCurrentVersion(processInstanceId, documentName);
        } catch (final SObjectNotFoundException e) {
            // nothing to do
        }
    }

    /**
     * @param newValue          the new value
     * @param documentName      the name of the document
     * @param processInstanceId the id of the process instance
     * @param authorId          the author id
     * @param description       used only when creating a document
     * @throws SBonitaReadException
     * @throws SObjectCreationException
     * @throws SObjectModificationException
     */
    public void createOrUpdateDocument(final DocumentValue newValue, final String documentName, final long processInstanceId, final long authorId,
                                       String description)
            throws SBonitaReadException, SObjectCreationException, SObjectModificationException {
        final SDocument document = createDocumentObject(newValue, authorId);
        try {
            // Let's check if the document already exists:
            final SMappedDocument mappedDocument = documentService.getMappedDocument(processInstanceId, documentName);
            // a document exist, update it with the new values
            documentService.updateDocument(mappedDocument, document);
        } catch (final SObjectNotFoundException e) {
            documentService.attachDocumentToProcessInstance(document, processInstanceId, documentName, description);
        }
    }

    public void setDocumentList(final List<DocumentValue> documentList, final String documentName, final long processInstanceId, final long authorId)
            throws SBonitaReadException, SObjectCreationException, SObjectNotFoundException, SObjectModificationException, SObjectAlreadyExistsException {
        // get the list having the name
        final List<SMappedDocument> currentList = getExistingDocumentList(documentName, processInstanceId);
        // iterate on elements
        int index;
        for (index = 0; index < documentList.size(); index++) {
            processDocumentOnIndex(documentList.get(index), documentName, processInstanceId, currentList, index, authorId);
        }

        // when no more elements in documentList remove elements above
        removeOthersDocuments(currentList);
    }

    void updateExistingDocument(final SMappedDocument documentToUpdate, final int index, final DocumentValue documentValue, final long authorId)
            throws SObjectModificationException {
        if (documentValue.hasChanged()) {
            documentService.updateDocumentOfList(documentToUpdate, createDocumentObject(documentValue, authorId), index);
        } else {
            //  update the index if needed
            if (documentToUpdate.getIndex() != index) {
                documentService.updateDocumentIndex(documentToUpdate, index);
            }
        }
    }

    SMappedDocument getDocumentHavingDocumentIdAndRemoveFromList(final List<SMappedDocument> currentList, final Long documentId, final String documentName,
                                                                 final Long processInstanceId) throws SObjectNotFoundException {
        final Iterator<SMappedDocument> iterator = currentList.iterator();
        while (iterator.hasNext()) {
            final SMappedDocument next = iterator.next();
            if (next.getId() == documentId) {
                iterator.remove();
                return next;
            }
        }
        throw new SObjectNotFoundException("The document with id " + documentId + " was not in the list " + documentName + " of process instance "
                + processInstanceId);
    }

    List<SMappedDocument> getExistingDocumentList(final String documentName, final long processInstanceId) throws SBonitaReadException,
            SObjectNotFoundException {
        List<SMappedDocument> currentList;
        currentList = getAllDocumentOfTheList(processInstanceId, documentName);
        // if it's not a list it throws an exception
        if (currentList.isEmpty() && !isListDefinedInDefinition(documentName, processInstanceId)) {
            throw new SObjectNotFoundException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                    + ", nothing in database and nothing declared in the definition");
        }
        return currentList;
    }

    void removeOthersDocuments(final List<SMappedDocument> currentList) throws SObjectModificationException {
        for (final SMappedDocument mappedDocument : currentList) {
            documentService.removeCurrentVersion(mappedDocument);
        }

    }

    void processDocumentOnIndex(final DocumentValue documentValue, final String documentName, final long processInstanceId,
                                final List<SMappedDocument> currentList, final int index, final long authorId) throws SObjectCreationException, SObjectAlreadyExistsException,
            SObjectNotFoundException, SObjectModificationException {
        if (documentValue.getDocumentId() != null) {
            // if hasChanged update
            final SMappedDocument documentToUpdate = getDocumentHavingDocumentIdAndRemoveFromList(currentList, documentValue.getDocumentId(), documentName,
                    processInstanceId);
            updateExistingDocument(documentToUpdate, index, documentValue, authorId);
        } else {
            // create new element
            documentService.attachDocumentToProcessInstance(createDocumentObject(documentValue, authorId), processInstanceId, documentName, null, index);
        }
    }

    public DocumentValue toCheckedDocumentValue(final Object newValue) throws SOperationExecutionException {
        if (newValue != null) {
            final boolean isFileInput = newValue instanceof FileInputValue;
            if (isFileInput) {
                FileInputValue fileInput = ((FileInputValue) newValue);
                return toDocumentValue(fileInput);
            }
            final boolean isDocumentWithContent = newValue instanceof DocumentValue;
            if (!isDocumentWithContent) {
                throw new SOperationExecutionException("Document operation only accepts an expression returning a DocumentValue and not "
                        + newValue.getClass().getName());
            }
        }
        return (DocumentValue) newValue;
    }

    public DocumentValue toDocumentValue(FileInputValue fileInput) {
        return new DocumentValue(fileInput.getContent(), fileInput.getContentType(), fileInput.getFileName());
    }

    public DocumentValue toDocumentValue(Document document) throws SOperationExecutionException {
        DocumentValue documentValue;
        if (document.hasContent()) {
            try {
                byte[] documentContent = documentService.getDocumentContent(document.getContentStorageId());
                documentValue = new DocumentValue(documentContent, document.getContentMimeType(), document.getContentFileName());
            } catch (SObjectNotFoundException e) {
                throw new SOperationExecutionException("Unable to execute set document operation because the content of the document to use is not found", e);
            }
        } else {
            documentValue = new DocumentValue(document.getUrl());
        }
        return documentValue;
    }

    public List<DocumentValue> toCheckedList(final Object newValue) throws SOperationExecutionException {
        if (!(newValue instanceof List)) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");
        }
        List<DocumentValue> newList = new ArrayList<>(((List) newValue).size());
        for (final Object item : (List<?>) newValue) {
            if (item instanceof FileInputValue) {
                newList.add(toDocumentValue((FileInputValue) item));
                continue;
            }
            if (item instanceof Document) {
                newList.add(toDocumentValue((Document) item));
                continue;
            }
            if (item instanceof DocumentValue) {
                newList.add((DocumentValue) item);
                continue;

            }
            if (item == null) {
                //ignore the item
                continue;
            }
            throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");

        }
        return newList;
    }

}
