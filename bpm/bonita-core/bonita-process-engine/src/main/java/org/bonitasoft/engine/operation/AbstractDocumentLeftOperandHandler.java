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
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.exception.SDocumentNotFoundException;
import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilderFactory;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractDocumentLeftOperandHandler implements LeftOperandHandler {

    private ActivityInstanceService activityInstanceService;
    private SessionAccessor sessionAccessor;
    private SessionService sessionService;
    private DocumentService documentService;

    public AbstractDocumentLeftOperandHandler(ActivityInstanceService activityInstanceService, SessionAccessor sessionAccessor, SessionService sessionService,
            DocumentService documentService) {
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.documentService = documentService;
    }

    protected long getProcessInstanceId(long containerId, String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        long processInstanceId;
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            processInstanceId = containerId;
        } else {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
            processInstanceId = flowNodeInstance.getParentProcessInstanceId();
        }
        return processInstanceId;
    }

    @SuppressWarnings("unchecked")
    protected List<DocumentValue> toCheckedList(Object newValue) throws SOperationExecutionException {
        if (!(newValue instanceof List)) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");
        }
        for (Object item : ((List) newValue)) {
            if (!(item instanceof DocumentValue)) {
                throw new SOperationExecutionException("Document operation only accepts an expression returning a list of DocumentValue");
            }
        }
        return (List<DocumentValue>) newValue;
    }

    protected DocumentValue toCheckedDocumentValue(Object newValue) throws SOperationExecutionException {
        final boolean isDocumentWithContent = newValue instanceof DocumentValue;
        if (!isDocumentWithContent && newValue != null) {
            throw new SOperationExecutionException("Document operation only accepts an expression returning a DocumentValue and not "
                    + newValue.getClass().getName());
        }
        return (DocumentValue) newValue;
    }

    protected SDocument createDocumentObject(final DocumentValue documentValue) throws SSessionNotFoundException {
        final SDocumentBuilder processDocumentBuilder = BuilderFactory.get(SDocumentBuilderFactory.class).createNewInstance();
        processDocumentBuilder.setFileName(documentValue.getFileName());
        processDocumentBuilder.setMimeType(documentValue.getMimeType());
        processDocumentBuilder.setAuthor(getAuthorId());
        processDocumentBuilder.setCreationDate(System.currentTimeMillis());
        processDocumentBuilder.setHasContent(documentValue.hasContent());
        processDocumentBuilder.setURL(documentValue.getUrl());
        processDocumentBuilder.setContent(documentValue.getContent());
        return processDocumentBuilder.done();
    }

    protected long getAuthorId() throws SSessionNotFoundException {
        long authorId;
        try {
            final long sessionId = sessionAccessor.getSessionId();
            authorId = sessionService.getSession(sessionId).getUserId();
        } catch (final SessionIdNotSetException e) {
            authorId = -1;
        }
        return authorId;
    }

    protected void deleteDocument(String documentName, long processInstanceId) throws SObjectModificationException {
        try {
            documentService.removeCurrentVersion(processInstanceId, documentName);
        } catch (final SDocumentNotFoundException e) {
            // nothing to do
        }
    }
}
