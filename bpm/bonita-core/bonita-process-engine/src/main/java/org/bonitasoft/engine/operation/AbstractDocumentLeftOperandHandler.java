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
package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.bpm.contract.FileInputValue;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractDocumentLeftOperandHandler implements LeftOperandHandler {

    private final ActivityInstanceService activityInstanceService;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;

    public AbstractDocumentLeftOperandHandler(final ActivityInstanceService activityInstanceService, final SessionAccessor sessionAccessor,
                                              final SessionService sessionService) {
        this.activityInstanceService = activityInstanceService;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
    }

    protected long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        long processInstanceId;
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            processInstanceId = containerId;
        } else {
            final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
            processInstanceId = flowNodeInstance.getParentProcessInstanceId();
        }
        return processInstanceId;
    }

    protected DocumentValue toCheckedDocumentValue(final Object newValue) throws SOperationExecutionException {
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

    protected DocumentValue toDocumentValue(FileInputValue fileInput) {
        return new DocumentValue(fileInput.getContent(), null, fileInput.getFileName());
    }

    protected long getAuthorId() {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

}
