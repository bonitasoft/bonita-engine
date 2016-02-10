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
package org.bonitasoft.engine.business.application.model.impl;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Elias Ricken de Medeiros
 */
public class SApplicationImpl extends PersistentObjectId implements SApplication {

    private static final long serialVersionUID = 4993767054990446857L;

    private String token;

    private String description;

    private String version;

    private String iconPath;

    private long creationDate;

    private long createdBy;

    private long lastUpdateDate;

    private long updatedBy;

    private String state;

    private Long homePageId;

    private String displayName;

    private Long profileId;

    private Long layoutId;

    private Long themeId;

    public SApplicationImpl() {
        super();
    }

    public SApplicationImpl(final String token, final String displayName, final String version, final long creationDate, final long createdBy,
                            final String state, final Long layoutId, final Long themeId) {
        super();
        this.token = token;
        this.displayName = displayName;
        this.version = version;
        this.creationDate = creationDate;
        lastUpdateDate = creationDate; //at instantiation the creation date is the same as last update date
        this.createdBy = createdBy;
        updatedBy = createdBy;
        this.state = state;
        this.layoutId = layoutId;
        this.themeId = themeId;
    }

    @Override
    public String getDiscriminator() {
        return SApplication.class.getName();
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public long getCreatedBy() {
        return createdBy;
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public long getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public String getState() {
        return state;
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

    @Override
    public Long getLayoutId() {
        return layoutId;
    }

    @Override
    public Long getThemeId() {
        return themeId;
    }

    public void setProfileId(final Long profileId) {
        this.profileId = profileId;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public void setLastUpdateDate(final long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void setUpdatedBy(final long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setLayoutId(final Long layoutId) {
        this.layoutId = layoutId;
    }

    public void setThemeId(final Long themeId) {
        this.themeId = themeId;
    }

    @Override
    public String toString() {
        return "SApplicationImpl [token=" + token + ", description=" + description + ", version=" + version + ", iconPath=" + iconPath
                + ", creationDate=" + creationDate + ", createdBy=" + createdBy + ", lastUpdateDate=" + lastUpdateDate + ", updatedBy=" + updatedBy
                + ", state=" + state + ", homePageId=" + homePageId + ", displayName=" + displayName + ", profileId=" + profileId + ", layoutId=" + layoutId
                + ", getId()=" + getId() + ", getTenantId()=" + getTenantId() + "]";
    }

}
