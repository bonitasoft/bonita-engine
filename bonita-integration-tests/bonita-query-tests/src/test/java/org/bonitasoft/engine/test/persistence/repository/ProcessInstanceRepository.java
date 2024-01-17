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

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.identity.model.SUser;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessInstanceRepository extends TestRepository {

    public ProcessInstanceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getPossibleUserIdsOfPendingTasks(final long activityInstanceId) {
        final Query namedQuery = getNamedQuery("getPossibleUserIdsOfPendingTasks");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SUser> searchPossibleUserIdsOfPendingTasks(final long activityInstanceId) {
        final Query namedQuery = getNamedQuery("searchSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    public long getNumberOfSUserWhoCanStartPendingTask(final long activityInstanceId) {
        final Query namedQuery = getNamedQuery("getNumberOfSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SUser> searchSUserWhoCanStartProcess(final long processId) {
        final Query namedQuery = getNamedQuery("searchSUserWhoCanStartProcess");
        namedQuery.setParameter("processId", processId);
        namedQuery.setParameter("trueValue", true);
        return namedQuery.list();
    }

    public long getNumberOfSUserWhoCanStartProcess(final long processId) {
        final Query namedQuery = getNamedQuery("getNumberOfSUserWhoCanStartProcess");
        namedQuery.setParameter("processId", processId);
        namedQuery.setParameter("trueValue", true);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public boolean isTaskPendingForUser(final long activityInstanceId, final long userId) {
        final Query namedQuery = getNamedQuery("isTaskPendingForUser");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue() == 1;
    }

    public long countChildrenInstanceIdsOfProcessInstance(final long processInstanceId) {
        final Query namedQuery = getNamedQuery("getNumberOfChildInstancesOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getChildrenInstanceIdsOfProcessInstance(final long processInstanceId) {
        final Query namedQuery = getNamedQuery("getChildInstanceIdsOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return namedQuery.list();
    }

    public long getNumberOfFailedSProcessInstanceSupervisedBy(final long userId) {
        final Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailedAndSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public List<SProcessInstance> searchFailedSProcessInstanceSupervisedBy(final long userId) {
        final Query namedQuery = getNamedQuery("searchSProcessInstanceFailedAndSupervisedBy");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public long getNumberOfSProcessInstanceFailed() {
        final Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailed");
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public long getNumberOfSProcessInstanceFailedForProcessDefinition(final long processDefinitionId) {
        Query namedQuery = getNamedQuery("getNumberOfSProcessInstanceFailed");
        namedQuery = getSession()
                .createQuery(namedQuery.getQueryString() + " AND p.processDefinitionId = " + processDefinitionId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessInstance> searchSProcessInstanceFailedForProcessDefinition(final long processDefinitionId) {
        Query namedQuery = getNamedQuery("searchSProcessInstanceFailed");
        namedQuery = getSession()
                .createQuery(namedQuery.getQueryString() + " AND p.processDefinitionId = " + processDefinitionId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SProcessInstance> searchSProcessInstanceFailed() {
        final Query namedQuery = getNamedQuery("searchSProcessInstanceFailed");
        return namedQuery.list();
    }

    public long getNumberOfProcessInstances(final long processDefinitionId) {
        final Query namedQuery = getNamedQuery("countProcessInstancesOfProcessDefinition");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        return (Long) namedQuery.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<SAProcessInstance> getArchivedProcessInstancesInAllStates(final List<Long> sourceProcessInstanceIds) {
        final Query namedQuery = getNamedQuery("getArchivedProcessInstancesInAllStates");
        namedQuery.setParameterList("sourceObjectIds", sourceProcessInstanceIds);
        return namedQuery.list();
    }

    public long getNumberOfTimerEventTriggerInstances(final long processInstanceId, final String jobTriggerName) {
        Query namedQuery = getNamedQuery("getNumberOfSTimerEventTriggerInstanceByProcessInstance");
        if (jobTriggerName != null) {
            namedQuery = getSession()
                    .createQuery(namedQuery.getQueryString() + " AND e.name = '" + jobTriggerName + "'");
        }
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<STimerEventTriggerInstance> searchTimerEventTriggerInstances(final long processInstanceId,
            final String jobTriggerName) {
        Query namedQuery = getNamedQuery("searchSTimerEventTriggerInstanceByProcessInstance");
        if (jobTriggerName != null) {
            namedQuery = getSession()
                    .createQuery(namedQuery.getQueryString() + " AND e.name = '" + jobTriggerName + "'");
        }
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return namedQuery.list();
    }

    public List<Long> getProcessInstanceIdsToRecover(final long maxLastUpdate) {
        final Query<Long> namedQuery = getNamedQuery("getProcessInstanceIdsToRecover");
        namedQuery.setParameter("maxLastUpdate", maxLastUpdate);
        return namedQuery.list();
    }

}
