/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (author ^ author >>> 32);
        result = prime * result + (contentMimeType == null ? 0 : contentMimeType.hashCode());
        result = prime * result + (contentStorageId == null ? 0 : contentStorageId.hashCode());
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (fileName == null ? 0 : fileName.hashCode());
        result = prime * result + (hasContent ? 1231 : 1237);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
        result = prime * result + (url == null ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentImpl other = (DocumentImpl) obj;
        if (author != other.author) {
                return false;
        }
        if (contentMimeType == null) {
            if (other.contentMimeType != null) {
                return false;
            }
        } else if (!contentMimeType.equals(other.contentMimeType)) {
            return false;
        }
        if (contentStorageId == null) {
            if (other.contentStorageId != null) {
                return false;
            }
        } else if (!contentStorageId.equals(other.contentStorageId)) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        } else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (hasContent != other.hasContent) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DocumentImpl [id=");
        builder.append(id);
        builder.append(", processInstanceId=");
        builder.append(processInstanceId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", author=");
        builder.append(author);
        builder.append(", creationDate=");
        builder.append(creationDate);
        builder.append(", hasContent=");
        builder.append(hasContent);
        builder.append(", fileName=");
        builder.append(fileName);
        builder.append(", contentMimeType=");
        builder.append(contentMimeType);
        builder.append(", contentStorageId=");
        builder.append(contentStorageId);
        builder.append(", url=");
        builder.append(url);
        builder.append("]");
        return builder.toString();
    }

}
