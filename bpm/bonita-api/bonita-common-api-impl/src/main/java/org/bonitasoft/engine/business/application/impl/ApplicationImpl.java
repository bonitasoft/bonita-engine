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
package org.bonitasoft.engine.business.application.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.BaseElementImpl;
import org.bonitasoft.engine.business.application.Application;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationImpl extends BaseElementImpl implements Application {

    private static final long serialVersionUID = -5393587887795907117L;
    private final String version;
    private String iconPath;
    private Date creationDate;
    private long createdBy;
    private Date lastUpdateDate;
    private long updatedBy;
    private String state;
    private Long homePageId;
    private String displayName;
    private Long profileId;
    private final String description;
    private final String token;

    public ApplicationImpl(final String token, final String version, final String description) {
        this.token = token;
        this.version = version;
        this.description = description;
    }

    @Override
    public String getToken() {
        return token;
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

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
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
    public long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public Long getHomePageId() {
        return homePageId;
    }

    public void setHomePageId(final Long homePageId) {
        this.homePageId = homePageId;
    }

    @Override
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(final Long profileId) {
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationImpl)) return false;
        if (!super.equals(o)) return false;

        ApplicationImpl that = (ApplicationImpl) o;

        if (createdBy != that.createdBy) return false;
        if (updatedBy != that.updatedBy) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (homePageId != null ? !homePageId.equals(that.homePageId) : that.homePageId != null) return false;
        if (iconPath != null ? !iconPath.equals(that.iconPath) : that.iconPath != null) return false;
        if (lastUpdateDate != null ? !lastUpdateDate.equals(that.lastUpdateDate) : that.lastUpdateDate != null)
            return false;
        if (profileId != null ? !profileId.equals(that.profileId) : that.profileId != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (iconPath != null ? iconPath.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (int) (createdBy ^ (createdBy >>> 32));
        result = 31 * result + (lastUpdateDate != null ? lastUpdateDate.hashCode() : 0);
        result = 31 * result + (int) (updatedBy ^ (updatedBy >>> 32));
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (homePageId != null ? homePageId.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (profileId != null ? profileId.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationImpl [version=" + version + ", iconPath=" + iconPath + ", creationDate=" + creationDate + ", createdBy=" + createdBy
                + ", lastUpdateDate=" + lastUpdateDate + ", updatedBy=" + updatedBy + ", state=" + state + ", homePageId=" + homePageId + ", displayName="
                + displayName + ", profileId=" + profileId + ", description=" + description + ", token=" + token + ", getId()=" + getId() + "]";
    }

}
