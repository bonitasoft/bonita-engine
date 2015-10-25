package org.bonitasoft.engine.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.naming.Context;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 */
public class TestEngine {

    public static final String DEFAULT_TECHNICAL_LOGGER_USERNAME = "install";

    public static final String DEFAULT_TECHNICAL_LOGGER_PASSWORD = "install";
    private static final String BONITA_HOME_DEFAULT_PATH = "target/bonita-home";

    private static final String BONITA_HOME_PROPERTY = "bonita.home";


    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class.getName());

    private Object h2Server;

    public void start() throws Exception {
        System.out.println("=====================================================");
        System.out.println("=========  INITIALIZATION OF TEST ENVIRONMENT =======");
        System.out.println("=====================================================");

        final long startTime = System.currentTimeMillis();
        prepareEnvironment();

        initPlatformAndTenant();

        System.out.println("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    String prepareBonitaHome() throws IOException {
        final String bonitaHomePath = System.getProperty(BONITA_HOME_PROPERTY);
        if (bonitaHomePath == null || bonitaHomePath.trim().isEmpty()) {
            final InputStream bonitaHomeIS = this.getClass().getResourceAsStream("/bonita-home.zip");
            if (bonitaHomeIS == null) {
                throw new IllegalStateException("No bonita home found in the class path");
            }
            final File outputFolder = new File(BONITA_HOME_DEFAULT_PATH);
            outputFolder.mkdir();
            IOUtil.unzipToFolder(bonitaHomeIS, outputFolder);
            System.setProperty(BONITA_HOME_PROPERTY, outputFolder.getAbsolutePath() + "/bonita-home");
        }
        return System.getProperty(BONITA_HOME_PROPERTY);
    }

    public void prepareEnvironment()
            throws IOException, ClassNotFoundException, NoSuchMethodException, BonitaHomeNotSetException, IllegalAccessException, InvocationTargetException {

        System.out.println("=========  PREPARE ENVIRONMENT =======");
        final String bonitaHome = prepareBonitaHome();


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

    private Object startH2Server()
            throws ClassNotFoundException, NoSuchMethodException, IOException, BonitaHomeNotSetException, IllegalAccessException, InvocationTargetException {
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

    public void stop() throws Exception {
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

        stopAndCleanPlatformAndTenant(true);
        deletePlatformStructure();
    }

    public void deletePlatformStructure() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

    public static void cleanPlatform(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.cleanPlatform();
    }

    public void stopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        logoutOnPlatform(session);
    }

    public void stopAndCleanPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (platformAPI.isNodeStarted()) {
            stopPlatformAndTenant(platformAPI, undeployCommands);
            cleanPlatform(platformAPI);
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
            throw new IllegalStateException(
                    "Some threads are still active : \nCacheManager potential issues:" + cacheManagerThreads + "\nOther threads:" + unexpectedThreads);
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
        createPlatformStructure();
        initializeAndStartPlatformWithDefaultTenant(true);
    }

    public PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    public PlatformSession loginOnPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    public void initializeAndStartPlatformWithDefaultTenant(final boolean deployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        initializeAndStartPlatformWithDefaultTenant(platformAPI, deployCommands);
        logoutOnPlatform(session);
    }

    public void deployCommandsOnDefaultTenant() throws BonitaException {
        final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
        final APISession session = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
        ClientEventUtil.deployCommand(session);
        loginAPI.logout(session);
    }

    public void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    public void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();

        if (deployCommands) {
            deployCommandsOnDefaultTenant();
        }
    }

    public static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
    }


    public void createPlatformStructure() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        createPlatformStructure(platformAPI, false);
        platformLoginAPI.logout(session);
    }


    private void createPlatformStructure(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        if (platformAPI.isPlatformCreated()) {
            if (PlatformState.STARTED.equals(platformAPI.getPlatformState())) {
                stopPlatformAndTenant(platformAPI, deployCommands);
            }
            platformAPI.cleanPlatform();
            platformAPI.deletePlatform();
        }
        platformAPI.createPlatform();
    }

    public void stopPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (undeployCommands) {
            final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
            final APISession session = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
            ClientEventUtil.undeployCommand(session);
            loginAPI.logout(session);
        }

        platformAPI.stopNode();
    }


}
