package com.bonitasoft.engine.bpm.test;

import javax.naming.Context;

import org.bonitasoft.engine.bpm.test.AllBPMTests;
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
    public static void beforeClass() {
        System.err.println("=================== AllBPMTests.beforeClass()");
        setupSpringContext();
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== AllBPMTests.afterClass()");

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
