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

import org.bonitasoft.engine.core.process.document.model.SAProcessDocument;
import org.bonitasoft.engine.core.process.document.model.builder.SAProcessDocumentBuilder;
import org.bonitasoft.engine.core.process.document.model.impl.SAProcessDocumentImpl;

/**
 * @author Zhang Bole
 */
public class SAProcessDocumentBuilderImpl implements SAProcessDocumentBuilder {

    private final SAProcessDocumentImpl processDocument;

    public SAProcessDocumentBuilderImpl(final SAProcessDocumentImpl processDocument) {
        super();
        this.processDocument = processDocument;
    }

    @Override
    public SAProcessDocumentBuilder setProcessInstanceId(final long processInstanceId) {
        this.processDocument.setProcessInstanceId(processInstanceId);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setAuthor(final long author) {
        this.processDocument.setAuthor(author);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setCreationDate(final long creationDate) {
        this.processDocument.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setContentMimeType(final String contentMimeType) {
        this.processDocument.setContentMimeType(contentMimeType);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setFileName(final String fileName) {
        this.processDocument.setFileName(fileName);
        return this;
    }

    @Override
    public SAProcessDocument done() {
        return processDocument;
    }

    @Override
    public SAProcessDocumentBuilder setId(final long id) {
        processDocument.setId(id);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setName(final String name) {
        processDocument.setName(name);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setHasContent(final boolean hasContent) {
        processDocument.setHasContent(hasContent);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setContentStorageId(final String contentStorageId) {
        processDocument.setContentStorageId(contentStorageId);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setURL(final String url) {
        processDocument.setURL(url);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setArchiveDate(final long archiveDate) {
        processDocument.setArchiveDate(archiveDate);
        return this;
    }

    @Override
    public SAProcessDocumentBuilder setSourceObjectId(final long sourceObjectId) {
        processDocument.setSourceObjectId(sourceObjectId);
        return this;
    }

}
