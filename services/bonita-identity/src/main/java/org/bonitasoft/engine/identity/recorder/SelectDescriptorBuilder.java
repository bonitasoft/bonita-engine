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
package org.bonitasoft.engine.identity.recorder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Bole Zhang
 */
public class SelectDescriptorBuilder {

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final int fromIndex,
            final int numberOfGroups) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfGroups, SGroup.class, "id",
                OrderByType.ASC);
        return getChildrenOfGroup(group, queryOptions);
    }

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("groupPath", group.getPath());
        return new SelectListDescriptor<>("getChildrenOfGroup", parameters, SGroup.class, queryOptions);
    }

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfGroups) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfGroups, SGroup.class, field, order);
        return getChildrenOfGroup(group, queryOptions);
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz,
            final String elementName, final long id) {
        return new SelectByIdDescriptor<>(clazz, id);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz,
            final String elementName, final int fromIndex,
            final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz,
            final String elementName,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<>("get" + elementName + "s", parameters, clazz, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz,
            final String elementName, final String field,
            final OrderByType order, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, clazz, field, order);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElementsByIds(final Class<T> clazz,
            final String elementName,
            final Collection<Long> ids) {
        final Map<String, Object> parameters = Collections.singletonMap("ids", ids);
        final int maxResults = ids != null ? ids.size() : 0;
        return new SelectListDescriptor<>("get" + elementName + "sByIds", parameters, clazz,
                new QueryOptions(0, maxResults));
    }

    public static SelectOneDescriptor<SGroup> getGroupByName(final String groupName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", groupName);
        return new SelectOneDescriptor<>("getGroupByName", parameters, SGroup.class);
    }

    public static SelectOneDescriptor<SGroup> getGroupByPath(final String parentPath, final String groupName) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", groupName);
        parameters.put("parentPath", parentPath);
        return new SelectOneDescriptor<>("getGroupByNameAndPath", parameters, SGroup.class);
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getLightElementById(final Class<T> clazz,
            final String elementName, final long id) {
        return new SelectByIdDescriptor<>(clazz, id);
    }

    public static SelectOneDescriptor<SUserMembership> getLightUserMembership(final long userId, final long groupId,
            final long roleId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", userId);
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<>("getLightUserMembershipWithIds", parameters,
                SUserMembership.class);
    }

    public static SelectOneDescriptor<SCustomUserInfoDefinition> getCustomUserInfoDefinitionByName(final String name) {
        final Map<String, Object> parameters = Collections.singletonMap("name", name);
        return new SelectOneDescriptor<>("getCustomUserInfoDefinitionByName", parameters,
                SCustomUserInfoDefinition.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName,
            final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<>("getNumberOf" + elementName, emptyMap, clazz, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfGroupChildren(final String groupParentPath) {
        final Map<String, Object> parameters = Collections.singletonMap("parentPath", groupParentPath);
        return new SelectOneDescriptor<>("getNumberOfGroupChildren", parameters, SGroup.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUserMembershipsOfUser(final long userId) {
        final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
        return new SelectOneDescriptor<>("getNumberOfUserMembershipsOfUser", parameters, SUserMembership.class,
                Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByGroup(final long groupId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<>("getNumberOfUsersByGroup", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByMembership(final long groupId, final long roleId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<>("getNumberOfUsersByMembership", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByRole(final long roleId) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", roleId);
        return new SelectOneDescriptor<>("getNumberOfUsersByRole", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<SRole> getRoleByName(final String roleName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", roleName);
        return new SelectOneDescriptor<>("getRoleByName", parameters, SRole.class);
    }

    public static SelectOneDescriptor<SUser> getUserByUserName(final String userName) {
        final Map<String, Object> parameters = Collections.singletonMap("userName", userName);
        return new SelectOneDescriptor<>("getUserByUserName", parameters, SUser.class);
    }

    public static SelectOneDescriptor<SContactInfo> getUserContactInfo(final long userId, final boolean isPersonal) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", userId);
        parameters.put("personal", isPersonal);
        return new SelectOneDescriptor<>("getUserContactInfo", parameters, SContactInfo.class);
    }

    public static SelectOneDescriptor<SUserMembership> getUserMembership(final long userId, final long groupId,
            final long roleId) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", userId);
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<>("getUserMembershipWithIds", parameters, SUserMembership.class);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsByGroup(final long groupId,
            final int startIndex, final int maxResults) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<>("getUserMembershipsByGroup", parameters, SUserMembership.class,
                new QueryOptions(startIndex,
                        maxResults));
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsByRole(final long roleId,
            final int startIndex, final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", roleId);
        return new SelectListDescriptor<>("getUserMembershipsByRole", parameters, SUserMembership.class,
                new QueryOptions(startIndex, maxResults));
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex,
            final int numberOfMemberships) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfMemberships,
                List.of(new OrderByOption(SUserMembership.class, "id",
                        OrderByType.ASC)));
        return getUserMembershipsOfUser(userId, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("userId", userId);
        return new SelectListDescriptor<>("getUserMembershipsOfUser", parameters, SUserMembership.class,
                queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId, final String field,
            final OrderByType order,
            final int fromIndex, final int numberOfMemberships) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfMemberships, SUserMembership.class, field,
                order);
        return getUserMembershipsOfUser(userId, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsWithGroup(final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<>("getUserMembershipsWithGroup", parameters,
                SUserMembership.class, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsWithRole(final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<>("getUserMembershipsWithRole", parameters,
                SUserMembership.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getUsersByGroup(groupId, queryOptions);
    }

    private static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<>("getUsersInGroup", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersByGroup(final long groupId, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getActiveUsersByGroup(groupId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersByGroup(final long groupId, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getInactiveUsersByGroup(groupId, queryOptions);
    }

    private static SelectListDescriptor<SUser> getInactiveUsersByGroup(final long groupId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", groupId);
        parameters.put("enabled", false);
        return new SelectListDescriptor<>("getUsersInGroupWithEnabledParameter", parameters, SUser.class, queryOptions);
    }

    private static SelectListDescriptor<SUser> getActiveUsersByGroup(final long groupId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", groupId);
        parameters.put("enabled", true);
        return new SelectListDescriptor<>("getUsersInGroupWithEnabledParameter", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersByGroup(final long groupId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getActiveUsersByGroup(groupId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersByGroup(final long groupId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getInactiveUsersByGroup(groupId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersByGroup(groupId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersWithManager(final long managerUserId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("managerUserId", managerUserId);
        return new SelectListDescriptor<>("getUsersWithManager", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersWithManager(long managerUserId, QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("managerUserId", managerUserId);
        parameters.put("enabled", true);
        return new SelectListDescriptor<>("getUsersWithManagerWithEnabledParameter", parameters, SUser.class,
                queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersWithManager(long managerUserId,
            QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("managerUserId", managerUserId);
        parameters.put("enabled", false);
        return new SelectListDescriptor<>("getUsersWithManagerWithEnabledParameter", parameters, SUser.class,
                queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId) {
        return getUsersByMembership(groupId, roleId, null);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId,
            final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, "id",
                OrderByType.DESC); // FIXME should have "id" here
        return getUsersByMembership(groupId, roleId, queryOptions);
    }

    private static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<>("getUsersByMembership", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId,
            final String field, final OrderByType order,
            final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersByMembership(groupId, roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersWithRole(final long roleId) {
        return getUsersWithRole(roleId, null);
    }

    public static SelectListDescriptor<SUser> getUsersWithRole(final long roleId, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getUsersWithRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersWithRole(long roleId, int fromIndex, int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getActiveUsersWithRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersWithRole(long roleId, int fromIndex, int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getInactiveUsersWithRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersWithRole(final long roleId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", roleId);
        return new SelectListDescriptor<>("getUsersWithRole", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersWithRole(long roleId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("roleId", roleId);
        parameters.put("enabled", true);
        return new SelectListDescriptor<>("getUsersWithRoleWithEnabledParameter", parameters, SUser.class,
                queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersWithRole(long roleId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("roleId", roleId);
        parameters.put("enabled", false);
        return new SelectListDescriptor<>("getUsersWithRoleWithEnabledParameter", parameters, SUser.class,
                queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersWithRole(final long roleId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersWithRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getActiveUsersWithRole(final long roleId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getActiveUsersWithRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getInactiveUsersWithRole(final long roleId, final String field,
            final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getInactiveUsersWithRole(roleId, queryOptions);
    }
}
