/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.util.Objects;

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
        return Objects.hash(super.hashCode(), isDefault, description, creationDate, createdBy, lastUpdateDate,
                lastUpdatedBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        ProfileImpl profile = (ProfileImpl) o;
        return isDefault == profile.isDefault && createdBy == profile.createdBy
                && lastUpdatedBy == profile.lastUpdatedBy && Objects.equals(description, profile.description)
                && Objects.equals(creationDate, profile.creationDate)
                && Objects.equals(lastUpdateDate, profile.lastUpdateDate);
    }

}
