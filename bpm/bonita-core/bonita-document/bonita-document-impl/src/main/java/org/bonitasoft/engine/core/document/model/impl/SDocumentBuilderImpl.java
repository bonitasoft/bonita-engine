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
package org.bonitasoft.engine.core.document.model.impl;

import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.SDocumentBuilder;

/**
 * @author Zhao Na
 */
public class SDocumentBuilderImpl implements SDocumentBuilder {

    private final SDocumentImpl document;
    
    public SDocumentBuilderImpl(final SDocumentImpl document) {
        super();
        this.document = document;
    }

    @Override
    public SDocumentBuilder setAuthor(final long author) {
        this.document.setAuthor(author);
        return this;
    }

    @Override
    public SDocumentBuilder setCreationDate(final long creationDate) {
        this.document.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SDocumentBuilder setContentMimeType(final String contentMimeType) {
        this.document.setContentMimeType(contentMimeType);
        return this;
    }

    @Override
    public SDocumentBuilder setContentFileName(final String contentFileName) {
        this.document.setContentFileName(contentFileName);
        return this;
    }

    @Override
    public SDocument done() {
        return document;
    }

    @Override
    public SDocumentBuilder setDocumentId(final String documentId) {
        this.document.setId(documentId);
        return this;
    }

}
