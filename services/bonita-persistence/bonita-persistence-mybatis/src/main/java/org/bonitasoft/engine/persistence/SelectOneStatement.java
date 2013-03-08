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
public class SelectOneStatement<T> extends ReadStatement<T> {

    public SelectOneStatement(final String statement, final Object parameter) {
        super(statement, parameter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T execute(final SqlSession sqlSession) {
        return (T) sqlSession.selectOne(this.statement, this.parameter);
    }

}
