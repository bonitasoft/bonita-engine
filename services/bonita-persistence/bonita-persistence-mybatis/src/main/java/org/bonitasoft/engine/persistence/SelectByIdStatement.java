/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
