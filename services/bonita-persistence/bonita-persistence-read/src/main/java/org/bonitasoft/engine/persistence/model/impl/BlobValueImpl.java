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
package org.bonitasoft.engine.persistence.model.impl;

import java.io.Serializable;

import org.bonitasoft.engine.persistence.model.BlobValue;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class BlobValueImpl implements BlobValue {

    private static final long serialVersionUID = -7451235454786974186L;

    private long id;

    private Serializable value;

    private long tenantId;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return BlobValueImpl.class.getName();
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
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
        final BlobValueImpl other = (BlobValueImpl) obj;
        if (id != other.id) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

    public void setValue(final Serializable value) {
        this.value = value;
    }

}
