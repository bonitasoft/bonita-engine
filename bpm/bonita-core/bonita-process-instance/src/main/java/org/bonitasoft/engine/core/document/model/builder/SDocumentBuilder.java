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
package org.bonitasoft.engine.core.document.model.builder;

import org.bonitasoft.engine.core.document.model.SDocument;

/**
 * @author Baptiste Mesta
 */
public class SDocumentBuilder {

    private final SDocument entity;

    public SDocumentBuilder() {
        entity = new SDocument();
    }

    public SDocumentBuilder setAuthor(final long author) {
        entity.setAuthor(author);
        return this;
    }

    public SDocumentBuilder setCreationDate(final long creationDate) {
        entity.setCreationDate(creationDate);
        return this;
    }

    public SDocumentBuilder setHasContent(final boolean hasContent) {
        entity.setHasContent(hasContent);
        return this;
    }

    public SDocumentBuilder setFileName(final String fileName) {
        entity.setFileName(fileName);
        return this;
    }

    public SDocumentBuilder setMimeType(final String mimeType) {
        entity.setMimeType(mimeType);
        return this;
    }

    public SDocumentBuilder setContent(final byte[] content) {
        entity.setContent(content);
        return this;
    }

    public SDocumentBuilder setURL(final String url) {
        entity.setUrl(url);
        return this;
    }

    public SDocument done() {
        return entity;
    }

}
