package org.bonitasoft.engine.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;


public class JTATransactionServiceImplTest {

    @Test
    public void beginTransaction() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
        verify(txManager, times(1)).begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
    }

    @Test(expected=STransactionCreationException.class)
    public void doNotSupportNestedTransaction() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        txService.begin();
    }

    @Test
    public void beginTransactionFailed() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        when(txManager.getTransaction()).thenThrow(new SystemException("Mocked"));

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
    public void completeTransactionFailed() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_ACTIVE);
        doThrow(new SystemException("Mocked")).when(txManager).commit();

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

        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        when(txManager.getTransaction()).thenThrow(new SystemException("Mocked"));

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));

        try {
            txService.begin();
            fail("The begin should have thrown an exception.");
        } catch (STransactionCreationException e) {
            verify(txManager, times(1)).rollback();
        }
    }

    @Test
    public void setRollbackOnly() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));

        txService.setRollbackOnly();
        verify(txManager).setRollbackOnly();
    }

    /**
     * The method call has to be executed between a transaction.
     * @throws Exception
     */
    @Test
    public void testExecuteInTransactionWithCommit() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));
        Callable<?> callable = mock(Callable.class);

        txService.executeInTransaction(callable);

        verify(txManager).begin();
        verify(callable).call();
        verify(txManager).commit();
    }

    /**
     * The method call has to be executed between a transaction.
     * @throws Exception
     */
    @Test
    public void testExecuteInTransactionWithRollback() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        // First to allow to start the transaction, then to force to call rollback
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_MARKED_ROLLBACK);

        JTATransactionServiceImpl txService = spy(new JTATransactionServiceImpl(logger, txManager));

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
    public void testRegisterSynchronization() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        // First to allow to start the transaction
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        Transaction transaction = mock(Transaction.class);
        when(txManager.getTransaction()).thenReturn(transaction);

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);
        txService.begin();
        txService.registerBonitaSynchronization(mock(BonitaTransactionSynchronization.class));

        verify(transaction).registerSynchronization(any(Synchronization.class));
        assertEquals(1, txService.getBonitaSynchronizations().size());
    }

    @Test
    public void synchronization_executed_in_multithreaded_use_of_transaction_service() throws Exception {
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        TransactionManager txManager = mock(TransactionManager.class);

        // Allow both clients to start a transaction
        when(txManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION).thenReturn(Status.STATUS_NO_TRANSACTION);
        when(txManager.getTransaction()).thenReturn(mock(Transaction.class));

        JTATransactionServiceImpl txService = new JTATransactionServiceImpl(logger, txManager);

        CountDownLatch synchroRegistered = new CountDownLatch(1);
        CountDownLatch assertExecuted = new CountDownLatch(1);
        CountDownLatch secondTransactionStarted = new CountDownLatch(1);
        TransactionSynchroClient1 tc1 = new TransactionSynchroClient1(txService, secondTransactionStarted, synchroRegistered, assertExecuted);
        TransactionSynchroClient2 tc2 = new TransactionSynchroClient2(txService, secondTransactionStarted, synchroRegistered, assertExecuted);
        // Thread T1 : start tx
        // Thread T2 : start tx
        // Thread T1 : register a synchronization
        // => assert that T2 has no synchronization registered
        // Thread T2 : commit the transaction
        // => assert that the synchronization was not executed
        Thread t1 = new Thread(tc1);
        t1.setName("TransactionClient 1");
        Thread t2 = new Thread(tc2);
        t2.setName("TransactionClient 2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }


    class TransactionSynchroClient1 implements Runnable {

        private final TransactionService transactionService;
        private final CountDownLatch secondTransactionStarted;
        private final CountDownLatch synchroRegistered;
        private final CountDownLatch assertExecuted;

        public TransactionSynchroClient1(final TransactionService transactionService, final CountDownLatch secondTransactionStarted, final CountDownLatch synchroRegistered, final CountDownLatch assertExecuted) {
            this.transactionService = transactionService;
            this.secondTransactionStarted = secondTransactionStarted;
            this.synchroRegistered = synchroRegistered;
            this.assertExecuted = assertExecuted;
        }

        @Override
        public void run() {
            try {
                transactionService.begin();
                try {
                    secondTransactionStarted.await();

                    BonitaTransactionSynchronization txSync = mock(BonitaTransactionSynchronization.class);
                    transactionService.registerBonitaSynchronization(txSync);
                    // Ensure the synchro was registered in this thread
                    assertEquals(1, transactionService.getBonitaSynchronizations().size());
                    synchroRegistered.countDown();

                    assertExecuted.await();
                } finally {
                    transactionService.complete();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    class TransactionSynchroClient2 implements Runnable {

        private final TransactionService transactionService;
        private final CountDownLatch synchroRegistered;
        private final CountDownLatch assertExecuted;
        private final CountDownLatch secondTransactionStarted;

        public TransactionSynchroClient2(final TransactionService transactionService, final CountDownLatch secondTransactionStarted, final CountDownLatch synchroRegistered, final CountDownLatch assertExecuted) {
            this.transactionService = transactionService;
            this.secondTransactionStarted = secondTransactionStarted;
            this.synchroRegistered = synchroRegistered;
            this.assertExecuted = assertExecuted;
        }

        @Override
        public void run() {
            try {
                transactionService.begin();
                secondTransactionStarted.countDown();
                try {
                    synchroRegistered.await();

                    // Ensure there's no synchro in this thread
                    assertEquals(0, transactionService.getBonitaSynchronizations().size());
                    assertExecuted.countDown();
                } finally {
                    transactionService.complete();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
