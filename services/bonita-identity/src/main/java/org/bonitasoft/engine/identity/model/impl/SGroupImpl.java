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

import java.util.Objects;

import org.bonitasoft.engine.identity.model.SGroup;

/**
 * @author Anthony Birembaut
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SGroupImpl extends SNamedElementImpl implements SGroup {

    private static final long serialVersionUID = -3998305885633448998L;

    private String parentPath;

    private long createdBy;

    private long creationDate;

    private long lastUpdate;

    private Long iconId;

    public SGroupImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SGroup.class.getName();
    }

    @Override
    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(final String parentPath) {
        this.parentPath = parentPath;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final long creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(final long lastUpdate) {
        this.lastUpdate = lastUpdate;
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
        return "SGroupImpl{" +
                "parentPath='" + parentPath + '\'' +
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
        if (!super.equals(o))
            return false;
        SGroupImpl sGroup = (SGroupImpl) o;
        return createdBy == sGroup.createdBy &&
                creationDate == sGroup.creationDate &&
                lastUpdate == sGroup.lastUpdate &&
                Objects.equals(parentPath, sGroup.parentPath) &&
                Objects.equals(iconId, sGroup.iconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentPath, createdBy, creationDate, lastUpdate, iconId);
    }
}
