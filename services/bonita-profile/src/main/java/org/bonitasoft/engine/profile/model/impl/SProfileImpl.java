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
package org.bonitasoft.engine.profile.model.impl;

import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SProfileImpl implements SProfile {

    private static final long serialVersionUID = 9223087187374465662L;

    private long id;

    private long tenantId;

    private boolean isDefault;

    private String name;

    private String description;

    private long creationDate;

    private long createdBy;

    private long lastUpdateDate;

    private long lastUpdatedBy;

    public SProfileImpl() {
        super();
    }

    public SProfileImpl(final SProfile profile) {
        super();
        id = profile.getId();
        isDefault = profile.isDefault();
        name = profile.getName();
        description = profile.getDescription();
        creationDate = profile.getCreationDate();
        createdBy = profile.getCreatedBy();
        lastUpdateDate = profile.getLastUpdateDate();
        lastUpdatedBy = profile.getLastUpdatedBy();
    }

    @Override
    public String getDiscriminator() {
        return SProfileImpl.class.getName();
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
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(final long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (creationDate ^ creationDate >>> 32);
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (int) (lastUpdateDate ^ lastUpdateDate >>> 32);
        result = prime * result + (int) (lastUpdatedBy ^ lastUpdatedBy >>> 32);
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
        final SProfileImpl other = (SProfileImpl) obj;
        if (tenantId != other.tenantId) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (isDefault != other.isDefault) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (creationDate != other.creationDate) {
            return false;
        }
        if (createdBy != other.createdBy) {
            return false;
        }
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (lastUpdatedBy != other.lastUpdatedBy) {
            return false;
        }

        return true;
    }

}
