package org.bonitasoft.engine;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.scheduler.SchedulerService;
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

    private static SchedulerService schedulerService;

    static {
        CommonServiceTest.setupSpringContextIfNeeded();
        servicesBuilder = new ServicesBuilder();
        transactionService = servicesBuilder.buildTransactionService();
        platformService = servicesBuilder.buildPlatformService();
        schedulerService = servicesBuilder.buildSchedulerService();
    }

    @BeforeClass
    public static void setUpPlatform() throws Exception {
        System.err.println("=================== AllTestsWithJNDI.beforeClass()");
        TestUtil.createPlatform(transactionService, platformService);
    }

    @AfterClass
    public static void tearDownPlatform() throws Exception {
        System.err.println("=================== AllTestsWithJNDI.afterClass()");
        TestUtil.deletePlatForm(transactionService, platformService, schedulerService);
        CommonServiceTest.closeSpringContext();
    }

}
