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
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeInstanceRepository extends TestRepository {

    public FlowNodeInstanceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getFlowNodeInstanceIdsToRestart(final QueryOptions queryOptions) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getFlowNodeInstanceIdsToRestart");
        namedQuery.setMaxResults(queryOptions.getNumberOfResults());
        namedQuery.setFirstResult(queryOptions.getFromIndex());
        return (List<Long>) namedQuery.list();
    }

    public long getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(final long rootProcessDefinitionId, final long userId) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameter("rootProcessDefinitionId", rootProcessDefinitionId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SHumanTaskInstance> searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(final long rootProcessDefinitionId, final long userId) {
        getSessionWithTenantFilter();
        Query namedQuery = getNamedQuery("searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor");
        namedQuery = getSession().createQuery(namedQuery.getQueryString() + " ORDER BY a.name");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameter("rootProcessDefinitionId", rootProcessDefinitionId);
        return namedQuery.list();
    }

    public long getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess(final long rootProcessDefinitionId) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess");
        namedQuery.setParameter("rootProcessDefinitionId", rootProcessDefinitionId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SHumanTaskInstance> searchSHumanTaskInstanceAssignedAndPendingByRootProcess(final long rootProcessDefinitionId) {
        getSessionWithTenantFilter();
        Query namedQuery = getNamedQuery("searchSHumanTaskInstanceAssignedAndPendingByRootProcess");
        namedQuery = getSession().createQuery(namedQuery.getQueryString() + " ORDER BY a.name");
        namedQuery.setParameter("rootProcessDefinitionId", rootProcessDefinitionId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SFlowNodeInstanceStateCounter> getNumberOfArchivedFlowNodesInAllStates(long processInstanceId) {
        getSessionWithTenantFilter();
        Query namedQuery = getNamedQuery("getNumberOfArchivedFlowNodesInAllStates");
        namedQuery.setParameter("parentProcessInstanceId", processInstanceId);
        return (List<SFlowNodeInstanceStateCounter>) namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SFlowNodeInstanceStateCounter> getNumberOfFlowNodesInAllStates(long processInstanceId) {
        getSessionWithTenantFilter();
        Query namedQuery = getNamedQuery("getNumberOfFlowNodesInAllStates");
        namedQuery.setParameter("parentProcessInstanceId", processInstanceId);
        return (List<SFlowNodeInstanceStateCounter>) namedQuery.list();
    }

}
