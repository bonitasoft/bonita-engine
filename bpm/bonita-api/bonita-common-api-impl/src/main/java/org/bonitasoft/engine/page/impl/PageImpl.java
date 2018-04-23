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
package org.bonitasoft.engine.page.impl;

import java.util.Date;

import org.bonitasoft.engine.page.Page;

/**
 * @author Laurent Leseigneur
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

    private final String contentType;

    private final Long processDefinitionId;

    public PageImpl(final long pageId, final String name, final String displayName, final boolean provided, final String description,
            final long installationDate,
            final long installedBy, final long lastModificationDate, final long lastUpdatedBy, final String zipName, String contentType,
            Long processDefinitionId) {
        this.pageId = pageId;
        this.name = name;
        this.displayName = displayName;
        this.provided = provided;
        this.description = description;
        this.lastUpdatedBy = lastUpdatedBy;
        this.contentName = zipName;
        this.contentType = contentType;
        this.processDefinitionId = processDefinitionId;
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
    public String getContentType() {
        return contentType;
    }

    @Override
    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("PageImpl [pageId=").append(pageId).append(", name=").append(name).append(", provided=").append(provided)
                .append(", description=").append(description).append(", installationDate=").append(installationDate).append(", installedBy=")
                .append(installedBy).append(", lastModificationDate=").append(lastModificationDate).append(", displayName=").append(displayName)
                .append(", lastUpdatedBy=").append(lastUpdatedBy).append(", contentName=").append(contentName).append(", contentType=")
                .append(contentType).append(", processDefinitionId=").append(processDefinitionId).append("]").toString();
    }

}
