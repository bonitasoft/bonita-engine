/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.document.api.ProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Updates of creates a document of the process.
 * this operation accepts only {@link DocumentValue} {@link DocumentValue} provides filename, mimetype and content
 * The document that will be update/created have the name given to the leftOperand (leftOperand.getName())
 * If the document already exists on the process instance (document with same name), it is update.
 * If there is no document with this name, it is created.
 * 
 * @author Baptiste Mesta
 */
public class DocumentOperationExecutorStrategy implements OperationExecutorStrategy {

    private static final String TYPE_DOCUMENT_CREATE_UPDATE = "DOCUMENT_CREATE_UPDATE";

    ProcessDocumentService processDocumentService;

    private final ActivityInstanceService activityInstanceService;

    private final SProcessDocumentBuilder processDocumentBuilder;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    public DocumentOperationExecutorStrategy(final ProcessDocumentService processDocumentService, final ActivityInstanceService activityInstanceService,
            final SProcessDocumentBuilder processDocumentBuilder, final SessionAccessor sessionAccessor, final SessionService sessionService) {
        super();
        this.processDocumentService = processDocumentService;
        this.activityInstanceService = activityInstanceService;
        this.processDocumentBuilder = processDocumentBuilder;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public void execute(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final boolean isDocumentWithContent = value instanceof DocumentValue;
        if (!isDocumentWithContent && value != null) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a DocumentValue and not "
                    + value.getClass().getName());
        }

        long processInstanceId;
        try {
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstanceId = containerId;
            } else {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
                processInstanceId = flowNodeInstance.getRootContainerId();
            }
            final String documentName = operation.getLeftOperand().getName();
            if (value == null) {
                // we just delete the current version
                try {
                    processDocumentService.removeCurrentVersion(processInstanceId, documentName);
                } catch (final SDocumentNotFoundException e) {
                    // nothing to do
                }
            } else {

                final long sessionId = sessionAccessor.getSessionId();
                final long authorId = sessionService.getSession(sessionId).getUserId();
                final DocumentValue documentValue = (DocumentValue) value;
                if (documentValue.hasContent()) {
                    try {
                        final SProcessDocument currentDocument = processDocumentService.getDocument(processInstanceId, documentName);
                        // a document exist, update it with the new values
                        processDocumentBuilder.createNewInstance();
                        processDocumentBuilder.setName(documentName);
                        processDocumentBuilder.setFileName(documentValue.getFileName());
                        processDocumentBuilder.setContentMimeType(documentValue.getMimeType());
                        processDocumentBuilder.setProcessInstanceId(currentDocument.getProcessInstanceId());
                        processDocumentBuilder.setAuthor(authorId);
                        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
                        processDocumentBuilder.setHasContent(true);
                        final SProcessDocument document = processDocumentBuilder.done();
                        processDocumentService.updateDocumentOfProcessInstance(document, documentValue.getContent());
                    } catch (final SDocumentNotFoundException e) {
                        // not found we create a new one
                        processDocumentBuilder.createNewInstance();
                        processDocumentBuilder.setName(documentName);
                        processDocumentBuilder.setFileName(documentValue.getFileName());
                        processDocumentBuilder.setContentMimeType(documentValue.getMimeType());
                        processDocumentBuilder.setProcessInstanceId(processInstanceId);
                        processDocumentBuilder.setAuthor(authorId);
                        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
                        processDocumentBuilder.setHasContent(true);
                        final SProcessDocument document = processDocumentBuilder.done();
                        processDocumentService.attachDocumentToProcessInstance(document, documentValue.getContent());
                    }
                } else {
                    try {
                        final SProcessDocument currentDocument = processDocumentService.getDocument(processInstanceId, documentName);
                        processDocumentBuilder.createNewInstance();
                        processDocumentBuilder.setName(documentName);
                        processDocumentBuilder.setFileName(currentDocument.getContentFileName());
                        processDocumentBuilder.setContentMimeType(currentDocument.getContentMimeType());
                        processDocumentBuilder.setProcessInstanceId(currentDocument.getProcessInstanceId());
                        processDocumentBuilder.setAuthor(authorId);
                        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
                        processDocumentBuilder.setHasContent(false);
                        processDocumentBuilder.setURL(documentValue.getUrl());
                        final SProcessDocument document = processDocumentBuilder.done();
                        processDocumentService.updateDocumentOfProcessInstance(document);
                    } catch (final SDocumentNotFoundException e) {
                        // not found we create a new one
                        processDocumentBuilder.createNewInstance();
                        processDocumentBuilder.setName(documentName);
                        processDocumentBuilder.setProcessInstanceId(processInstanceId);
                        processDocumentBuilder.setAuthor(authorId);
                        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
                        processDocumentBuilder.setHasContent(false);
                        processDocumentBuilder.setURL(documentValue.getUrl());
                        final SProcessDocument document = processDocumentBuilder.done();
                        processDocumentService.attachDocumentToProcessInstance(document);
                    }
                }
            }
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }

    }

    @Override
    public String getOperationType() {
        return TYPE_DOCUMENT_CREATE_UPDATE;
    }

}
