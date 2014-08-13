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

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 */
public class SDocumentImpl implements SDocument {

    private String id;

    private long author;

    private long creationDate;

    private String contentMimeType;

    private String contentFileName;

    public SDocumentImpl() {
        super();
    }

    @Override
    public String getStorageId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public long getAuthor() {
        return author;
    }

    public void setAuthor(final long author) {
        this.author = author;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String getContentMimeType() {
        return contentMimeType;
    }

    public void setContentMimeType(final String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    @Override
    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(final String fileName) {
        this.contentFileName = fileName;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SDocumentImpl [id=").append(id);
        builder.append(", author=").append(author);
        builder.append(", creationDate=").append(creationDate);
        builder.append(", contentMimeType=").append(contentMimeType);
        builder.append(", contentFileName=").append(contentFileName).append("]");
        return builder.toString();
    }

}
