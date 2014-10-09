/*
 *
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
 *
 */

package org.bonitasoft.engine.core.document.api.impl;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SSessionNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * Helper class that set/get/update document on API level (it uses the process definition)
 *
 * @author Baptiste Mesta
 */
public class DocumentHelper {

    private DocumentService documentService;
    private ProcessDefinitionService processDefinitionService;
    private ProcessInstanceService processInstanceService;

    public DocumentHelper(DocumentService documentService, ProcessDefinitionService processDefinitionService, ProcessInstanceService processInstanceService) {
        this.documentService = documentService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
    }

    public List<SMappedDocument> getAllDocumentOfTheList(long processInstanceId, String name) throws SBonitaReadException {
        QueryOptions queryOptions = new QueryOptions(0, 100);
        List<SMappedDocument> mappedDocuments;
        List<SMappedDocument> result = new ArrayList<SMappedDocument>();
        do {
            mappedDocuments = documentService.getDocumentList(name, processInstanceId, queryOptions.getFromIndex(), queryOptions.getNumberOfResults());
            result.addAll(mappedDocuments);
            queryOptions = QueryOptions.getNextPage(queryOptions);
        } while (mappedDocuments.size() == 100);
        return result;
    }

    public boolean isListDefinedInDefinition(String documentName, long processInstanceId) throws org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException, SBonitaReadException {
        try {
            SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processInstance.getProcessDefinitionId());
            List<SDocumentListDefinition> documentDefinitions = processDefinition.getProcessContainer().getDocumentListDefinitions();
            for (SDocumentListDefinition documentDefinition : documentDefinitions) {
                if (documentName.equals(documentDefinition.getName())) {
                    return true;
                }
            }
        } catch (SProcessInstanceNotFoundException e) {
            throw new SObjectNotFoundException("Unable to find the list " + documentName + ", nothing in database and the process instance "
                    + processInstanceId + " is not found", e);
        } catch (SProcessInstanceReadException e) {
            throw new SBonitaReadException(e);
        } catch (SProcessDefinitionNotFoundException e) {
            throw new SObjectNotFoundException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                    + ", nothing in database and the process definition is not found", e);
        } catch (SProcessDefinitionReadException e) {
            throw new SBonitaReadException(e);
        }
        return false;
    }

    public SDocument createDocumentObject(final DocumentValue documentValue, long authorId) {
        final SDocumentBuilder processDocumentBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        processDocumentBuilder.setFileName(documentValue.getFileName());
        processDocumentBuilder.setMimeType(documentValue.getMimeType());
        processDocumentBuilder.setAuthor(authorId);
        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
        processDocumentBuilder.setHasContent(documentValue.hasContent());
        processDocumentBuilder.setURL(documentValue.getUrl());
        processDocumentBuilder.setContent(documentValue.getContent());
        return processDocumentBuilder.done();
    }

    public void deleteDocument(String documentName, long processInstanceId) throws SObjectModificationException {
        try {
            documentService.removeCurrentVersion(processInstanceId, documentName);
        } catch (final SObjectNotFoundException e) {
            // nothing to do
        }
    }

    public void createOrUpdateDocument(DocumentValue newValue, String documentName, long processInstanceId, long authorId) throws SSessionNotFoundException,
            SBonitaReadException, SObjectCreationException, SObjectModificationException {
        final SDocument document = createDocumentObject(newValue, authorId);
        try {
            // Let's check if the document already exists:
            SMappedDocument mappedDocument = documentService.getMappedDocument(processInstanceId, documentName);
            // a document exist, update it with the new values
            documentService.updateDocument(mappedDocument, document);
        } catch (final SObjectNotFoundException e) {
            documentService.attachDocumentToProcessInstance(document, processInstanceId, documentName, null);
        }
    }

    public void setDocumentList(List<DocumentValue> documentList, String documentName, long processInstanceId, long authorId) throws SBonitaReadException,
            SObjectCreationException, SSessionNotFoundException, SObjectNotFoundException,
            SObjectModificationException, SObjectAlreadyExistsException{
        // get the list having the name
        List<SMappedDocument> currentList = getExistingDocumentList(documentName, processInstanceId);
        // iterate on elements
        int index;
        for (index = 0; index < documentList.size(); index++) {
            processDocumentOnIndex(documentList, documentName, processInstanceId, currentList, index, authorId);
        }

        // when no more elements in documentList remove elements above
        removeOthersDocuments(currentList);
    }

    private void updateExistingDocument(SMappedDocument documentToUpdate, int index, DocumentValue documentValue, long authorId) throws SObjectCreationException,
            SSessionNotFoundException, SObjectModificationException {
        if (documentValue.hasChanged()) {
            documentService.updateDocumentOfList(documentToUpdate, createDocumentObject(documentValue, authorId), index);
        } else {
            //  update the index if needed
            if (documentToUpdate.getIndex() != index) {
                documentService.updateDocumentIndex(documentToUpdate, index);
            }
        }
    }

    private SMappedDocument getDocumentHavingDocumentIdAndRemoveFromList(List<SMappedDocument> currentList, Long documentId, String documentName,
                                                                         Long processInstanceId) throws org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException {
        Iterator<SMappedDocument> iterator = currentList.iterator();
        while (iterator.hasNext()) {
            SMappedDocument next = iterator.next();
            if (next.getId() == documentId) {
                iterator.remove();
                return next;
            }
        }
        throw new org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException("The document with id " + documentId + " was not in the list " + documentName + " of process instance "
                + processInstanceId);
    }



    private List<SMappedDocument> getExistingDocumentList(String documentName, long processInstanceId) throws SBonitaReadException, org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException {
        List<SMappedDocument> currentList;
        currentList = getAllDocumentOfTheList(processInstanceId, documentName);
        // if it's not a list it throws an exception
        if (currentList.isEmpty() && !isListDefinedInDefinition(documentName, processInstanceId)) {
            throw new org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                    + ", nothing in database and nothing declared in the definition");
        }
        return currentList;
    }

    private void removeOthersDocuments(List<SMappedDocument> currentList) throws SObjectNotFoundException, SObjectModificationException {
        for (SMappedDocument mappedDocument : currentList) {
            documentService.removeCurrentVersion(mappedDocument);
        }

    }

    private void processDocumentOnIndex(List<DocumentValue> documentList, String documentName, long processInstanceId, List<SMappedDocument> currentList,
                                        int index, long authorId) throws SObjectCreationException, SSessionNotFoundException, SObjectAlreadyExistsException, SObjectNotFoundException, SObjectModificationException {
        DocumentValue documentValue = documentList.get(index);

        if (documentValue.getDocumentId() != null) {
            // if hasChanged update
            SMappedDocument documentToUpdate = getDocumentHavingDocumentIdAndRemoveFromList(currentList, documentValue.getDocumentId(), documentName,
                    processInstanceId);
            updateExistingDocument(documentToUpdate, index, documentValue, authorId);
        } else {
            // create new element
            documentService.attachDocumentToProcessInstance(createDocumentObject(documentValue, authorId), processInstanceId, documentName, null, index);
        }
    }
}
