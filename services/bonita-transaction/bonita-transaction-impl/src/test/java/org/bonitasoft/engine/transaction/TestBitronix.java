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
