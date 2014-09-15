/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.model.impl;

import org.bonitasoft.engine.persistence.PersistentObjectId;

import com.bonitasoft.engine.business.application.model.SApplication;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationImpl extends PersistentObjectId implements SApplication {

    private static final long serialVersionUID = 4993767054990446857L;

    private String name;

    private String description;

    private String version;

    private String path;

    private String iconPath;

    private long creationDate;

    private long createdBy;

    private long lastUpdateDate;

    private long updatedBy;

    private String state;

    private long homePageId;

    private String displayName;

    private Long profileId;

    public SApplicationImpl() {
        super();
    }

    public SApplicationImpl(final String name, final String displayName, final String version, final String path, final long creationDate, final long createdBy, final String state) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.version = version;
        this.path = path;
        this.creationDate = creationDate;
        lastUpdateDate = creationDate; //at instantiation the creation date is the same as last update date
        this.createdBy = createdBy;
        updatedBy = createdBy;
        this.state = state;
    }

    @Override
    public String getDiscriminator() {
        return SApplication.class.getName();
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
    public String getVersion() {
        return version;
    }

    @Override
    public String getPath() {
        return path;
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
    public long getHomePageId() {
        return homePageId;
    }

    public void setHomePageId(final long homePageId) {
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (int) (creationDate ^ creationDate >>> 32);
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (int) (homePageId ^ homePageId >>> 32);
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (int) (lastUpdateDate ^ lastUpdateDate >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (path == null ? 0 : path.hashCode());
        result = prime * result + (profileId == null ? 0 : profileId.hashCode());
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (int) (updatedBy ^ updatedBy >>> 32);
        result = prime * result + (version == null ? 0 : version.hashCode());
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
        final SApplicationImpl other = (SApplicationImpl) obj;
        if (createdBy != other.createdBy) {
            return false;
        }
        if (creationDate != other.creationDate) {
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
        if (homePageId != other.homePageId) {
            return false;
        }
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (profileId == null) {
            if (other.profileId != null) {
                return false;
            }
        } else if (!profileId.equals(other.profileId)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (updatedBy != other.updatedBy) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SApplicationImpl [name=" + name + ", description=" + description + ", version=" + version + ", path=" + path + ", iconPath=" + iconPath
                + ", creationDate=" + creationDate + ", createdBy=" + createdBy + ", lastUpdateDate=" + lastUpdateDate + ", updatedBy=" + updatedBy
                + ", state=" + state + ", homePageId=" + homePageId + ", displayName=" + displayName + ", profileId=" + profileId + ", getId()=" + getId()
                + ", getTenantId()=" + getTenantId() + "]";
    }

}
