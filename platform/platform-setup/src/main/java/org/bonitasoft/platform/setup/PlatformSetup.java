/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.platform.setup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.bonitasoft.platform.version.impl.VersionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Class that setup an environment for the engine to start on.
 * <p/>
 * It creates tables and insert the default configuration
 *
 * @author Baptiste Mesta
 */
@Component
public class PlatformSetup {

    public static final String BONITA_SETUP_FOLDER = "org.bonitasoft.platform.setup.folder";

    static final String BONITA_SETUP_ACTION = "org.bonitasoft.platform.setup.action";

    private final static Logger LOGGER = LoggerFactory.getLogger(PlatformSetup.class);

    public static final String PLATFORM_CONF_FOLDER_NAME = "platform_conf";

    public static final String BONITA_CLIENT_HOME_FOLDER = "bonita.client.home";

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private VersionService versionService;

    @Value("${db.vendor}")
    private String dbVendor;

    @Autowired
    private DataSource dataSource;

    private Path initialConfigurationFolder;
    private Path currentConfigurationFolder;
    private Path licensesFolder;

    PlatformSetup(ScriptExecutor scriptExecutor, ConfigurationService configurationService, VersionService versionService, DataSource dataSource) {
        this.scriptExecutor = scriptExecutor;
        this.configurationService = configurationService;
        this.versionService = versionService;
        this.dataSource = dataSource;
    }

    public PlatformSetup(String dbVendor) {
        this.dbVendor = dbVendor;
    }

    public PlatformSetup() {
    }

    public void setDataSource(String driverClassName, String username, String password, String url) {
        dataSource = new DataSourceBuilder(PlatformSetup.class.getClassLoader())
                .driverClassName(driverClassName)
                .username(username)
                .password(password)
                .url(url)
                .build();
    }

    /**
     * Entry point that create the tables and insert the default configuration
     *
     * @throws PlatformException
     */
    public void init() throws PlatformException {
        initPlatformSetup();
        if (isPlatformAlreadyCreated()) {
            LOGGER.info("Platform is already created. Nothing to do.");
            return;
        }
        preventFromPushingZeroLicense();
        initializePlatform();
        LOGGER.info("Platform created.");
        if (Files.isDirectory(initialConfigurationFolder)) {
            LOGGER.info("Database will be initialized with configuration files from folder: " + initialConfigurationFolder.toString());
            pushFromFolder(initialConfigurationFolder);
        } else {
            LOGGER.warn("Database will be initialized with configuration files from classpath");
            initConfigurationWithClasspath();
        }
        pushLicenses();
        LOGGER.info("Initial configuration files successfully pushed to database");
    }

    boolean isPlatformAlreadyCreated() {
        return scriptExecutor.isPlatformAlreadyCreated();
    }

    private void pushFromFolder(Path folderToPush) throws PlatformException {
        configurationService.storeAllConfiguration(folderToPush.toFile());
    }

    private void checkPushFolderExists(Path folderToPush) throws PlatformException {
        if (!Files.isDirectory(folderToPush)) {
            throw new PlatformException(
                    "Unable to push configuration from " + folderToPush
                            + ", as directory does not exists. To modify your configuration, run 'setup pull', update your configuration files from "
                            + currentConfigurationFolder + " folder, and then push your new configuration.");
        }
    }

    void clean() {
        configurationService.deleteAllConfiguration();
    }

    /**
     * push all configuration files and licenses
     *
     * @throws PlatformException
     */
    public void push() throws PlatformException {
        initPlatformSetup();
        if (!isPlatformAlreadyCreated()) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        preventFromPushingZeroLicense();
        checkPlatformVersion();
        checkPushFolderExists(currentConfigurationFolder);
        LOGGER.info("Configuration currently in database will be replace by configuration from folder: " + currentConfigurationFolder.toString());
        clean();
        pushFromFolder(currentConfigurationFolder);
        pushLicenses();
        LOGGER.info("Configuration files successfully pushed to database. You can now restart Bonita BPM to reflect your changes.");
    }

    /**
     * Entry point to retrieve all configuration files and write them to folder
     * each file will be located under sub folder according to its purpose. See {@link org.bonitasoft.platform.configuration.type.ConfigurationType} for all
     * available values
     * For tenant specific files, a tenants/[TENANT_ID] folder is created prior to configuration type
     *
     * @throws PlatformException
     */
    void pull() throws PlatformException {
        initPlatformSetup();
        checkPlatformVersion();
        LOGGER.info("Pulling configuration into folder: " + currentConfigurationFolder);
        if (Files.isDirectory(licensesFolder)) {
            LOGGER.info("Pulling licenses into folder: " + licensesFolder);
        }
        pull(currentConfigurationFolder, licensesFolder);
        LOGGER.info("Configuration (and license) files successfully pulled. You can now edit them. Use \"setup push\" when done.");
    }

