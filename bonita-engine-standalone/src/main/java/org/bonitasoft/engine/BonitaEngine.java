package org.bonitasoft.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.UserTransaction;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformLogoutException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.platform.setup.PlatformSetup;
import org.bonitasoft.platform.setup.PlatformSetupAccessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jndi.JndiTemplate;

public class BonitaEngine {

    private ClassPathXmlApplicationContext applicationContext;
    private BonitaDatabaseConfiguration bonitaDatabaseConfiguration;
    private BonitaDatabaseConfiguration businessDataDatabaseConfiguration;
    private MemoryJNDISetup memoryJNDISetup;
    private BasicManagedDataSource bonitaDataSource;
    private BasicManagedDataSource businessDataDataSource;
    private BasicDataSource bonitaSequenceManagerDataSource;
    private BasicDataSource notManagedBizDataSource;
    private javax.transaction.UserTransaction userTransaction;
    private javax.transaction.TransactionManager arjunaTransactionManager;
    public static final String BONITA_BDM_DB_VENDOR = "sysprop.bonita.bdm.db.vendor";
    public static final String BONITA_DB_VENDOR = "sysprop.bonita.db.vendor";

    public void initializeEnvironment() throws Exception {
        if (applicationContext == null) {
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, Collections.emptyMap());
            applicationContext = new ClassPathXmlApplicationContext("classpath:local-server.xml");
            applicationContext.refresh();
            initializeBonitaDatabaseConfiguration();
            initializeBusinessDataDatabaseConfiguration();
            arjunaTransactionManager = TransactionManager.transactionManager();
            userTransaction = UserTransaction.userTransaction();
            initializeBonitaDataSource();
            initializeBusinessDataSource();
            initializeBonitaSequenceManagerDataSource();
            initializeNotManagedBizDataSource();
            initializeJNDI();
        }
    }

    private void initializeBonitaDatabaseConfiguration() {
        if (bonitaDatabaseConfiguration == null) {
            bonitaDatabaseConfiguration = createDefaultDBConfiguration(BONITA_DB_VENDOR);
        }
        setSystemPropertyIfNotSet(BONITA_DB_VENDOR, bonitaDatabaseConfiguration.getDbVendor());
    }

    public void initializeBusinessDataDatabaseConfiguration() {
        if (businessDataDatabaseConfiguration == null) {
            businessDataDatabaseConfiguration = createDefaultDBConfiguration(BONITA_BDM_DB_VENDOR);
        }
        setSystemPropertyIfNotSet(BONITA_BDM_DB_VENDOR, businessDataDatabaseConfiguration.getDbVendor());
    }

    public BonitaDatabaseConfiguration createDefaultDBConfiguration(String dbVendorSystemPropertyName) {
        BonitaDatabaseConfiguration databaseConfiguration = new BonitaDatabaseConfiguration();
        String bonitaDBVendor = System.getProperty(dbVendorSystemPropertyName, "h2");
        databaseConfiguration.setUrl(resolveProperty(bonitaDBVendor + ".db.url"));
        databaseConfiguration.setDbVendor(bonitaDBVendor);
        databaseConfiguration.setUser(resolveProperty(bonitaDBVendor + ".db.user"));
        databaseConfiguration.setPassword(bonitaDBVendor + ".db.password");
        databaseConfiguration.setDriver(resolveProperty(bonitaDBVendor + ".db.driver.class"));
        return databaseConfiguration;
    }

    private String setSystemPropertyIfNotSet(String systemPropertyName, String defaultValue) {
        String value = System.getProperty(systemPropertyName);
        if (value != null) {
            return value;
        }
        System.setProperty(systemPropertyName, defaultValue);
        return defaultValue;
    }

    private void initializeBonitaDataSource() {
        bonitaDataSource = new BasicManagedDataSource();
        bonitaDataSource.setInitialSize(1);
        bonitaDataSource.setMaxTotal(7);
        bonitaDataSource.setDriverClassName(bonitaDatabaseConfiguration.getDriver());
        bonitaDataSource.setTransactionManager(arjunaTransactionManager);
        bonitaDataSource.setUrl(bonitaDatabaseConfiguration.getUrl());
        bonitaDataSource.setUsername(bonitaDatabaseConfiguration.getUserName());
        bonitaDataSource.setPassword(bonitaDatabaseConfiguration.getPassword());
    }

    private void initializeBusinessDataSource() {
        businessDataDataSource = new BasicManagedDataSource();
        businessDataDataSource.setInitialSize(1);
        businessDataDataSource.setMaxTotal(3);
        businessDataDataSource.setDriverClassName(businessDataDatabaseConfiguration.getDriver());
        businessDataDataSource.setTransactionManager(arjunaTransactionManager);
        businessDataDataSource.setUrl(businessDataDatabaseConfiguration.getUrl());
        businessDataDataSource.setUsername(businessDataDatabaseConfiguration.getUserName());
        businessDataDataSource.setPassword(businessDataDatabaseConfiguration.getPassword());
    }

    private void initializeBonitaSequenceManagerDataSource() {
        bonitaSequenceManagerDataSource = new BasicDataSource();
        bonitaSequenceManagerDataSource.setInitialSize(1);
        bonitaSequenceManagerDataSource.setMaxTotal(7);
        bonitaSequenceManagerDataSource.setDriverClassName(bonitaDatabaseConfiguration.getDriver());
        bonitaSequenceManagerDataSource.setUrl(bonitaDatabaseConfiguration.getUrl());
        bonitaSequenceManagerDataSource.setUsername(bonitaDatabaseConfiguration.getUserName());
        bonitaSequenceManagerDataSource.setPassword(bonitaDatabaseConfiguration.getPassword());
    }

    private void initializeNotManagedBizDataSource() {
        notManagedBizDataSource = new BasicDataSource();
        notManagedBizDataSource.setInitialSize(1);
        notManagedBizDataSource.setMaxTotal(3);
        notManagedBizDataSource.setDriverClassName(businessDataDatabaseConfiguration.getDriver());
        notManagedBizDataSource.setUrl(businessDataDatabaseConfiguration.getUrl());
        notManagedBizDataSource.setUsername(businessDataDatabaseConfiguration.getUserName());
        notManagedBizDataSource.setPassword(businessDataDatabaseConfiguration.getPassword());
    }

    private void initializeJNDI() throws NamingException {
        Map<String, Object> jndiMapping = new HashMap<>();
        jndiMapping.put("java:comp/env/bonitaDS", bonitaDataSource);
        jndiMapping.put("java:comp/env/bonitaSequenceManagerDS", bonitaSequenceManagerDataSource);
        jndiMapping.put("java:comp/env/BusinessDataDS", businessDataDataSource);
        jndiMapping.put("java:comp/env/NotManagedBizDataDS", notManagedBizDataSource);
        jndiMapping.put("java:comp/env/TransactionManager", arjunaTransactionManager);
        jndiMapping.put("java:comp/UserTransaction", userTransaction);
        JndiTemplate jndiTemplate = new JndiTemplate();
        memoryJNDISetup = new MemoryJNDISetup(jndiTemplate, jndiMapping);
        memoryJNDISetup.init();
    }

    public void start() throws Exception {
        initializeEnvironment();
        PlatformSetup platformSetup = PlatformSetupAccessor.getPlatformSetup();
        platformSetup.init();

        PlatformSession platformSession = loginOnPlatform();

        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);

        if (!platformAPI.isPlatformInitialized()) {
            platformAPI.initializePlatform();
        }
        platformAPI.startNode();
        logoutFromPlatform(platformSession);
    }

    private void logoutFromPlatform(PlatformSession platformSession)
            throws PlatformLogoutException, SessionNotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        PlatformAPIAccessor.getPlatformLoginAPI().logout(platformSession);
    }

    private PlatformSession loginOnPlatform() throws PlatformLoginException {
        return new LocalLoginMechanism().login();
    }

    public void stop() throws Exception {
        PlatformSession platformSession = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        if (platformAPI.isNodeStarted()) {
            platformAPI.stopNode();
        }
        logoutFromPlatform(platformSession);
        applicationContext.close();
        memoryJNDISetup.clean();
    }


    public BonitaDatabaseConfiguration getBonitaDatabaseConfiguration() {
        return bonitaDatabaseConfiguration;
    }

    public void setBonitaDatabaseConfiguration(BonitaDatabaseConfiguration database) {
        this.bonitaDatabaseConfiguration = database;
    }

    public BonitaDatabaseConfiguration getBusinessDataDatabaseConfiguration() {
        return businessDataDatabaseConfiguration;
    }

    public void setBusinessDataDatabaseConfiguration(BonitaDatabaseConfiguration businessDataDatabaseConfiguration) {
        this.businessDataDatabaseConfiguration = businessDataDatabaseConfiguration;
    }

    private String resolveProperty(String name) {
        return applicationContext.getBeanFactory().resolveEmbeddedValue("${" + name + "}");
    }


    BasicManagedDataSource getBonitaDataSource() {
        return bonitaDataSource;
    }

    BasicManagedDataSource getBusinessDataDataSource() {
        return businessDataDataSource;
    }
}
