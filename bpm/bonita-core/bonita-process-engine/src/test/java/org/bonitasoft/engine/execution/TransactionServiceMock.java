/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Baptiste Mesta
 */
public class TransactionServiceMock implements TransactionService {

    @Override
    public void begin() {
    }

    @Override
    public void complete() {
    }

    @Override
    public TransactionState getState() {
        return null;
    }

    @Deprecated
    @Override
    public boolean isTransactionActive() {
        return false;
    }

    @Override
    public void setRollbackOnly() {
    }

    @Override
    public boolean isRollbackOnly() {
        return false;
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        begin();
        try {
            return callable.call();
        } catch (final Exception e) {
            setRollbackOnly();
            throw e;
        } finally {
            complete();
        }
    }

    @Override
    public void registerBonitaSynchronization(final BonitaTransactionSynchronization txSync) {
    }

    @Override
    public void registerBeforeCommitCallable(final Callable<Void> callable) {
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return 0;
    }

}
