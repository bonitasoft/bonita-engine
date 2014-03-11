package org.bonitasoft.engine.transaction;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TransactionLifeCycleImplTest extends TransactionLifeCycleTest {

    static BitronixTransactionTestDelegate delegate;

    @BeforeClass
    public static void setUpBitronixTransactionManager() {
        delegate = new BitronixTransactionTestDelegate();
        delegate.setUpBitronixTransactionManager();
    }

    @AfterClass
    public static void stopBitronixTransactionManager() {
        delegate.stopBitronixTransactionManager();
    }

    @Override
    protected TransactionService getTxService() {
        return delegate.getTxService();
    }

}
