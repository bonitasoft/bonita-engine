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

import java.util.List;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.document.api.DocumentService;
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

        long processInstanceId;
        try {
            processInstanceId = getProcessInstanceId(containerId, containerType);

            // get the list having the name
            List<SMappedDocument> currentList;
            currentList = documentService.getDocumentList(documentName, processInstanceId);
            // if it's not a list it throws an exception

            if (currentList.isEmpty() && !isListDefinedInDefinition(documentName, processInstanceId, processDefinitionService, processInstanceService)) {
                throw new SOperationExecutionException("Unable to find the list " + documentName + " on process instance " + processInstanceId
                        + "nothing in database and nothing declared in the definition");
            }
            // iterate on elements
            int index;
            for (index = 0; index < documentList.size(); index++) {
                DocumentValue documentValue = documentList.get(index);

                // if documentId not null
                if (documentValue.getDocumentId() != null) {
                    // if hasChanged update
                    if (documentValue.hasChanged()) {
                        documentService.updateDocumentOfList(currentList.get(index), createDocumentObject(documentValue), index);
                    } else {
                        // else update the index if needed
                        if (currentList.get(index).getIndex() != index) {
                            // update index
                            documentService.updateDocumentIndex(currentList.get(index), index);
                        }
                    }

                } else {
                    // if documentId null
                    // create new element
                    documentService.attachDocumentToProcessInstance(createDocumentObject(documentValue), processInstanceId, documentName, null, index);
                }
            }

            // when no more elements in documentList remove elements above
            for (; index < currentList.size(); index++) {
                documentService.removeCurrentVersion(currentList.get(index));
            }

            return documentList;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }

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
