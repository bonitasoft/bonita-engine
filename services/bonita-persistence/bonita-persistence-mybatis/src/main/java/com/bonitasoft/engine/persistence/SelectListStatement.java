/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Charles Souillard
 */
public class SelectListStatement<T> extends ReadStatement<List<T>> {

    private final RowBounds rowBounds;

    public SelectListStatement(final String statement, final Object parameter, final RowBounds rowBounds) {
        super(statement, parameter);
        this.rowBounds = rowBounds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> execute(final SqlSession sqlSession) {
        return sqlSession.selectList(this.statement, this.parameter, this.rowBounds);
    }

}
