package com.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.impl.SApplicationImpl;
import com.bonitasoft.engine.page.SPageWithContent;
import com.bonitasoft.engine.page.impl.SPageWithContentImpl;
import com.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;

/**
 * Test Repository
 * Need to be used in a transactional context
 */
public class TestRepository {

    private final SessionFactory sessionFactory;

    public TestRepository(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Session getSessionWithTenantFilter() {
        final Session session = getSession();
        session.enableFilter("tenantFilter").setParameter("tenantId", PersistentObjectBuilder.DEFAULT_TENANT_ID);
        return session;
    }

    protected Query getNamedQuery(final String queryName) {
        return getSessionWithTenantFilter().getNamedQuery(queryName);
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SPageWithContent add(final SPageWithContentImpl sPageWithContentImpl) {
        getSession().save(sPageWithContentImpl);
        return (SPageWithContentImpl) getSession().get(sPageWithContentImpl.getClass(),
                new PersistentObjectId(sPageWithContentImpl.getId(), sPageWithContentImpl.getTenantId()));
    }

    public SApplication add(final SApplicationImpl application) {
        getSession().save(application);
        return (SApplication) getSession().get(application.getClass(),
                new PersistentObjectId(application.getId(), application.getTenantId()));
    }
}
