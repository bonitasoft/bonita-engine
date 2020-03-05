/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.UserTransaction;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.jndi.JndiTemplate;

@Slf4j
public class BonitaEngine {

    private BonitaDataSourceInitializer bonitaDataSourceInitializer = new BonitaDataSourceInitializer();
    private boolean initialized;
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
        if (!initialized) {
            initialized = true;
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, Collections.emptyMap());
            initializeBonitaDatabaseConfiguration();
            initializeBusinessDataDatabaseConfiguration();
            arjunaTransactionManager = TransactionManager.transactionManager();
            userTransaction = UserTransaction.userTransaction();
            bonitaDataSource = bonitaDataSourceInitializer.createManagedDataSource(bonitaDatabaseConfiguration,
                    arjunaTransactionManager);
            businessDataDataSource = bonitaDataSourceInitializer
                    .createManagedDataSource(businessDataDatabaseConfiguration, arjunaTransactionManager);
            bonitaSequenceManagerDataSource = bonitaDataSourceInitializer.createDataSource(bonitaDatabaseConfiguration);
            notManagedBizDataSource = bonitaDataSourceInitializer.createDataSource(businessDataDatabaseConfiguration);
            initializeJNDI();
        }
    }

    private void initializeBonitaDatabaseConfiguration() {
        if (bonitaDatabaseConfiguration == null || bonitaDatabaseConfiguration.isEmpty()) {
            bonitaDatabaseConfiguration = DefaultBonitaDatabaseConfigurations
                    .defaultConfiguration(System.getProperty(BONITA_DB_VENDOR, "h2"), "bonita");
        }
        setSystemPropertyIfNotSet(BONITA_DB_VENDOR, bonitaDatabaseConfiguration.getDbVendor());
        log.info("Using database configuration for bonita {}", bonitaDatabaseConfiguration);
    }

    private void initializeBusinessDataDatabaseConfiguration() {
        if (businessDataDatabaseConfiguration == null || businessDataDatabaseConfiguration.isEmpty()) {
            businessDataDatabaseConfiguration = DefaultBonitaDatabaseConfigurations
                    .defaultConfiguration(System.getProperty(BONITA_BDM_DB_VENDOR, "h2"), "business_data");
        }
        setSystemPropertyIfNotSet(BONITA_BDM_DB_VENDOR, businessDataDatabaseConfiguration.getDbVendor());
        log.info("Using database configuration for business data {}", businessDataDatabaseConfiguration);
    }

    private void setSystemPropertyIfNotSet(String systemPropertyName, String defaultValue) {
        String value = System.getProperty(systemPropertyName);
        if (value != null) {
            return;
        }
        System.setProperty(systemPropertyName, defaultValue);
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
            throws PlatformLogoutException, SessionNotFoundException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
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

    BasicManagedDataSource getBonitaDataSource() {
        return bonitaDataSource;
    }

    BasicManagedDataSource getBusinessDataDataSource() {
        return businessDataDataSource;
    }
}
