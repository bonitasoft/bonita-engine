/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.test.persistence.repository;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.bonitasoft.engine.business.application.SApplication;
import com.bonitasoft.engine.business.application.SApplicationMenu;
import com.bonitasoft.engine.business.application.SApplicationPage;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationRepository extends TestRepository {

    public ApplicationRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public SApplication getApplication(final long applicationId) {
        final Query namedQuery = getNamedQuery("getApplicationById");
        namedQuery.setParameter("id", applicationId);
        return (SApplication) namedQuery.uniqueResult();
    }

    public SApplication getApplicationByName(final String name) {
        final Query namedQuery = getNamedQuery("getApplicationByName");
        namedQuery.setParameter("name", name);
        return (SApplication) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPage(final long applicationPageId) {
        final Query namedQuery = getNamedQuery("getApplicationPageById");
        namedQuery.setParameter("id", applicationPageId);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPageByNameAndApplicationName(final String applicationName, final String applicationPageName) {
        final Query namedQuery = getNamedQuery("getApplicationPageByNameAndApplicationName");
        namedQuery.setParameter("applicationName", applicationName);
        namedQuery.setParameter("applicationPageName", applicationPageName);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPageByNameAndApplicationId(final long applicationId, final String applicationPageName) {
        final Query namedQuery = getNamedQuery("getApplicationPageByNameAndApplicationId");
        namedQuery.setParameter("applicationId", applicationId);
        namedQuery.setParameter("applicationPageName", applicationPageName);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationHomePage(final long applicationId) {
        final Query namedQuery = getNamedQuery("getApplicationHomePage");
        namedQuery.setParameter("applicationId", applicationId);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationMenu getApplicationMenu(final long applicationMenuId) {
        final Query namedQuery = getNamedQuery("getApplicationMenuById");
        namedQuery.setParameter("id", applicationMenuId);
        return (SApplicationMenu) namedQuery.uniqueResult();
    }

}
