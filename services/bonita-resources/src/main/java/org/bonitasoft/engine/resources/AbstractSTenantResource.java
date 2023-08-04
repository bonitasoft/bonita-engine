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
package org.bonitasoft.engine.resources;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

/**
 * @author Baptiste Mesta
 */
@Data
@NoArgsConstructor
@MappedSuperclass
@IdClass(PersistentObjectId.class)
public class AbstractSTenantResource implements PersistentObject {

    @Id
    private long tenantId;
    @Id
    private long id;

    protected String name;
    protected long lastUpdatedBy;
    protected long lastUpdateDate;

    @Enumerated(EnumType.STRING)
    protected TenantResourceType type;

    @Enumerated(EnumType.STRING)
    protected STenantResourceState state;

    public AbstractSTenantResource(String name, TenantResourceType type, long lastUpdatedBy, long lastUpdateDate,
            STenantResourceState state) {
        this.name = name;
        this.type = type;
        this.lastUpdatedBy = lastUpdatedBy;
        this.lastUpdateDate = lastUpdateDate;
        this.state = state;
    }
}
