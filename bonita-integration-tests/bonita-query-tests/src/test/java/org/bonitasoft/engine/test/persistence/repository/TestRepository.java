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
import java.util.Random;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Test Repository
 * Need to be used in a transactional context
 */
@Repository
public class TestRepository {

    private final SessionFactory sessionFactory;

    public TestRepository(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Query getNamedQuery(final String queryName) {
        return getSession().getNamedQuery(queryName);
    }

    public void flush() {
        getSession().flush();
    }

    private Long getTenantId(PersistentObject entity) {
        Long tenantId = null;
        try {
            tenantId = ClassReflector.invokeGetter(entity, "getTenantId");
        } catch (final Exception ignored) {
            //not set
        }
        return tenantId;
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentObject> T getById(final Class<? extends PersistentObject> clazz, long id,
            long tenantId) {
        return (T) getSession().get(clazz,
                new PersistentObjectId(id, tenantId));
    }

    public Long selectCount(String queryName, Pair... parameters) {
        Query namedQuery = getNamedQuery(queryName);
        setParameters(namedQuery, parameters);
        return ((Long) namedQuery.uniqueResult());
    }

    public PersistentObject selectOne(String queryName, Pair... parameters) {
        Query namedQuery = getNamedQuery(queryName);
        setParameters(namedQuery, parameters);
        return ((PersistentObject) namedQuery.uniqueResult());
    }

    public <T> List<T> selectList(String name, Pair... parameters) {
        Query<T> namedQuery = getNamedQuery(name);
        setParameters(namedQuery, parameters);
        return namedQuery.list();
    }

    protected <T> void setParameters(Query<T> namedQuery, Pair[] parameters) {
        for (Pair parameter : parameters) {
            namedQuery.setParameter(((String) parameter.getKey()), parameter.getValue());
        }
    }

    /**
     * Utility method to run native queries on tables while developping tests:
     * e.g.
     * `repository.list("SELECT user_.* from user_ where user_.username = 'walter.bates'", SUser.class)`
     * will return the list of object SUser from database
     */
    public List<Object[]> list(String sqlQuery, Class type) {
        return getSession().createSQLQuery(sqlQuery).addEntity(type).list();
    }

    public <T extends PersistentObject> T add(T entity) {
        if (entity.getId() <= 0) {
            entity.setId(new Random().nextLong());
        }
        getSession().save(entity);
        return (T) getSession().get(entity.getClass(), new PersistentObjectId(entity.getId(), getTenantId(entity)));
    }

    public <T extends PersistentObject> void add(T... entities) {
        for (T entity : entities) {
            add(entity);
        }
    }
}
