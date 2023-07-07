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

import org.bonitasoft.engine.persistence.PersistentObject;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

/**
 * @author Haroun EL ALAMI
 */
@Repository
public class TemporaryContentRepository extends TestRepository {

    public TemporaryContentRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public <T extends PersistentObject> T add(T entity) {
        if (entity.getId() <= 0) {
            entity.setId(new Random().nextLong());
        }
        getSession().save(entity);
        return (T) getSession().get(entity.getClass(), entity.getId());
    }

    public <T extends PersistentObject> T getById(final Class<? extends PersistentObject> clazz, long id) {
        return (T) getSession().get(clazz, id);
    }

}
