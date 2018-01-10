package org.bonitasoft.engine.test.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.api.ApiAccessType;
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
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Baptiste Mesta
 */
public class EngineStarter {

    private static final String DATABASE_DIR = "org.bonitasoft.h2.database.dir";

    protected static final Logger LOGGER = LoggerFactory.getLogger(EngineStarter.class.getName());

    private boolean dropOnStart = true;
    private boolean dropOnStop = true;
    private ClassPathXmlApplicationContext applicationContext;

    public void start() throws Exception {
        LOGGER.info("=====================================================");
        LOGGER.info("============  Starting Bonita Engine  ===========");
        LOGGER.info("=====================================================");
        final long startTime = System.currentTimeMillis();
        if (System.getProperty("org.bonitasoft.engine.api-type") == null) {
            //force it to local if not specified
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, Collections.<String, String> emptyMap());
        }
        if (APITypeManager.getAPIType().equals(ApiAccessType.LOCAL)) {
            prepareEnvironment();
            setupPlatform();
            initPlatformAndTenant();
        }
        deployCommandsOnDefaultTenant();
        LOGGER.info("==== Finished initialization (took " + (System.currentTimeMillis() - startTime) / 1000 + "s)  ===");
    }

    protected void setupPlatform() throws NamingException, PlatformException {
        PlatformSetup platformSetup = PlatformSetupAccessor.getPlatformSetup();
        if (isDropOnStart()) {
            platformSetup.destroy();
        }
        platformSetup.init();
    }

    //--------------  engine life cycle methods

    protected void prepareEnvironment() {
        LOGGER.info("=========  PREPARE ENVIRONMENT =======");
        String dbVendor = setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");
        //is h2 and not started outside
        if (Objects.equals("h2", dbVendor)) {
            setSystemPropertyIfNotSet(DATABASE_DIR, "target/database");
        }
        //init jndi
        applicationContext = new ClassPathXmlApplicationContext("classpath:local-server.xml");
        applicationContext.refresh();
    }

    protected void shutdown() throws BonitaException {
        undeployCommands();
        deleteTenantAndPlatform();
    }

    protected void deleteTenantAndPlatform() throws BonitaException {
        LOGGER.info("=========  CLEAN PLATFORM =======");
        final PlatformSession session = loginOnPlatform();
        final PlatformAPI platformAPI = getPlatformAPI(session);
        if (platformAPI.isNodeStarted()) {
            platformAPI.stopNode();
            if (dropOnStop) {
                platformAPI.cleanPlatform();
            }
        }
        logoutOnPlatform(session);
    }

    protected PlatformAPI getPlatformAPI(PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformAPI(session);
    }

    protected void checkThreadsAreStopped() throws InterruptedException {
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
        final List<String> startWithFilter = Arrays.asList("H2 ", "Timer-0" /* postgres driver related */, "bitronix" , "main", "Reference Handler",
                "Signal Dispatcher", "Finalizer", "com.google.common.base.internal.Finalizer", "process reaper", "ReaderThread",
                "Abandoned connection cleanup thread", "Monitor Ctrl-Break"/* Intellij */, "daemon-shutdown",
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
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        final PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = getPlatformAPI(session);

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
        initializeAndStartPlatformWithDefaultTenant(platformAPI);
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

    protected LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLoginAPI();
    }

    protected void logoutOnPlatform(final PlatformSession session) throws BonitaException {
        final PlatformLoginAPI platformLoginAPI = getPlatformLoginAPI();
        platformLoginAPI.logout(session);
    }

    protected void initializeAndStartPlatformWithDefaultTenant(final PlatformAPI platformAPI) throws BonitaException {
        platformAPI.initializePlatform();
        platformAPI.startNode();
    }

    protected static String setSystemPropertyIfNotSet(final String property, final String value) {
        final String finalValue = System.getProperty(property, value);
        System.setProperty(property, finalValue);
        return finalValue;
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

        if (applicationContext != null) {
            applicationContext.close();
        }

        checkTempFoldersAreCleaned();
        checkThreadsAreStopped();
    }

    protected void checkTempFoldersAreCleaned() throws IOException {
        final List<File> folders = getTemporaryFolders();
        removeLicensesFolderAndDeleteIt(folders);
        StringBuilder builder = new StringBuilder();
        for (File folder : folders) {
            builder.append("[");
            builder.append(folder.getName());
            builder.append("] ");
        }
        if (!folders.isEmpty()) {
            throw new IllegalStateException("Temporary configuration folders are not cleaned:" + builder.toString());
        }
        LOGGER.info("Temporary configuration folder is cleaned");
    }

    private List<File> getTemporaryFolders() {
        File tempFolder = new File(IOUtil.TMP_DIRECTORY);
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("bonita_");
            }
        };
        return new ArrayList<>(Arrays.asList(tempFolder.listFiles(filter)));
    }

    private void removeLicensesFolderAndDeleteIt(List<File> list) throws IOException {
        Iterator<File> iterator = list.iterator();
        while (iterator.hasNext()) {
            File tempFolder = iterator.next();
            //folder of licenses not deleted because the shutdown hook that delete temp files
            //is executed as the same time as the shutdown hook that stops the engine
            if (tempFolder.getName().contains("bonita_engine") && tempFolder.getName().contains(ManagementFactory.getRuntimeMXBean().getName())) {
                Path licenses = tempFolder.toPath().resolve("licenses");
                if (Files.exists(licenses)) {
                    FileUtils.deleteDirectory(licenses.toFile());
                }
                if (tempFolder.list().length == 0) {
                    FileUtils.deleteDirectory(tempFolder);
                    //remove this directory because there was only the licenses there
                    iterator.remove();
                }
            }
        }
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
