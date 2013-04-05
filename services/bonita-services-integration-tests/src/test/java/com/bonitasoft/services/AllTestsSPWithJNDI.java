/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services;

import javax.naming.Context;                                                                                                      

import org.bonitasoft.engine.AllTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bonitasoft.services.event.handler.IdentityServiceUsingEventServiceTest;
import com.bonitasoft.services.event.handler.RecorderAndEventServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
    AllTests.class,
    IdentityServiceUsingEventServiceTest.class,
    RecorderAndEventServiceTest.class
})
public class AllTestsSPWithJNDI {

    static ConfigurableApplicationContext springContext;

    @BeforeClass
    public static void beforeClass() {
        System.err.println("=================== AllTestsSP.beforeClass()");
        setupSpringContext();
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== AllTestsSP.afterClass()");

        closeSpringContext();
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("bonita.test.db.vendor", "h2");

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
