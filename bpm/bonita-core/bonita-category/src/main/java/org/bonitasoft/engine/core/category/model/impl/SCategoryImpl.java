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
package org.bonitasoft.engine.core.category.model.impl;

import org.bonitasoft.engine.core.category.model.SCategory;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SCategoryImpl implements SCategory {

    private static final long serialVersionUID = 1294608200299613682L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private long creator;

    private long creationDate;

    private long lastUpdateDate;

    public SCategoryImpl() {
        super();
    }

    public SCategoryImpl(final String name) {
        this.name = name;
    }

    public SCategoryImpl(final SCategory category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.creator = category.getCreator();
        this.creationDate = category.getCreationDate();
        this.lastUpdateDate = category.getLastUpdateDate();
    }

    public long getTenantId() {
        return this.tenantId;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCreator(final long creator) {
        this.creator = creator;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdateDate(final long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getDiscriminator() {
        return SCategoryImpl.class.getName();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public long getCreator() {
        return this.creator;
    }

    @Override
    public long getCreationDate() {
        return this.creationDate;
    }

    @Override
    public long getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    @Override
    public String toString() {
        return "SCategoryImpl [id=" + id + ", name=" + name + ", description=" + description + ", creator=" + creator + ", creationDate=" + creationDate
                + ", lastUpdateDate=" + lastUpdateDate + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (creationDate ^ (creationDate >>> 32));
        result = prime * result + (int) (creator ^ (creator >>> 32));
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (int) (lastUpdateDate ^ (lastUpdateDate >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
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
        final SCategoryImpl other = (SCategoryImpl) obj;
        if (creationDate != other.creationDate) {
            return false;
        }
        if (creator != other.creator) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
