/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.transaction.TechnicalTransaction;
import org.bonitasoft.engine.transaction.STransactionPrepareException;
import org.bonitasoft.engine.transaction.TransactionResourceState;

/**
 * @author Charles Souillard
 */
public class MybatisTechnicalTransaction implements TechnicalTransaction {

    private final MybatisSession session;

    private final MybatisTechnicalTransactionListener listener;

    protected TransactionResourceState state;

    protected final List<Serializable> enlistedResources;

    public MybatisTechnicalTransaction(final MybatisTechnicalTransactionListener listener, final SqlSession sqlSession, final boolean cacheEnabled) {
        this.state = TransactionResourceState.CREATED;
        this.enlistedResources = new ArrayList<Serializable>();
        this.listener = listener;
        this.session = new MybatisSession(sqlSession, cacheEnabled);
    }

    @Override
    public TransactionResourceState getState() {
        return this.state;
    }

    @Override
    public void enlistSynchronizationResource(final Serializable resource) {
        this.enlistedResources.add(resource);
    }

    @Override
    public List<Serializable> getEnlistedSynchronizationResource() {
        return Collections.unmodifiableList(this.enlistedResources);
    }

    @Override
    public void commit() {
        this.state = TransactionResourceState.COMMITTED;
        try {
            this.session.commit();
        } catch (final Throwable e) {
            this.state = TransactionResourceState.ROLLEDBACK;
        } finally {
            this.closeSession();
        }
    }

    public MybatisSession getSession() {
        return this.session;
    }

    private void closeSession() {
        this.session.close();
        if (this.listener != null) {
            this.listener.close(this.session);
        }
    }

    @Override
    public void prepare() throws STransactionPrepareException {
        try {
            this.session.prepare();
        } catch (final SPersistenceException e) {
            throw new STransactionPrepareException("unable to prepare session: " + this.session, e);
        }
    }

    @Override
    public void rollback() {
        this.state = TransactionResourceState.ROLLEDBACK;
        try {
            this.session.rollback();
        } finally {
            this.closeSession();
        }
    }

}
