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

    public void initializeEnvironment() throws Exception {
        if (applicationContext == null) {
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, Collections.emptyMap());
            applicationContext = new ClassPathXmlApplicationContext("classpath:local-server.xml");
            applicationContext.refresh();
            arjunaTransactionManager = TransactionManager.transactionManager();
            userTransaction = UserTransaction.userTransaction();
            initializeBonitaDataSource();
            initializeBusinessDataSource();
            initializeBonitaSequenceManagerDataSource();
            initializeNotManagedBizDataSource();
            initializeJNDI();
        }
    }

    private void initializeBonitaDataSource() {
        bonitaDataSource = new BasicManagedDataSource();
        bonitaDataSource.setDriverClassName(resolveProperty("${sysprop.bonita.db.vendor}.db.driver.class"));
        bonitaDataSource.setTransactionManager(arjunaTransactionManager);
        bonitaDataSource.setInitialSize(1);
        bonitaDataSource.setMaxTotal(7);
        bonitaDataSource.setUrl(resolveProperty("${sysprop.bonita.db.vendor}.db.url"));
        bonitaDataSource.setUsername(resolveProperty("${sysprop.bonita.db.vendor}.db.user"));
        bonitaDataSource.setPassword(resolveProperty("${sysprop.bonita.db.vendor}.db.password"));
    }

    private void initializeBusinessDataSource(){
        businessDataDataSource = new BasicManagedDataSource();
        businessDataDataSource.setDriverClassName(resolveProperty("${sysprop.bonita.db.vendor}.db.driver.class"));
        businessDataDataSource.setTransactionManager(arjunaTransactionManager);
        businessDataDataSource.setInitialSize(1);
        businessDataDataSource.setMaxTotal(3);
        businessDataDataSource.setUrl(resolveProperty("${sysprop.bonita.db.vendor}.db.url"));
        businessDataDataSource.setUsername(resolveProperty("${sysprop.bonita.db.vendor}.db.user"));
        businessDataDataSource.setPassword(resolveProperty("${sysprop.bonita.db.vendor}.db.password"));
    }

    private void initializeBonitaSequenceManagerDataSource(){
        bonitaSequenceManagerDataSource = new BasicDataSource();
        bonitaSequenceManagerDataSource.setDriverClassName(resolveProperty("${sysprop.bonita.db.vendor}.db.driver.class"));
        bonitaSequenceManagerDataSource.setInitialSize(1);
        bonitaSequenceManagerDataSource.setMaxTotal(7);
        bonitaSequenceManagerDataSource.setUrl(resolveProperty("${sysprop.bonita.db.vendor}.db.url"));
        bonitaSequenceManagerDataSource.setUsername(resolveProperty("${sysprop.bonita.db.vendor}.db.user"));
        bonitaSequenceManagerDataSource.setPassword(resolveProperty("${sysprop.bonita.db.vendor}.db.password"));
    }

    private void initializeNotManagedBizDataSource(){
        notManagedBizDataSource = new BasicDataSource();
        notManagedBizDataSource.setDriverClassName(resolveProperty("${sysprop.bonita.db.vendor}.db.driver.class"));
        notManagedBizDataSource.setInitialSize(1);
        notManagedBizDataSource.setMaxTotal(3);
        notManagedBizDataSource.setUrl(resolveProperty("${sysprop.bonita.db.vendor}.db.url"));
        notManagedBizDataSource.setUsername(resolveProperty("${sysprop.bonita.db.vendor}.db.user"));
        notManagedBizDataSource.setPassword(resolveProperty("${sysprop.bonita.db.vendor}.db.password"));
    }

    private void initializeJNDI() throws NamingException {
        Map<String, Object> jndiMapping = new HashMap<>();
        jndiMapping.put("java:comp/env/bonitaDS",bonitaDataSource);
        jndiMapping.put("java:comp/env/bonitaSequenceManagerDS",bonitaSequenceManagerDataSource);
        jndiMapping.put("java:comp/env/BusinessDataDS",businessDataDataSource);
        jndiMapping.put("java:comp/env/NotManagedBizDataDS",notManagedBizDataSource);
        jndiMapping.put("java:comp/env/TransactionManager",arjunaTransactionManager);
        jndiMapping.put("java:comp/UserTransaction",userTransaction);
        JndiTemplate jndiTemplate = new JndiTemplate();
        memoryJNDISetup = new MemoryJNDISetup(jndiTemplate,jndiMapping);
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

    private void logoutFromPlatform(PlatformSession platformSession) throws PlatformLogoutException, SessionNotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
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

    public void setBonitaDatabaseConfiguration(BonitaDatabaseConfiguration database) {
        this.bonitaDatabaseConfiguration = database;
        System.setProperty("sysprop.bonita.db.vendor", database.getDbVendor());
        setSystemPropertyIfNotNull(database.getServer(), "db.server.name");
        setSystemPropertyIfNotNull(database.getPort(), "db.server.port");
        setSystemPropertyIfNotNull(database.getDatabaseName(), "db.database.name");
        setSystemPropertyIfNotNull(database.getUser(), "db.user");
        setSystemPropertyIfNotNull(database.getPassword(), "db.password");
    }

    private void setSystemPropertyIfNotNull(String property, String key) {
        if (property != null) {
            System.setProperty(key, property);
        }
    }

    public void setBusinessDataDatabaseConfiguration(BonitaDatabaseConfiguration businessDataDatabaseConfiguration) {
        // FIXME: do something with it

        this.businessDataDatabaseConfiguration = businessDataDatabaseConfiguration;
    }

    public BonitaDatabaseConfiguration getBonitaDatabaseConfiguration() {
        return bonitaDatabaseConfiguration;
    }

    public BonitaDatabaseConfiguration getBusinessDataDatabaseConfiguration() {
        return businessDataDatabaseConfiguration;
    }

    private String resolveProperty(String name) {
        return applicationContext.getBeanFactory().resolveEmbeddedValue("${" + name + "}");
    }
}
