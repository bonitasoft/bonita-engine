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
package org.bonitasoft.engine.identity.recorder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final int fromIndex, final int numberOfGroups) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfGroups, SGroup.class, "id", OrderByType.ASC);
        return getChildrenOfGroup(group, queryOptions);
    }

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("groupPath", (Object) group.getPath());
        return new SelectListDescriptor<SGroup>("getChildrenOfGroup", parameters, SGroup.class, queryOptions);
    }

    public static SelectListDescriptor<SGroup> getChildrenOfGroup(final SGroup group, final String field, final OrderByType order, final int fromIndex,
            final int numberOfGroups) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfGroups, SGroup.class, field, order);
        return getChildrenOfGroup(group, queryOptions);
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("get" + elementName + "ById", clazz, id);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final int fromIndex,
            final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName,
            final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<T>("get" + elementName + "s", parameters, clazz, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElements(final Class<T> clazz, final String elementName, final String field,
            final OrderByType order, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements, clazz, field, order);
        return getElements(clazz, elementName, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElementsByIds(final Class<T> clazz, final String elementName,
            final Collection<Long> ids) {
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) ids);
        final int maxResults = ids != null ? ids.size() : 0;
        return new SelectListDescriptor<T>("get" + elementName + "sByIds", parameters, clazz, new QueryOptions(0, maxResults));
    }

    public static SelectOneDescriptor<SGroup> getGroupByName(final String groupName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) groupName);
        return new SelectOneDescriptor<SGroup>("getGroupByName", parameters, SGroup.class);
    }

    public static SelectOneDescriptor<SGroup> getGroupByPath(final String parentPath, final String groupName) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", groupName);
        parameters.put("parentPath", parentPath);
        return new SelectOneDescriptor<SGroup>("getGroupByNameAndPath", parameters, SGroup.class);
    }

    public static <T extends PersistentObject> SelectByIdDescriptor<T> getLightElementById(final Class<T> clazz, final String elementName, final long id) {
        return new SelectByIdDescriptor<T>("getLight" + elementName + "ById", clazz, id);
    }

    public static SelectOneDescriptor<SUserMembership> getLightUserMembership(final long userId, final long groupId, final long roleId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<SUserMembership>("getLightUserMembershipWithIds", parameters, SUserMembership.class);
    }

    public static SelectOneDescriptor<SCustomUserInfoDefinition> getCustomUserInfoDefinitionByName(final String name) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) name);
        return new SelectOneDescriptor<SCustomUserInfoDefinition>("getCustomUserInfoDefinitionByName", parameters, SCustomUserInfoDefinition.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfElement(final String elementName, final Class<? extends PersistentObject> clazz) {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOf" + elementName, emptyMap, clazz, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfGroupChildren(final String groupParentPath) {
        final Map<String, Object> parameters = Collections.singletonMap("parentPath", (Object) groupParentPath);
        return new SelectOneDescriptor<Long>("getNumberOfGroupChildren", parameters, SGroup.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUserMembershipsOfUser(final long userId) {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return new SelectOneDescriptor<Long>("getNumberOfUserMembershipsOfUser", parameters, SUserMembership.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByGroup(final long groupId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<Long>("getNumberOfUsersByGroup", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByMembership(final long groupId, final long roleId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<Long>("getNumberOfUsersByMembership", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersByRole(final long roleId) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", (Object) roleId);
        return new SelectOneDescriptor<Long>("getNumberOfUsersByRole", parameters, SUser.class, Long.class);
    }

    public static SelectOneDescriptor<SRole> getRoleByName(final String roleName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) roleName);
        return new SelectOneDescriptor<SRole>("getRoleByName", parameters, SRole.class);
    }

    public static SelectOneDescriptor<SUser> getUserByUserName(final String userName) {
        final Map<String, Object> parameters = Collections.singletonMap("userName", (Object) userName);
        return new SelectOneDescriptor<SUser>("getUserByUserName", parameters, SUser.class);
    }

    public static SelectOneDescriptor<SContactInfo> getUserContactInfo(final long userId, final boolean isPersonal) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("personal", isPersonal);
        return new SelectOneDescriptor<SContactInfo>("getUserContactInfo", parameters, SContactInfo.class);
    }

    public static SelectOneDescriptor<SUserMembership> getUserMembership(final long userId, final long groupId, final long roleId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectOneDescriptor<SUserMembership>("getUserMembershipWithIds", parameters, SUserMembership.class);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsByGroup(final long groupId, final int startIndex, final int maxResults) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<SUserMembership>("getUserMembershipsByGroup", parameters, SUserMembership.class, new QueryOptions(startIndex,
                maxResults));
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsByRole(final long roleId, final int startIndex, final int maxResults) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", (Object) roleId);
        return new SelectListDescriptor<SUserMembership>("getUserMembershipsByRole", parameters, SUserMembership.class,
                new QueryOptions(startIndex, maxResults));
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId) {
        return getUserMembershipsOfUser(userId, new QueryOptions(Arrays.asList(new OrderByOption(SUserMembership.class, "id", OrderByType.ASC))));
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId, final int fromIndex, final int numberOfMemberships) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfMemberships, Arrays.asList(new OrderByOption(SUserMembership.class, "id",
                OrderByType.ASC)));
        return getUserMembershipsOfUser(userId, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return new SelectListDescriptor<SUserMembership>("getUserMembershipsOfUser", parameters, SUserMembership.class, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsOfUser(final long userId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfMemberships) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfMemberships, SUserMembership.class, field, order);
        return getUserMembershipsOfUser(userId, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsWithGroup(final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<SUserMembership>("getUserMembershipsWithGroup", parameters, SUserMembership.class, queryOptions);
    }

    public static SelectListDescriptor<SUserMembership> getUserMembershipsWithRole(final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<SUserMembership>("getUserMembershipsWithRole", parameters, SUserMembership.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getUsersByGroup(groupId, queryOptions);
    }

    private static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<SUser>("getUsersByGroup", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByGroup(final long groupId, final String field, final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersByGroup(groupId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByManager(final long managerUserId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("managerUserId", managerUserId);
        return new SelectListDescriptor<SUser>("getUsersByManager", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId) {
        return getUsersByMembership(groupId, roleId, null);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId, final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, "id", OrderByType.DESC); // FIXME should have "id" here
        return getUsersByMembership(groupId, roleId, queryOptions);
    }

    private static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("roleId", roleId);
        parameters.put("groupId", groupId);
        return new SelectListDescriptor<SUser>("getUsersByMembership", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByMembership(final long groupId, final long roleId, final String field, final OrderByType order,
            final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersByMembership(groupId, roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByRole(final long roleId) {
        return getUsersByRole(roleId, null);
    }

    public static SelectListDescriptor<SUser> getUsersByRole(final long roleId, final int fromIndex, final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers);
        return getUsersByRole(roleId, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByRole(final long roleId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("roleId", (Object) roleId);
        return new SelectListDescriptor<SUser>("getUsersByRole", parameters, SUser.class, queryOptions);
    }

    public static SelectListDescriptor<SUser> getUsersByRole(final long roleId, final String field, final OrderByType order, final int fromIndex,
            final int numberOfUsers) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfUsers, SUser.class, field, order);
        return getUsersByRole(roleId, queryOptions);
    }

}
