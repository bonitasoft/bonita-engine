/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import java.util.Date;

import com.bonitasoft.engine.page.Page;

/**
 * 
 * @author laurent Leseigneur
 * 
 */
public class PageImpl implements Page {

    private static final long serialVersionUID = 5785414687043871169L;

    private final long pageId;

    private final String name;

    private final boolean provided;

    private final String description;

    private final Date installationDate;

    private final long installedBy;

    private final Date lastModificationDate;

    private final String displayName;

    private final long lastUpdatedBy;

    private final String contentName;

    public PageImpl(final long pageId, final String name, final String displayName, final boolean provided, final String description,
            final long installationDate,
            final long installedBy, final long lastModificationDate, final long lastUpdatedBy, final String contentName) {
        this.pageId = pageId;
        this.name = name;
        this.displayName = displayName;
        this.provided = provided;
        this.description = description;
        this.lastUpdatedBy = lastUpdatedBy;
        this.contentName = contentName;
        this.installationDate = new Date(installationDate);
        this.installedBy = installedBy;
        this.lastModificationDate = new Date(lastModificationDate);
    }

    @Override
    public long getId() {
        return pageId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isProvided() {
        return provided;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getInstallationDate() {
        return installationDate;
    }

    @Override
    public long getInstalledBy() {
        return installedBy;
    }

    @Override
    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public long getPageId() {
        return pageId;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public String getContentName() {
        return contentName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (contentName == null ? 0 : contentName.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (installationDate == null ? 0 : installationDate.hashCode());
        result = prime * result + (int) (installedBy ^ installedBy >>> 32);
        result = prime * result + (lastModificationDate == null ? 0 : lastModificationDate.hashCode());
        result = prime * result + (int) (lastUpdatedBy ^ lastUpdatedBy >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (int) (pageId ^ pageId >>> 32);
        result = prime * result + (provided ? 1231 : 1237);
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
        PageImpl other = (PageImpl) obj;
        if (contentName == null) {
            if (other.contentName != null) {
                return false;
            }
        } else if (!contentName.equals(other.contentName)) {
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
        if (installationDate == null) {
            if (other.installationDate != null) {
                return false;
            }
        } else if (!installationDate.equals(other.installationDate)) {
            return false;
        }
        if (installedBy != other.installedBy) {
            return false;
        }
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null) {
                return false;
            }
        } else if (!lastModificationDate.equals(other.lastModificationDate)) {
            return false;
        }
        if (lastUpdatedBy != other.lastUpdatedBy) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (pageId != other.pageId) {
            return false;
        }
        if (provided != other.provided) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PageImpl [pageId=" + pageId + ", name=" + name + ", provided=" + provided + ", description=" + description + ", installationDate="
                + installationDate + ", installedBy=" + installedBy + ", lastModificationDate=" + lastModificationDate + ", displayName=" + displayName
                + ", lastUpdatedBy=" + lastUpdatedBy + ", contentName=" + contentName + "]";
    }

}
