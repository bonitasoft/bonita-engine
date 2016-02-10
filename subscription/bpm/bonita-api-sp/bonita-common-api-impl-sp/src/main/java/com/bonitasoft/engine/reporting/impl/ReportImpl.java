/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.reporting.impl;

import java.util.Arrays;
import java.util.Date;

import com.bonitasoft.engine.reporting.Report;

/**
 * @author Matthieu Chaffotte
 */
public class ReportImpl implements Report {

    private static final long serialVersionUID = 5445403438892593799L;

    private final long id;

    private final String name;

    private boolean provided;

    private String description;

    private final Date installationDate;

    private final long installedBy;

    private Date lastModificationDate;

    private byte[] screenshot;

    public ReportImpl(final long id, final String name, final long installationDate, final long installedBy) {
        this.id = id;
        this.name = name;
        this.installationDate = new Date(installationDate);
        this.installedBy = installedBy;
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
    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setProvided(final boolean provided) {
        this.provided = provided;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLastModificationDate(final Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setScreenshot(final byte[] screenshot) {
        this.screenshot = screenshot;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((installationDate == null) ? 0 : installationDate.hashCode());
        result = prime * result + (int) (installedBy ^ (installedBy >>> 32));
        result = prime * result + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (provided ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(screenshot);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportImpl other = (ReportImpl) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id != other.id)
            return false;
        if (installationDate == null) {
            if (other.installationDate != null)
                return false;
        } else if (!installationDate.equals(other.installationDate))
            return false;
        if (installedBy != other.installedBy)
            return false;
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null)
                return false;
        } else if (!lastModificationDate.equals(other.lastModificationDate))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (provided != other.provided)
            return false;
        if (!Arrays.equals(screenshot, other.screenshot))
            return false;
        return true;
    }

}
