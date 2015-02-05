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
package org.bonitasoft.engine.archive.model;

import org.bonitasoft.engine.persistence.ArchivedPersistentObject;

public abstract class SPersistentObjectImpl implements ArchivedPersistentObject {

    private static final long serialVersionUID = 1L;

    private long id;

    private long tenantId;

    private long archiveDate;

    public SPersistentObjectImpl() {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
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
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final SPersistentObjectImpl other = (SPersistentObjectImpl) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public long getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final long archiveDate) {
        this.archiveDate = archiveDate;
    }

}
