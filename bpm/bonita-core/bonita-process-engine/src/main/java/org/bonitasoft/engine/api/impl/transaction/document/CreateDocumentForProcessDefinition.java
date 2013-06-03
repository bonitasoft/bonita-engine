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
public class CreateDocumentForProcessDefinition extends CreateDocument {

    private final SProcessDocumentService processDocumentService;

    private final long processDefinitionId;

    private final String author;

    private final String fileName;

    private final String mimeType;

    private final byte[] content;

    public CreateDocumentForProcessDefinition(final SProcessDocumentService processDocumentService, final long processDefinitionId, final String author,
            final String fileName, final String mimeType, final byte[] content) {
        this.processDocumentService = processDocumentService;
        this.processDefinitionId = processDefinitionId;
        this.author = author;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.content = content;
    }

    @Override
    public SProcessDocument createDocument() throws SProcessDocumentCreationException, SProcessDocumentAlreadyExistsException, SProcessDocumentException {
        return getProcessDocumentService()
                .createDocumentForProcessDefinition(getProcessDefinitionId(), getAuthor(), getFileName(), getMimeType(), getContent());
    }

    public String getFileName() {
        return fileName;
    }

    public SProcessDocumentService getProcessDocumentService() {
        return processDocumentService;
    }

    public byte[] getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

}
