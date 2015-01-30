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
package org.bonitasoft.engine.profile.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.profile.model.SProfile;
import org.bonitasoft.engine.profile.model.SProfileEntry;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class SelectDescriptorBuilder {

    private static final String ROLE_ID = "roleId";

    private static final String GROUP_ID = "groupId";

    private static final String USER_ID = "userId";

    private static final String PROFILE_ID = "profileId";

    private static final String PARENT_ID = "parentId";

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName, final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOf" + elementName, parameters, clazz, Long.class);
    }

    public static <T extends PersistentObject> SelectOneDescriptor<T> getElementByNameDescriptor(final Class<T> clazz, final String elementName,
            final String name) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) name);
        return new SelectOneDescriptor<T>("get" + elementName + "ByName", parameters, clazz);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final int fromIndex,
            final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final String field,
            final OrderByType order, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, clazz, field, order);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<T>("get" + elementName + "s", parameters, clazz, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfEntriesOfProfile(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("getNumberOfEntriesOfProfile", parameters, SProfileEntry.class);
    }

    public static SelectListDescriptor<SProfileEntry> getEntriesOfProfile(final long profileId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfProfileEntries) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfProfileEntries, SProfileEntry.class, field, order);
        return getEntriesOfProfile(profileId, queryOptions);
    }

    public static SelectListDescriptor<SProfileEntry> getEntriesOfProfile(final long profileId, final int fromIndex, final int numberOfProfileEntries) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfProfileEntries);
        return getEntriesOfProfile(profileId, queryOptions);
    }

    public static SelectListDescriptor<SProfileEntry> getEntriesOfProfile(final long profileId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectListDescriptor<SProfileEntry>("getEntriesOfProfile", parameters, SProfileEntry.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileEntry> getEntriesOfProfile(final long profileId, final long parentId, final String field,
            final OrderByType order, final int fromIndex, final int numberOfProfileEntries) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfProfileEntries, SProfileEntry.class, field, order);
        return getEntriesOfProfile(profileId, parentId, queryOptions);
    }

    public static SelectListDescriptor<SProfileEntry> getEntriesOfProfile(final long profileId, final long parentId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put(PROFILE_ID, profileId);
        parameters.put(PARENT_ID, parentId);
        return new SelectListDescriptor<SProfileEntry>("getEntriesOfProfileByParentId", parameters, SProfileEntry.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> serarchSProfileMembersForUser(final long profileId, final int fromIndex, final int numberOfUserProfiles,
            final List<OrderByOption> orderByOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);

        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUserProfiles, orderByOptions);
        return new SelectListDescriptor<SProfileMember>("searchSProfileMembersForUser", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfSProfileMembersForUser(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("countSProfileMembersForUser", parameters, SProfileMember.class);
    }

    public static SelectListDescriptor<SProfileMember> getSProfileMembersForGroup(final long profileId, final int fromIndex, final int numberOfUserProfiles,
            final List<OrderByOption> orderByOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);

        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUserProfiles, orderByOptions);
        return new SelectListDescriptor<SProfileMember>("searchSProfileMembersForGroup", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfSProfileMembersForGroup(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("countSProfileMembersForGroup", parameters, SProfileMember.class);
    }

    public static SelectListDescriptor<SProfileMember> getSProfileMembersForRole(final long profileId, final int fromIndex, final int numberOfUserProfiles,
            final List<OrderByOption> orderByOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);

        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUserProfiles, orderByOptions);
        return new SelectListDescriptor<SProfileMember>("searchSProfileMembersForRole", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfSProfileMembersForRole(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("countSProfileMembersForRole", parameters, SProfileMember.class);
    }

    public static SelectListDescriptor<SProfileMember> getSProfileMembersForRoleAndGroup(final long profileId, final int fromIndex,
            final int numberOfUserProfiles, final List<OrderByOption> orderByOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);

        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUserProfiles, orderByOptions);
        return new SelectListDescriptor<SProfileMember>("searchSProfileMembersForRoleAndGroup", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfSProfileMembersForRoleAndGroup(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("countSProfileMembersForRoleAndGroup", parameters, SProfileMember.class);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfUser(final long userId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put(USER_ID, userId);
        return new SelectListDescriptor<SProfileMember>("getDirectProfileMembersOfUser", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfGroup(final long groupId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put(GROUP_ID, groupId);
        return new SelectListDescriptor<SProfileMember>("getDirectProfileMembersOfGroup", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfRole(final long roleId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put(ROLE_ID, roleId);
        return new SelectListDescriptor<SProfileMember>("getDirectProfileMembersOfRole", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getProfileMembers(final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        return new SelectListDescriptor<SProfileMember>("getProfileMembers", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfile> getProfilesOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfile.class, field, order);
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, (Object) userId);
        return new SelectListDescriptor<SProfile>("getProfilesOfUser", parameters, SProfile.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getSProfileMembersWithoutDisplayName(final long profileId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectListDescriptor<SProfileMember>("getSProfileMembersWithoutDisplayName", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersOfProfile(final long profileId) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, (Object) profileId);
        return new SelectOneDescriptor<Long>("getNumberOfProfileMembersOfProfile", parameters, SProfileMember.class);
    }

    public static SelectByIdDescriptor<SProfileMember> getProfileMemberWithoutDisplayName(final long profileMemberId) {
        return new SelectByIdDescriptor<SProfileMember>("getProfileMemberWithoutDisplayNameById", SProfileMember.class, profileMemberId);
    }

}
