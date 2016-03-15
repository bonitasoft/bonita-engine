package org.bonitasoft.engine.test.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.test.ClientEventUtil;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.bonitasoft.platform.setup.PlatformSetupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Baptiste Mesta
 */
public class EngineStarter {

    private static final String BONITA_HOME_DEFAULT_PATH = "target/bonita-home";

    private static final String BONITA_HOME_PROPERTY = "bonita.home";

    private Object h2Server;
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineStarter.class.getName());

    private Map<String, byte[]> overrideConfiguration = new HashMap<>();
    private boolean dropOnStart = true;
    private boolean dropOnStop = true;
    private String dbVendor;

    public void start() throws Exception {
        LOGGER.info("=====================================================");
        LOGGER.info("============  Starting Bonita BPM Engine  ===========");
        LOGGER.info("=====================================================");
        final long startTime = System.currentTimeMillis();
        prepareEnvironment();
        setupPlatform();
        initPlatformAndTenant();
        LOGGER.info("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    private void setupPlatform() throws NamingException, PlatformSetupException {
        PlatformSetup platformSetup = new PlatformSetup(dbVendor);
        if (isDropOnStart()) {
            platformSetup.destroy();
        }
        platformSetup.setup();
    }


    //--------------  engine life cycle methods

    protected String prepareBonitaHome() throws IOException {
        String bonitaHomePath = System.getProperty(BONITA_HOME_PROPERTY);
        if (bonitaHomePath == null || bonitaHomePath.trim().isEmpty()) {
            final InputStream bonitaHomeIS = getBonitaHomeInputStream();
            if (bonitaHomeIS == null) {
                throw new IllegalStateException("No bonita home found in the class path");
            }
            final File outputFolder = new File(BONITA_HOME_DEFAULT_PATH);
            LOGGER.info("No bonita home specified using: " + outputFolder.getAbsolutePath());
            if (outputFolder.exists()) {
                FileUtils.deleteDirectory(outputFolder);
            }
            assert outputFolder.mkdir();
            IOUtil.unzipToFolder(bonitaHomeIS, outputFolder);
            bonitaHomePath = outputFolder.getAbsolutePath() + "/bonita-home";
            for (Map.Entry<String, byte[]> customConfig : overrideConfiguration.entrySet()) {
                IOUtil.write(new File(bonitaHomePath, customConfig.getKey()), customConfig.getValue());
            }
            System.setProperty(BONITA_HOME_PROPERTY, bonitaHomePath);

        }
        return bonitaHomePath;
    }

    protected InputStream getBonitaHomeInputStream() {
        return this.getClass().getResourceAsStream("/bonita-home.zip");
    }

    protected void prepareEnvironment() throws Exception {
        LOGGER.info("=========  PREPARE ENVIRONMENT =======");
        prepareBonitaHome();
        dbVendor = setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");
        if ("h2".equals(dbVendor)) {
            LOGGER.info("Using h2, starting H2 server: ");
            this.h2Server = startH2Server();
        }
        //init jndi
        new ClassPathXmlApplicationContext("classpath:local-server.xml").refresh();
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
        LOGGER.info("h2 server started on port: " + (h2Port + nbTry));
        return server;
    }

    private Object startH2OnPort(String h2Port, Method createTcpServer) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final String[] args = new String[]{"-tcp", "-tcpAllowOthers", "-tcpPort", h2Port};
        final Object server = createTcpServer.invoke(createTcpServer, new Object[]{args});
        final Method start = server.getClass().getMethod("start");
        LOGGER.info("Starting h2 on port " + h2Port);
        try {
            start.invoke(server);
        } catch (InvocationTargetException e) {
            LOGGER.info("Unable to start h2 on port " + h2Port, e);
            return null;
        }
        return server;
    }

    private void stopH2Server(Object h2Server) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        final Class<?> h2ServerClass = Class.forName("org.h2.tools.Server");
        final Method stop = h2ServerClass.getMethod("stop");
        stop.invoke(h2Server);
        LOGGER.info("h2 server stopped");
    }

    protected void shutdown() throws BonitaException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        try {
            deleteTenantAndPlatform();
        } finally {
            cleanupEnvironment();
        }
    }

    protected void cleanupEnvironment() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        LOGGER.info("=========  CLEAN ENVIRONMENT =======");
        if (this.h2Server != null) {
            stopH2Server(this.h2Server);
        }
    }

    protected void deleteTenantAndPlatform() throws BonitaException {
        LOGGER.info("=========  CLEAN PLATFORM =======");

        stopAndCleanPlatformAndTenant(true);
        if (dropOnStop) {
            deletePlatformStructure();
        }
    }

    protected void deletePlatformStructure() throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        platformAPI.deletePlatform();
        logoutOnPlatform(session);
    }

    protected static void cleanPlatform(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.cleanPlatform();
    }

    protected void stopAndCleanPlatformAndTenant(final boolean undeployCommands) throws BonitaException {
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);
        stopAndCleanPlatformAndTenant(platformAPI, undeployCommands);
        logoutOnPlatform(session);
    }

    protected void stopAndCleanPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (platformAPI.isNodeStarted()) {
            stopPlatformAndTenant(platformAPI, undeployCommands);
            if (dropOnStop) {
                cleanPlatform(platformAPI);
            }
        }
    }

    private void checkThreadsAreStopped() throws InterruptedException {
        LOGGER.info("=========  CHECK ENGINE IS SHUTDOWN =======");
        final Set<Thread> keySet = Thread.getAllStackTraces().keySet();
        List<Thread> expectedThreads = new ArrayList<>();
        List<Thread> cacheManagerThreads = new ArrayList<>();
        List<Thread> unexpectedThreads = new ArrayList<>();
        for (Thread thread : keySet) {
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
        // there is no clean way to kill them, a shutdown hook is doing this
        // killing them using hibernate implementation classes is causing weird behaviours
        int nbOfThreads = keySet.size();
        int nbOfExpectedThreads = expectedThreads.size() + 2;
        boolean fail = nbOfThreads > nbOfExpectedThreads;
        LOGGER.info(nbOfThreads + " threads are alive. " + nbOfExpectedThreads + " are expected.");
        if (cacheManagerThreads.size() > 2) {
            LOGGER.info("Only 2 CacheManager threads are expected (PlatformHibernatePersistenceService + TenantHibernatePersistenceService) but "
                    + cacheManagerThreads.size() + " are found:");
            for (Thread thread : cacheManagerThreads) {
                printThread(thread);
            }
        }
        if (unexpectedThreads.size() > 0) {
            LOGGER.info("The following list of threads is not expected:");
            for (Thread thread : unexpectedThreads) {
                printThread(thread);
            }
        }
        if (fail) {
            throw new IllegalStateException(
                    "Some threads are still active : \nCacheManager potential issues:" + cacheManagerThreads + "\nOther threads:" + unexpectedThreads);
        }
        LOGGER.info("All engine threads are stopped properly");
    }

    private boolean isCacheManager(Thread thread) {
        return thread.getName().startsWith("net.sf.ehcache.CacheManager");
    }

    private void printThread(final Thread thread) {
        LOGGER.info("\n");
        LOGGER.info("Thread is still alive:" + thread.getName());
        for (StackTraceElement stackTraceElement : thread.getStackTrace()) {
            LOGGER.info("        at " + stackTraceElement.toString());
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
                "Abandoned connection cleanup thread", "AWT-AppKit"/* bonecp related */, "Monitor Ctrl-Break"/* Intellij */, "daemon-shutdown",
                "surefire-forkedjvm",
                "Restlet");
        for (final String prefix : startWithFilter) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        //shutdown hook not executed in main thread
        return thread.getId() == Thread.currentThread().getId();
    }

    protected void initPlatformAndTenant() throws Exception {
        final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(session);

        if (!platformAPI.isPlatformInitialized()) {
            LOGGER.info("=========  INIT PLATFORM =======");
            createPlatformAndTenant(platformAPI);
        } else {
            LOGGER.info("=========  REUSING EXISTING PLATFORM =======");
            platformAPI.startNode();
        }
        platformLoginAPI.logout(session);
    }

    protected void createPlatformAndTenant(PlatformAPI platformAPI) throws BonitaException {
        initializeAndStartPlatformWithDefaultTenant(platformAPI, true);
    }

    protected PlatformLoginAPI getPlatformLoginAPI() throws BonitaException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

    protected PlatformSession loginOnPlatform() throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        return platformLoginAPI.login("platformAdmin", "platform");
    }

    protected void deployCommandsOnDefaultTenant() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        final APISession session = login(loginAPI);
        ClientEventUtil.deployCommand(session);
        logout(loginAPI, session);
    }

    private void logout(LoginAPI loginAPI, APISession session) throws SessionNotFoundException, LogoutException {
        loginAPI.logout(session);
    }

    private APISession login(LoginAPI loginAPI) throws LoginException {
        return loginAPI.login(TestEngineImpl.TECHNICAL_USER_NAME, TestEngineImpl.TECHNICAL_USER_PASSWORD);
    }

    private LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

    protected void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    protected void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI, final boolean deployCommands) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();
        if (deployCommands) {
            deployCommandsOnDefaultTenant();
        }
    }

    protected static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
    }

    protected void stopPlatformAndTenant(final PlatformAPI platformAPI, final boolean undeployCommands) throws BonitaException {
        if (undeployCommands) {
            undeployCommands();
        }

        platformAPI.stopNode();
    }

    protected void undeployCommands() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        final APISession session = login(loginAPI);
        ClientEventUtil.undeployCommand(session);
        logout(loginAPI, session);
    }

    public void stop() throws Exception {
        LOGGER.info("=====================================================");
        LOGGER.info("============ CLEANING OF TEST ENVIRONMENT ===========");
        LOGGER.info("=====================================================");

        shutdown();
        checkThreadsAreStopped();
    }

    public void overrideConfiguration(String path, byte[] file) {
        overrideConfiguration.put(path, file);
    }

    public void setDropOnStart(boolean dropOnStart) {
        this.dropOnStart = dropOnStart;
    }

    public boolean isDropOnStart() {
        return dropOnStart;
    }

    public void setDropOnStop(boolean dropOnStop) {
        this.dropOnStop = dropOnStop;
    }
}
