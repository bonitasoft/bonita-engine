/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import org.apache.ibatis.session.SqlSession;

/**
 * @author Charles Souillard
 */
public class SelectByIdStatement<T> extends ReadStatement<T> {

    private final Class<? extends PersistentObject> entityClass;

    private final long id;

    public SelectByIdStatement(final String statement, final Object parameter, final Class<? extends PersistentObject> entityClass, final long id) {
        super(statement, parameter);
        this.entityClass = entityClass;
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T execute(final SqlSession sqlSession) {
        return (T) sqlSession.selectOne(this.statement, this.parameter);
    }

    public long getId() {
        return this.id;
    }

    public Class<? extends PersistentObject> getEntityClass() {
        return this.entityClass;
    }

}
