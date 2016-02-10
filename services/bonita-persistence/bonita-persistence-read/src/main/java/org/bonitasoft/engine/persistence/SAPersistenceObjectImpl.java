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
package org.bonitasoft.engine.persistence;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class SAPersistenceObjectImpl implements ArchivedPersistentObject {

    private static final long serialVersionUID = 1963528483109540503L;

    protected long id;

    protected long tenantId;

    protected long archiveDate;

    protected long sourceObjectId;

    public SAPersistenceObjectImpl(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public SAPersistenceObjectImpl() {
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (archiveDate ^ archiveDate >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
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
        final SAPersistenceObjectImpl other = (SAPersistenceObjectImpl) obj;
        if (archiveDate != other.archiveDate) {
            return false;
        }
        if (id != other.id) {
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
        builder.append(this.getClass().getSimpleName() + "[id=");
        builder.append(id);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", archiveDate=");
        builder.append(archiveDate);
        builder.append(", sourceObjectId=");
        builder.append(sourceObjectId);
        builder.append("]");
        return builder.toString();
    }

}
