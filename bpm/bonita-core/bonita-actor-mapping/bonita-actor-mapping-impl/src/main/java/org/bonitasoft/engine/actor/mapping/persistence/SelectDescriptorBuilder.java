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
package org.bonitasoft.engine.actor.mapping.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SelectDescriptorBuilder {

    public static SelectByIdDescriptor<SActor> getActor(final long actorId) {
        return new SelectByIdDescriptor<SActor>("getActorById", SActor.class, actorId);
    }

    public static SelectOneDescriptor<SActor> getActor(final String actorName, final long scopeId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", actorName);
        parameters.put("scopeId", scopeId);
        return new SelectOneDescriptor<SActor>("getActorFromNameAndScopeId", parameters, SActor.class);
    }

    public static SelectByIdDescriptor<SActorMember> getActorMember(final long actorMemberId) {
        return new SelectByIdDescriptor<SActorMember>("getActorMemberById", SActorMember.class, actorMemberId);
    }

    public static SelectOneDescriptor<SActorMember> getActorMember(final long actorId, final long userId, final long groupId, final long roleId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("actorId", actorId);
        parameters.put("userId", userId);
        parameters.put("groupId", groupId);
        parameters.put("roleId", roleId);
        return new SelectOneDescriptor<SActorMember>("getActorMember", parameters, SActorMember.class);
    }

    public static SelectListDescriptor<SActorMember> getActorMembers(final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        final Map<String, Object> parameters = Collections.emptyMap();
        return new SelectListDescriptor<SActorMember>("getActorMembers", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembers(final long actorId, final int fromIndex, final int numberOfElements) {
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfElements);
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfActor", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfGroup(final long groupId, final int fromIndex, final int numberOfActorMembers) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("groupId", groupId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfActorMembers);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfGroup", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfRole(final long roleId, final int fromIndex, final int numberOfActorMembers) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("roleId", roleId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfActorMembers);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfRole", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<SActorMember> getActorMembersOfUser(final long userId, final int fromIndex, final int numberOfActorMembers) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("userId", userId);
        final QueryOptions queryOptions = new QueryOptions(fromIndex, numberOfActorMembers);
        return new SelectListDescriptor<SActorMember>("getActorMembersOfUser", parameters, SActorMember.class, queryOptions);
    }

    public static SelectListDescriptor<Long> getActorMembersInitiatorForProcess(final long processDefinitionId, final int index,
            final int numberPerPage) {
        final Map<String, Object> parameters = new HashMap<String, Object>(1);
        parameters.put("processDefinitionId", processDefinitionId);
        final QueryOptions queryOptions = new QueryOptions(index, numberPerPage, SActorMember.class, "id", OrderByType.ASC);
        return new SelectListDescriptor<Long>("getActorMembersInitiatorForProcess", parameters, SActorMember.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfUserMembersForUserOrManagerForActorMembers(final long userId, final List<Long> actorMemberIds) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("userId", userId);
        parameters.put("actorMemberIds", actorMemberIds);
        return new SelectOneDescriptor<Long>("getNumberOfUserMembersForUserOrManagerForActorMembers", parameters, SUserMembership.class);
    }

    public static SelectListDescriptor<SActor> getActorsOfScope(final long scopeId, final QueryOptions queryOptions) {
        final Map<String, Object> parameters = Collections.singletonMap("scopeId", (Object) scopeId);
        return new SelectListDescriptor<SActor>("getActorsOfScope", parameters, SActor.class, queryOptions);
    }

    public static <T extends PersistentObject> SelectListDescriptor<T> getElementsByIds(final Class<T> clazz, final String elementName,
            final Collection<Long> ids) {
        final QueryOptions queryOptions = new QueryOptions(0, ids.size(), clazz, "id", OrderByType.ASC);
        final Map<String, Object> parameters = Collections.singletonMap("ids", (Object) ids);
        return new SelectListDescriptor<T>("get" + elementName + "sByIds", parameters, clazz, queryOptions);
    }

    public static SelectListDescriptor<SActor> getFullActorsListOfUser(final Set<Long> scopeIds, final long userId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scopeIds", scopeIds);
        parameters.put("userId", userId);
        final QueryOptions queryOptions = new QueryOptions(SActor.class, "name", OrderByType.ASC);
        return new SelectListDescriptor<SActor>("getActorsOfUser", parameters, SActor.class, queryOptions);
    }

    public static SelectOneDescriptor<Long> getNumberOfActorMembers(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfActorMembersOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfActorMembersOfGroupWithActor(final long groupId, final long actorId) {
        final Map<String, Object> parameters = new HashMap<String, Object>(2);
        parameters.put("groupId", groupId);
        parameters.put("actorId", actorId);
        return new SelectOneDescriptor<Long>("getNumberOfActorMembersOfGroupWithActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfGroupsOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfGroupsOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfMembershipsOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfMembershipsOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfRolesOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfRolesOfActor", parameters, SActorMember.class);
    }

    public static SelectOneDescriptor<Long> getNumberOfUsersOfActor(final long actorId) {
        final Map<String, Object> parameters = Collections.singletonMap("actorId", (Object) actorId);
        return new SelectOneDescriptor<Long>("getNumberOfUsersOfActor", parameters, SActorMember.class);
    }

}
