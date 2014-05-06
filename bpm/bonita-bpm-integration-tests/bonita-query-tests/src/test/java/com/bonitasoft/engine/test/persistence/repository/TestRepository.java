package com.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.bonitasoft.engine.page.SPageWithContent;
import com.bonitasoft.engine.page.impl.SPageWithContentImpl;

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

    protected Query getNamedQuery(final String queryName) {
        return getSession().getNamedQuery(queryName);
    }

    /**
     * Need to replicate add method for each object because we don't have any superclass or interface providing getTenantId method
     */
    public SPageWithContent add(final SPageWithContentImpl sPageWithContentImpl) {
        getSession().save(sPageWithContentImpl);
        return (SPageWithContentImpl) getSession().get(sPageWithContentImpl.getClass(),
                new PersistentObjectId(sPageWithContentImpl.getId(), sPageWithContentImpl.getTenantId()));
    }
}
