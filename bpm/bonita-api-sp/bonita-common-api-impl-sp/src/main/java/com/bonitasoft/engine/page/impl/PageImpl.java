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

    public PageImpl(final long pageId, final String name, final boolean provided, final String description, final long installationDate,
            final long installedBy, final long lastModificationDate) {
        this.pageId = pageId;
        this.name = name;
        this.provided = provided;
        this.description = description;
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

}
