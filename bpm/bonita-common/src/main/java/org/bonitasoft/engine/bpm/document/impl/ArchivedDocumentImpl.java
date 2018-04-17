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

import org.bonitasoft.engine.bpm.document.ArchivedDocument;

/**
 * @author Zhang Bole
 * @author Baptiste Mesta
 */
public class ArchivedDocumentImpl extends DocumentImpl implements ArchivedDocument {

    private static final long serialVersionUID = -6573747806944970704L;
    private Date archiveDate;
    private long sourceObjectId;

    public ArchivedDocumentImpl() {
        super();
    }

    public ArchivedDocumentImpl(final String name) {
        super();
        setName(name);
    }

    @Override
    public String getDocumentURL() {
        return getUrl();
    }

    @Override
    public boolean getDocumentHasContent() {
        return hasContent();
    }

    @Override
    public long getDocumentAuthor() {
        return getAuthor();
    }

    @Override
    public String getDocumentContentMimeType() {
        return getContentMimeType();
    }

    @Override
    public String getDocumentContentFileName() {
        return getFileName();
    }

    @Override
    public Date getDocumentCreationDate() {
        return getCreationDate();
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final ArchivedDocumentImpl that = (ArchivedDocumentImpl) o;

        if (sourceObjectId != that.sourceObjectId) {
            return false;
        }
        if (archiveDate != null ? !archiveDate.equals(that.archiveDate) : that.archiveDate != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (archiveDate != null ? archiveDate.hashCode() : 0);
        result = 31 * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "ArchivedDocumentImpl{" +
                "archiveDate=" + archiveDate +
                ", sourceObjectId=" + sourceObjectId +
                super.toString() + "} ";
    }
}
