/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.test;

import static org.bonitasoft.engine.bpm.CommonBPMServicesTest.platformServiceAccessor;

import javax.naming.Context;

import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.AllBPMTests;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Suite.class)
@SuiteClasses({
    ParameterAndDataExpressionIntegrationTest.class,
    AllBPMTests.class,
})
public class AllBPMSPTestsWithJNDI {

    static ConfigurableApplicationContext springContext;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.err.println("=================== AllBPMSPTestsWithJNDI.beforeClass()");
        setupSpringContext();

        final PlatformAPIImpl platformAPI = new PlatformAPIImpl();
        platformServiceAccessor().getSessionAccessor().setSessionInfo(1l, -1);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.err.println("=================== AllBPMSPTestsWithJNDI.afterClass()");

        TransactionService transactionService = platformServiceAccessor().getTransactionService();
        TestUtil.closeTransactionIfOpen(transactionService);
        // stopScheduler();
        PlatformService platformService = platformServiceAccessor().getPlatformService();
        SessionAccessor sessionAccessor = platformServiceAccessor().getSessionAccessor();
        TestUtil.deleteDefaultTenantAndPlatForm(transactionService, platformService, sessionAccessor, platformServiceAccessor().getSessionService());

        closeSpringContext();
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    private static void closeSpringContext() {
        springContext.close();
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }
}
