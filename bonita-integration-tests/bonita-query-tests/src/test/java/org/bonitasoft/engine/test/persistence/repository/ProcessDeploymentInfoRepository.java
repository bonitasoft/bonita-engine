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
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ProcessDeploymentInfoRepository extends TestRepository {

    public ProcessDeploymentInfoRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor");
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessDefinitionDeployInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("searchSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasks");
        namedQuery = getSession().createQuery(namedQuery.getQueryString() + " ORDER BY process_definition.id");
        return namedQuery.list();
    }

    public long getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasks");
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

}
