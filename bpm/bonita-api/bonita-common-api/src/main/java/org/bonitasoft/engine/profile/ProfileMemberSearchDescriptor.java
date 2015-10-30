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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;

/**
 * Defines the fields that can be used in the {@link org.bonitasoft.engine.search.SearchOptions} when searching for {@link ProfileMember}s
 * 
 * @author Julien Mege
 * @author Celine Souchet
 * @see SearchOptions
 * @see org.bonitasoft.engine.api.ProfileAPI#searchProfileMembers(String, SearchOptions)
 */
public final class ProfileMemberSearchDescriptor {

    /**
     * Used to filter or order by the {@link ProfileMember} identifier
     *
     * @see ProfileMember
     */
    public static final String ID = "id";

    /**
     * Used to filter or order by the identifier of the related {@link Profile}
     *
     * @see Profile
     * @see ProfileMember
     */
    public static final String PROFILE_ID = "profileId";

    /**
     * Used to filter or order by the first part of the display name of {@link ProfileMember}
     *
     * @see ProfileMember#getDisplayNamePart1()
     */
    public static final String DISPLAY_NAME_PART1 = "displayNamePart1";

    /**
     * Used to filter or order by the second part of the display name of {@link ProfileMember}
     *
     * @see ProfileMember#getDisplayNamePart2()
     */
    public static final String DISPLAY_NAME_PART2 = "displayNamePart2";

    /**
     * Used to filter or order by the third part of the display name of {@link ProfileMember}
     *
     * @see ProfileMember#getDisplayNamePart3()
     */
    public static final String DISPLAY_NAME_PART3 = "displayNamePart3";

    /**
     * Used to filter or order by the identifier of the {@link User} related to the {@link ProfileMember}
     *
     * @see User
     * @see ProfileMember
     */
    public static final String USER_ID = "userId";

    /**
     * Used to filter or order by the identifier of the {@link org.bonitasoft.engine.identity.Group} related to the {@link ProfileMember}
     *
     * @see org.bonitasoft.engine.identity.Group
     * @see ProfileMember
     */
    public static final String GROUP_ID = "groupId";

    /**
     * Used to filter or order by the identifier of the {@link org.bonitasoft.engine.identity.Role} related to the {@link ProfileMember}
     *
     * @see org.bonitasoft.engine.identity.Role
     * @see ProfileMember
     */
    public static final String ROLE_ID = "roleId";

}
