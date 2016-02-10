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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SRole;

/**
 * @author Anthony Birembaut
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class SRoleImpl extends SNamedElementImpl implements SRole {

    private static final long serialVersionUID = 1062675620062005462L;

    private String iconName;

    private String iconPath;

    private long createdBy;

    private long creationDate;

    private long lastUpdate;

    public SRoleImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SRole.class.getName();
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return "SRoleImpl [getDescription()=" + getDescription() + ", getLabel()=" + getDisplayName() + ", getName()=" + getName() + ", getId()=" + getId()
                + ", getIconName()=" + getIconName() + ", getIconPath()=" + getIconPath() + ", getCreatedBy()=" + getCreatedBy() + ", getCreationDate()="
                + getCreationDate() + ", getLastUpdate()=" + getLastUpdate() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (int) (creationDate ^ creationDate >>> 32);
        result = prime * result + (iconName == null ? 0 : iconName.hashCode());
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (int) (lastUpdate ^ lastUpdate >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SRoleImpl other = (SRoleImpl) obj;
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate != other.creationDate) {
            return false;
        }
        if (iconName == null) {
            if (other.iconName != null) {
                return false;
            }
        } else if (!iconName.equals(other.iconName)) {
            return false;
        }
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        if (lastUpdate != other.lastUpdate) {
            return false;
        }
        return true;
    }

}
