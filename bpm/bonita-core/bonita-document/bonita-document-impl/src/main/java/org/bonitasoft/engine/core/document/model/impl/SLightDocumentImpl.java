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
package org.bonitasoft.engine.core.document.model.impl;

import org.bonitasoft.engine.core.document.model.SLightDocument;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 */
public class SLightDocumentImpl implements SLightDocument {

    private static final long serialVersionUID = 3494829428880067405L;

    private long id;

    private long tenantId;

    private long author;

    private long creationDate;

    private boolean hasContent;

    private String fileName;

    private String mimeType;

    private String url;

    public SLightDocumentImpl() {
    }

    public SLightDocumentImpl(SLightDocument document) {
        this.id = document.getId();
        this.tenantId = document.getId();
        this.author = document.getAuthor();
        this.creationDate = document.getCreationDate();
        this.hasContent = document.hasContent();
        this.fileName = document.getFileName();
        this.mimeType = document.getMimeType();
        this.url = document.getUrl();
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getDiscriminator() {
        return SLightDocumentImpl.class.getName();
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
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setHasContent(final boolean hasContent) {
        this.hasContent = hasContent;
    }

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SLightDocumentImpl sDocument = (SLightDocumentImpl) o;

        if (author != sDocument.author)
            return false;
        if (creationDate != sDocument.creationDate)
            return false;
        if (hasContent != sDocument.hasContent)
            return false;
        if (id != sDocument.id)
            return false;
        if (tenantId != sDocument.tenantId)
            return false;
        if (fileName != null ? !fileName.equals(sDocument.fileName) : sDocument.fileName != null)
            return false;
        if (mimeType != null ? !mimeType.equals(sDocument.mimeType) : sDocument.mimeType != null)
            return false;
        if (url != null ? !url.equals(sDocument.url) : sDocument.url != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (tenantId ^ (tenantId >>> 32));
        result = 31 * result + (int) (author ^ (author >>> 32));
        result = 31 * result + (int) (creationDate ^ (creationDate >>> 32));
        result = 31 * result + (hasContent ? 1 : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SDocumentImpl{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", author=" + author +
                ", creationDate=" + creationDate +
                ", hasContent=" + hasContent +
                ", fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
