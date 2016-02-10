/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.page.SPageWithContent;
import org.bonitasoft.engine.page.impl.SPageWithContentImpl;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
}
