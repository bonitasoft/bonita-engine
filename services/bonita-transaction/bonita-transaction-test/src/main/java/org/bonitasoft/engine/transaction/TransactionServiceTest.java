package org.bonitasoft.engine.transaction;

import org.junit.Test;

public abstract class TransactionServiceTest {

    protected abstract TransactionService getTxService() throws Exception;

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithActiveTx() throws Exception {
        final TransactionService txService = this.getTxService();

        txService.begin();
        txService.begin();
    }

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithCreatedTx() throws Exception {
        final TransactionService txService = this.getTxService();
        txService.begin();
        txService.begin();
    }

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithRollbackOnlyTx() throws Exception {
        final TransactionService txService = this.getTxService();

        txService.begin();
        txService.setRollbackOnly();
        txService.begin();
    }

}
