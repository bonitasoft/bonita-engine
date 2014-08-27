/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services;

import org.bonitasoft.engine.AllTests;
import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.ServicesBuilder;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.services.event.handler.IdentityServiceUsingEventServiceTest;
import com.bonitasoft.services.event.handler.RecorderAndEventServiceTest;
import com.bonitasoft.services.monitoring.MonitoringTests;

@RunWith(Suite.class)
@SuiteClasses({
    AllTests.class,
    IdentityServiceUsingEventServiceTest.class,
    RecorderAndEventServiceTest.class,
    // Last test suite in order to check the correct begin/complete transactions
    MonitoringTests.class
})
public class AllTestsSPWithJNDI {

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
    public static void beforeClass() throws Exception {
        System.err.println("=================== AllTestsSPWithJNDI.beforeClass()");
        CommonServiceTest.setupSpringContextIfNeeded();
        TestUtil.createPlatform(transactionService, platformService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.err.println("=================== AllTestsSPWithJNDI.afterClass()");
        TestUtil.deletePlatForm(transactionService, platformService, schedulerService);
        CommonServiceTest.closeSpringContext();
    }

}
