/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import javax.naming.Context;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Suite.class)
@SuiteClasses({
        BPMSPTests.class,
        BPMLocalSuiteTests.class,
        LocalLogTest.class,
        APIMethodSPTest.class,
        ConnectorExecutionTimeOutTest.class,
        ConnectorImplementationLocalSPTest.class
})
public class LocalIntegrationTestsSP {

    static ConfigurableApplicationContext springContext;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.beforeClass()");

        setupSpringContext();
        try {
            APITestSPUtil.createPlatformStructure();
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(LocalIntegrationTestsSP.class);
            logger.error("unable to create platform", e);
            final PlatformSession session = APITestSPUtil.loginPlatform();
            final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
            platformAPI.stopNode();
            platformAPI.cleanPlatform();
            APITestSPUtil.deletePlatformStructure();
            APITestSPUtil.createPlatformStructure();
        }
        System.setProperty("delete.job.frequency", "0/30 * * * * ?");
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        System.err.println("=================== LocalIntegrationTestsSP.afterClass()");

        APITestSPUtil.deletePlatformStructure();
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