    public void pull(Path configurationFolder, Path licensesFolder) throws PlatformException {
        try {
            recreateDirectory(configurationFolder);
            if (Files.isDirectory(licensesFolder)) {
                FileUtils.cleanDirectory(licensesFolder.toFile());
            }
            configurationService.writeAllConfigurationToFolder(configurationFolder.toFile(), licensesFolder.toFile());
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    private void recreateDirectory(Path... folders) throws IOException {
        for (Path folder : folders) {
            if (Files.exists(folder)) {
                FileUtils.deleteDirectory(folder.toFile());
            }
            Files.createDirectories(folder);
        }
    }

    private void checkPlatformVersion() throws PlatformException {
        if (!versionService.isValidPlatformVersion()) {
            throw new PlatformException("Platform version [" + versionService.getPlatformVersion() +
                    "] is not supported by current platform setup version [" + versionService.getPlatformSetupVersion() + "]");
        }
    }

    /**
     * lookup for license file and push them to database
     *
     * @throws PlatformException
     */
    private void pushLicenses() throws PlatformException {
        if (!Files.isDirectory(licensesFolder)) {
            //do nothing in community
            return;
        }
        LOGGER.info("Pushing license files from folder:" + licensesFolder.toString());
        configurationService.storeLicenses(licensesFolder.toFile());
    }

    private void initializePlatform() throws PlatformException {
        scriptExecutor.createAndInitializePlatformIfNecessary();
    }

    void initProperties() {
        if (dbVendor == null) {
            dbVendor = System.getProperty("sysprop.bonita.db.vendor");
        }
        String setupFolderPath = System.getProperty(BONITA_SETUP_FOLDER);
        Path platformConfFolder;
        if (setupFolderPath != null) {
            LOGGER.info("System property " + BONITA_SETUP_FOLDER + " is set to " + setupFolderPath);
            platformConfFolder = Paths.get(setupFolderPath).resolve(PLATFORM_CONF_FOLDER_NAME);
        } else {
            platformConfFolder = Paths.get(PLATFORM_CONF_FOLDER_NAME);
        }
        initializeFoldersPaths(platformConfFolder);
    }

    private void initializeFoldersPaths(Path platformConfFolder) {
        initialConfigurationFolder = platformConfFolder.resolve("initial");
        currentConfigurationFolder = platformConfFolder.resolve("current");
        licensesFolder = getLicenseInitialFolder(platformConfFolder);
    }

    private Path getLicenseInitialFolder(Path platformConfFolder) {
        final String bonita_client_home = System.getProperty(BONITA_CLIENT_HOME_FOLDER);
        if (bonita_client_home != null) {
            return Paths.get(bonita_client_home);
        }
        return platformConfFolder.resolve("licenses");
    }

    private void initConfigurationWithClasspath() throws PlatformException {
        try {
            List<BonitaConfiguration> platformInitConfigurations = new ArrayList<>();
            addIfExists(platformInitConfigurations, ConfigurationType.PLATFORM_INIT_ENGINE, "bonita-platform-init-community-custom.properties");
            addIfExists(platformInitConfigurations, ConfigurationType.PLATFORM_INIT_ENGINE, "bonita-platform-init-custom.xml");
            configurationService.storePlatformInitEngineConf(platformInitConfigurations);

            List<BonitaConfiguration> platformConfigurations = new ArrayList<>();
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-community-custom.properties");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-custom.xml");
            //SP
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-private-community.properties");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-sp-custom.properties");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-sp-cluster-custom.properties");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-sp-custom.xml");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-platform-hibernate-cache.xml");
            addIfExists(platformConfigurations, ConfigurationType.PLATFORM_ENGINE, "bonita-tenant-hibernate-cache.xml");
            configurationService.storePlatformEngineConf(platformConfigurations);

            List<BonitaConfiguration> tenantTemplateConfigurations = new ArrayList<>();
            addIfExists(tenantTemplateConfigurations, ConfigurationType.TENANT_TEMPLATE_ENGINE, "bonita-tenant-community-custom.properties");
            addIfExists(tenantTemplateConfigurations, ConfigurationType.TENANT_TEMPLATE_ENGINE, "bonita-tenants-custom.xml");
            //SP
            addIfExists(tenantTemplateConfigurations, ConfigurationType.TENANT_TEMPLATE_ENGINE, "bonita-tenant-sp-custom.properties");
            addIfExists(tenantTemplateConfigurations, ConfigurationType.TENANT_TEMPLATE_ENGINE, "bonita-tenant-sp-cluster-custom.properties");
            addIfExists(tenantTemplateConfigurations, ConfigurationType.TENANT_TEMPLATE_ENGINE, "bonita-tenant-sp-custom.xml");
            configurationService.storeTenantTemplateEngineConf(tenantTemplateConfigurations);

            List<BonitaConfiguration> securityScripts = new ArrayList<>();
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ActorMemberPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ActorPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "CaseContextPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "CasePermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "CaseVariablePermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "CommentPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ConnectorInstancePermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "DocumentPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessConfigurationPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessConnectorDependencyPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessInstantiationPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessResolutionProblemPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProcessSupervisorPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProfileEntryPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "ProfilePermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "TaskExecutionPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "TaskPermissionRule.groovy");
            addIfExists(securityScripts, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, "UserPermissionRule.groovy");
            configurationService.storeTenantTemplateSecurityScripts(securityScripts);

            List<BonitaConfiguration> portalTenantTemplate = new ArrayList<>();
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "authenticationManager-config.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "compound-permissions-mapping.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "console-config.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "custom-permissions-mapping.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "dynamic-permissions-checks.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "forms-config.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "resources-permissions-mapping.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "security-config.properties");
            addIfExists(portalTenantTemplate, ConfigurationType.TENANT_TEMPLATE_PORTAL, "autologin-v6.json");

            configurationService.storeTenantTemplatePortalConf(portalTenantTemplate);

            List<BonitaConfiguration> portalPlatform = new ArrayList<>();
            addIfExists(portalPlatform, ConfigurationType.PLATFORM_PORTAL, "cache-config.xml");
            addIfExists(portalPlatform, ConfigurationType.PLATFORM_PORTAL, "jaas-standard.cfg");
            addIfExists(portalPlatform, ConfigurationType.PLATFORM_PORTAL, "platform-tenant-config.properties");
            addIfExists(portalPlatform, ConfigurationType.PLATFORM_PORTAL, "security-config.properties");
            configurationService.storePlatformPortalConf(portalPlatform);

        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    private void addIfExists(List<BonitaConfiguration> tenantTemplateConfigurations, ConfigurationType configurationType, String resourceName)
            throws IOException {
        BonitaConfiguration bonitaConfiguration = getBonitaConfigurationFromClassPath(configurationType.name().toLowerCase(), resourceName);
        if (bonitaConfiguration != null) {
            tenantTemplateConfigurations.add(bonitaConfiguration);
        }
    }

    private void initServices() throws PlatformException {
        if (scriptExecutor == null) {
            scriptExecutor = new ScriptExecutor(dbVendor, dataSource);
        }
        if (configurationService == null) {
            final DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
            configurationService = new ConfigurationServiceImpl(new JdbcTemplate(dataSource), new TransactionTemplate(dataSourceTransactionManager), dbVendor);
        }
        if (versionService == null) {
            versionService = new VersionServiceImpl(new JdbcTemplate(dataSource), dbVendor);
        }
    }

    private void initDataSource() throws PlatformException {
        try {
            if (dataSource == null) {
                dataSource = new DataSourceLookup().lookup();
            }
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                LOGGER.info("Connected to '" + dbVendor + "' database with url: '" + metaData.getURL() + "' with user: '" + metaData.getUserName() + "'");
            }
        } catch (NamingException | SQLException e) {
            throw new PlatformException(e);
        }
    }

    private BonitaConfiguration getBonitaConfigurationFromClassPath(String folder, String resourceName) throws IOException {
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/" + folder + "/" + resourceName)) {
            if (resourceAsStream == null) {
                return null;
            }
            LOGGER.debug("Using configuration from classpath " + resourceName);
            return new BonitaConfiguration(resourceName, IOUtils.toByteArray(resourceAsStream));
        }
    }

    public void destroy() throws PlatformException {
        initPlatformSetup();
        if (isPlatformAlreadyCreated()) {
            scriptExecutor.deleteTables();
        }
    }

    public void initPlatformSetup() throws PlatformException {
        initProperties();
        initDataSource();
        initServices();
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    void preventFromPushingZeroLicense() throws PlatformException {
        if (Files.isDirectory(licensesFolder)) {
            final String[] licenseFiles = licensesFolder.toFile().list(new RegexFileFilter(".*\\.lic"));
            if (licenseFiles.length == 0) {
                throw new PlatformException("No license (.lic file) found.\n"
                        + "This would prevent Bonita BPM Platform subscription edition to start normally.\n" +
                        "Place your license file in " + licensesFolder.toString() + " and then try again.");
            }
        }
    }
}
