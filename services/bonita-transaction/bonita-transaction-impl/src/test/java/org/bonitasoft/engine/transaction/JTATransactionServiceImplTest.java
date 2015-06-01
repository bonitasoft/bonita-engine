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
package org.bonitasoft.engine.transaction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.MyTransactionManager.MyTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JTATransactionServiceImplTest {

    @Mock
    TechnicalLoggerService logger;
    @Mock
    TransactionManager txManager;

    @Test
    public void beginTransaction() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
        verify(txManager, times(1)).begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
    }

    @Test(expected = STransactionCreationException.class)
    public void doNotSupportNestedCalls() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
        txService.begin();
    }

    @Test
    public void beginTransactionFailed() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        doThrow(new SystemException("Mocked")).when(txManager).begin();

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        try {
            txService.begin();
            fail("Thanks to the mock an exception must have been thrown");
        } catch (STransactionCreationException e) {
            verify(txManager, times(1)).begin();
            assertEquals(0, txService.getNumberOfActiveTransactions());
        }
    }

    @Test
    public void numberOfActiveTransactionsWhenCompleteFailed() throws Exception {
        MyTransactionManager.MyTransaction transaction = new MyTransactionManager.MyTransaction() {

            @Override
            public int internalCommit() throws SystemException {
                throw new SystemException("Mocked");
            }
        };

        TransactionManager txManager = new MyTransactionManager(transaction);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
        try {
            txService.complete();
            fail("Thanks to the mock an exception must have been thrown");
        } catch (STransactionException e) {
            assertEquals(0, txService.getNumberOfActiveTransactions());
        }
    }

    @Test
    public void beginTransactionEventFailed() throws Exception {
        // We want to ensure that when an exception was thrown after the transaction's begin then
        // we close the open transaction to be in a consistent state.
        when(logger.isLoggable(JTATransactionServiceImpl.class, TechnicalLogSeverity.TRACE)).thenReturn(true);

        TransactionManager txManager = mock(TransactionManager.class);
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        when(txManager.getTransaction()).thenThrow(new SystemException("Mocked"));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        try {
            txService.begin();
            fail("The begin should have thrown an exception.");
        } catch (STransactionCreationException e) {
            verify(txManager, times(1)).rollback();
            assertEquals(0, txService.getNumberOfActiveTransactions());
        }
    }

    @Test
    public void setRollbackOnly() throws Exception {

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.setRollbackOnly();
        verify(txManager).setRollbackOnly();
    }

    /**
     * The method call has to be executed between a transaction.
     *
     * @throws Exception
     */
    @Test
    public void testExecuteInTransactionWithCommit() throws Exception {
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_ACTIVE);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        Callable<?> callable = mock(Callable.class);

        txService.executeInTransaction(callable);

        verify(txManager).begin();
        verify(callable).call();
        verify(txManager).commit();
    }

    /**
     * The method call has to be executed between a transaction.
     *
     * @throws Exception
     */
    @Test
    public void testExecuteInTransactionWithRollback() throws Exception {
        // First to allow to start the transaction, then to force to call rollback
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_MARKED_ROLLBACK);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        Callable<?> callable = mock(Callable.class);
        when(callable.call()).thenThrow(new Exception("Mocked exception"));

        try {
            txService.executeInTransaction(callable);
            fail("An exception should have been thrown.");
        } catch (Exception e) {
        }
        verify(txManager).begin();
        verify(callable).call();
        verify(txManager).setRollbackOnly();
        verify(txManager).rollback();
    }

    @Test
    public void testTransactionSynchronizationInManagedTransaction() throws Exception {
        Transaction transaction = mock(MyTransaction.class);
        TransactionManager txManager = new MyTransactionManager(transaction);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        try {
            txService.begin();
        } finally {
            txService.complete();
        }

        // 2 : for the ResetCounter and DecrementNumberOfActiveTransactions
        verify(transaction, times(2)).registerSynchronization(Mockito.any(Synchronization.class));
    }

    @Test
    public void testTransactionSynchronizationInNotManagedTransaction() throws Exception {
        Transaction transaction = mock(MyTransaction.class);
        TransactionManager txManager = new MyTransactionManager(transaction);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        try {
            txManager.begin();
            try {
                txService.begin();
            } finally {
                txService.complete();
            }
        } finally {
            txManager.commit();
        }

        // 2 : for the ResetCounter and DecrementNumberOfActiveTransactions
        verify(transaction, times(2)).registerSynchronization(Mockito.any(Synchronization.class));
    }

    private class MyBeforeCommitCallable implements Callable<Void> {

        private boolean called = false;

        @Override
        public Void call() throws Exception {
            called = true;

            return null;
        }

        public boolean isCalled() {
            return called;
        }
    }

    @Test
    public void testBeforeCommitCallablesAreExecutedWhileCommit() throws Exception {
        Transaction transaction = new MyTransaction();
        TransactionManager txManager = new MyTransactionManager(transaction);

        final JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        final MyBeforeCommitCallable callable = new MyBeforeCommitCallable();
        try {
            txService.begin();
            txService.registerBeforeCommitCallable(callable);
            assertFalse(callable.isCalled());
        } finally {
            txService.complete();
        }

        assertTrue(callable.isCalled());
    }

    @Test
    public void testBeforeCommitCallablesAreNotExecutedWhileMarkedAsRollback() throws Exception {
        Transaction transaction = new MyTransaction();
        TransactionManager txManager = new MyTransactionManager(transaction);

        final JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        final MyBeforeCommitCallable callable = new MyBeforeCommitCallable();

        try {
            txService.begin();
            txService.registerBeforeCommitCallable(callable);
            assertFalse(callable.isCalled());
            txService.setRollbackOnly();
        } finally {
            txService.complete();
        }

        assertFalse(callable.isCalled());
    }

    @Test(expected = STransactionException.class)
    public void completeShouldDecrementCounterIfSystemExceptionOccurs() throws Exception {
        doReturn(Status.STATUS_PREPARED).when(txManager).getStatus();
        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        doThrow(SystemException.class).when(txService).commit();

        try {
            txService.complete();
        } finally {
            verify(txService).resetTxCounter(any(JTATransactionServiceImpl.TransactionServiceContext.class));
        }
    }

    @Test(expected = STransactionException.class)
    public void completeShouldDecrementCounterIfTransactionExceptionOccurs() throws Exception {
        doReturn(Status.STATUS_PREPARED).when(txManager).getStatus();
        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        doThrow(STransactionCommitException.class).when(txService).commit();

        try {
            txService.complete();
        } finally {
            verify(txService).resetTxCounter(any(JTATransactionServiceImpl.TransactionServiceContext.class));
        }
    }

    @Test(expected = STransactionCreationException.class)
    public void beginShouldResetCounterIfTransactionCreationExceptionOccurs() throws Exception {
        doReturn(Status.STATUS_PREPARED).when(txManager).getStatus();
        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        doThrow(STransactionCreationException.class).when(txService).createTransaction(anyBoolean());

        try {
            txService.begin();
        } finally {
            verify(txService).resetTxCounter(any(JTATransactionServiceImpl.TransactionServiceContext.class));
        }
    }

    @Test(expected = STransactionCreationException.class)
    public void beginShouldResetCounterIfSystemExceptionOccurs() throws Exception {
        doReturn(Status.STATUS_PREPARED).when(txManager).getStatus();
        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        doThrow(SystemException.class).when(txService).createTransaction(anyBoolean());

        try {
            txService.begin();
        } finally {
            verify(txService).resetTxCounter(any(JTATransactionServiceImpl.TransactionServiceContext.class));
        }
    }
}
