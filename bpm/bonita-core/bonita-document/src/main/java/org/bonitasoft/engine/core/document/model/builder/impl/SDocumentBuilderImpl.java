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
 **/
package org.bonitasoft.engine.core.document.model.builder.impl;

import org.bonitasoft.engine.core.document.model.SDocument;
import org.bonitasoft.engine.core.document.model.builder.SDocumentBuilder;
import org.bonitasoft.engine.core.document.model.impl.SDocumentImpl;

/**
 * @author Baptiste Mesta
 */
public class SDocumentBuilderImpl implements SDocumentBuilder {

    private final SDocumentImpl entity;

    public SDocumentBuilderImpl() {
        entity = new SDocumentImpl();
    }

    @Override
    public SDocumentBuilder setAuthor(final long author) {
        entity.setAuthor(author);
        return this;
    }

    @Override
    public SDocumentBuilder setCreationDate(final long creationDate) {
        entity.setCreationDate(creationDate);
        return this;
    }

    @Override
    public SDocumentBuilder setHasContent(final boolean hasContent) {
        entity.setHasContent(hasContent);
        return this;
    }

    @Override
    public SDocumentBuilder setFileName(final String fileName) {
        entity.setFileName(fileName);
        return this;
    }

    @Override
    public SDocumentBuilder setMimeType(final String mimeType) {
        entity.setMimeType(mimeType);
        return this;
    }

    @Override
    public SDocumentBuilder setContent(final byte[] content) {
        entity.setContent(content);
        return this;
    }

    @Override
    public SDocumentBuilder setURL(final String url) {
        entity.setUrl(url);
        return this;
    }

    @Override
    public SDocument done() {
        return entity;
    }
}
