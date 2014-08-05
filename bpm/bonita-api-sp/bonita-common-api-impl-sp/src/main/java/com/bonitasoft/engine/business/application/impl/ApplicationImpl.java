/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.DescriptionElementImpl;

import com.bonitasoft.engine.business.application.Application;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationImpl extends DescriptionElementImpl implements Application {

    private static final long serialVersionUID = -5393587887795907117L;
    private final String version;
    private final String path;
    private String iconPath;
    private Date creationDate;
    private long createdBy;
    private Date lastUpdateDate;
    private long updatedBy;
    private String state;

    public ApplicationImpl(final String name, final String version, final String path, final String description) {
        super(name, description);
        this.version = version;
        this.path = path;
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (createdBy ^ createdBy >>> 32);
        result = prime * result + (creationDate == null ? 0 : creationDate.hashCode());
        result = prime * result + (iconPath == null ? 0 : iconPath.hashCode());
        result = prime * result + (lastUpdateDate == null ? 0 : lastUpdateDate.hashCode());
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (int) (updatedBy ^ updatedBy >>> 32);
        result = prime * result + (path == null ? 0 : path.hashCode());
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
        final ApplicationImpl other = (ApplicationImpl) obj;
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
        if (iconPath == null) {
            if (other.iconPath != null) {
                return false;
            }
        } else if (!iconPath.equals(other.iconPath)) {
            return false;
        }
        if (lastUpdateDate == null) {
            if (other.lastUpdateDate != null) {
                return false;
            }
        } else if (!lastUpdateDate.equals(other.lastUpdateDate)) {
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
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
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
        return "ApplicationImpl [version=" + version + ", url=" + path + ", iconPath=" + iconPath + ", creationDate=" + creationDate + ", createdBy="
                + createdBy + ", lastUpdateDate=" + lastUpdateDate + ", updatedBy=" + updatedBy + ", status=" + state + ", name=" + getName()
                + ", id=" + getId() + "]";
    }

}
