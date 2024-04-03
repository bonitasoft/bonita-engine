/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.test.persistence.repository;

import java.util.List;

import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.SPageWithContent;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PageRepository extends TestRepository {

    public PageRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SPageWithContent getPageContent(final long id) {
        final Query namedQuery = getNamedQuery("getPageContent");
        namedQuery.setParameter("id", id);
        return (SPageWithContent) namedQuery.uniqueResult();
    }

    public SPage getPageByName(final String name) {
        final Query namedQuery = getNamedQuery("getPageByName");
        namedQuery.setParameter("pageName", name);
        return (SPage) namedQuery.uniqueResult();
    }

    public SPage getPageByNameAndProcessDefinitionId(final String name, long processDefinitionId) {
        final Query namedQuery = getNamedQuery("getPageByNameAndProcessDefinitionId");
        namedQuery.setParameter("pageName", name);
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        return (SPage) namedQuery.uniqueResult();
    }

    public List<SPage> getPageByProcessDefinitionId(long processDefinitionId) {
        final Query namedQuery = getNamedQuery("getPageByProcessDefinitionId");
        namedQuery.setParameter("processDefinitionId", processDefinitionId);
        return namedQuery.list();
    }

    public SPageMapping getPageMappingByKey(final String key) {
        final Query namedQuery = getNamedQuery("getPageMappingByKey");
        namedQuery.setParameter("key", key);
        return (SPageMapping) namedQuery.uniqueResult();
    }

    public SPageMapping getPageMappingByPageId(final long pageId) {
        final Query namedQuery = getNamedQuery("getPageMappingByPageId");
        namedQuery.setParameter("pageId", pageId);
        return (SPageMapping) namedQuery.uniqueResult();
    }
}
