/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;

public class TransactionServiceForTest implements TransactionService {

    public TransactionServiceForTest() {
    }

    @Override
    public void begin() {
    }

    @Override
    public void complete() {
    }

    @Override
    public TransactionState getState() throws STransactionException {
        return null;
    }

    @Override
    public boolean isTransactionActive() throws STransactionException {
        return false;
    }

    @Override
    public void setRollbackOnly() {
    }

    @Override
    public boolean isRollbackOnly() throws STransactionException {
        return false;
    }

    @Override
    public void registerBonitaSynchronization(final BonitaTransactionSynchronization txSync) {
    }

    @Override
    public <T> T executeInTransaction(final Callable<T> callable) throws Exception {
        return callable.call();
    }

    @Override
    public long getNumberOfActiveTransactions() {
        return -1;
    }

}
