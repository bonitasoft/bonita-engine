/*
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
 */

package org.bonitasoft.engine.test;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.EJB3ServerAPI;
import org.bonitasoft.engine.api.HTTPServerAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.TCPServerAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;

import javax.naming.Context;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.bonitasoft.engine.api.PlatformAPIAccessor.getPlatformAPI;
import static org.bonitasoft.engine.api.PlatformAPIAccessor.getPlatformLoginAPI;
import static org.bonitasoft.engine.api.TenantAPIAccessor.getLoginAPI;

/**
 * @author mazourd
 */
public class PlatformStarter {

    private PlatformLoginAPI platformLoginAPI;

    private PlatformAPI platformAPI;

    private static final String BONITA_HOME_DEFAULT_PATH = "target/bonita-home";

    private static final String BONITA_HOME_PROPERTY = "bonita.home";

    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";

    private static final int BUFFER_SIZE = 4096;

    private Object h2Server;

    public void startEngine() throws Exception {
        unzip("/home/mazourd/work/bonita-engine/bonita-integration-tests/bonita-test-api/target/bonita-home-7.1.0-SNAPSHOT.zip",
                "/home/mazourd/work/bonita-engine/bonita-integration-tests/bonita-test-api/target");
        prepareEnvironment();
        initPlatformAndTenant();
        final LoginAPI loginAPI = getLoginAPI();
        loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
    }

    public void stopEngine() throws ClassNotFoundException, BonitaException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
            InterruptedException {
        System.out.println("=====================================================");
        System.out.println("============ CLEANING OF TEST ENVIRONMENT ===========");
        System.out.println("=====================================================");
        shutdown();
        checkThreadsAreStopped();
        /*final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(LoginAPI.class.getName(), serverAPI);
        final LoginAPI loginAPI = (LoginAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { LoginAPI.class }, sessionInterceptor);
        loginAPI.logout(loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD));*/
    }

    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public void prepareEnvironment() throws IOException, ClassNotFoundException, NoSuchMethodException, BonitaHomeNotSetException, IllegalAccessException,
            InvocationTargetException {

        System.out.println("=========  PREPARE ENVIRONMENT =======");
        String bonitaHome = setSystemPropertyIfNotSet(BONITA_HOME_PROPERTY, BONITA_HOME_DEFAULT_PATH);
        final String dbVendor = setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // paste the default local server properties
        // TODO do not handle the default local server like this
        File platformInit = new File(bonitaHome, "engine-server/conf/platform-init");
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/local-server.xml"), new File(platformInit, "local-server.xml"));
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/local-server.properties"), new File(platformInit, "local-server.properties"));

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.test.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.test.local");

        if ("h2".equals(dbVendor)) {
            this.h2Server = startH2Server();
        }
    }

    private Object startH2Server() throws ClassNotFoundException, NoSuchMethodException, IOException, BonitaHomeNotSetException, IllegalAccessException,
            InvocationTargetException {
        final int h2Port = 6666;
        //        final String h2Port = (String) BonitaHomeServer.getInstance().getPrePlatformInitProperties().get("h2.db.server.port");

        final Class<?> h2ServerClass = Thread.currentThread().getContextClassLoader().loadClass("org.h2.tools.Server");
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
        final String[] args = new String[] { "-tcp", "-tcpAllowOthers", "-tcpPort", h2Port };
        final Object server = createTcpServer.invoke(createTcpServer, new Object[] { args });
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
            System.out.println("=========  CLEAN PLATFORM =======");
            final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
            if (platformAPI.isNodeStarted()) {
                final LoginAPI loginAPI = getLoginAPI();
                final APISession apiSession = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
                ClientEventUtil.undeployCommand(apiSession);
                loginAPI.logout(apiSession);
                platformAPI.stopNode();
                platformAPI.cleanPlatform();
            }
            platformLoginAPI.logout(session);
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
            System.out.println("Only 2 CacheManager threads are expected (PlatformHibernatePersistenceService + TenantHibernatePersistenceService) but "
                    + cacheManagerThreads.size() + " are found:");
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
            throw new IllegalStateException("Some threads are still active : \nCacheManager potential issues:" + cacheManagerThreads + "\nOther threads:"
                    + unexpectedThreads);
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
        platformLoginAPI = getPlatformLoginAPI();
        PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = getPlatformAPI(session);
        if (platformAPI.isPlatformCreated()) {
            if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
                platformAPI.stopNode();
            }
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
        }
        platformAPI.createPlatform();

        platformLoginAPI.logout(session);
        session = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = getPlatformAPI(session);
        platformAPI.initializePlatform();
        platformAPI.startNode();
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(LoginAPI.class.getName(), serverAPI);
        final LoginAPI loginAPI = (LoginAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { LoginAPI.class }, sessionInterceptor);
        final APISession apiSession = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
        ClientEventUtil.deployCommand(apiSession);
        loginAPI.logout(apiSession);
    }

    private static ServerAPI getServerAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ApiAccessType apiType;
        try {
            apiType = APITypeManager.getAPIType();
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
        Map<String, String> parameters = null;
        switch (apiType) {
            case LOCAL:
                return LocalServerAPIFactory.getServerAPI();
            case EJB3:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new EJB3ServerAPI(parameters);
            case HTTP:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new HTTPServerAPI(parameters);
            case TCP:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new TCPServerAPI(parameters);
            default:
                throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
        }
    }

    private static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
    }
}
