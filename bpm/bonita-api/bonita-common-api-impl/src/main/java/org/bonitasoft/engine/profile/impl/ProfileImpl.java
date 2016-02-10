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
package org.bonitasoft.engine.profile.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.profile.Profile;

/**
 * @author Celine Souchet
 */
public class ProfileImpl extends NamedElementImpl implements Profile {

    private static final long serialVersionUID = 9223087187374465662L;

    private boolean isDefault;

    private String description;

    private Date creationDate;

    private long createdBy;

    private Date lastUpdateDate;

    private long lastUpdatedBy;

    public ProfileImpl(final String name) {
        super(name);
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Deprecated
    public void setIconPath(final String iconPath) {
        // nothing to do
    }

    @Deprecated
    @Override
    public String getIconPath() {
        return null;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
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
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
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
        int result = super.hashCode();
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (lastUpdateDate == null ? 0 : lastUpdateDate.hashCode());
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
        if (!super.equals(obj)) {
            return false;
        }
        final ProfileImpl other = (ProfileImpl) obj;
        if (isDefault != other.isDefault) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (createdBy != other.createdBy) {
            return false;
        }
        if (lastUpdateDate == null) {
            if (other.lastUpdateDate != null) {
                return false;
            }
        } else if (!lastUpdateDate.equals(other.lastUpdateDate)) {
            return false;
        }
        if (lastUpdatedBy != other.lastUpdatedBy) {
            return false;
        }

        return true;
    }

}
