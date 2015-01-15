package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.data.instance.model.archive.SADataInstance;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class SADataInstanceRepository extends TestRepository {

    public SADataInstanceRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<SADataInstance> getSADataInstancesByDataInstanceIdAndArchiveDate(final List<Long> dataInstanceIds, final long time, final long tenantId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        final Query namedQuery = getNamedQuery("getSADataInstancesByDataInstanceIdAndArchiveDate");
        namedQuery.setParameterList("dataInstanceIds", dataInstanceIds);
        namedQuery.setParameter("time", time);
        return namedQuery.list();
    }

}
