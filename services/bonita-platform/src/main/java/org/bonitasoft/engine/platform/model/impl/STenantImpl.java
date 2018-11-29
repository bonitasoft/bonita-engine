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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.platform.model.STenant;

/**
 * @author Charles Souillard
 */
@Data
@NoArgsConstructor
public class STenantImpl implements STenant {

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

    public STenantImpl(final String name, final String createdBy, final long created, final String status, final boolean defaultTenant) {
        super();
        this.name = name;
        this.createdBy = createdBy;
        this.created = created;
        this.status = status;
        this.defaultTenant = defaultTenant;
    }

    @Override
    public boolean isActivated() {
        // the tenant is activated as soon as it is not deactivated (but it can be paused)
        return !STenant.DEACTIVATED.equals(getStatus());
    }

    @Override
    public boolean isPaused() {
        return PAUSED.equals(status);
    }

}
