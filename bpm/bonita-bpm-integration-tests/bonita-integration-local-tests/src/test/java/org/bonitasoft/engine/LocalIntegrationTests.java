package org.bonitasoft.engine;

import java.io.File;
import java.io.IOException;

import javax.naming.Context;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.APIMethodTest;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Suite.class)
@SuiteClasses({
        BPMLocalSuiteTests.class,
        BPMRemoteTests.class,
        APIMethodTest.class
})
public class LocalIntegrationTests {

    private static final String TMP_BONITA_HOME = "target/eclipse-bonita-home";

    private static final String BONITA_HOME = "bonita.home";

    static ConfigurableApplicationContext springContext;

    @BeforeClass
    public static void beforeClass() throws BonitaException, IOException {
        System.err.println("=================== LocalIntegrationTests.beforeClass()");
        setupBonitaHome();
        setupSpringContext();

        APITestUtil.createPlatformStructure();
    }

    private static void setupBonitaHome() throws IOException {
        if (System.getProperties().toString().contains("org.eclipse.osgi")) {
            final String bonitaHome = System.getProperty(BONITA_HOME);
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

    @AfterClass
    public static void afterClass() throws BonitaException, IOException {
        System.err.println("=================== LocalIntegrationTests.afterClass()");

        APITestUtil.deletePlatformStructure();

        closeSpringContext();
        cleanBonitaHome();
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
