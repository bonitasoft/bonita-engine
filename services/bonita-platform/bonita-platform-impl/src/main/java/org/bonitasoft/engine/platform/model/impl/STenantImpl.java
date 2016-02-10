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
package org.bonitasoft.engine.platform.model.impl;

import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Charles Souillard
 */
public class STenantImpl implements STenant {

    private static final long serialVersionUID = 1L;

    private long tenantId;

    private long id;

    private String name;

    private String description;

    private String iconName;

    private String iconPath;

    private String status;

    private long created;

    private String createdBy;

    private boolean defaultTenant;

    private STenantImpl() {
    }

    public STenantImpl(final String name, final String createdBy, final long created, final String status, final boolean defaultTenant) {
        super();
        this.name = name;
        this.createdBy = createdBy;
        this.created = created;
        this.status = status;
        this.defaultTenant = defaultTenant;
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean isActivated() {
        // the tenant is activated as soon as it is not deactivated (but it can be paused)
        return !STenant.DEACTIVATED.equals(getStatus());
    }

    @Override
    public long getCreated() {
        return created;
    }

    public void setCreated(final long created) {
        this.created = created;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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
        return STenant.class.getName();
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public String getIconName() {
        return iconName;
    }

    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(final String iconPath) {
        this.iconPath = iconPath;
    }

    @Override
    public boolean isDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(final boolean defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    @Override
    public boolean isPaused() {
        return PAUSED.equals(status);
    }

    @Override
    public String toString() {
        return "STenantImpl [tenantId=" + tenantId + ", id=" + id + ", name=" + name + ", description=" + description + ", iconName=" + iconName
                + ", iconPath=" + iconPath + ", status=" + status + ", created=" + created + ", createdBy=" + createdBy + ", defaultTenant=" + defaultTenant
                + "]";
    }
}
