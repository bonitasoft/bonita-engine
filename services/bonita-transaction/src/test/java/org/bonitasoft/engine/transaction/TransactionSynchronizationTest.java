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

import static org.junit.Assert.*;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.transaction.synchronization.SimpleSynchronization;
import org.bonitasoft.engine.transaction.synchronization.StaticSynchronization;
import org.bonitasoft.engine.transaction.synchronization.StaticSynchronizationResult;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionSynchronizationTest {

    private static TransactionManager transactionManager;
    private TransactionService txService;

    @BeforeClass
    public static void setupTransactionManager() {
        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Before
    public void before() {
        txService = new JTATransactionServiceImpl(new TechnicalLoggerSLF4JImpl(), transactionManager);
    }

    @After
    public void closeTransactions() throws Exception {
        if (txService.isTransactionActive()) {
            txService.complete();
        }
    }

    @Test
    public void testSimpleRegisterSynchronization() throws Exception {
        txService.begin();

        final SimpleSynchronization simpleSynchronization = new SimpleSynchronization();

        txService.registerBonitaSynchronization(simpleSynchronization);

        assertFalse(simpleSynchronization.isBeforeCompletion());
        assertFalse(simpleSynchronization.isAfterCompletion());
        assertEquals(Status.STATUS_NO_TRANSACTION, simpleSynchronization.getAfterCompletionStatus());

    }

    private void testSynchronizationStatus(final boolean rollback, final int expectedStatus)
            throws Exception {
        txService.begin();

        final SimpleSynchronization[] synchronizations = new SimpleSynchronization[] { new SimpleSynchronization(),
                new SimpleSynchronization() };

        for (final SimpleSynchronization sync : synchronizations) {
            txService.registerBonitaSynchronization(sync);
        }

        for (final SimpleSynchronization sync : synchronizations) {
            assertFalse(sync.isBeforeCompletion());
            assertFalse(sync.isAfterCompletion());
            assertEquals(Status.STATUS_NO_TRANSACTION, sync.getAfterCompletionStatus());
        }

        if (rollback) {
            txService.setRollbackOnly();
        }
        txService.complete();

        for (final SimpleSynchronization sync : synchronizations) {
            assertEquals(!rollback, sync.isBeforeCompletion());
            assertTrue(sync.isAfterCompletion());
            assertEquals(expectedStatus, sync.getAfterCompletionStatus());
        }
    }

    @Test
    public void testSynchronizationStatusOnCommit() throws Exception {
        testSynchronizationStatus(false, Status.STATUS_COMMITTED);
    }

    @Test
    public void testSynchronizationStatusOnRollback() throws Exception {
        testSynchronizationStatus(true, Status.STATUS_ROLLEDBACK);
    }

    private void testRegisteredSynchronizationsOrder(final boolean rollback,
            final StaticSynchronization... synchronizations) throws Exception {
        // in fact the same on commit or rollback... Synchronizations are always called
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

    private void testRegisteredSynchronizationsOrderOnFailure(final boolean rollback, final boolean failOnBefore,
            final boolean failOnAfter) throws Exception {
        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1, failOnBefore, failOnAfter),
                new StaticSynchronization(2),
                new StaticSynchronization(3));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1), new StaticSynchronization(2,
                failOnBefore, failOnAfter),
                new StaticSynchronization(3));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1), new StaticSynchronization(2),
                new StaticSynchronization(3, failOnBefore,
                        failOnAfter));

        testRegisteredSynchronizationsOrder(rollback, new StaticSynchronization(1, failOnBefore, failOnAfter),
                new StaticSynchronization(2, failOnBefore,
                        failOnAfter),
                new StaticSynchronization(3, failOnBefore, failOnAfter));

    }

    @Test(expected = STransactionCommitException.class)
    public void testRegisteredSynchronizationsOrderOnFailureDuringBeforeCompletionOnCommit() throws Exception {
        testRegisteredSynchronizationsOrderOnFailure(false, true, false);
    }

    @Test(expected = STransactionCommitException.class)
    public void testRegisteredSynchronizationsOrderOnFailureDuringBeforeAndAfterCompletionOnCommit() throws Exception {
        testRegisteredSynchronizationsOrderOnFailure(false, true, true);
    }

}
