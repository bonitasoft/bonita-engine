/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Julien Mege
 * @author Elias Ricken de Medeiros
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

    public static HashMap<String, Serializable> memberAsProfileMembersMap(final SProfileMember profileMember) {
        final HashMap<String, Serializable> profileMemeber = new HashMap<String, Serializable>();
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

    public static List<HashMap<String, Serializable>> membersAsProfileMembersMapList(final List<SProfileMember> serverObjects) {
        final ArrayList<HashMap<String, Serializable>> profileMemberMaps = new ArrayList<HashMap<String, Serializable>>();
        for (final SProfileMember profileMember : serverObjects) {
            profileMemberMaps.add(memberAsProfileMembersMap(profileMember));
        }
        return profileMemberMaps;
    }

}
