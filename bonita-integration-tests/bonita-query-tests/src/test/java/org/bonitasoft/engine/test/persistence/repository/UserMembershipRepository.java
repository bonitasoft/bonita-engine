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
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Emmanuel Duchastenier
 */
@Repository
public class UserMembershipRepository extends TestRepository {

    public UserMembershipRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Long getNumberOfUserMembersForUserOrManagerForActorMembers(long userId, final List<Long> actorMemberIds) {
        Query namedQuery = getNamedQuery("getNumberOfUserMembersForUserOrManagerForActorMembers");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameterList("actorMemberIds", actorMemberIds);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public List getUserMembershipsByGroup(SGroup group) {
        Query namedQuery = getNamedQuery("getUserMembershipsByGroup");
        namedQuery.setParameter("groupId", group.getId());
        return namedQuery.list();

    }

    public List getUserMembershipsByRole(SRole role) {
        Query namedQuery = getNamedQuery("getUserMembershipsByRole");
        namedQuery.setParameter("roleId", role.getId());
        return namedQuery.list();
    }

    public List getUserMembershipsOfUser(long userId) {
        Query namedQuery = getNamedQuery("getUserMembershipsOfUser");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public List getUserMembershipWithIds(long roleId, long groupId, long userId) {
        Query namedQuery = getNamedQuery("getUserMembershipWithIds");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameter("roleId", roleId);
        namedQuery.setParameter("groupId", groupId);
        return namedQuery.list();
    }

    public List getUserMemberships() {
        Query namedQuery = getNamedQuery("getUserMemberships");
        return namedQuery.list();
    }

    public List getSUserMembershipById(long id) {
        Query namedQuery = getNamedQuery("getSUserMembershipById");
        namedQuery.setParameter("id", id);
        return namedQuery.list();
    }

    public List searchUserMembership() {
        Query namedQuery = getNamedQuery("searchUserMembership");
        return namedQuery.list();
    }

}
