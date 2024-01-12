/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.document.api.impl.DocumentHelper;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.springframework.stereotype.Component;

/**
 * Handles document lists
 * this operation accepts only a List of {@link org.bonitasoft.engine.bpm.document.DocumentValue}
 *
 * @author Baptiste Mesta
 */
@Component
public class DocumentListLeftOperandHandler extends AbstractDocumentLeftOperandHandler {

    public final DocumentHelper documentHelper;

    public DocumentListLeftOperandHandler(final ActivityInstanceService activityInstanceService,
            final SessionAccessor sessionAccessor,
            final SessionService sessionService,
            final DocumentHelper documentHelper) {
        super(activityInstanceService, sessionAccessor, sessionService);
        this.documentHelper = documentHelper;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue,
            final long containerId,
            final String containerType)
            throws SOperationExecutionException {
        final List<DocumentValue> documentList = documentHelper.toCheckedList(newValue);
        final String documentName = sLeftOperand.getName();
        try {
            final long processInstanceId = getProcessInstanceId(containerId, containerType);
            documentHelper.setDocumentList(documentList, documentName, processInstanceId,
                    getAuthorId(containerId, containerType));
            return documentList;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e.getMessage(), e);
        }

    }

    @Override
    public String getType() {
        return LeftOperand.TYPE_DOCUMENT_LIST;
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType)
            throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a document is not supported");
    }

}
