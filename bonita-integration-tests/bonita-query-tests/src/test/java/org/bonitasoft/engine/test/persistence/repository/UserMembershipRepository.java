/*
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
 */
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * author Emmanuel Duchastenier
 */
public class UserMembershipRepository extends TestRepository {

    public UserMembershipRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Long getNumberOfUserMembersForUserOrManagerForActorMembers(long userId, final List<Long> actorMemberIds) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getNumberOfUserMembersForUserOrManagerForActorMembers");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameterList("actorMemberIds", actorMemberIds);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public List getUserMembershipsByGroup(SGroup group) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getUserMembershipsByGroup");
        namedQuery.setParameter("groupId", group.getId());
        return namedQuery.list();

    }

    public List getUserMembershipsByRole(SRole role) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getUserMembershipsByRole");
        namedQuery.setParameter("roleId", role.getId());
        return namedQuery.list();
    }

    public List getUserMembershipsOfUser(long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getUserMembershipsOfUser");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public List getUserMembershipWithIds(long roleId, long groupId, long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getUserMembershipWithIds");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameter("roleId", roleId);
        namedQuery.setParameter("groupId", groupId);
        return namedQuery.list();
    }

    public List getUserMemberships() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getUserMemberships");
        return namedQuery.list();
    }

    public List getSUserMembershipById(long id) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getSUserMembershipById");
        namedQuery.setParameter("id", id);
        return namedQuery.list();
    }

    public List searchUserMembership() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("searchUserMembership");
        return namedQuery.list();
    }

}
