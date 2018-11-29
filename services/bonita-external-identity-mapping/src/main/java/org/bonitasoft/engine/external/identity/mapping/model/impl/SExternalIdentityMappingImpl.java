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
package org.bonitasoft.engine.external.identity.mapping.model.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
public class SExternalIdentityMappingImpl implements SExternalIdentityMapping {

    private long id;
    private long tenantId;
    private String kind;
    private String externalId;
    private long userId = -1;
    private long groupId = -1;
    private long roleId = -1;
    private transient String displayNamePart1;
    private transient String displayNamePart2;
    private transient String displayNamePart3;

    public SExternalIdentityMappingImpl(final String externalId) {
        this.externalId = externalId;
    }

    public SExternalIdentityMappingImpl(final long id, final long tenantId, final String externalId, final long userId, final long groupId, final long roleId,
            final String displayNamePart1, final String displayNamePart2, final String displayNamePart3) {
        this.id = id;
        this.tenantId = tenantId;
        this.externalId = externalId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.displayNamePart1 = displayNamePart1;
        this.displayNamePart2 = displayNamePart2;
        this.displayNamePart3 = displayNamePart3;
    }

}
