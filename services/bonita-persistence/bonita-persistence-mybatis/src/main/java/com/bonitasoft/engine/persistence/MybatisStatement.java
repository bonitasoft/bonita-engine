/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import org.apache.ibatis.session.SqlSession;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Charles Souillard
 */
public abstract class MybatisStatement<T> {

    protected final String statement;

    protected final Object parameter;

    public MybatisStatement(final String statement, final Object parameter) {
        super();
        this.statement = statement;
        this.parameter = parameter;
    }

    public abstract T execute(final SqlSession sqlSession) throws SPersistenceException;

    @Override
    public String toString() {
        return "MybatisStatement [parameter=" + this.parameter + ", statement=" + this.statement + "]";
    }

}
