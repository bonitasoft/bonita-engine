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

import org.bonitasoft.engine.page.SContentType;
import org.bonitasoft.engine.page.SPage;

/**
 * @author Baptiste Mesta
 */
public class SPageImpl implements SPage {

    private static final long serialVersionUID = 6328720053646015171L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private String displayName;

    private long installationDate;

    private long installedBy;

    private boolean provided;

    private long lastModificationDate;

    private long lastUpdatedBy;

    private String contentName;

    private String contentType;

    private long processDefinitionId;

    protected SPageImpl() {

    }

    public SPageImpl(final String name, final String description, final String displayName, final long installationDate, final long installedBy,
            final boolean provided, final long lastModificationDate, final long lastUpdatedBy, final String contentName) {
        this(name, installationDate, installedBy, provided, contentName);
        setDescription(description);
        setDisplayName(displayName);
        setProvided(provided);
        setLastModificationDate(lastModificationDate);
        setLastUpdatedBy(lastUpdatedBy);
    }

    /**
     * @param sPage
     */
    public SPageImpl(final SPage sPage) {
        this(sPage.getName(), sPage.getDescription(), sPage.getDisplayName(), sPage.getInstallationDate(), sPage.getInstalledBy(), sPage.isProvided(), sPage
                .getLastModificationDate(), sPage.getLastUpdatedBy(), sPage.getContentName());
        setContentType(sPage.getContentType());
        setProcessDefinitionId(sPage.getProcessDefinitionId());
    }

    public SPageImpl(final String name, final long installationDate, final long installedBy, final boolean provided, final String contentName) {
        setName(name);
        setInstallationDate(installationDate);
        setInstalledBy(installedBy);
        setProvided(provided);
        setContentName(contentName);
        setContentType(SContentType.PAGE);

    }

    @Override
    public String getDiscriminator() {
        return SPage.class.getName();
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
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public long getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(final long installationDate) {
        this.installationDate = installationDate;
    }

    @Override
    public long getInstalledBy() {
        return installedBy;
    }

    public void setInstalledBy(final long installedBy) {
        this.installedBy = installedBy;
    }

    @Override
    public boolean isProvided() {
        return provided;
    }

    public void setProvided(final boolean provided) {
        this.provided = provided;
    }

    @Override
    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(final long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
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
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setContentName(final String contentName) {
        this.contentName = contentName;
    }

    @Override
    public long getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(final long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("SPageImpl [tenantId=").append(tenantId).append(", id=").append(id).append(", name=").append(name)
                .append(", description=").append(description).append(", displayName=").append(displayName).append(", installationDate=")
                .append(installationDate).append(", installedBy=").append(installedBy).append(", provided=").append(provided).append(", lastModificationDate=")
                .append(lastModificationDate).append(", lastUpdatedBy=").append(lastUpdatedBy).append(", contentName=").append(contentName)
                .append(", contentType=").append(contentType).append(", processDefinitionId=").append(processDefinitionId).append("]")
                .toString();
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }
}
