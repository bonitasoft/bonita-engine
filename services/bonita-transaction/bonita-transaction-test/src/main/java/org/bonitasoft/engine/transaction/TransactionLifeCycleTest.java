package org.bonitasoft.engine.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Test;

public abstract class TransactionLifeCycleTest {

    protected abstract TransactionService getTxService() throws Exception;

    @After
    public void closeTransactions() throws Exception {
        TestUtil.closeTransactionIfOpen(getTxService());
    }

    @Test
    public void testActiveTransactionState() throws Exception {
        final TransactionService txService = getTxService();

        txService.begin();
        assertEquals(TransactionState.ACTIVE, txService.getState());
    }

    @Test
    public void testCommitedTransactionState() throws Exception {
        final TransactionService txService = getTxService();

        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.complete();

        assertEquals(TransactionState.COMMITTED, sync.getTxCompletionState());
        assertEquals(TransactionState.NO_TRANSACTION, txService.getState());
    }

    @Test
    public void testRollbackOnlyTransactionState() throws Exception {
        final TransactionService txService = getTxService();

        txService.begin();
        txService.setRollbackOnly();

        assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
    }

    @Test
    public void testRolledbackTransactionState() throws Exception {
        final TransactionService txService = getTxService();

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
        final TransactionService txService = getTxService();

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
        final TransactionService txService = getTxService();

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
        final TransactionService txService = getTxService();

        txService.begin();
        txService.setRollbackOnly();
        assertTrue(txService.isRollbackOnly());
    }

    @Test
    public void testSetRollbackOnlyOnActiveTx() throws Exception {
        final TransactionService txService = getTxService();

        txService.begin();
        txService.setRollbackOnly();
        assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
    }

    @Test
    public void testSetRollbackOnlyOnCommitedTx() throws Exception {
        final TransactionService txService = getTxService();

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
        final TransactionService txService = getTxService();

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
        final TransactionService txService = getTxService();

        txService.begin();
        txService.setRollbackOnly();
        try {
            txService.begin();
            fail("Impossible to begin a tx with state " + TransactionState.ROLLBACKONLY);
        } catch (final STransactionCreationException e) {
            assertEquals(TransactionState.ROLLBACKONLY, txService.getState());
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
        public void afterCompletion(TransactionState txState) {
            this.txCompletionState = txState;
        }
    }

}
