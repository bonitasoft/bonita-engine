package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;


public class ProcessInstanceRepository extends TestRepository {

    public ProcessInstanceRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @SuppressWarnings("unchecked")
    public List<Long> getPossibleUserIdsOfPendingTasks(long activityInstanceId) {
        Query namedQuery = getNamedQuery("getPossibleUserIdsOfPendingTasks");
        namedQuery.setParameter("humanTaskInstanceId", activityInstanceId);
        return namedQuery.list();
    }
}
