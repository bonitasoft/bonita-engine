/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.rest.server.api.tenant;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

import org.bonitasoft.engine.tenant.TenantResource;
import org.bonitasoft.engine.tenant.TenantResourceState;
import org.bonitasoft.engine.tenant.TenantResourceType;

/**
 * Created by Anthony Birembaut
 */
public class TenantResourceItem implements Serializable {

    /**
     * UID
     */
    private static final long serialVersionUID = -2269943868521108237L;

    private String id;
    private String name;
    private TenantResourceType type;
    private TenantResourceState state;
    private String lastUpdatedBy;
    private final String lastUpdateDate;
    private String fileUpload;

    public TenantResourceItem(final TenantResource tenantResource) {
        this(tenantResource, "");
    }

    public TenantResourceItem(final TenantResource tenantResource, String fileUpload) {
        id = String.valueOf(tenantResource.getId());
        name = tenantResource.getName();
        type = tenantResource.getType();
        lastUpdatedBy = String.valueOf(tenantResource.getLastUpdatedBy());
        lastUpdateDate = tenantResource.getLastUpdateDate().format(DateTimeFormatter.ISO_DATE_TIME);
        state = tenantResource.getState();
        this.fileUpload = fileUpload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenantResourceType getType() {
        return type;
    }

    public void setType(TenantResourceType type) {
        this.type = type;
    }

    public TenantResourceState getState() {
        return state;
    }

    public void setState(TenantResourceState state) {
        this.state = state;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(final String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setFileUpload(String fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getFileUpload() {
        return fileUpload;
    }
}
