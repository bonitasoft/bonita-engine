/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine;

import javax.naming.Context;

import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class CommonServiceTest {

    private static ServicesBuilder servicesBuilder;

    private static PlatformService platformService;

    private static TransactionService txService;

    private static SessionAccessor sessionAccessor;

    private static SessionService sessionService;

    private static long defaultTenantId;

    private final static Logger LOGGER = LoggerFactory.getLogger(CommonServiceTest.class);

    static ConfigurableApplicationContext springContext;

    private static boolean contextSpringLoaded = false;

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        /** set bonita.services.folder to target/test-classes/conf as it is done in pom.xml -> no need to edit test configuration */
        setSystemPropertyIfNotSet("bonita.services.folder", "target/test-classes/conf");

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    public static void closeSpringContext() {
        springContext.close();
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + d.getTestClass().getName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable e, final Description d) {
            LOGGER.info("Failed test: " + d.getTestClass().getName() + "." + d.getMethodName());
            LOGGER.info("-----------------------------------------------------------------------------------------------");
        }

        @Override
        public void succeeded(final Description d) {
            LOGGER.info("Succeeded test: " + d.getTestClass().getName() + "." + d.getMethodName());
            LOGGER.info("-----------------------------------------------------------------------------------------------");
        }
    };

    static {
        // Needs to be done before anything else:
        setupSpringContextIfNeeded();

        servicesBuilder = new ServicesBuilder();
        platformService = servicesBuilder.buildPlatformService();
        txService = servicesBuilder.buildTransactionService();
        sessionAccessor = servicesBuilder.buildSessionAccessor();
        sessionService = servicesBuilder.buildSessionService();
    }

    public static void setupSpringContextIfNeeded() {
        if (!CommonServiceTest.contextSpringLoaded) {
            setupSpringContext();
            contextSpringLoaded = true;
        }
    }

    @BeforeClass
    public static void initPlatform() throws Exception {
        setupSpringContextIfNeeded();
        defaultTenantId = TestUtil.createDefaultTenant(txService, platformService, sessionAccessor, sessionService);
    }

    @AfterClass
    public static void cleanPlatform() throws Exception {
        TestUtil.closeTransactionIfOpen(txService);
        TestUtil.deleteDefaultTenant(txService, platformService, sessionAccessor, sessionService);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(txService);
    }

    public static ServicesBuilder getServicesBuilder() {
        return servicesBuilder;
    }

    protected WorkService getWorkService() {
        return servicesBuilder.buildWorkService();
    }

    protected static PlatformService getPlatformService() {
        return platformService;
    }

    protected static TransactionService getTransactionService() {
        return txService;
    }

    protected static SessionAccessor getSessionAccessor() {
        return sessionAccessor;
    }

    protected static SessionService getSessionService() {
        return sessionService;
    }

    protected long getDefaultTenantId() {
        return defaultTenantId;
    }
}
