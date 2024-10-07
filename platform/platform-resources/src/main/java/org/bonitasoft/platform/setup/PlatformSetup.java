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
package org.bonitasoft.platform.setup;

import static java.lang.System.lineSeparator;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.model.LightBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.database.DatabaseVendor;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.version.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Class that setup an environment for the engine to start on.
 * <p/>
 * It creates tables and insert the default configuration
 *
 * @author Baptiste Mesta
 */
@Component
@ConditionalOnSingleCandidate(PlatformSetup.class)
public class PlatformSetup {

    public static final long DEFAULT_TENANT_ID = 1L;

    public static final String BONITA_SETUP_FOLDER = "org.bonitasoft.platform.setup.folder";

    public static final String PLATFORM_CONF_FOLDER_NAME = "platform_conf";

    public static final String BONITA_CLIENT_HOME_FOLDER = "bonita.client.home";

    public static final String BONITA_DB_VENDOR_PROPERTY = "sysprop.bonita.db.vendor";
    public static final String BONITA_BDM_DB_VENDOR_PROPERTY = "sysprop.bonita.bdm.db.vendor";

    protected static final Logger LOGGER = LoggerFactory.getLogger(PlatformSetup.class);

    private final ScriptExecutor scriptExecutor;

    @Getter // Used by distrib bundle tests
    private final ConfigurationService configurationService;

    @Getter
    private final VersionService versionService;

    private final DataSource dataSource;

    protected String dbVendor;
    protected String bdmDbVendor;

    private Path initialConfigurationFolder;
    private Path currentConfigurationFolder;
    private Path backupConfigurationFolder;
    protected Path licensesFolder;
    private Path backupLicensesFolder;
    private final ResourcePatternResolver cpResourceResolver = new PathMatchingResourcePatternResolver(
            PlatformSetup.class.getClassLoader());

    public PlatformSetup(ScriptExecutor scriptExecutor, ConfigurationService configurationService,
            VersionService versionService, DataSource dataSource, @Value("${db.vendor}") String dbVendor,
            @Value("${bdm.db.vendor}") String bdmDbVendor) {
        this.scriptExecutor = scriptExecutor;
        this.configurationService = configurationService;
        this.versionService = versionService;
        this.dataSource = dataSource;
        this.dbVendor = dbVendor;
        this.bdmDbVendor = bdmDbVendor;
    }

    /**
     * Gets the value of the system property indicated by the key {@link PlatformSetup#BONITA_DB_VENDOR_PROPERTY}
     *
     * @return the string value of the system property, or null if there is no property with that key
     */
    public static String getPropertyBonitaDbVendor() {
        return System.getProperty(BONITA_DB_VENDOR_PROPERTY);
    }

    /**
     * Gets the value of the system property indicated by the key {@link PlatformSetup#BONITA_BDM_DB_VENDOR_PROPERTY}
     *
     * @return the string value of the system property, or null if there is no property with that key
     */
    public static String getPropertyBonitaBdmDbVendor() {
        return System.getProperty(BONITA_BDM_DB_VENDOR_PROPERTY);
    }

    /**
     * Entry point that create the tables and insert the default configuration
     */
    public void init() throws PlatformException {
        initPlatformSetup();
        if (isPlatformAlreadyCreated()) {
            LOGGER.info("Platform is already created.");
            if (Files.isDirectory(initialConfigurationFolder)) {
                LOGGER.info("Upgrading default configuration with files from folder: {}", initialConfigurationFolder);
                updateDefaultConfigurationFromFolder(initialConfigurationFolder);
            } else {
                LOGGER.info("Upgrading default configuration with files from classpath");
                updateDefaultConfigurationFromClasspath();
            }
            insertNewConfigurationsFromClasspathIfExist();
            return;
        }
        preventFromPushingZeroLicense();
        initializePlatform();
        LOGGER.info("Platform created.");
        if (Files.isDirectory(initialConfigurationFolder)) {
            LOGGER.info("Database will be initialized with configuration files from folder: {}",
                    initialConfigurationFolder);
            pushFromFolder(initialConfigurationFolder);
        } else {
            LOGGER.warn("Database will be initialized with configuration files from classpath");
            insertNewConfigurationsFromClasspathIfExist();
        }
        pushLicenses(true);
        LOGGER.info("Initial configuration files successfully pushed to database");
        initializeTenant();

    }

