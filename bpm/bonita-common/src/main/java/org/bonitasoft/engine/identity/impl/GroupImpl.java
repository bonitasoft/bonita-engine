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
import java.util.Objects;

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

    private long createdBy;

    private String parentPath;

    private Date creationDate;

    private Date lastUpdate;

    private Long iconId;

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
        return iconId != null ? iconId.toString() : "";
    }

    @Override
    public String getIconPath() {
        return iconId != null ? iconId.toString() : "";
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
    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    @Override
    public String toString() {
        return "GroupImpl{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy=" + createdBy +
                ", parentPath='" + parentPath + '\'' +
                ", creationDate=" + creationDate +
                ", lastUpdate=" + lastUpdate +
                ", iconId=" + iconId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GroupImpl group = (GroupImpl) o;
        return id == group.id &&
                createdBy == group.createdBy &&
                Objects.equals(name, group.name) &&
                Objects.equals(displayName, group.displayName) &&
                Objects.equals(description, group.description) &&
                Objects.equals(parentPath, group.parentPath) &&
                Objects.equals(creationDate, group.creationDate) &&
                Objects.equals(lastUpdate, group.lastUpdate) &&
                Objects.equals(iconId, group.iconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, description, createdBy, parentPath, creationDate, lastUpdate, iconId);
    }
}
