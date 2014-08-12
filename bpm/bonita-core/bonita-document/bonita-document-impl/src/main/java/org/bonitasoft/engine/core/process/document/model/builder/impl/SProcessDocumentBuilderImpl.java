/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.document.model.builder.impl;

import org.bonitasoft.engine.core.process.document.model.SProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.impl.SProcessDocumentImpl;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SProcessDocumentBuilderImpl implements SProcessDocumentBuilder {

    private final SProcessDocumentImpl processDocument;

    public SProcessDocumentBuilderImpl(final SProcessDocumentImpl processDocument) {
        super();
        this.processDocument = processDocument;
    }

    @Override
    public SProcessDocumentBuilder setProcessInstanceId(final long processInstanceId) {
        this.processDocument.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setAuthor(final long authorId) {
        this.processDocument.setAuthor(authorId);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setCreationDate(final long creationDate) {
        this.processDocument.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setContentMimeType(final String contentMimeType) {
        this.processDocument.setContentMimeType(contentMimeType);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setFileName(final String fileName) {
        this.processDocument.setFileName(fileName);
        return this;
    }

    @Override
    public SProcessDocument done() {
        return processDocument;
    }

    @Override
    public SProcessDocumentBuilder setId(final long id) {
        processDocument.setId(id);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setName(final String name) {
        processDocument.setName(name);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setHasContent(final boolean hasContent) {
        processDocument.setHasContent(hasContent);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setContentStorageId(final String contentStorageId) {
        processDocument.setContentStorageId(contentStorageId);
        return this;
    }

    @Override
    public SProcessDocumentBuilder setURL(final String url) {
        processDocument.setURL(url);
        return this;
    }

}
