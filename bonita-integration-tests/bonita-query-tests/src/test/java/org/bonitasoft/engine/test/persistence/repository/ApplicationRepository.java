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

import org.bonitasoft.engine.business.application.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Elias Ricken de Medeiros
 */
@Repository
public class ApplicationRepository extends TestRepository {

    public ApplicationRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
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

    public SApplicationPage getApplicationPageByTokenAndApplicationToken(final String applicationToken,
            final String applicationPageToken) {
        final Query namedQuery = getNamedQuery("getApplicationPageByTokenAndApplicationToken");
        namedQuery.setParameter("applicationToken", applicationToken);
        namedQuery.setParameter("applicationPageToken", applicationPageToken);
        return (SApplicationPage) namedQuery.uniqueResult();
    }

    public SApplicationPage getApplicationPageByTokenAndApplicationId(final long applicationId,
            final String applicationPageToken) {
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

    public List<String> getAllPagesForProfile(final long profileId) {
        final Query namedQuery = getNamedQuery("getAllPagesForProfile");
        namedQuery.setParameter("profileId", profileId);
        return namedQuery.list();
    }

    public Long getNumberOfApplicationOfUser(final long userId) {
        final Query namedQuery = getNamedQuery("getNumberOfSApplicationOfUser");
        namedQuery.setParameter("userId", userId);
        return (Long) namedQuery.uniqueResult();
    }

    public List<SApplication> searchApplicationOfUser(final long userId) {
        final Query namedQuery = getNamedQuery("searchSApplicationOfUser");
        namedQuery.setParameter("userId", userId);
        return namedQuery.list();
    }

    public void update(final AbstractSApplication application) {
        getSession().update(application);
    }

}
