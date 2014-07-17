package org.bonitasoft.engine.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TransactionLifeCycleTest {

    TransactionService txService;

    protected abstract TransactionService getTxService() throws Exception;


    @Before
    public void before() throws Exception {
        txService = getTxService();
    }

    @After
    public void closeTransactions() throws Exception {
        // Do not forget to close the transaction
        if (txService.isTransactionActive()) {
            txService.complete();
        }

    }

    @Test
    public void testActiveTransactionState() throws Exception {
        txService.begin();
        assertEquals(TransactionState.ACTIVE, txService.getState());
    }

    @Test
    public void testCommitedTransactionState() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.complete();

        assertEquals(TransactionState.COMMITTED, sync.getTxCompletionState());
        assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
    }

    @Test
    public void testRollbackOnlyTransactionState() throws Exception {
        txService.begin();
        txService.setRollbackOnly();

        assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
        txService.complete();
    }

    @Test
    public void testRolledbackTransactionState() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.setRollbackOnly();
        txService.complete();

        assertEquals(TransactionState.ROLLEDBACK, sync.getTxCompletionState());
        assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
    }

    @Test
    public void testGetState1() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        assertEquals(TransactionState.ACTIVE, txService.getState());

        txService.setRollbackOnly();
        assertEquals(TransactionState.ROLLBACKONLY, txService.getState());

        txService.complete();
        assertEquals(TransactionState.ROLLEDBACK, sync.getTxCompletionState());
        assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
    }

    @Test
    public void testGetState2() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        assertEquals(TransactionState.ACTIVE, txService.getState());

        txService.complete();
        assertEquals(TransactionState.COMMITTED, sync.getTxCompletionState());
        assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
    }

    @Test
    public void testTransactionIsRollbackOnly() throws Exception {
        txService.begin();
        txService.setRollbackOnly();
        assertTrue(txService.isRollbackOnly());
        txService.complete();
    }

    @Test
    public void testSetRollbackOnlyOnActiveTx() throws Exception {
        txService.begin();
        txService.setRollbackOnly();
        assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
        txService.complete();
    }

    @Test
    public void testSetRollbackOnlyOnCommitedTx() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.complete();
        try {
            txService.setRollbackOnly();
            fail("Impossible to call setRollbackOnly on a tx with state " + TransactionState.COMMITTED);
        } catch (final STransactionException e) {
            assertEquals(TransactionState.COMMITTED, sync.getTxCompletionState());
            assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
        }
    }

    @Test
    public void testBeginActiveTx() throws Exception {
        txService.begin();
        try {
            txService.begin();
            fail("Impossible to begin a tx with state " + TransactionState.ACTIVE);
        } catch (final STransactionCreationException e) {
            assertEquals(TransactionState.ACTIVE, txService.getState());
        }
    }

    @Test
    public void testBeginRollebackOnlyTx() throws Exception {
        txService.begin();
        txService.setRollbackOnly();
        try {
            txService.begin();
            System.out.println("++++" + txService.getNumberOfActiveTransactions());
            fail("Impossible to begin a tx with state " + TransactionState.ROLLBACKONLY);
        } catch (final STransactionCreationException e) {
            assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
        } finally {
            txService.complete();
        }
    }

    // This is a dumb implementation of the BonitaTransactionSynchronization interface just to
    // keep a reference to transaction completion's state.
    private class TxSync implements BonitaTransactionSynchronization {

        private TransactionState txCompletionState;

        public TxSync() {
        }

        public TransactionState getTxCompletionState() {
            return txCompletionState;
        }

        @Override
        public void beforeCommit() {
            // Nothig to do
        }

        @Override
        public void afterCompletion(final TransactionState txState) {
            this.txCompletionState = txState;
        }
    }

}
