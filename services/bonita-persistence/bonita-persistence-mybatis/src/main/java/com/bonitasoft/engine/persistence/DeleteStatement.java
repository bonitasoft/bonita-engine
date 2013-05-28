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

/**
 * @author Charles Souillard
 */
public class DeleteStatement extends WriteStatement {

    public DeleteStatement(final String statement, final Object parameter) {
        super(statement, parameter);
    }

    @Override
    public Void execute(final SqlSession sqlSession) {
        sqlSession.delete(this.statement, this.parameter);
        return null;
    }

}
