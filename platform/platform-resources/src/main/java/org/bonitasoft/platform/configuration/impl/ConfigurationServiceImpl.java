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
package org.bonitasoft.platform.configuration.impl;

import static org.bonitasoft.platform.configuration.type.ConfigurationType.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.model.LightBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.configuration.util.AllConfigurationResourceVisitor;
import org.bonitasoft.platform.configuration.util.AutoUpdateConfigurationVisitor;
import org.bonitasoft.platform.configuration.util.CleanAndStoreAllConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.CleanAndStoreConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.ConfigurationResourceVisitor;
import org.bonitasoft.platform.configuration.util.DeleteAllConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetAllConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetConfigurationsInTransaction;
import org.bonitasoft.platform.configuration.util.GetMandatoryStructureConfiguration;
import org.bonitasoft.platform.configuration.util.LicensesResourceVisitor;
import org.bonitasoft.platform.configuration.util.StoreConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.StoreConfigurationsIfNotExist;
import org.bonitasoft.platform.configuration.util.UpdateConfigurationInTransaction;
import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Emmanuel Duchastenier
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    private final String dbVendor;

    public ConfigurationServiceImpl(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate,
            @Value("${db.vendor}") String dbVendor) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dbVendor = dbVendor;
    }

    @Override
    public List<BonitaConfiguration> getPlatformPortalConf() {
        return getBonitaConfigurations(PLATFORM_PORTAL);
    }

    @Override
    public List<BonitaConfiguration> getPlatformEngineConf() {
        return getBonitaConfigurations(PLATFORM_ENGINE);
    }

    @Override
    public void storeTenantSecurityScripts(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, TENANT_SECURITY_SCRIPTS);
    }

    @Override
    public void storePlatformConfiguration(File configurationRootFolder) throws PlatformException {
        storeConfiguration(configurationRootFolder, PLATFORM_ENGINE);
    }

    @Override
    public void storeAllConfiguration(Path configurationRootFolder) throws PlatformException {
        List<FullBonitaConfiguration> fullBonitaConfigurations = new ArrayList<>();
        AllConfigurationResourceVisitor allConfigurationResourceVisitor = new AllConfigurationResourceVisitor(
                fullBonitaConfigurations);
        try {
            Files.walkFileTree(configurationRootFolder, allConfigurationResourceVisitor);
            transactionTemplate.execute(
                    new CleanAndStoreAllConfigurationInTransaction(jdbcTemplate, dbVendor, fullBonitaConfigurations));
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    @Override
    public void updateDefaultConfiguration(Path configurationRootFolder)
            throws PlatformException {
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        try {
            Files.walkFileTree(configurationRootFolder, new AutoUpdateConfigurationVisitor(bonitaConfigurations));
        } catch (IOException e) {
            throw new PlatformException(e);
        }
        updateTenantPortalConf(bonitaConfigurations);
    }

    @Override
    public void storeTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, TENANT_PORTAL);
    }

    @Override
    public void updateTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations) {
        // update default configuration at TENANT_PORTAL level:
        transactionTemplate.execute(
                new UpdateConfigurationInTransaction(jdbcTemplate, dbVendor, bonitaConfigurations, TENANT_PORTAL));
    }

    @Override
    public List<BonitaConfiguration> getTenantPortalConf() {
        return getBonitaConfigurations(TENANT_PORTAL);
    }

    @Override
    public BonitaConfiguration getTenantPortalConfiguration(String file) {
        return getBonitaConfiguration(TENANT_PORTAL, file);
    }

    @Override
    public List<File> writeAllConfigurationToFolder(File configurationFolder, File licenseFolder)
            throws PlatformException {
        FolderResolver folderResolver = new FolderResolver(configurationFolder.toPath(), licenseFolder.toPath());
        List<File> writtenFiles = new ArrayList<>();
        for (FullBonitaConfiguration fullBonitaConfiguration : getAllConfiguration()) {
            File confFile = new File(folderResolver.getFolder(fullBonitaConfiguration),
                    fullBonitaConfiguration.getResourceName());
            writtenFiles.add(confFile);
            LOGGER.debug(String.format("writing file %s to folder %s", confFile.getName(),
                    confFile.getParentFile().getAbsolutePath()));
            try (FileOutputStream output = new FileOutputStream(confFile)) {
                IOUtils.write(fullBonitaConfiguration.getResourceContent(), output);
            } catch (IOException e) {
                throw new PlatformException(e);
            }
        }
        return writtenFiles;
    }

    protected List<FullBonitaConfiguration> getAllConfiguration() {
        return transactionTemplate.execute(new GetAllConfigurationInTransaction(jdbcTemplate));
    }

    private void storeConfiguration(File configurationRootFolder, ConfigurationType type)
            throws PlatformException {
        final Path path = configurationRootFolder.toPath();
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        ConfigurationResourceVisitor configurationResourceVisitor = new ConfigurationResourceVisitor(
                bonitaConfigurations);
        try {
            Files.walkFileTree(path, configurationResourceVisitor);
            storeConfiguration(bonitaConfigurations, type);
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    private void storeConfiguration(List<BonitaConfiguration> bonitaConfigurations, ConfigurationType type) {
        transactionTemplate.execute(
                new StoreConfigurationInTransaction(jdbcTemplate, dbVendor, bonitaConfigurations, type));
    }

    private void cleanAndStoreLicenseConfiguration(List<BonitaConfiguration> bonitaConfigurations) {
        transactionTemplate.execute(new CleanAndStoreConfigurationInTransaction(jdbcTemplate, dbVendor,
                bonitaConfigurations, ConfigurationType.LICENSES));
    }

    @Override
    public List<BonitaConfiguration> getTenantEngineConf() {
        return getBonitaConfigurations(TENANT_ENGINE);
    }

    List<BonitaConfiguration> getBonitaConfigurations(ConfigurationType type) {
        return transactionTemplate.execute(new GetConfigurationsInTransaction(jdbcTemplate, type));
    }

    @Override
    public List<BonitaConfiguration> getTenantSecurityScripts() {
        return getBonitaConfigurations(TENANT_SECURITY_SCRIPTS);
    }

    private BonitaConfiguration getBonitaConfiguration(ConfigurationType type, String resourceName) {
        return transactionTemplate
                .execute(new GetConfigurationInTransaction(jdbcTemplate, type, resourceName));
    }

    @Override
    public void storeLicenses(File licensesFolder) throws PlatformException {
        final Path path = licensesFolder.toPath();
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        LicensesResourceVisitor licensesResourceVisitor = new LicensesResourceVisitor(bonitaConfigurations);
        try {
            Files.walkFileTree(path, licensesResourceVisitor);
            cleanAndStoreLicenseConfiguration(bonitaConfigurations);
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    @Override
    public List<BonitaConfiguration> getLicenses() {
        return getBonitaConfigurations(LICENSES);
    }

    @Override
    public void deleteAllConfiguration() {
        transactionTemplate.execute(new DeleteAllConfigurationInTransaction(jdbcTemplate));
    }

    @Override
    public List<LightBonitaConfiguration> getMandatoryStructureConfiguration() {
        return transactionTemplate.execute(new GetMandatoryStructureConfiguration(jdbcTemplate));
    }

    @Override
    public void storeConfigurationsIfNotExist(List<FullBonitaConfiguration> configurations) {
        transactionTemplate.execute(new StoreConfigurationsIfNotExist(jdbcTemplate, dbVendor, configurations));
    }
}
