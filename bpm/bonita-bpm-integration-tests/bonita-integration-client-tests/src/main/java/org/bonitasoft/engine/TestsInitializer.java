package org.bonitasoft.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        // wait for thread to stop
        // FIXME To uncomment when fix BS-7731
        checkThreadsAreStopped();
    }

    protected void deleteTenantAndPlatform() throws BonitaException {
        new APITestUtil().stopAndCleanPlatformAndTenant(true);
        new APITestUtil().deletePlatformStructure();
    }

    private void checkThreadsAreStopped() throws InterruptedException {
        System.out.println("Checking if all Threads are stopped");
        Set<Thread> keySet = Thread.getAllStackTraces().keySet();
        Iterator<Thread> iterator = keySet.iterator();
        ArrayList<Thread> list = new ArrayList<Thread>();
        while (iterator.hasNext()) {
            Thread thread = iterator.next();
            if (isEngine(thread)) {
                // wait for the thread to die
                thread.join(5000);
                // if still alive print it
                if (thread.isAlive()) {
                    list.add(thread);
                }
            }
        }
        if (!list.isEmpty()) {
            throw new IllegalStateException("Some threads are still active : " + list);
        }
        System.out.println("All engine threads are stopped properly");
    }

    private boolean isEngine(final Thread thread) {
        String name = thread.getName();
        ThreadGroup threadGroup = thread.getThreadGroup();
        if (threadGroup != null && threadGroup.getName().equals("system")) {
            return false;
        }
        List<String> startWithFilter = Arrays.asList("H2 ", "Timer-0" /* postgres driver related */, "BoneCP", "bitronix", "main", "Reference Handler",
                "Signal Dispatcher", "Finalizer", "com.google.common.base.internal.Finalizer"/* guava, used by bonecp */, "process reaper", "ReaderThread",
                "Abandoned connection cleanup thread", "AWT-AppKit"/* bonecp related */);
        for (String prefix : startWithFilter) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        return true;
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
            System.out.println("Using BONITA_HOME: " + destDir.getAbsolutePath());
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

    private void closeSpringContext() {
        try {
            // if in local we try to unload engine
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> initializerClass = contextClassLoader.loadClass(getInitializerListenerClassName());
            initializerClass.getMethod("unload").invoke(null);
        } catch (final Exception e) {
            System.out.println("Unable to execute the unload handler, maybe test are not local: " + e.getMessage());
            // not in local, do nothing
        }
        springContext.close();
    }

    protected String getInitializerListenerClassName() {
        return "org.bonitasoft.engine.EngineInitializer";
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }
}
