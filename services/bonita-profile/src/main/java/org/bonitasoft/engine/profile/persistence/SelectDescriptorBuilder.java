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

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final long id) {
        return new SelectByIdDescriptor<>(clazz, id);
    }

    public static <T extends PersistentObject> SelectOneDescriptor<T> getElementByNameDescriptor(final Class<T> clazz, final String elementName,
            final String name) {
        final Map<String, Object> parameters = Collections.singletonMap("name", name);
        return new SelectOneDescriptor<>("get" + elementName + "ByName", parameters, clazz);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfUser(final long userId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(USER_ID, userId);
        return new SelectListDescriptor<>("getDirectProfileMembersOfUser", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfGroup(final long groupId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(GROUP_ID, groupId);
        return new SelectListDescriptor<>("getDirectProfileMembersOfGroup", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfileMember> getDirectProfileMembersOfRole(final long roleId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfileMember.class, field, order);
        final Map<String, Object> parameters = new HashMap<>(1);
        parameters.put(ROLE_ID, roleId);
        return new SelectListDescriptor<>("getDirectProfileMembersOfRole", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectListDescriptor<SProfile> getProfilesOfUser(final long userId, final int fromIndex, final int numberOfElements, final String field,
            final OrderByType order) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfile.class, field, order);
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, userId);
        return new SelectListDescriptor<>("getProfilesOfUser", parameters, SProfile.class, queryOptions);
    }
    
    public static SelectListDescriptor<SProfile> getProfilesWithNavigationOfUser(long userId, int fromIndex,
            int numberOfElements, String field, OrderByType order) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, SProfile.class, field, order);
        final Map<String, Object> parameters = Collections.singletonMap(USER_ID, userId);
        return new SelectListDescriptor<>("getProfilesWithNavigationOfUser", parameters, SProfile.class, queryOptions);
    }


    public static SelectListDescriptor<SProfileMember> getSProfileMembersWithoutDisplayName(final long profileId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap(PROFILE_ID, profileId);
        return new SelectListDescriptor<>("getSProfileMembersWithoutDisplayName", parameters, SProfileMember.class, queryOptions);
    }

    public static SelectByIdDescriptor<SProfileMember> getProfileMemberWithoutDisplayName(final long profileMemberId) {
        return new SelectByIdDescriptor<>(SProfileMember.class, profileMemberId);
    }

   
}
