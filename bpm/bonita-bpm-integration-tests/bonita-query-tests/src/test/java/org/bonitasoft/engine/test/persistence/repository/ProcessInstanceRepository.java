package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.hibernate.Query;
import org.hibernate.SessionFactory;


public class ProcessInstanceRepository extends TestRepository {

    public ProcessInstanceRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getPossibleUserIdsOfPendingTasks(long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getPossibleUserIdsOfPendingTasks");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<SUser> searchPossibleUserIdsOfPendingTasks(long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("searchSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }

    public long getNumberOfSUserWhoCanStartPendingTask(long activityInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getNumberOfSUserWhoCanStartPendingTask");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    public long countChildrenInstanceIdsOfProcessInstance(long processInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getNumberOfChildInstancesOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Long> getChildrenInstanceIdsOfProcessInstance(long processInstanceId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        Query namedQuery = getNamedQuery("getChildInstanceIdsOfProcessInstance");
        namedQuery.setParameter("processInstanceId", processInstanceId);
        return namedQuery.list();
    }
}
