/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Zhang Bole
 */
public class ArchivedDocumentImpl extends NamedElementImpl implements ArchivedDocument {

    private static final long serialVersionUID = -6573747806944970703L;

    private Date archiveDate;

    private long processInstanceId;

    private long sourceObjectId;

    private String contentStorageId;

    private String documentURL;

    private boolean hasContent;

    private long documentAuthor;

    private String documentContentFileName;

    private String documentContentMimeType;

    private Date documentCreationDate;

    public ArchivedDocumentImpl(final String name) {
        super(name);
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public long getDocumentAuthor() {
        return documentAuthor;
    }

    public void setDocumentAuthor(final long documentAuthor) {
        this.documentAuthor = documentAuthor;
    }

    @Override
    public String getContentStorageId() {
        return contentStorageId;
    }

    public void setContentStorageId(final String contentStorageId) {
        this.contentStorageId = contentStorageId;
    }

    @Override
    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(final String documentURL) {
        this.documentURL = documentURL;
    }

    @Override
    public boolean getDocumentHasContent() {
        return hasContent;
    }

    public void setDocumentHasContent(final boolean hasContent) {
        this.hasContent = hasContent;
    }

    @Override
    public String getDocumentContentFileName() {
        return documentContentFileName;
    }

    public void setDocumentContentFileName(final String documentContentFileName) {
        this.documentContentFileName = documentContentFileName;
    }

    @Override
    public String getDocumentContentMimeType() {
        return documentContentMimeType;
    }

    public void setDocumentContentMimeType(final String documentContentMimeType) {
        this.documentContentMimeType = documentContentMimeType;
    }

    @Override
    public Date getDocumentCreationDate() {
        return documentCreationDate;
    }

    public void setDocumentCreationDate(final Date documentCreationDate) {
        this.documentCreationDate = documentCreationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ArchivedDocumentImpl that = (ArchivedDocumentImpl) o;

        if (documentAuthor != that.documentAuthor) return false;
        if (hasContent != that.hasContent) return false;
        if (processInstanceId != that.processInstanceId) return false;
        if (sourceObjectId != that.sourceObjectId) return false;
        if (archiveDate != null ? !archiveDate.equals(that.archiveDate) : that.archiveDate != null) return false;
        if (contentStorageId != null ? !contentStorageId.equals(that.contentStorageId) : that.contentStorageId != null)
            return false;
        if (documentContentFileName != null ? !documentContentFileName.equals(that.documentContentFileName) : that.documentContentFileName != null)
            return false;
        if (documentContentMimeType != null ? !documentContentMimeType.equals(that.documentContentMimeType) : that.documentContentMimeType != null)
            return false;
        if (documentCreationDate != null ? !documentCreationDate.equals(that.documentCreationDate) : that.documentCreationDate != null)
            return false;
        if (documentURL != null ? !documentURL.equals(that.documentURL) : that.documentURL != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (archiveDate != null ? archiveDate.hashCode() : 0);
        result = 31 * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = 31 * result + (int) (sourceObjectId ^ (sourceObjectId >>> 32));
        result = 31 * result + (contentStorageId != null ? contentStorageId.hashCode() : 0);
        result = 31 * result + (documentURL != null ? documentURL.hashCode() : 0);
        result = 31 * result + (hasContent ? 1 : 0);
        result = 31 * result + (int) (documentAuthor ^ (documentAuthor >>> 32));
        result = 31 * result + (documentContentFileName != null ? documentContentFileName.hashCode() : 0);
        result = 31 * result + (documentContentMimeType != null ? documentContentMimeType.hashCode() : 0);
        result = 31 * result + (documentCreationDate != null ? documentCreationDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArchivedDocumentImpl{" +
                "archiveDate=" + archiveDate +
                ", processInstanceId=" + processInstanceId +
                ", sourceObjectId=" + sourceObjectId +
                ", contentStorageId='" + contentStorageId + '\'' +
                ", documentURL='" + documentURL + '\'' +
                ", hasContent=" + hasContent +
                ", documentAuthor=" + documentAuthor +
                ", documentContentFileName='" + documentContentFileName + '\'' +
                ", documentContentMimeType='" + documentContentMimeType + '\'' +
                ", documentCreationDate=" + documentCreationDate +
                "} " + super.toString();
    }
}
