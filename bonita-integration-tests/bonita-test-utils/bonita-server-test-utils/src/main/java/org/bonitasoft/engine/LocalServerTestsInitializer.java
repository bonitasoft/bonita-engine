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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.test.APITestUtil;

public class LocalServerTestsInitializer {

    private static final String BONITA_HOME_DEFAULT_PATH = "target/bonita-home";

    private static final String BONITA_HOME_PROPERTY = "bonita.home";

    private static LocalServerTestsInitializer INSTANCE;
    private Object h2Server;

    public static LocalServerTestsInitializer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalServerTestsInitializer();
        }
        return INSTANCE;
    }

    public static void beforeAll() throws Exception {
        LocalServerTestsInitializer.getInstance().before();
    }

    public static void afterAll() throws Exception {
        LocalServerTestsInitializer.getInstance().after();

    }

    protected void before() throws Exception {
        System.out.println("=====================================================");
        System.out.println("=========  INITIALIZATION OF TEST ENVIRONMENT =======");
        System.out.println("=====================================================");

        final long startTime = System.currentTimeMillis();
        prepareEnvironment();

        initPlatformAndTenant();

        System.out.println("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    public void prepareEnvironment() throws IOException, ClassNotFoundException, NoSuchMethodException, BonitaHomeNotSetException, IllegalAccessException, InvocationTargetException {

        System.out.println("=========  PREPARE ENVIRONMENT =======");
        String bonitaHome = setSystemPropertyIfNotSet(BONITA_HOME_PROPERTY, BONITA_HOME_DEFAULT_PATH);
        final String dbVendor = setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // paste the default local server properties
        // TODO do not handle the default local server like this
        File platformInit = new File(bonitaHome, "engine-server/conf/platform-init");
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/local-server.xml"), new File(platformInit, "local-server.xml"));
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/local-server.properties"), new File(platformInit, "local-server.properties"));

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        if ("h2".equals(dbVendor)) {
            this.h2Server = startH2Server();
        }
    }

    private Object startH2Server() throws ClassNotFoundException, NoSuchMethodException, IOException, BonitaHomeNotSetException, IllegalAccessException, InvocationTargetException {
        final int h2Port = 6666;
//        final String h2Port = (String) BonitaHomeServer.getInstance().getPrePlatformInitProperties().get("h2.db.server.port");

        final Class<?> h2ServerClass = Class.forName("org.h2.tools.Server");
        final Method createTcpServer = h2ServerClass.getMethod("createTcpServer", String[].class);

        int nbTry = -1;
        Object server;
        do {
            nbTry++;
            server = startH2OnPort(String.valueOf(h2Port + nbTry), createTcpServer);
        } while (server == null && nbTry <= 10);
        if (nbTry > 10) {
            throw new IOException("h2 server not started, all ports occupied");
        }
        System.setProperty("db.server.port", String.valueOf(h2Port + nbTry));
        System.err.println("--- H2 Server started on port " + h2Port + " ---");
        return server;
    }

    private Object startH2OnPort(String h2Port, Method createTcpServer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final String[] args = new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", h2Port};
        final Object server = createTcpServer.invoke(createTcpServer, new Object[]{args});
        final Method start = server.getClass().getMethod("start");
        try {
            start.invoke(server);
        } catch (InvocationTargetException e) {
            return null;
        }
        return server;
    }

    private void stopH2Server(Object h2Server) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        final Class<?> h2ServerClass = Class.forName("org.h2.tools.Server");
        final Method stop = h2ServerClass.getMethod("stop");
        stop.invoke(h2Server);
        System.err.println("--- H2 Server stopped ---");
    }

    protected void after() throws Exception {
        System.out.println("=====================================================");
        System.out.println("============ CLEANING OF TEST ENVIRONMENT ===========");
        System.out.println("=====================================================");

        shutdown();
        checkThreadsAreStopped();
    }

    public void shutdown() throws BonitaException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        try {
            deleteTenantAndPlatform();
        } finally {
            cleanupEnvironment();
        }
    }

    public void cleanupEnvironment() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        System.out.println("=========  CLEAN ENVIRONMENT =======");
        if (this.h2Server != null) {
            stopH2Server(this.h2Server);
        }
    }

    public void deleteTenantAndPlatform() throws BonitaException {
        System.out.println("=========  CLEAN PLATFORM =======");
        final APITestUtil apiTestUtil = new APITestUtil();
        apiTestUtil.stopAndCleanPlatformAndTenant(true);
        apiTestUtil.deletePlatformStructure();
    }

    private void checkThreadsAreStopped() throws InterruptedException {
        System.out.println("=========  CHECK ENGINE IS SHUTDOWN =======");
        final Set<Thread> keySet = Thread.getAllStackTraces().keySet();
        List<Thread> expectedThreads = new ArrayList<>();
        List<Thread> cacheManagerThreads = new ArrayList<>();
        List<Thread> unexpectedThreads = new ArrayList<>();
        final Iterator<Thread> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            final Thread thread = iterator.next();
            if (isExpectedThread(thread)) {
                expectedThreads.add(thread);
            } else {
                if (isCacheManager(thread)) {
                    cacheManagerThreads.add(thread);
                } else {
                    unexpectedThreads.add(thread);
                }
            }
        }
        //2 cache manager threads are allowed
        // one for PlatformHibernatePersistenceService
        // one for TenantHibernatePersistenceService
        // there is no clean way to kill them, a shutdownhook is doing this
        // killing them using hibernate implementation classes is causing weird behaviours
        int nbOfThreads = keySet.size();
        int nbOfExpectedThreads = expectedThreads.size() + 2;
        boolean fail = nbOfThreads > nbOfExpectedThreads;
        System.out.println(nbOfThreads + " are alive. " + nbOfExpectedThreads + " are expected.");
        if (cacheManagerThreads.size() > 2) {
            System.out.println("Only 2 CacheManager threads are expected (PlatformHibernatePersistenceService + TenantHibernatePersistenceService) but " + cacheManagerThreads.size() + " are found:");
            for (Thread thread : cacheManagerThreads) {
                printThread(thread);
            }
        }
        if (unexpectedThreads.size() > 0) {
            System.out.println("The following list of threads is not expected:");
            for (Thread thread : unexpectedThreads) {
                printThread(thread);
            }
        }
        if (fail) {
            throw new IllegalStateException("Some threads are still active : \nCacheManager potential issues:" + cacheManagerThreads + "\nOther threads:" + unexpectedThreads);
        }
        System.out.println("All engine threads are stopped properly");
    }


    private boolean isCacheManager(Thread thread) {
        return thread.getName().startsWith("net.sf.ehcache.CacheManager");
    }


    private void printThread(final Thread thread) {
        System.out.println("\n");
        System.out.println("Thread is still alive:" + thread.getName());
        for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
            System.out.println("        at " + stackTraceElement.toString());
        }
    }


    private boolean isExpectedThread(final Thread thread) {
        final String name = thread.getName();
        final ThreadGroup threadGroup = thread.getThreadGroup();
        if (threadGroup != null && threadGroup.getName().equals("system")) {
            return true;
        }
        final List<String> startWithFilter = Arrays.asList("H2 ", "Timer-0" /* postgres driver related */, "BoneCP", "bitronix", "main", "Reference Handler",
                "Signal Dispatcher", "Finalizer", "com.google.common.base.internal.Finalizer"/* guava, used by bonecp */, "process reaper", "ReaderThread",
                "Abandoned connection cleanup thread", "AWT-AppKit"/* bonecp related */, "Monitor Ctrl-Break"/* Intellij */);
        for (final String prefix : startWithFilter) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public void initPlatformAndTenant() throws Exception {
        System.out.println("=========  INIT PLATFORM =======");
        new APITestUtil().createPlatformStructure();
        new APITestUtil().initializeAndStartPlatformWithDefaultTenant(true);
    }

    private static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
    }
}
