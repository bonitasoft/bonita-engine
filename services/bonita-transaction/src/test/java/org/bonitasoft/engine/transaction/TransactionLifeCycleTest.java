/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.transaction;

import static javax.transaction.Status.STATUS_COMMITTED;
import static javax.transaction.Status.STATUS_ROLLEDBACK;
import static org.junit.Assert.*;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionLifeCycleTest {

    private static TransactionManager transactionManager;
    private TransactionService txService;

    @BeforeClass
    public static void setupTransactionManager() {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Before
    public void before() {
        txService = new JTATransactionServiceImpl(transactionManager);
    }

    @SuppressWarnings("deprecation")
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
        assertEquals(Status.STATUS_ACTIVE, transactionManager.getStatus());
    }

    @Test
    public void testCommittedTransactionState() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.complete();

        assertEquals(STATUS_COMMITTED, sync.getTxCompletionState());
        assertEquals(Status.STATUS_NO_TRANSACTION, transactionManager.getStatus());
    }

    @Test
    public void testRollbackOnlyTransactionState() throws Exception {
        txService.begin();
        txService.setRollbackOnly();

        assertEquals(Status.STATUS_MARKED_ROLLBACK, transactionManager.getStatus());
        txService.complete();
    }

    @Test
    public void testRolledbackTransactionState() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        txService.setRollbackOnly();
        txService.complete();

        assertEquals(STATUS_ROLLEDBACK, sync.getTxCompletionState());
        assertEquals(Status.STATUS_NO_TRANSACTION, transactionManager.getStatus());
    }

    @Test
    public void testGetState1() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        assertEquals(Status.STATUS_ACTIVE, transactionManager.getStatus());

        txService.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, transactionManager.getStatus());

        txService.complete();
        assertEquals(STATUS_ROLLEDBACK, sync.getTxCompletionState());
        assertEquals(Status.STATUS_NO_TRANSACTION, transactionManager.getStatus());
    }

    @Test
    public void testGetState2() throws Exception {
        TxSync sync = new TxSync();
        txService.begin();
        txService.registerBonitaSynchronization(sync);
        assertEquals(Status.STATUS_ACTIVE, transactionManager.getStatus());

        txService.complete();
        assertEquals(STATUS_COMMITTED, sync.getTxCompletionState());
        assertEquals(Status.STATUS_NO_TRANSACTION, transactionManager.getStatus());
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
        assertEquals(Status.STATUS_MARKED_ROLLBACK, transactionManager.getStatus());
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
            fail("Impossible to call setRollbackOnly on a tx with state COMMITTED");
        } catch (final STransactionException e) {
            assertEquals(STATUS_COMMITTED, sync.getTxCompletionState());
            assertEquals(Status.STATUS_NO_TRANSACTION, transactionManager.getStatus());
        }
    }

    @Test
    public void testBeginActiveTx() throws Exception {
        txService.begin();
        try {
            txService.begin();
            fail("Impossible to begin a tx with state ACTIVE");
        } catch (final STransactionCreationException e) {
            assertEquals(Status.STATUS_ACTIVE, transactionManager.getStatus());
        }
    }

    @Test
    public void testBeginRollbackOnlyTx() throws Exception {
        txService.begin();
        txService.setRollbackOnly();
        try {
            txService.begin();
            System.out.println("++++" + txService.getNumberOfActiveTransactions());
            fail("Impossible to begin a tx with state ROLLBACKONLY");
        } catch (final STransactionCreationException e) {
            assertEquals(Status.STATUS_MARKED_ROLLBACK, transactionManager.getStatus());
        } finally {
            txService.complete();
        }
    }

    // This is a dumb implementation of the BonitaTransactionSynchronization interface just to
    // keep a reference to transaction completion's state.
    private static class TxSync implements BonitaTransactionSynchronization {

        private int txCompletionState;

        public TxSync() {
        }

        public int getTxCompletionState() {
            return txCompletionState;
        }

        @Override
        public void afterCompletion(final int txState) {
            this.txCompletionState = txState;
        }
    }

}
