package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class ProcessInstanceRepository extends TestRepository {

    public ProcessInstanceRepository(SessionFactory sessionFactory) {
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
