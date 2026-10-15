/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import lombok.Getter;
import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * @author Emmanuel Duchastenier
 */
@Getter
public class TenantResource implements BonitaObject {

    public static final TenantResource NONE = new TenantResource(0, "", null, 0, 0, null);

    private final long id;
    private final String name;
    private final TenantResourceType type;
    private final OffsetDateTime lastUpdateDate;
    private final long lastUpdatedBy;
    private final TenantResourceState state;

    public TenantResource(long id, String name, TenantResourceType type, long lastUpdateDate, long lastUpdatedBy,
            TenantResourceState state) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lastUpdateDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastUpdateDate), ZoneOffset.UTC);
        this.lastUpdatedBy = lastUpdatedBy;
        this.state = state;
    }

}
