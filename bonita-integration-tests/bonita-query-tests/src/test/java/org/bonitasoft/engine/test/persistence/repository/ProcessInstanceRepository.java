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

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ProcessInstanceRepository extends TestRepository {

    public ProcessInstanceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getPossibleUserIdsOfPendingTasks(final long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getPossibleUserIdsOfPendingTasks");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SUser> searchPossibleUserIdsOfPendingTasks(final long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    public long getNumberOfSUserWhoCanStartPendingTask(final long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }
    public boolean isTaskPendingForUser(final long activityInstanceId, final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("isTaskPendingForUser");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue() == 1;
    }

    public long countChildrenInstanceIdsOfProcessInstance(final long processInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfChildInstancesOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getChildrenInstanceIdsOfProcessInstance(final long processInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getChildInstanceIdsOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return namedQuery.list();
    }

    public long getNumberOfFailedSProcessInstanceSupervisedBy(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailedAndSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public List<SProcessInstance> searchFailedSProcessInstanceSupervisedBy(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSProcessInstanceFailedAndSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public long getNumberOfSProcessInstanceFailed() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailed");
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public long getNumberOfSProcessInstanceFailedForProcessDefinition(final long processDefinitionId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailed");
        namedQuery = getSession().createQuery(namedQuery.getQueryString() + " AND p.processDefinitionId = " + processDefinitionId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessInstance> searchSProcessInstanceFailedForProcessDefinition(final long processDefinitionId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("searchSProcessInstanceFailed");
        namedQuery = getSession().createQuery(namedQuery.getQueryString() + " AND p.processDefinitionId = " + processDefinitionId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessInstance> searchSProcessInstanceFailed() {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSProcessInstanceFailed");
        return namedQuery.list();
    }

    public long getNumberOfProcessInstances(final long processDefinitionId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("countProcessInstancesOfProcessDefinition");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        return (Long) namedQuery.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<SAProcessInstance> getArchivedProcessInstancesInAllStates(final List<Long> sourceProcessInstanceIds) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getArchivedProcessInstancesInAllStates");
        namedQuery.setParameterList("sourceObjectIds", sourceProcessInstanceIds);
        return namedQuery.list();
    }

    public long getNumberOfTimerEventTriggerInstances(final long processInstanceId, final String jobTriggerName) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getNumberOfSTimerEventTriggerInstanceByProcessInstance");
        if (jobTriggerName != null) {
            namedQuery = getSession().createQuery(namedQuery.getQueryString() + " AND e.name = '" + jobTriggerName + "'");
        }
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SEventTriggerInstance> searchTimerEventTriggerInstances(final long processInstanceId, final String jobTriggerName) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("searchSTimerEventTriggerInstanceByProcessInstance");
        if (jobTriggerName != null) {
            namedQuery = getSession().createQuery(namedQuery.getQueryString() + " AND e.name = '" + jobTriggerName + "'");
        }
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return namedQuery.list();
    }

}
