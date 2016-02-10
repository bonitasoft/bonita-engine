/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

/**
 * @author Matthieu Chaffotte
 */
public class SReportImpl implements SReport {

    private static final long serialVersionUID = 6328720053646015171L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private long installationDate;

    private long installedBy;

    private boolean provided;

    private long lastModificationDate;

    private byte[] screenshot;

    protected SReportImpl() {
        super();
    }

    public SReportImpl(final SReport report) {
        this(report.getName(), report.getInstallationDate(), report.getInstalledBy(), report.isProvided(), report.getDescription(), report
                .getLastModificationDate(), report.getScreenshot());
    }

    public SReportImpl(final String name, final long installationDate, final long installedBy, final boolean provided, final String description,
            final long lastModificationDate, final byte[] screenshot) {
        this(name, installationDate, installedBy, provided);
        setDescription(description);
        setLastModificationDate(lastModificationDate);
        setScreenshot(screenshot);
    }

    public SReportImpl(final String name, final long installationDate, final long installedBy, final boolean provided) {
        super();
        this.name = name;
        this.installationDate = installationDate;
        this.installedBy = installedBy;
        this.provided = provided;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getDiscriminator() {
        return SReport.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getInstallationDate() {
        return installationDate;
    }

    @Override
    public long getInstalledBy() {
        return installedBy;
    }

    @Override
    public long getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public boolean isProvided() {
        return provided;
    }

    @Override
    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLastModificationDate(final long lastModificationDate) {
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
        result = prime * result + (int) (installationDate ^ (installationDate >>> 32));
        result = prime * result + (int) (installedBy ^ (installedBy >>> 32));
        result = prime * result + (int) (lastModificationDate ^ (lastModificationDate >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (provided ? 1231 : 1237);
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
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
        final SReportImpl other = (SReportImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (installationDate != other.installationDate) {
            return false;
        }
        if (installedBy != other.installedBy) {
            return false;
        }
        if (lastModificationDate != other.lastModificationDate) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (provided != other.provided) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
