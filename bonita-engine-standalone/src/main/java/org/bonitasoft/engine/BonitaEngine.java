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

    private BonitaDataSourceInitializer bonitaDataSourceInitializer = new BonitaDataSourceInitializer();
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
            bonitaDataSource = bonitaDataSourceInitializer.createManagedDataSource(bonitaDatabaseConfiguration, arjunaTransactionManager);
            businessDataDataSource = bonitaDataSourceInitializer.createManagedDataSource(businessDataDatabaseConfiguration, arjunaTransactionManager);
            bonitaSequenceManagerDataSource = bonitaDataSourceInitializer.createDataSource(bonitaDatabaseConfiguration);
            notManagedBizDataSource = bonitaDataSourceInitializer.createDataSource(businessDataDatabaseConfiguration);
            initializeJNDI();
        }
    }

    private void initializeBonitaDatabaseConfiguration() {
        if (bonitaDatabaseConfiguration == null || bonitaDatabaseConfiguration.isEmpty() ) {
            bonitaDatabaseConfiguration = createDefaultDBConfiguration(BONITA_DB_VENDOR);
        }
        setSystemPropertyIfNotSet(BONITA_DB_VENDOR, bonitaDatabaseConfiguration.getDbVendor());
    }

    private void initializeBusinessDataDatabaseConfiguration() {
        if (businessDataDatabaseConfiguration == null || businessDataDatabaseConfiguration.isEmpty()) {
            businessDataDatabaseConfiguration = createDefaultDBConfiguration(BONITA_BDM_DB_VENDOR);
        }
        setSystemPropertyIfNotSet(BONITA_BDM_DB_VENDOR, businessDataDatabaseConfiguration.getDbVendor());
    }

    private BonitaDatabaseConfiguration createDefaultDBConfiguration(String dbVendorSystemPropertyName) {
        BonitaDatabaseConfiguration databaseConfiguration = new BonitaDatabaseConfiguration();
        String bonitaDBVendor = System.getProperty(dbVendorSystemPropertyName, "h2");
        databaseConfiguration.setUrl(resolveProperty(bonitaDBVendor + ".db.url"));
        databaseConfiguration.setDbVendor(bonitaDBVendor);
        databaseConfiguration.setUser(resolveProperty(bonitaDBVendor + ".db.user"));
        databaseConfiguration.setPassword(bonitaDBVendor + ".db.password");
        databaseConfiguration.setDriverClassName(resolveProperty(bonitaDBVendor + ".db.driver.class"));
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
