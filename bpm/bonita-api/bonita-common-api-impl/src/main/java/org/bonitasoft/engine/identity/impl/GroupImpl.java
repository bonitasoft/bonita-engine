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
package org.bonitasoft.engine.identity.impl;

import java.util.Date;

import org.bonitasoft.engine.identity.Group;

/**
 * @author Lu Kai
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class GroupImpl implements Group {

    private static final long serialVersionUID = 3063794706652296174L;

    private final long id;

    private final String name;

    private String displayName;

    private String description;

    private String iconName;

    private String iconPath;

    private long createdBy;

    private String parentPath;

    private Date creationDate;

    private Date lastUpdate;

    public GroupImpl(final long id, final String name) {
        super();
        this.id = id;
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastUpdate(final Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setParentPath(final String parentPath) {
        this.parentPath = parentPath;
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
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String getParentPath() {
        return parentPath;
    }

    @Override
    public String getPath() {
        if (parentPath == null) {
            return "/" + getName();
        }
        return parentPath + "/" + getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createdBy ^ (createdBy >>> 32));
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((iconName == null) ? 0 : iconName.hashCode());
        result = prime * result + ((iconPath == null) ? 0 : iconPath.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parentPath == null) ? 0 : parentPath.hashCode());
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
        final GroupImpl other = (GroupImpl) obj;
        if (id != other.id) {
            return false;
        }
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate == null) {
            if (other.creationDate != null) {
                return false;
            }
        } else if (!creationDate.equals(other.creationDate)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
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
        if (lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!lastUpdate.equals(other.lastUpdate)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parentPath == null) {
            if (other.parentPath != null) {
                return false;
            }
        } else if (!parentPath.equals(other.parentPath)) {
            return false;
        }
        return true;
    }

}
