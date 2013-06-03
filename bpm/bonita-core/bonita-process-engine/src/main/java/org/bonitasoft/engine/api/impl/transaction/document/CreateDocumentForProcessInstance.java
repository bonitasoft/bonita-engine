/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.document;

import org.bonitasoft.engine.core.process.document.api.SProcessDocumentAlreadyExistsException;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentCreationException;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentException;
import org.bonitasoft.engine.core.process.document.api.SProcessDocumentService;
import org.bonitasoft.engine.core.process.document.model.SProcessDocument;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class CreateDocumentForProcessInstance extends CreateDocumentForProcessDefinition {

    private final long processInstanceId;

    public CreateDocumentForProcessInstance(final SProcessDocumentService processDocumentService, final long processDefinitionId, final long processInstanceId,
            final String author, final String fileName, final String mimeType, final byte[] content) {
        super(processDocumentService, processDefinitionId, author, fileName, mimeType, content);
        this.processInstanceId = processInstanceId;
    }

    @Override
    public SProcessDocument createDocument() throws SProcessDocumentCreationException, SProcessDocumentAlreadyExistsException, SProcessDocumentException {
        return getProcessDocumentService().createDocumentForProcessInstance(getProcessDefinitionId(), processInstanceId, getAuthor(), getFileName(),
                getMimeType(), getContent());
    }

}
