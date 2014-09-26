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
package org.bonitasoft.engine.operation;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.exception.SProcessDocumentCreationException;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Handles document lists
 * this operation accepts only a List of {@link org.bonitasoft.engine.bpm.document.DocumentValue}
 *
 * @author Baptiste Mesta
 */
public class DocumentListLeftOperandHandler extends AbstractDocumentLeftOperandHandler {

    DocumentService documentService;
    private ProcessDefinitionService processDefinitionService;
    private ProcessInstanceService processInstanceService;

    public DocumentListLeftOperandHandler(final DocumentService documentService, final ActivityInstanceService activityInstanceService,
            final SessionAccessor sessionAccessor, final SessionService sessionService, ProcessDefinitionService processDefinitionService,
            ProcessInstanceService processInstanceService) {
        super(activityInstanceService, sessionAccessor, sessionService, documentService);
        this.documentService = documentService;
        this.processDefinitionService = processDefinitionService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        List<DocumentValue> documentList = toCheckedList(newValue);
        final String documentName = sLeftOperand.getName();

        try {
            long processInstanceId = getProcessInstanceId(containerId, containerType);
            // get the list having the name
            List<SMappedDocument> currentList = getExistingDocumentList(documentName, processInstanceId);
            // iterate on elements
            int index;
            for (index = 0; index < documentList.size(); index++) {
                processDocumentOnIndex(documentList, documentName, processInstanceId, currentList, index);
            }

            // when no more elements in documentList remove elements above
            removeOthersDocuments(currentList);

            return documentList;
        } catch (final SOperationExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }

    }

    private List<SMappedDocument> getExistingDocumentList(String documentName, long processInstanceId) throws SBonitaReadException, SObjectNotFoundException,
            SOperationExecutionException {
        List<SMappedDocument> currentList;
        currentList = documentService.getDocumentList(documentName, processInstanceId);
        // if it's not a list it throws an exception
        if (currentList.isEmpty() && !isListDefinedInDefinition(documentName, processInstanceId, processDefinitionService, processInstanceService)) {
//            try {
//                documentService.getMappedDocument(processInstanceId,documentName);
//            } catch (SDocumentNotFoundException e) {
//                throw new SOperationExecutionException("Unable to find the list " + documentName + " on process instance " + processInstanceId
//                        + ", nothing in database and nothing declared in the definition");
//            }
            throw new SOperationExecutionException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                    + ", nothing in database and nothing declared in the definition");
        }
        return currentList;
    }

    private void removeOthersDocuments(List<SMappedDocument> currentList) throws SDocumentNotFoundException, SObjectModificationException {
        for (SMappedDocument mappedDocument : currentList) {
            documentService.removeCurrentVersion(mappedDocument);
        }

    }

    private void processDocumentOnIndex(List<DocumentValue> documentList, String documentName, long processInstanceId, List<SMappedDocument> currentList,
            int index) throws SProcessDocumentCreationException, SSessionNotFoundException, SOperationExecutionException {
        DocumentValue documentValue = documentList.get(index);

        if (documentValue.getDocumentId() != null) {
            // if hasChanged update
            SMappedDocument documentToUpdate = getDocumentHavingDocumentIdAndRemoveFromList(currentList, documentValue.getDocumentId(), documentName, processInstanceId);
            updateExistingDocument(documentToUpdate, index, documentValue);
        } else {
            // create new element
            documentService.attachDocumentToProcessInstance(createDocumentObject(documentValue), processInstanceId, documentName, null, index);
        }
    }

    private void updateExistingDocument(SMappedDocument documentToUpdate, int index, DocumentValue documentValue) throws SProcessDocumentCreationException,
            SSessionNotFoundException, SOperationExecutionException {
        if (documentValue.hasChanged()) {
            documentService.updateDocumentOfList(documentToUpdate, createDocumentObject(documentValue), index);
        } else {
            //  update the index if needed
            if ( documentToUpdate.getIndex() != index) {
                documentService.updateDocumentIndex(documentToUpdate, index);
            }
        }
    }

    private SMappedDocument getDocumentHavingDocumentIdAndRemoveFromList(List<SMappedDocument> currentList, Long documentId, String documentName, Long processInstanceId) throws SOperationExecutionException {
        Iterator<SMappedDocument> iterator = currentList.iterator();
        while(iterator.hasNext()){
            SMappedDocument next = iterator.next();
            if (next.getDocumentId() == documentId) {
                iterator.remove();
                return next;
            }
        }
        throw new SOperationExecutionException("The document with id " + documentId + " was not in the list "+documentName+" of process instance "+processInstanceId);
    }

    @Override
    public String getType() {
        return LeftOperand.TYPE_DOCUMENT_LIST;
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType) throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a document is not supported");
    }

    @Override
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) {
        return null;
    }

    @Override
    public boolean supportBatchUpdate() {
        return true;
    }

    public static boolean isListDefinedInDefinition(String documentName, long processInstanceId, ProcessDefinitionService processDefinitionService,
            ProcessInstanceService processInstanceService) throws SObjectNotFoundException, SBonitaReadException {
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

}
