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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SPlatform implements PersistentObject {

    public static final String CREATED_BY = "createdBy";
    public static final String CREATED = "created";
    public static final String ID = "id";
    public static final String INITIAL_VERSION = "initialVersion";
    public static final String PREVIOUS_VERSION = "previousVersion";
    public static final String VERSION = "version";
    public static final String INFORMATION = "information";
    private long tenantId;
    private long id;
    private long created;
    private String createdBy;
    private String initialVersion;
    private String previousVersion;
    private String version;
    private String information;

    public SPlatform(final String version, final String previousVersion, final String initialVersion,
            final String createdBy, final long created) {
        this.version = version;
        this.previousVersion = previousVersion;
        this.initialVersion = initialVersion;
        this.createdBy = createdBy;
        this.created = created;
    }

}
