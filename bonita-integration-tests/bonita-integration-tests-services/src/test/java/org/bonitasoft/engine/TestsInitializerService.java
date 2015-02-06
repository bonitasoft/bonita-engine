/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.util.PlatformUtil;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestsInitializerService {

    private static final String BONITA_SERVICES_FOLDER_DEFAULT_PATH = "target/conf";

    private static final String BONITA_SERVICES_FOLDER_PROPERTY = "bonita.services.folder";

    private static ServicesBuilder servicesBuilder;

    private static TransactionService transactionService;

    private static PlatformService platformService;

    private static SchedulerService schedulerService;

    static ConfigurableApplicationContext springContext;

    private static boolean contextSpringLoaded = false;

    private static TestsInitializerService INSTANCE;

    private static TestsInitializerService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestsInitializerService();
        }
        return INSTANCE;
    }

    public static void beforeAll() throws Exception {
        TestsInitializerService.getInstance().before();
    }

    public static void afterAll() throws Exception {
        TestsInitializerService.getInstance().after();

    }

    protected void before() throws Exception {
        System.out.println("=====================================================");
        System.out.println("=========  INITIALIZATION OF TEST ENVIRONMENT =======");
        System.out.println("=====================================================");

        final long startTime = System.currentTimeMillis();

        setupSpringContextIfNeeded();
        servicesBuilder = new ServicesBuilder();
        transactionService = servicesBuilder.buildTransactionService();
        platformService = servicesBuilder.buildPlatformService();
        schedulerService = servicesBuilder.buildSchedulerService();

        try {
            TestUtil.createPlatform(transactionService, platformService);
        } catch (final Exception e) {
            PlatformUtil.deletePlatform(servicesBuilder.buildTransactionService(), platformService);
            TestUtil.createPlatform(transactionService, platformService);
        }
        System.out.println("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    protected void after() throws Exception {
        System.out.println("=====================================================");
        System.out.println("============ CLEANING OF TEST ENVIRONMENT ===========");
        System.out.println("=====================================================");

        TestUtil.deletePlatForm(transactionService, platformService, schedulerService);
        closeSpringContext();
    }

    /**
     * @since 6.4.0
     */
    private void setupSpringContextIfNeeded() {
        if (!contextSpringLoaded) {
            setupSpringContext();
            contextSpringLoaded = true;
        }
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        /** set bonita.services.folder to target/test-classes/conf as it is done in pom.xml -> no need to edit test configuration */
        setSystemPropertyIfNotSet(BONITA_SERVICES_FOLDER_PROPERTY, BONITA_SERVICES_FOLDER_DEFAULT_PATH);
        setSystemPropertyIfNotSet("bonita.home", "target");

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

}
