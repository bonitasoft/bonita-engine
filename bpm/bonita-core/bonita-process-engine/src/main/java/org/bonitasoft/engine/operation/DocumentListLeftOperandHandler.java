/**
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
import java.util.Map;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
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

    public final DocumentHelper documentHelper;
    final DocumentService documentService;

    public DocumentListLeftOperandHandler(final DocumentService documentService, final ActivityInstanceService activityInstanceService,
            final SessionAccessor sessionAccessor, final SessionService sessionService, final ProcessDefinitionService processDefinitionService,
            final ProcessInstanceService processInstanceService) {
        super(activityInstanceService, sessionAccessor, sessionService);
        this.documentService = documentService;
        documentHelper = new DocumentHelper(documentService, processDefinitionService, processInstanceService);
    }

    public DocumentListLeftOperandHandler(final ActivityInstanceService activityInstanceService, final SessionAccessor sessionAccessor,
            final SessionService sessionService, final DocumentService documentService, final DocumentHelper documentHelper) {
        super(activityInstanceService, sessionAccessor, sessionService);
        this.documentHelper = documentHelper;
        this.documentService = documentService;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        final List<DocumentValue> documentList = toCheckedList(newValue);
        final String documentName = sLeftOperand.getName();
        try {
            final long processInstanceId = getProcessInstanceId(containerId, containerType);
            documentHelper.setDocumentList(documentList, documentName, processInstanceId, getAuthorId());
            return documentList;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e.getMessage(), e);
        }

    }

    @SuppressWarnings("unchecked")
    public List<DocumentValue> toCheckedList(final Object newValue) throws SOperationExecutionException {
        if (!(newValue instanceof List)) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");
        }
        for (final Object item : (List<?>) newValue) {
            if (!(item instanceof DocumentValue)) {
                throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");
            }
        }
        return (List<DocumentValue>) newValue;
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
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) {
        //do nothing
    }

    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException {
        for (SLeftOperand leftOperand : sLeftOperand) {
            loadLeftOperandInContext(leftOperand, expressionContext, contextToSet);
        }
    }

}
