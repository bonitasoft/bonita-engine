package com.bonitasoft.engine.test.persistence.repository;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.bonitasoft.engine.page.SPage;
import com.bonitasoft.engine.page.SPageContent;

public class PageRepository extends TestRepository {

    public PageRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SPageContent getPageContent(final long id) {
        Query namedQuery = getNamedQuery("getPageContent");
        namedQuery.setParameter("id", id);
        return (SPageContent) namedQuery.uniqueResult();
    }

    public SPage getPageByName(final String name) {
        Query namedQuery = getNamedQuery("getPageByName");
        namedQuery.setParameter("pageName", name);
        return (SPage) namedQuery.uniqueResult();
    }
}
