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
package org.bonitasoft.engine.api.impl.transaction.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class ProfileMemberUtils {

    public static final String PROFILE_ID = "profileId";

    public static final String USER_ID = "userId";

    public static final String GROUP_ID = "groupId";

    public static final String ROLE_ID = "roleId";

    public static final String PROFILE_MEMBER_ID = "profileMemberId";

    public static final String DISPLAY_NAME_PART1 = "displayNamePart1";

    public static final String DISPLAY_NAME_PART2 = "displayNamePart2";

    public static final String DISPLAY_NAME_PART3 = "displayNamePart3";

    public static final String ROLE_AND_GROUP_TYPE = "roleAndGroup";

    public static final String ROLE_TYPE = "role";

    public static final String GROUP_TYPE = "group";

    public static final String USER_TYPE = "user";

    /**
     * Search command parameters
     */
    public static final String PROFILE_MEMBER_SEARCH_INDEX = "fromIndex";

    public static final String PROFILE_MEMBER_SEARCH_NUMBER = "numberOfProfiles";

    public static final String PROFILE_MEMBER_SEARCH_FIELD = "field";

    public static final String PROFILE_MEMBER_SEARCH_ORDER = "order";

    public static final String PROFILE_MEMBER_SEARCH_OPTIONS_KEY = "searchOptions";

    public static final String PROFILE_MEMBER_TYPE = "memberType";

    /**
     * Query Suffix
     */
    public static final String USER_SUFFIX = "ForUser";

    public static final String GROUP_SUFFIX = "ForGroup";

    public static final String ROLE_SUFFIX = "ForRole";

    public static final String ROLE_AND_GROUP_SUFFIX = "ForRoleAndGroup";

    private ProfileMemberUtils() {
        // For Sonar
    }

    public static Map<String, Serializable> memberAsProfileMembersMap(final SProfileMember profileMember) {
        final Map<String, Serializable> profileMemeber = new HashMap<String, Serializable>();
        profileMemeber.put(PROFILE_ID, profileMember.getProfileId());
        profileMemeber.put(USER_ID, profileMember.getUserId());
        profileMemeber.put(GROUP_ID, profileMember.getGroupId());
        profileMemeber.put(ROLE_ID, profileMember.getRoleId());
        profileMemeber.put(PROFILE_MEMBER_ID, profileMember.getId());
        profileMemeber.put(DISPLAY_NAME_PART1, profileMember.getDisplayNamePart1());
        profileMemeber.put(DISPLAY_NAME_PART2, profileMember.getDisplayNamePart2());
        profileMemeber.put(DISPLAY_NAME_PART3, profileMember.getDisplayNamePart3());
        return profileMemeber;
    }

    public static List<Map<String, Serializable>> membersAsProfileMembersMapList(final List<SProfileMember> serverObjects) {
        final List<Map<String, Serializable>> profileMemberMaps = new ArrayList<Map<String, Serializable>>();
        for (final SProfileMember profileMember : serverObjects) {
            profileMemberMaps.add(memberAsProfileMembersMap(profileMember));
        }
        return profileMemberMaps;
    }

}
