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
import org.bonitasoft.engine.home.BonitaHomeServer;
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

        initPlatformAndTenant();

        System.out.println("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
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
        System.setProperty("sysprop.h2.db.server.port",String.valueOf(h2Port + nbTry));
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
            if (this.h2Server != null) {
                stopH2Server(this.h2Server);
            }
        }
    }

    protected void deleteTenantAndPlatform() throws BonitaException {
        final APITestUtil apiTestUtil = new APITestUtil();
        apiTestUtil.stopAndCleanPlatformAndTenant(true);
        apiTestUtil.deletePlatformStructure();
    }

    private void checkThreadsAreStopped() throws InterruptedException {
        System.out.println("Checking if all Threads are stopped");
        final Set<Thread> keySet = Thread.getAllStackTraces().keySet();
        final Iterator<Thread> iterator = keySet.iterator();
        final ArrayList<Thread> list = new ArrayList<Thread>();
        while (iterator.hasNext()) {
            final Thread thread = iterator.next();
            if (isEngine(thread) && !thread.getName().startsWith("net.sf.ehcache.CacheManager")) {
                // wait for the thread to die
                thread.join(10000);
                // if still alive print it
                if (thread.isAlive()) {
                    list.add(thread);
                }
            }
        }
        if (!list.isEmpty()) {
            for (final Thread thread : list) {
                System.out.println("thread is still alive:" + thread.getName());
                for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
                    System.out.println(stackTraceElement.toString());
                }
            }
            throw new IllegalStateException("Some threads are still active : " + list);
        }
        System.out.println("All engine threads are stopped properly");
    }

    private boolean isEngine(final Thread thread) {
        final String name = thread.getName();
        final ThreadGroup threadGroup = thread.getThreadGroup();
        if (threadGroup != null && threadGroup.getName().equals("system")) {
            return false;
        }
        final List<String> startWithFilter = Arrays.asList("H2 ", "Timer-0" /* postgres driver related */, "BoneCP", "bitronix", "main", "Reference Handler",
                "Signal Dispatcher", "Finalizer", "com.google.common.base.internal.Finalizer"/* guava, used by bonecp */, "process reaper", "ReaderThread",
                "Abandoned connection cleanup thread", "AWT-AppKit"/* bonecp related */, "Monitor Ctrl-Break"/* Intellij */);
        for (final String prefix : startWithFilter) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        //ehcache
        return !thread.getName().matches("[0-9]+_[A-Za-z_]+.data");
    }

    protected void initPlatformAndTenant() throws Exception {
        new APITestUtil().createPlatformStructure();
        new APITestUtil().initializeAndStartPlatformWithDefaultTenant(true);
    }

    private static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
    }
}
