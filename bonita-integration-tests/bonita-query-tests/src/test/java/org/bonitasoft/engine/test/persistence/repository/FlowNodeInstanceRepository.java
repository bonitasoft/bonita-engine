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

import java.time.Duration;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstanceStateCounter;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Elias Ricken de Medeiros
 */
@Repository
public class FlowNodeInstanceRepository extends TestRepository {

    public FlowNodeInstanceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SFlowNodeInstance getById(long id) {
        final Query namedQuery = getNamedQuery("getSFlowNodeInstanceById");
        namedQuery.setParameter("id", id);
        return (SFlowNodeInstance) namedQuery.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getFlowNodeInstanceIdsToRecover(Duration considerElementsOlderThan,
            final QueryOptions queryOptions) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getFlowNodeInstanceIdsToRecover");
        namedQuery.setMaxResults(queryOptions.getNumberOfResults());
        namedQuery.setFirstResult(queryOptions.getFromIndex());
        namedQuery.setParameter("maxLastUpdate", System.currentTimeMillis() - considerElementsOlderThan.toMillis());
        return (List<Long>) namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getGatewayInstanceIdsToRecover(Duration considerElementsOlderThan,
            final QueryOptions queryOptions) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getGatewayInstanceIdsToRecover");
        namedQuery.setMaxResults(queryOptions.getNumberOfResults());
        namedQuery.setFirstResult(queryOptions.getFromIndex());
        namedQuery.setParameter("maxLastUpdate", System.currentTimeMillis() - considerElementsOlderThan.toMillis());
        return (List<Long>) namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public SGatewayInstance getActiveGatewayInstanceOfProcess(long parentProcessInstanceId, String name) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getActiveGatewayInstanceOfProcess");
        namedQuery.setParameter("parentProcessInstanceId", parentProcessInstanceId);
        namedQuery.setParameter("name", name);
        return (SGatewayInstance) namedQuery.uniqueResult();
    }

    public long getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(final long rootProcessDefinitionId,
            final long userId) {
        getSessionWithTenantFilter();
        final Query namedQuery = getNamedQuery("getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor");
        namedQuery.setParameter("userId", userId);
        namedQuery.setParameter("rootProcessDefinitionId", rootProcessDefinitionId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SHumanTaskInstance> searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(
            final long rootProcessDefinitionId, final long userId) {
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
    public List<SHumanTaskInstance> searchSHumanTaskInstanceAssignedAndPendingByRootProcess(
            final long rootProcessDefinitionId) {
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

    @SuppressWarnings("unchecked")
    public List<SFlowNodeInstanceStateCounter> getNumberOfFlowNodesOfProcessDefinitionInAllStates(
            long processDefinitionId) {
        getSessionWithTenantFilter();
        Query namedQuery = getNamedQuery("getNumberOfFlowNodesOfProcessDefinitionInAllStates");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        return (List<SFlowNodeInstanceStateCounter>) namedQuery.list();
    }

}
