/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PlatformPersistentObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tenant")
public class STenant implements PlatformPersistentObject {

    public static final String PAUSED = "PAUSED";
    public static final String DEACTIVATED = "DEACTIVATED";
    public static final String ACTIVATED = "ACTIVATED";
    public static final String CREATED_BY = "createdBy";
    public static final String CREATED = "created";
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String ICON_NAME = "iconName";
    public static final String ICON_PATH = "iconPath";
    public static final String DEFAULT_TENANT = "defaultTenant";
    @Id
    private long id;
    private String name;
    private String description;
    private String iconName;
    private String iconPath;
    private String status;
    private long created;
    private String createdBy;
    private boolean defaultTenant;

    public STenant(final String name, final String createdBy, final long created, final String status,
            final boolean defaultTenant) {
        super();
        this.name = name;
        this.createdBy = createdBy;
        this.created = created;
        this.status = status;
        this.defaultTenant = defaultTenant;
    }

    /**
     * Return true if the tenant is activated else return false.
     *
     * @return true if the tenant is activated
     * @since 6.0
     */
    public boolean isActivated() {
        return ACTIVATED.equals(status);
    }

    public boolean isDeactivated() {
        return DEACTIVATED.equals(status);
    }

    public boolean isPaused() {
        return PAUSED.equals(status);
    }

    @Override
    public void setTenantId(long id) {
        //no tenant id
    }
}
