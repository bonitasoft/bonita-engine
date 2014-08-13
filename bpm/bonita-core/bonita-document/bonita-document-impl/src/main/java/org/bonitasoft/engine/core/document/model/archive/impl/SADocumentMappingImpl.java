/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.document.model.archive.impl;

import org.bonitasoft.engine.core.document.model.SDocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SADocumentMappingImpl implements SADocumentMapping {

    private static final long serialVersionUID = -3715732213157352106L;

    private long id;

    private long tenantId;

    private long processInstanceId;

    private String documentName;

    private long documentAuthor;

    private long documentCreationDate;

    private boolean documentHasContent;

    private String documentContentFileName;

    private String documentContentMimeType;

    private String contentStorageId;

    private String documentURL;

    private long archiveDate;

    private long sourceObjectId;

    public SADocumentMappingImpl() {
        super();
    }

    public SADocumentMappingImpl(final SDocumentMapping documentMapping) {
        tenantId = documentMapping.getTenantId();
        sourceObjectId = documentMapping.getId();
        processInstanceId = documentMapping.getProcessInstanceId();
        documentName = documentMapping.getDocumentName();
        documentAuthor = documentMapping.getDocumentAuthor();
        documentCreationDate = documentMapping.getDocumentCreationDate();
        documentContentFileName = documentMapping.getDocumentContentFileName();
        documentContentMimeType = documentMapping.getDocumentContentMimeType();
        contentStorageId = documentMapping.getContentStorageId();
        documentHasContent = documentMapping.documentHasContent();
        documentURL = documentMapping.getDocumentURL();
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
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

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public String getDiscriminator() {
        return SADocumentMappingImpl.class.getName();
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setDocumentStorageId(final String documentContentStorageId) {
        contentStorageId = documentContentStorageId;
    }

    @Override
    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(final String documentName) {
        this.documentName = documentName;

    }

    @Override
    public String getContentStorageId() {
        return contentStorageId;
    }

    public void setContentStorageId(final String contentStorageId) {
        this.contentStorageId = contentStorageId;

    }

    @Override
    public long getDocumentAuthor() {
        return documentAuthor;
    }

    public void setDocumentAuthor(final long documentAuthor) {
        this.documentAuthor = documentAuthor;
    }

    @Override
    public long getDocumentCreationDate() {
        return documentCreationDate;
    }

    public void setDocumentCreationDate(final long creationDate) {
        documentCreationDate = creationDate;
    }

    @Override
    public String getDocumentContentMimeType() {
        return documentContentMimeType;
    }

    public void setDocumentContentMimeType(final String documentContentMimeType) {
        this.documentContentMimeType = documentContentMimeType;
    }

    @Override
    public String getDocumentContentFileName() {
        return documentContentFileName;
    }

    public void setDocumentContentFileName(final String documentContentFileName) {
        this.documentContentFileName = documentContentFileName;
    }

    public void setDocumentHasContent(final boolean hasContent) {
        documentHasContent = hasContent;
    }

    @Override
    public boolean documentHasContent() {
        return documentHasContent;
    }

    @Override
    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(final String url) {
        documentURL = url;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (archiveDate ^ archiveDate >>> 32);
        result = prime * result + (contentStorageId == null ? 0 : contentStorageId.hashCode());
        result = prime * result + (int) (documentAuthor ^ documentAuthor >>> 32);
        result = prime * result + (documentContentFileName == null ? 0 : documentContentFileName.hashCode());
        result = prime * result + (documentContentMimeType == null ? 0 : documentContentMimeType.hashCode());
        result = prime * result + (int) (documentCreationDate ^ documentCreationDate >>> 32);
        result = prime * result + (documentHasContent ? 1231 : 1237);
        result = prime * result + (documentName == null ? 0 : documentName.hashCode());
        result = prime * result + (documentURL == null ? 0 : documentURL.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
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
        final SADocumentMappingImpl other = (SADocumentMappingImpl) obj;
        if (archiveDate != other.archiveDate) {
            return false;
        }
        if (contentStorageId == null) {
            if (other.contentStorageId != null) {
                return false;
            }
        } else if (!contentStorageId.equals(other.contentStorageId)) {
            return false;
        }
        if (documentAuthor != other.documentAuthor) {
            return false;
        }
        if (documentContentFileName == null) {
            if (other.documentContentFileName != null) {
                return false;
            }
        } else if (!documentContentFileName.equals(other.documentContentFileName)) {
            return false;
        }
        if (documentContentMimeType == null) {
            if (other.documentContentMimeType != null) {
                return false;
            }
        } else if (!documentContentMimeType.equals(other.documentContentMimeType)) {
            return false;
        }
        if (documentCreationDate != other.documentCreationDate) {
            return false;
        }
        if (documentHasContent != other.documentHasContent) {
            return false;
        }
        if (documentName == null) {
            if (other.documentName != null) {
                return false;
            }
        } else if (!documentName.equals(other.documentName)) {
            return false;
        }
        if (documentURL == null) {
            if (other.documentURL != null) {
                return false;
            }
        } else if (!documentURL.equals(other.documentURL)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SADocumentMappingImpl [id=");
        builder.append(id);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", processInstanceId=");
        builder.append(processInstanceId);
        builder.append(", documentName=");
        builder.append(documentName);
        builder.append(", documentAuthor=");
        builder.append(documentAuthor);
        builder.append(", documentCreationDate=");
        builder.append(documentCreationDate);
        builder.append(", documentHasContent=");
        builder.append(documentHasContent);
        builder.append(", documentContentFileName=");
        builder.append(documentContentFileName);
        builder.append(", documentContentMimeType=");
        builder.append(documentContentMimeType);
        builder.append(", contentStorageId=");
        builder.append(contentStorageId);
        builder.append(", documentURL=");
        builder.append(documentURL);
        builder.append(", archiveDate=");
        builder.append(archiveDate);
        builder.append(", sourceObjectId=");
        builder.append(sourceObjectId);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDocumentMapping.class;
    }

}
