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
package org.bonitasoft.engine.bpm.document.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.document.Document;

/**
 * @author Nicolas Chabanoles
 */
public class DocumentImpl implements Document {

    private static final long serialVersionUID = 1956686423434166830L;

    private long id;

    private long processInstanceId;

    private String name;

    private long author;

    private Date creationDate;

    private boolean hasContent;

    private String fileName;

    private String contentMimeType;

    private String contentStorageId;

    private String url;

    private String description;

    private String version;
    private int index;

    public DocumentImpl() {
        super();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public boolean hasContent() {
        return hasContent;
    }

    public void setHasContent(final boolean hasContent) {
        this.hasContent = hasContent;
    }

    public void setContentMimeType(final String contentMimeType) {
        this.contentMimeType = contentMimeType;
    }

    @Override
    public String getContentMimeType() {
        return contentMimeType;
    }

    @Override
    public long getAuthor() {
        return author;
    }

    @Override
    public String getContentFileName() {
        return fileName;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setAuthor(final long author) {
        this.author = author;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String getContentStorageId() {
        return contentStorageId;
    }

    public void setContentStorageId(final String contentStorageId) {
        this.contentStorageId = contentStorageId;

    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentImpl document = (DocumentImpl) o;

        if (author != document.author) return false;
        if (hasContent != document.hasContent) return false;
        if (id != document.id) return false;
        if (index != document.index) return false;
        if (processInstanceId != document.processInstanceId) return false;
        if (contentMimeType != null ? !contentMimeType.equals(document.contentMimeType) : document.contentMimeType != null)
            return false;
        if (contentStorageId != null ? !contentStorageId.equals(document.contentStorageId) : document.contentStorageId != null)
            return false;
        if (creationDate != null ? !creationDate.equals(document.creationDate) : document.creationDate != null)
            return false;
        if (description != null ? !description.equals(document.description) : document.description != null)
            return false;
        if (fileName != null ? !fileName.equals(document.fileName) : document.fileName != null) return false;
        if (name != null ? !name.equals(document.name) : document.name != null) return false;
        if (url != null ? !url.equals(document.url) : document.url != null) return false;
        if (version != null ? !version.equals(document.version) : document.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (author ^ (author >>> 32));
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (hasContent ? 1 : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (contentMimeType != null ? contentMimeType.hashCode() : 0);
        result = 31 * result + (contentStorageId != null ? contentStorageId.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }

    @Override
    public String toString() {
        return "DocumentImpl{" +
                "id=" + id +
                ", processInstanceId=" + processInstanceId +
                ", name='" + name + '\'' +
                ", author=" + author +
                ", creationDate=" + creationDate +
                ", hasContent=" + hasContent +
                ", fileName='" + fileName + '\'' +
                ", contentMimeType='" + contentMimeType + '\'' +
                ", contentStorageId='" + contentStorageId + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", index=" + index +
                '}';
    }
}
