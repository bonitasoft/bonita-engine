package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageContent;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

public class PageRepository extends TestRepository {

    public PageRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SPageContent getPageContent(final long id) {
        final Query namedQuery = getNamedQuery("getPageContent");
        namedQuery.setParameter("id", id);
        return (SPageContent) namedQuery.uniqueResult();
    }

    public SPage getPageByName(final String name) {
        final Query namedQuery = getNamedQuery("getPageByName");
        namedQuery.setParameter("pageName", name);
        return (SPage) namedQuery.uniqueResult();
    }
}
