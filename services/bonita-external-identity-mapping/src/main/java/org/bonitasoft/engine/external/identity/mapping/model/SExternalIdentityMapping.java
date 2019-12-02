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
package org.bonitasoft.engine.external.identity.mapping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SExternalIdentityMapping implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String KIND_KEY = "kind";
    public static final String USER_ID_KEY = "userId";
    public static final String GROUP_ID_KEY = "groupId";
    public static final String ROLE_ID_KEY = "roleId";
    public static final String EXTERNAL_ID_KEY = "externalId";
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

    public SExternalIdentityMapping(final String externalId) {
        this.externalId = externalId;
    }

    public SExternalIdentityMapping(final long id, final long tenantId, final String externalId, final long userId, final long groupId, final long roleId,
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
