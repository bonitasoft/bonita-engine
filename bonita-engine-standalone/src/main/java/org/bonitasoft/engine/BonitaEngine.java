package org.bonitasoft.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

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

    public void initializeEnvironment() throws Exception {
        if (applicationContext == null) {
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, Collections.emptyMap());
            applicationContext = new ClassPathXmlApplicationContext("classpath:local-server.xml");




            applicationContext.refresh();
            initializeJNDI(applicationContext);


        }
    }

    private void initializeJNDI(ClassPathXmlApplicationContext applicationContext) throws NamingException {
        Map<String, Object> jndiMapping = new HashMap<>();
        jndiMapping.put("java:comp/env/bonitaDS",applicationContext.getBean("bonitaDataSource"));
        jndiMapping.put("java:comp/env/bonitaSequenceManagerDS",applicationContext.getBean("bonitaSequenceManagerDataSource"));
        jndiMapping.put("java:comp/env/BusinessDataDS",applicationContext.getBean("businessDataDataSource"));
        jndiMapping.put("java:comp/env/NotManagedBizDataDS",applicationContext.getBean("notManagedBizDataSource"));
        jndiMapping.put("java:comp/env/TransactionManager",applicationContext.getBean("arjunaTransactionManager"));
        jndiMapping.put("java:comp/UserTransaction",applicationContext.getBean("userTransaction"));
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
}
