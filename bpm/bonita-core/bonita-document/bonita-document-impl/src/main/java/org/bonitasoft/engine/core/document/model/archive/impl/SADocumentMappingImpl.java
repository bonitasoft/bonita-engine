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
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.document.model.archive.SADocumentMapping;
import org.bonitasoft.engine.core.document.model.archive.SAMappedDocument;
import org.bonitasoft.engine.core.document.model.impl.SDocumentMappingImpl;
import org.bonitasoft.engine.core.document.model.impl.SMappedDocumentImpl;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Nicolas Chabanoles
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SADocumentMappingImpl extends SDocumentMappingImpl implements SADocumentMapping, ArchivedPersistentObject {

    private long archiveDate;

    private long sourceObjectId;


    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(long archiveDate) {
        this.archiveDate = archiveDate;
    }

    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }


    public SADocumentMappingImpl() {
    }

    public SADocumentMappingImpl(long documentId, long processInstanceId, long archiveDate, long sourceObjectId) {
        super(documentId, processInstanceId);
        this.archiveDate = archiveDate;
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SDocumentMapping.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SADocumentMappingImpl that = (SADocumentMappingImpl) o;

        if (archiveDate != that.archiveDate) return false;
        if (sourceObjectId != that.sourceObjectId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (archiveDate ^ (archiveDate >>> 32));
        result = 31 * result + (int) (sourceObjectId ^ (sourceObjectId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "SADocumentMappingImpl{" +
                "archiveDate=" + archiveDate +
                ", sourceObjectId=" + sourceObjectId +
                '}';
    }
}
