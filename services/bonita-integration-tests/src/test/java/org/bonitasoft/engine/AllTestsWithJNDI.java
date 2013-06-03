package org.bonitasoft.engine;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.SPlatformBuilder;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// FIXME add paltformtest suite
// FIXME abstract impl test suites
@RunWith(Suite.class)
@SuiteClasses({ AllTests.class })
public class AllTestsWithJNDI {

    private static ServicesBuilder servicesBuilder;

    private static TransactionService transactionService;

    private static PlatformService platformService;

    private static SPlatformBuilder platformBuilder;

    static {
        CommonServiceTest.setupSpringContextIfNeeded();
        servicesBuilder = new ServicesBuilder();
        transactionService = servicesBuilder.buildTransactionService();
        platformService = servicesBuilder.buildPlatformService();
        platformBuilder = servicesBuilder.buildPlatformBuilder();
    }

    @BeforeClass
    public static void setUpPlatform() throws Exception {
        System.err.println("=================== AllTestsWithJNDI.beforeClass()");
        TestUtil.createPlatform(transactionService, platformService, platformBuilder);
    }

    @AfterClass
    public static void tearDownPlatform() throws Exception {
        System.err.println("=================== AllTestsWithJNDI.afterClass()");
        TestUtil.deletePlatForm(transactionService, platformService);
        CommonServiceTest.closeSpringContext();
    }

}
