package org.bonitasoft.engine.transaction;

import static org.mockito.Mockito.mock;

import javax.transaction.TransactionManager;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.Configuration;
import bitronix.tm.TransactionManagerServices;

public class BitronixTransactionTestDelegate {

    private BitronixTransactionManager transactionManager;
    private TechnicalLoggerService techLoggerService;

    public void setUpBitronixTransactionManager() {
        final Configuration conf = TransactionManagerServices.getConfiguration();
        // TODO Make the following configurable
        conf.setServerId("jvm-1");
        conf.setJournal(null); // Disable the journal for the tests.

        transactionManager = TransactionManagerServices.getTransactionManager();

        techLoggerService = mock(TechnicalLoggerService.class);
    }

    public void stopBitronixTransactionManager() {
        transactionManager.shutdown();
    }

    protected TransactionService getTxService() {
        return new JTATransactionServiceImpl(getLoggerService(), getTransactionManager());
    }

    private TransactionManager getTransactionManager() {
        return transactionManager;
    }

    private TechnicalLoggerService getLoggerService() {
        return techLoggerService;
    }
}
