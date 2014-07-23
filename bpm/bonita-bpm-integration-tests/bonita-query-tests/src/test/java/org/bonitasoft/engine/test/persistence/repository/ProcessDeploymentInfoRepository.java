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
    public List<SProcessDefinitionDeployInfo> searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("searchSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public long getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(final long userId) {
        getSession().enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        final Query namedQuery = getNamedQuery("getNumberOfSProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser");
        namedQuery.setParameter("userId", userId);
        return ((Number) namedQuery.uniqueResult()).longValue();
    }

}
