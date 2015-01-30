/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.persistence.repository;

import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

/**
 * @author Elias Ricken de Medeiros
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

    public SApplication getApplicationByToken(final String token) {
        final Query namedQuery = getNamedQuery("getApplicationByToken");
        namedQuery.setParameter("token", token);
        return (SApplication) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPage(final long applicationPageId) {
        final Query namedQuery = getNamedQuery("getApplicationPageById");
        namedQuery.setParameter("id", applicationPageId);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPageByTokenAndApplicationToken(final String applicationToken, final String applicationPageToken) {
        final Query namedQuery = getNamedQuery("getApplicationPageByTokenAndApplicationToken");
        namedQuery.setParameter("applicationToken", applicationToken);
        namedQuery.setParameter("applicationPageToken", applicationPageToken);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPageByTokenAndApplicationId(final long applicationId, final String applicationPageToken) {
        final Query namedQuery = getNamedQuery("getApplicationPageByTokenAndApplicationId");
        namedQuery.setParameter("applicationId", applicationId);
        namedQuery.setParameter("applicationPageToken", applicationPageToken);
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

    public int getLastIndexForRootMenu() {
        final Query namedQuery = getNamedQuery("getLastIndexForRootMenu");
        return (Integer) namedQuery.uniqueResult();
    }

    public int getLastIndexForChildOf(final long parentMenuId) {
        final Query namedQuery = getNamedQuery("getLastIndexForChildOf");
        namedQuery.setParameter("parentId", parentMenuId);
        return (Integer) namedQuery.uniqueResult();
    }

}
