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
package org.bonitasoft.engine.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SProfileMember implements PersistentObject {

    public static final String DISPLAY_NAME_PART3 = "displayNamePart3";
    public static final String DISPLAY_NAME_PART2 = "displayNamePart2";
    public static final String DISPLAY_NAME_PART1 = "displayNamePart1";
    public static final String ROLE_ID = "roleId";
    public static final String GROUP_ID = "groupId";
    public static final String USER_ID = "userId";
    public static final String PROFILE_ID = "profileId";
    public static final String ID = "id";
    private long id;
    private long tenantId;
    private long profileId;
    private long userId = -1;
    private long groupId = -1;
    private long roleId = -1;
    private transient String displayNamePart1;
    private transient String displayNamePart2;
    private transient String displayNamePart3;

    public SProfileMember(final long profileId) {
        super();
        this.profileId = profileId;
    }
}
