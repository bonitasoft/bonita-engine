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
import java.util.Iterator;
import java.util.List;

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
        } catch (final SProcessDefinitionReadException e) {
            throw new SBonitaReadException(e);
        }
        return false;
    }

    public SDocument createDocumentObject(final DocumentValue documentValue, final long authorId) {
        final SDocumentBuilder processDocumentBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance(documentValue.getFileName(),
                documentValue.getMimeType(), authorId);
        processDocumentBuilder.setHasContent(documentValue.hasContent());
        processDocumentBuilder.setURL(documentValue.getUrl());
        processDocumentBuilder.setContent(documentValue.getContent());
        return processDocumentBuilder.done();
    }

    public void deleteDocument(final String documentName, final long processInstanceId) throws SObjectModificationException {
        try {
            documentService.removeCurrentVersion(processInstanceId, documentName);
        } catch (final SObjectNotFoundException e) {
            // nothing to do
        }
    }

    public void createOrUpdateDocument(final DocumentValue newValue, final String documentName, final long processInstanceId, final long authorId)
            throws SBonitaReadException, SObjectCreationException, SObjectModificationException {
        final SDocument document = createDocumentObject(newValue, authorId);
        try {
            // Let's check if the document already exists:
            final SMappedDocument mappedDocument = documentService.getMappedDocument(processInstanceId, documentName);
            // a document exist, update it with the new values
            documentService.updateDocument(mappedDocument, document);
        } catch (final SObjectNotFoundException e) {
            documentService.attachDocumentToProcessInstance(document, processInstanceId, documentName, null);
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
}
