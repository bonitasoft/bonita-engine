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
package org.bonitasoft.engine.profile.model.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
public class SProfileMemberImpl implements SProfileMember {

    private long id;
    private long tenantId;
    private long profileId;
    private long userId = -1;
    private long groupId = -1;
    private long roleId = -1;
    private transient String displayNamePart1;
    private transient String displayNamePart2;
    private transient String displayNamePart3;

    public SProfileMemberImpl(final long profileId) {
        super();
        this.profileId = profileId;
    }

    // used by Hibernate
    public SProfileMemberImpl(final long id, final long tenantId, final long profileId, final long userId, final long groupId, final long roleId,
            final String displayNamePart1, final String displayNamePart2, final String displayNamePart3) {
        super();
        this.id = id;
        this.tenantId = tenantId;
        this.profileId = profileId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.displayNamePart1 = displayNamePart1;
        this.displayNamePart2 = displayNamePart2;
        this.displayNamePart3 = displayNamePart3;
    }

}
