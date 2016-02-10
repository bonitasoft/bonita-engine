/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Matthieu Chaffotte
 */
public class TransactionExecutorImplTest {

    @SuppressWarnings("unchecked")
    @Test
    public void executeTransaction() throws Exception {
        final TransactionService transactionService = mock(TransactionService.class);
        final TransactionContent transactionContent = mock(TransactionContent.class);
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);

        executor.execute(transactionContent);

        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SBonitaException.class)
    public void executeTransactionWithSBonitaException() throws Exception {
        // To keep compatibility the SBonitaException has to be rethrown

        final TransactionService transactionService = new MockTransactionService();
        final TransactionContent transactionContent = mock(TransactionContent.class);
        @SuppressWarnings("deprecation")
        final TransactionExecutorImpl executor = new TransactionExecutorImpl(transactionService);

        Mockito.doThrow(new SBonitaException() {

            private static final long serialVersionUID = -2042720042398918977L;
        }).when(transactionContent).execute();

        executor.execute(transactionContent);

        verify(transactionService, times(1)).executeInTransaction(any(Callable.class));
    }

    // Minimal implementation of a TransactionService (we do not (and don't want to !) see the TransactionServiceForTest)
    class MockTransactionService implements TransactionService {

        @Override
        public void begin() {
            // TODO Auto-generated method stub

        }

        @Override
        public void complete() {
            // TODO Auto-generated method stub

        }

        @Override
        public TransactionState getState() {
            // TODO Auto-generated method stub
            return null;
        }

        @Deprecated
        @Override
        public boolean isTransactionActive() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setRollbackOnly() {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isRollbackOnly() {
            // TODO Auto-generated method stub
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
            // TODO Auto-generated method stub

        }

        @Override
        public void registerBeforeCommitCallable(final Callable<Void> callable) {
            // TODO Auto-generated method stub

        }

        @Override
        public long getNumberOfActiveTransactions() {
            // TODO Auto-generated method stub
            return 0;
        }

    }

}
