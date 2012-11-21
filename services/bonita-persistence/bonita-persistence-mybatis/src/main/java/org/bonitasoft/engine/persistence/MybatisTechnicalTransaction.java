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
