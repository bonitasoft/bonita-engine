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

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import org.junit.Test;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.internal.BitronixRollbackException;

public class TestBitronix {

    @Test(expected=BitronixRollbackException.class)
    public void testSetupTransactionManager() throws Exception {
        Configuration conf = TransactionManagerServices.getConfiguration();
        conf.setServerId("jvm-1");
        conf.setJournal(null); // Disable the journal for the tests.

        TransactionManager tm = TransactionManagerServices.getTransactionManager();
        try {
            tm.begin();
            tm.getTransaction().registerSynchronization(new Sync());
            tm.setRollbackOnly();
            tm.getTransaction().registerSynchronization(new Sync());
        } finally {
            tm.rollback();
            ((BitronixTransactionManager) tm).shutdown();
        }
    }

    public static class Sync implements Synchronization {

        @Override
        public void beforeCompletion() {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterCompletion(final int status) {
            // TODO Auto-generated method stub

        }

    }
}
