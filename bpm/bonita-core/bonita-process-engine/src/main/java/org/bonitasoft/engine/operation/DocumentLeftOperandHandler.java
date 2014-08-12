/**
 * Copyright (C) 2013, 2014 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.document.api.DocumentService;
import org.bonitasoft.engine.core.process.document.mapping.model.SDocumentMapping;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilder;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderFactory;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.document.SDocumentNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

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
public class DocumentLeftOperandHandler implements LeftOperandHandler {

    DocumentService documentService;

    private final ActivityInstanceService activityInstanceService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    public DocumentLeftOperandHandler(final DocumentService documentService, final ActivityInstanceService activityInstanceService,
            final SessionAccessor sessionAccessor, final SessionService sessionService) {
        this.documentService = documentService;
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        final boolean isDocumentWithContent = newValue instanceof DocumentValue;
        if (!isDocumentWithContent && newValue != null) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a DocumentValue and not "
                    + newValue.getClass().getName());
        }

        final String documentName = sLeftOperand.getName();
        long processInstanceId;
        try {
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstanceId = containerId;
            } else {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
                processInstanceId = flowNodeInstance.getParentProcessInstanceId();
            }
            if (newValue == null) {
                // we just delete the current version
                try {
                    documentService.removeCurrentVersion(processInstanceId, documentName);
                } catch (final SDocumentNotFoundException e) {
                    // nothing to do
                }
            } else {
                long authorId;
                try {
                    final long sessionId = sessionAccessor.getSessionId();
                    authorId = sessionService.getSession(sessionId).getUserId();
                } catch (final SessionIdNotSetException e) {
                    authorId = -1;
                }

                final DocumentValue documentValue = (DocumentValue) newValue;
                final boolean hasContent = documentValue.hasContent();
                try {
                    // Let's check if the document already exists:
                    documentService.getDocument(processInstanceId, documentName);

                    // a document exist, update it with the new values
                    final SDocumentMapping document = createDocument(documentName, processInstanceId, authorId, documentValue, hasContent,
                            documentValue.getUrl());
                    if (hasContent) {
                        documentService.updateDocumentOfProcessInstance(document, documentValue.getContent());
                    } else {
                        documentService.updateDocumentOfProcessInstance(document);
                    }
                } catch (final SDocumentNotFoundException e) {
                    final SDocumentMapping document = createDocument(documentName, processInstanceId, authorId, documentValue, hasContent,
                            documentValue.getUrl());
                    if (hasContent) {
                        documentService.attachDocumentToProcessInstance(document, documentValue.getContent());
                    } else {
                        documentService.attachDocumentToProcessInstance(document);
                    }
                }
            }
            return newValue;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }

    }

    private SDocumentMapping createDocument(final String documentName, final long processInstanceId, final long authorId, final DocumentValue documentValue,
            final boolean hasContent, final String documentUrl) {
        final SDocumentMappingBuilder processDocumentBuilder = BuilderFactory.get(SDocumentMappingBuilderFactory.class).createNewInstance();
        processDocumentBuilder.setDocumentName(documentName);
        processDocumentBuilder.setDocumentContentFileName(documentValue.getFileName());
        processDocumentBuilder.setDocumentContentMimeType(documentValue.getMimeType());
        processDocumentBuilder.setProcessInstanceId(processInstanceId);
        processDocumentBuilder.setDocumentAuthor(authorId);
        processDocumentBuilder.setDocumentCreationDate(System.currentTimeMillis());
        processDocumentBuilder.setHasContent(hasContent);
        processDocumentBuilder.setDocumentURL(documentUrl);
        return processDocumentBuilder.done();
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
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) {
        return null;
    }

    @Override
    public boolean supportBatchUpdate() {
        return true;
    }

}
