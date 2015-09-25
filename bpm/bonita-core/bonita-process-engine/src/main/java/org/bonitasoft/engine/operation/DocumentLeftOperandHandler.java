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
package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import java.util.List;
import java.util.Map;

/**
 * Updates of creates a document of the process.
 * this operation accepts only {@link DocumentValue} {@link DocumentValue} provides filename, mimetype and content
 * The document that will be update/created have the name given to the leftOperand (leftOperand.getName())
 * If the document already exists on the process instance (document with same name), it is update.
 * If there is no document with this name, it is created.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DocumentLeftOperandHandler extends AbstractDocumentLeftOperandHandler {

    private final DocumentHelper documentHelper;
    final DocumentService documentService;

    public DocumentLeftOperandHandler(final DocumentService documentService, final ActivityInstanceService activityInstanceService,
            final SessionAccessor sessionAccessor, final SessionService sessionService) {
        super(activityInstanceService, sessionAccessor, sessionService);
        this.documentService = documentService;
        documentHelper = new DocumentHelper(documentService, null, null);
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        final DocumentValue documentValue = documentHelper.toCheckedDocumentValue(newValue);
        final String documentName = sLeftOperand.getName();
        long processInstanceId;
        try {
            processInstanceId = getProcessInstanceId(containerId, containerType);
            if (newValue == null) {
                // we just delete the current version
                documentHelper.deleteDocument(documentName, processInstanceId);
            } else {
                if (documentValue.getDocumentId() != null && !documentValue.hasChanged()) {
                    //do not update if the document value say it did not changed
                    return newValue;
                }
                documentHelper.createOrUpdateDocument(documentValue, documentName, processInstanceId, getAuthorId(), null);
            }
            return newValue;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }

    }

    @Override
    public String getType() {
        return LeftOperand.TYPE_DOCUMENT;
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
