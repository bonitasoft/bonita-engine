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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.transaction.synchronization.SimpleSynchronization;
import org.bonitasoft.engine.transaction.synchronization.StaticSynchronization;
import org.bonitasoft.engine.transaction.synchronization.StaticSynchronizationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TransactionSynchronizationTest {

    TransactionService txService;

    protected abstract TransactionService getTxService() throws Exception;

    @Before
    public void before() throws Exception {
        txService = getTxService();
    }

    @SuppressWarnings("deprecation")
    @After
    public void closeTransactions() throws Exception {
        if (txService.isTransactionActive()) {
            txService.complete();
        }
    }

    // @Test
    // public void testGetRegisteredSynchronizations() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // final SimpleSynchronization sync1 = new SimpleSynchronization();
    // final SimpleSynchronization sync2 = new SimpleSynchronization();
    // final SimpleSynchronization sync3 = new SimpleSynchronization();
    //
    // txService.registerSynchronization(sync1);
    // txService.registerSynchronization(sync2);
    // txService.registerSynchronization(sync3);
    //
    // final List<Synchronization> expected = new Vector<Synchronization>();
    // expected.add(sync1);
    // expected.add(sync2);
    // expected.add(sync3);
    // assertEquals(expected, txService.getRegisteredSynchronizations());
    // }

    // @Test
    // public void testGetEmptyRegisteredSynchronizations() throws Exception {
    // final TransactionService txService = getTxService();
    //
    // txService.begin();
    // assertTrue(txService.getRegisteredSynchronizations().size() == 0);
    // }

    @Test
    public void testSimpleRegisterSynchronization() throws Exception {
        txService.begin();

        final SimpleSynchronization simpleSynchronization = new SimpleSynchronization();

        txService.registerBonitaSynchronization(simpleSynchronization);

        assertFalse(simpleSynchronization.isBeforeCompletion());
        assertFalse(simpleSynchronization.isAfterCompletion());
        assertEquals(TransactionState.NO_TRANSACTION, simpleSynchronization.getAfterCompletionStatus());

    }

    private void testSynchronizationStatus(final boolean rollback, final TransactionState expectedStatus) throws Exception {
        txService.begin();

        final SimpleSynchronization[] synchs = new SimpleSynchronization[] { new SimpleSynchronization(), new SimpleSynchronization() };

        for (final SimpleSynchronization sync : synchs) {
            txService.registerBonitaSynchronization(sync);
        }

        for (final SimpleSynchronization sync : synchs) {
            assertFalse(sync.isBeforeCompletion());
            assertFalse(sync.isAfterCompletion());
            assertEquals(TransactionState.NO_TRANSACTION, sync.getAfterCompletionStatus());
        }

        if (rollback) {
            txService.setRollbackOnly();
        }
        txService.complete();

        for (final SimpleSynchronization sync : synchs) {
            assertEquals(!rollback, sync.isBeforeCompletion());
            assertTrue(sync.isAfterCompletion());
            assertEquals(expectedStatus, sync.getAfterCompletionStatus());
        }
    }

    @Test
    public void testSynchronizationStatusOnCommit() throws Exception {
        testSynchronizationStatus(false, TransactionState.COMMITTED);
    }

    @Test
    public void testSynchronizationStatusOnRollback() throws Exception {
        testSynchronizationStatus(true, TransactionState.ROLLEDBACK);
    }

    private void testRegisteredSynchronizationsOrder(final boolean rollback, final StaticSynchronization... synchronizations) throws Exception {
        // in fact the same on commit or rollback... Synchronizations are always
        // called
        txService.begin();

        StaticSynchronizationResult.reset();

        final StringBuilder beforeBuilder = new StringBuilder();
        final StringBuilder afterBuilder = new StringBuilder();
        for (final StaticSynchronization synchronization : synchronizations) {
            txService.registerBonitaSynchronization(synchronization);

            if (!rollback) {
                beforeBuilder.append(synchronization.getBeforeCompletionComment());
            }
            afterBuilder.append(synchronization.getAfterCompletionComment());
        }

        final String expected = beforeBuilder.toString() + afterBuilder.toString();

        if (rollback) {
            txService.setRollbackOnly();
        }
        txService.complete();

        assertEquals(expected, StaticSynchronizationResult.COMMENT);
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnCommit() throws Exception {
        testRegisteredSynchronizationsOrder(false, new StaticSynchronization(1), new StaticSynchronization(2), new StaticSynchronization(3));
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnRollback() throws Exception {
        testRegisteredSynchronizationsOrder(true, new StaticSynchronization(1), new StaticSynchronization(2), new StaticSynchronization(3));
    }

    private void testRegisteredSynchronizationsOrderOnfailure(final boolean rollback, final boolean failOnBefore, final boolean failOnAfter) throws Exception {
        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1, failOnBefore, failOnAfter), new StaticSynchronization(2),
                new StaticSynchronization(3));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1), new StaticSynchronization(2, failOnBefore, failOnAfter),
                new StaticSynchronization(3));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1), new StaticSynchronization(2), new StaticSynchronization(3, failOnBefore,
                failOnAfter));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1, failOnBefore, failOnAfter), new StaticSynchronization(2, failOnBefore,
                failOnAfter), new StaticSynchronization(3, failOnBefore, failOnAfter));

    }

    @Test(expected = STransactionCommitException.class)
    public void testRegisteredSynchronizationsOrderOnfailureDuringBeforeCompletionOnCommit() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(false, true, false);
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnfailureDuringBeforeCompletionOnRollback() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(true, true, false);
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnfailureDuringAfterCompletionOnCommit() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(false, false, true);
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnfailureDuringAfterCompletionOnRollback() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(true, false, true);
    }

    @Test(expected = STransactionCommitException.class)
    public void testRegisteredSynchronizationsOrderOnfailureDuringBeforeAndAfterCompletionOnCommit() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(false, true, true);
    }

    @Test
    public void testRegisteredSynchronizationsOrderOnfailureDuringBeforeAndAfterCompletionOnRollback() throws Exception {
        testRegisteredSynchronizationsOrderOnfailure(true, true, true);
    }

}