    private void initializeTenant() {
        configurationService.storeTenantEngineConf(configurationService.getTenantTemplateEngineConf(),
                DEFAULT_TENANT_ID);
        configurationService
                .storeTenantSecurityScripts(configurationService.getTenantTemplateSecurityScripts(), DEFAULT_TENANT_ID);
        configurationService.storeTenantPortalConf(configurationService.getTenantTemplatePortalConf(),
                DEFAULT_TENANT_ID);
    }

    boolean isPlatformAlreadyCreated() {
        return scriptExecutor.isPlatformAlreadyCreated();
    }

    private void pushFromFolder(Path folderToPush) throws PlatformException {
        configurationService.storeAllConfiguration(folderToPush);
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
     */
    public void push() throws PlatformException {
        push(false);
    }

    public void forcePush() throws PlatformException {
        push(true);
    }

    /**
     * push all configuration files and licenses
     *
     * @param forcePush shall we skip the check for removed folders?
     */
    public void push(boolean forcePush) throws PlatformException {
        initPlatformSetup();
        if (!isPlatformAlreadyCreated()) {
            throw new PlatformException("Platform is not created. Run 'setup init' first.");
        }
        preventFromPushingZeroLicense();
        checkPlatformVersion();
        checkPushFolderExists(currentConfigurationFolder);
        LOGGER.info("Configuration currently in database will be replaced by configuration from folder: {}",
                currentConfigurationFolder);
        ensureNoCriticalFoldersAreDeleted(forcePush);
        pull(backupConfigurationFolder, backupLicensesFolder);
        LOGGER.info("Backup directory created: {}", backupConfigurationFolder);
        var hasLicenses = !getConfigurationService().getLicenses().isEmpty();
        clean();
        pushFromFolder(currentConfigurationFolder);
        pushLicenses(hasLicenses);
        LOGGER.info(
                "Configuration files successfully pushed to database. You can now restart Bonita to reflect your changes.");
    }

    private void ensureNoCriticalFoldersAreDeleted(boolean forcePush) throws PlatformException {
        final List<LightBonitaConfiguration> configurations = configurationService
                .getMandatoryStructureConfiguration();
        for (LightBonitaConfiguration configuration : configurations) {
            // check no mandatory folder from database is no more in the FileSystem and about to be deleted:
            final Path folder = getFolderFromConfiguration(configuration);
            if (!Files.isDirectory(folder)) {
                if (forcePush) {
                    LOGGER.warn("Force-pushing the deletion of folder {}", folder);
                } else {
                    throw new PlatformException("You are trying to remove a protected folder from configuration: " +
                            getSpecificErrorMessage(configuration, folder));
                }
            }
        }
    }

    protected Path getFolderFromConfiguration(LightBonitaConfiguration configuration) {
        if (configuration.getTenantId() == 0L) {
            return currentConfigurationFolder.resolve(configuration.getType().toLowerCase());
        } else {
            return currentConfigurationFolder.resolve("tenants").resolve(configuration.getTenantId().toString())
                    .resolve(configuration.getType().toLowerCase());
        }
    }

    private String getSpecificErrorMessage(LightBonitaConfiguration configuration, Path folder) {
        final String message;
        if (configuration.getTenantId() == 0L) {
            message = "You are not allowed to remove folder '" + folder.toString() + "'";
        } else {
            message = "You are not allowed to remove configuration folder for tenant " + configuration.getTenantId() +
                    ". To remove a tenant, please search for 'Platform API' on https://documentation.bonitasoft.com";
        }
        return message
                + lineSeparator()
                + "To restore the deleted folders, run 'setup pull'. You will lose the locally modified configuration.";
    }

    /**
     * Entry point to retrieve all configuration files and write them to folder
     * each file will be located under sub folder according to its purpose. See
     * {@link org.bonitasoft.platform.configuration.type.ConfigurationType} for all
     * available values
     * For tenant specific files, a tenants/[TENANT_ID] folder is created prior to configuration type
     */
    public void pull() throws PlatformException {
        initPlatformSetup();
        checkPlatformVersion();
        LOGGER.info("Pulling configuration into folder: {}", currentConfigurationFolder);
        if (Files.isDirectory(licensesFolder)) {
            LOGGER.info("Pulling licenses into folder: {}", licensesFolder);
        }
        pull(currentConfigurationFolder, licensesFolder);
        LOGGER.info(
                "Configuration (and license) files successfully pulled. You can now edit them. Use \"setup push\" when done.");
    }

    public void pull(Path configurationFolder, Path licensesFolder) throws PlatformException {
        try {
            recreateDirectory(configurationFolder);
            if (Files.isDirectory(licensesFolder)) {
                FileUtils.cleanDirectory(licensesFolder.toFile());
            }
            List<File> licenses = new ArrayList<>();
            List<File> files = configurationService.writeAllConfigurationToFolder(configurationFolder.toFile(),
                    licensesFolder.toFile());
            LOGGER.info("Retrieved following files in {}", configurationFolder);
            for (File file : files) {
                if (file.toPath().getParent().equals(licensesFolder)) {
                    licenses.add(file);
                } else {
                    LOGGER.info(configurationFolder.relativize(file.toPath()).toString());
                }
            }
            if (!licenses.isEmpty()) {
                LOGGER.info("Retrieved following licenses in {}", licensesFolder);
                for (File license : licenses) {
                    LOGGER.info(licensesFolder.relativize(license.toPath()).toString());
                }
            }
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
            String message = "The version of the platform (binaries) you are running [{0}] " +
                    "only support database schema in version [{1}] but the current database schema version is [{2}]. " +
                    "You might need to migrate your platform or use a different version of the binaries.";
            throw new PlatformException(MessageFormat.format(message,
                    versionService.getPlatformSetupVersion(),
                    versionService.getSupportedDatabaseSchemaVersion(),
                    versionService.retrieveDatabaseSchemaVersion()));
        }
    }

    /**
     * lookup for license file and push them to database
     */
    protected void pushLicenses(boolean hasLicenses) throws PlatformException {
        // Nothing to do in community
    }

    private void initializePlatform() throws PlatformException {
        scriptExecutor.createAndInitializePlatformIfNecessary();
    }

    void initProperties() throws PlatformException {
        if (dbVendor == null) {
            dbVendor = getPropertyBonitaDbVendor();
        }
        checkSupportedVendor(dbVendor);

        if (bdmDbVendor == null) {
            bdmDbVendor = getPropertyBonitaBdmDbVendor();
        }
        checkSupportedVendor(bdmDbVendor);

        String setupFolderPath = System.getProperty(BONITA_SETUP_FOLDER);
        Path platformConfFolder;
        if (setupFolderPath != null) {
            LOGGER.info("System property {} is set to {}", BONITA_SETUP_FOLDER, setupFolderPath);
            platformConfFolder = Paths.get(setupFolderPath).resolve(PLATFORM_CONF_FOLDER_NAME);
        } else {
            platformConfFolder = Paths.get(PLATFORM_CONF_FOLDER_NAME);
        }
        initializeFoldersPaths(platformConfFolder);
    }

    /**
     * Check that the given `databaseVendor` is supported using the community edition.
     *
     * @param databaseVendor database vendor to check
     * @throws PlatformException if the given database vendor is not supported
     * @throws IllegalArgumentException if `databaseVendor` is null or is not a recognized database vendor
     */
    protected void checkSupportedVendor(String databaseVendor) throws PlatformException, IllegalArgumentException {
        DatabaseVendor vendor = DatabaseVendor.parseValue(databaseVendor);
        if (vendor != DatabaseVendor.H2 && vendor != DatabaseVendor.POSTGRES) {
            throw new PlatformException("Database vendor '" + vendor + "' is not supported with the community edition");
        }
        LOGGER.debug("Database vendor '{}' is supported", vendor);
    }

    private void initializeFoldersPaths(Path platformConfFolder) {
        initialConfigurationFolder = platformConfFolder.resolve("initial");
        currentConfigurationFolder = platformConfFolder.resolve("current");

        Path rootBackupFolder = platformConfFolder.resolve("backup-" + System.currentTimeMillis());
        backupConfigurationFolder = rootBackupFolder.resolve("current");
        backupLicensesFolder = rootBackupFolder.resolve("licenses");

        licensesFolder = getLicenseInitialFolder(platformConfFolder);
    }

    private Path getLicenseInitialFolder(Path platformConfFolder) {
        final String bonita_client_home = System.getProperty(BONITA_CLIENT_HOME_FOLDER);
        if (bonita_client_home != null) {
            return Paths.get(bonita_client_home);
        }
        return platformConfFolder.resolve("licenses");
    }

    private void updateDefaultConfigurationFromFolder(Path folderToPush) throws PlatformException {
        configurationService.updateDefaultConfigurationForAllTenantsAndTemplate(folderToPush);
    }

    private void insertNewConfigurationsFromClasspathIfExist() throws PlatformException {
        final ArrayList<FullBonitaConfiguration> configurations = new ArrayList<>();
        final List<Long> allTenants = configurationService.getAllTenants();
        try {
            configurations.addAll(getConfigurationsMatchingPattern(PLATFORM_ENGINE, allTenants));
            configurations.addAll(getConfigurationsMatchingPattern(PLATFORM_PORTAL, allTenants));
            configurations.addAll(getConfigurationsMatchingPattern(TENANT_TEMPLATE_ENGINE, allTenants));
            configurations.addAll(getConfigurationsMatchingPattern(TENANT_TEMPLATE_PORTAL, allTenants));
            configurations.addAll(getConfigurationsMatchingPattern(TENANT_TEMPLATE_SECURITY_SCRIPTS, allTenants));

        } catch (IOException e) {
            throw new PlatformException(e);
        }
        configurationService.storeConfigurationsIfNotExist(configurations);
    }

    public List<FullBonitaConfiguration> getConfigurationsMatchingPattern(ConfigurationType type, List<Long> allTenants)
            throws IOException {
        final ArrayList<FullBonitaConfiguration> configurations = new ArrayList<>();
        final String typeLowercase = type.name().toLowerCase();
        Resource[] resources = cpResourceResolver
                .getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + typeLowercase + "/**");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                String resourceName = resource.getFilename();
                LOGGER.debug("Found configuration file '{}' of type '{}' in classpath", resourceName, type);
                try (InputStream resourceAsStream = resource.getInputStream()) {
                    final byte[] content = IOUtils.toByteArray(resourceAsStream);
                    // insert the file both at platform level for the template...
                    // eg. (TENANT_TEMPLATE_ENGINE, "bonita-tenant-sp-custom.xml", 0L):
                    configurations.add(new FullBonitaConfiguration(resourceName, content, type.name(), 0L));
                    if (typeLowercase.contains("_template_")) {
                        // also add a version of the configuration file for each existing tenant.
                        // eg. (TENANT_ENGINE, "bonita-tenant-sp-custom.xml", tenantId):
                        for (Long tenantId : allTenants) {
                            configurations.add(new FullBonitaConfiguration(resourceName, content,
                                    type.name().replace("_TEMPLATE", ""), tenantId));
                        }
                    }
                }
            }
        }
        return configurations;
    }

    private void updateDefaultConfigurationFromClasspath() throws PlatformException {
        List<BonitaConfiguration> portalTenant = new ArrayList<>(3);
        try {
            addIfExists(portalTenant, TENANT_TEMPLATE_PORTAL, "compound-permissions-mapping.properties");
            addIfExists(portalTenant, TENANT_TEMPLATE_PORTAL, "dynamic-permissions-checks.properties");
            addIfExists(portalTenant, TENANT_TEMPLATE_PORTAL, "resources-permissions-mapping.properties");
        } catch (IOException e) {
            throw new PlatformException(e);
        }
        configurationService.updateTenantPortalConfForAllTenantsAndTemplate(portalTenant);
    }

    private void addIfExists(List<BonitaConfiguration> configurations, ConfigurationType configurationType,
            String resourceName) throws IOException {
        BonitaConfiguration bonitaConfiguration = getBonitaConfigurationFromClassPath(
                configurationType.name().toLowerCase(), resourceName);
        if (bonitaConfiguration != null) {
            configurations.add(bonitaConfiguration);
        }
    }

    private void initDataSource() throws PlatformException {
        try {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                LOGGER.info("Connected to '{}' database with url: '{}' with user: '{}'", dbVendor, metaData.getURL(),
                        metaData.getUserName());
            }
        } catch (SQLException e) {
            throw new PlatformException(e);
        }
    }

    private BonitaConfiguration getBonitaConfigurationFromClassPath(String folder, String resourceName)
            throws IOException {
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream("/" + folder + "/" + resourceName)) {
            if (resourceAsStream == null) {
                return null;
            }
            LOGGER.debug("Using configuration from classpath {}", resourceName);
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
    }

    void preventFromPushingZeroLicense() throws PlatformException {
        if (Files.isDirectory(licensesFolder)) {
            final String[] licenseFiles = licensesFolder.toFile().list(new RegexFileFilter(".*\\.lic"));
            if (licenseFiles == null || licenseFiles.length == 0) {
                throw new PlatformException("No license (.lic file) found." + lineSeparator()
                        + "This would prevent Bonita Platform subscription edition to start normally." + lineSeparator()
                        + "Place your license file in '" + licensesFolder.toAbsolutePath().toString()
                        + "' and then try again.");
            }
        }
    }
}
