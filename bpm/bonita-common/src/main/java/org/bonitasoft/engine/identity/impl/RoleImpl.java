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

import org.bonitasoft.engine.identity.Role;

/**
 * @author Yanyan Liu
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public class RoleImpl implements Role {

    private static final long serialVersionUID = -659540126668387290L;

    private final long id;

    private final String name;

    private String displayName;

    private String description;

    private long createdBy;

    private Date creationDate;

    private Date lastUpdate;

    private Long iconId;

    public RoleImpl(final long id, final String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
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

    @Override
    public String getDescription() {
        return description;
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
    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    @Override
    public String toString() {
        return "RoleImpl{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", createdBy=" + createdBy +
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
        RoleImpl role = (RoleImpl) o;
        return id == role.id &&
                createdBy == role.createdBy &&
                Objects.equals(name, role.name) &&
                Objects.equals(displayName, role.displayName) &&
                Objects.equals(description, role.description) &&
                Objects.equals(creationDate, role.creationDate) &&
                Objects.equals(lastUpdate, role.lastUpdate) &&
                Objects.equals(iconId, role.iconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, description, createdBy, creationDate, lastUpdate, iconId);
    }
}
