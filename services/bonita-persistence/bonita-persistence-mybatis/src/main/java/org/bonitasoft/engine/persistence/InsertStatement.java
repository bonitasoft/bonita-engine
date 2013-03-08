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
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Charles Souillard
 * @author Frederic Bouquet
 */
public class InsertStatement extends WriteStatement {

    private final PersistentObject entity;

    public InsertStatement(final String statement, final Object parameter, final PersistentObject entity) {
        super(statement, parameter);
        this.entity = entity;
    }

    @Override
    public Void execute(final SqlSession sqlSession) throws SPersistenceException {
        sqlSession.insert(this.statement, this.parameter);
        return null;
    }

    public PersistentObject getEntity() {
        return this.entity;
    }

}
