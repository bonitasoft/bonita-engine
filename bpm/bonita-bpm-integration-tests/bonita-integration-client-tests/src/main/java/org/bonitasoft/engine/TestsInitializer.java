package org.bonitasoft.engine;

import java.io.File;
import java.io.IOException;

import javax.naming.Context;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.APITestUtil;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestsInitializer {

    private static final String TMP_BONITA_HOME = "target/eclipse-bonita-home";

    private static final String BONITA_HOME = "bonita.home";

    static ConfigurableApplicationContext springContext;

    private static TestsInitializer INSTANCE;

    public static void beforeAll() throws Exception {
        TestsInitializer.getInstance().before();
    }

    private static TestsInitializer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TestsInitializer();
        }
        return INSTANCE;
    }

    public static void afterAll() throws Exception {
        TestsInitializer.getInstance().after();

    }

    protected void after() throws Exception {
        System.out.println("=====================================================");
        System.out.println("============ CLEANING OF TEST ENVIRONMENT ===========");
        System.out.println("=====================================================");

        deleteTenantAndPlatform();
        closeSpringContext();
        cleanBonitaHome();
    }

    protected void deleteTenantAndPlatform() throws BonitaException {
        new APITestUtil().stopAndCleanPlatformAndTenant(true);
        new APITestUtil().deletePlatformStructure();
    }

    protected void before() throws Exception {
        System.out.println("=====================================================");
        System.out.println("=========  INITIALIZATION OF TEST ENVIRONMENT =======");
        System.out.println("=====================================================");
        long startTime = System.currentTimeMillis();
        setupBonitaHome();
        setupSpringContext();
        initPlatformAndTenant();
        System.out.println("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    protected void initPlatformAndTenant() throws Exception {
        new APITestUtil().createPlatformStructure();
        new APITestUtil().initializeAndStartPlatformWithDefaultTenant(true);
    }

    private static void setupBonitaHome() throws IOException {
        if (System.getProperties().toString().contains("org.eclipse.osgi")) {
            final String bonitaHome = System.getProperty(BONITA_HOME);
            if (bonitaHome == null) {
                throw new IllegalStateException("variable 'bonita.home' must be set");
            }
            final File destDir = new File(TMP_BONITA_HOME);
            FileUtils.deleteDirectory(destDir);
            FileUtils.copyDirectory(new File(bonitaHome), destDir);
            System.setProperty(BONITA_HOME, destDir.getAbsolutePath());
        }
    }

    private static void cleanBonitaHome() throws IOException {
        if (System.getProperties().toString().contains("org.eclipse.osgi")) {
            FileUtils.deleteDirectory(new File(TMP_BONITA_HOME));
        }
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
