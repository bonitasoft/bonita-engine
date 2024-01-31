/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import java.util.Random;

import org.bonitasoft.engine.temporary.content.STemporaryContent;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Haroun EL ALAMI
 */
@Repository
public class TemporaryContentRepository extends TestRepository {

    public TemporaryContentRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public STemporaryContent add(STemporaryContent entity) {
        if (entity.getId() <= 0) {
            entity.setId(new Random().nextLong());
        }
        getSession().save(entity);
        return getByKey(entity.getKey());
    }

    public STemporaryContent getByKey(String key) {
        final Query<STemporaryContent> namedQuery = getNamedQuery("getTemporaryResource");
        namedQuery.setParameter("key", key);
        STemporaryContent entity = namedQuery.getSingleResult();
        getSession().refresh(entity);
        return entity;
    }

    public int cleanOutDatedTemporaryContent(long creationDate) {
        final Query<STemporaryContent> namedQuery = getNamedQuery("cleanOutDatedTemporaryResources");
        namedQuery.setParameter("creationDate", creationDate);
        return namedQuery.executeUpdate();
    }

}
