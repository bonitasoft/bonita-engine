/**
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
import org.bonitasoft.platform.configuration.util.DeleteTenantConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetAllConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.GetConfigurationsInTransaction;
import org.bonitasoft.platform.configuration.util.GetMandatoryStructureConfiguration;
import org.bonitasoft.platform.configuration.util.LicensesResourceVisitor;
import org.bonitasoft.platform.configuration.util.StoreConfigurationInTransaction;
import org.bonitasoft.platform.configuration.util.UpdateConfigurationInTransactionForAllTenants;
import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Emmanuel Duchastenier
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final int NON_TENANT_RESOURCE = 0;

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;

    @Value("${db.vendor}")
    private String dbVendor;

    @Autowired
    public ConfigurationServiceImpl(JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    public ConfigurationServiceImpl(JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate, String dbVendor) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
        this.dbVendor = dbVendor;
    }

    @Override
    public List<BonitaConfiguration> getPlatformPortalConf() {
        return getNonTenantResource(PLATFORM_PORTAL);
    }

    @Override
    public List<BonitaConfiguration> getPlatformInitEngineConf() {
        return getNonTenantResource(PLATFORM_INIT_ENGINE);
    }

    @Override
    public List<BonitaConfiguration> getPlatformEngineConf() {
        return getNonTenantResource(PLATFORM_ENGINE);
    }

    @Override
    public List<BonitaConfiguration> getTenantTemplateEngineConf() {
        return getNonTenantResource(TENANT_TEMPLATE_ENGINE);
    }

    @Override
    public List<BonitaConfiguration> getTenantTemplateSecurityScripts() {
        return getNonTenantResource(ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS);
    }

    @Override
    public void storePlatformInitEngineConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, PLATFORM_INIT_ENGINE, NON_TENANT_RESOURCE);
    }

    @Override
    public void storePlatformEngineConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, PLATFORM_ENGINE, NON_TENANT_RESOURCE);
    }

    @Override
    public void storeTenantTemplateEngineConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, TENANT_TEMPLATE_ENGINE, NON_TENANT_RESOURCE);
    }

    @Override
    public void storeTenantTemplateSecurityScripts(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS, NON_TENANT_RESOURCE);
    }

    @Override
    public void storeTenantEngineConf(List<BonitaConfiguration> bonitaConfigurations, long tenantId) {
        storeConfiguration(bonitaConfigurations, TENANT_ENGINE, tenantId);
    }

    @Override
    public void storeTenantSecurityScripts(List<BonitaConfiguration> bonitaConfigurations, long tenantId) {
        storeConfiguration(bonitaConfigurations, ConfigurationType.TENANT_SECURITY_SCRIPTS, tenantId);
    }

    public List<BonitaConfiguration> getAllTenantsPortalConf() {
        return getNonTenantResource(TENANT_PORTAL);
    }

    public List<BonitaConfiguration> getAllTenantsEngineConf() {
        return getNonTenantResource(TENANT_ENGINE);
    }

    @Override
    public void storeTenantConfiguration(File configurationRootFolder, long tenantId) throws PlatformException {
        storeConfiguration(configurationRootFolder, TENANT_PORTAL, tenantId);
    }

    @Override
    public void storePlatformConfiguration(File configurationRootFolder) throws PlatformException {
        storeConfiguration(configurationRootFolder, PLATFORM_ENGINE, NON_TENANT_RESOURCE);
    }

    @Override
    public void storeAllConfiguration(Path configurationRootFolder) throws PlatformException {
        List<FullBonitaConfiguration> fullBonitaConfigurations = new ArrayList<>();
        AllConfigurationResourceVisitor allConfigurationResourceVisitor = new AllConfigurationResourceVisitor(fullBonitaConfigurations);
        try {
            Files.walkFileTree(configurationRootFolder, allConfigurationResourceVisitor);
            transactionTemplate.execute(new CleanAndStoreAllConfigurationInTransaction(jdbcTemplate, dbVendor, fullBonitaConfigurations));
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    @Override
    public void updateDefaultConfigurationForAllTenantsAndTemplate(Path configurationRootFolder) throws PlatformException {
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        try {
            Files.walkFileTree(configurationRootFolder, new AutoUpdateConfigurationVisitor(bonitaConfigurations));
        } catch (IOException e) {
            throw new PlatformException(e);
        }
        updateTenantPortalConfForAllTenantsAndTemplate(bonitaConfigurations);
    }

    @Override
    public void storeTenantTemplatePortalConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, ConfigurationType.TENANT_TEMPLATE_PORTAL, NON_TENANT_RESOURCE);
    }

    @Override
    public void storeTenantPortalConf(List<BonitaConfiguration> bonitaConfigurations, long tenantId) {
        storeConfiguration(bonitaConfigurations, ConfigurationType.TENANT_PORTAL, tenantId);
    }

    @Override
    public void updateTenantPortalConfForAllTenantsAndTemplate(List<BonitaConfiguration> bonitaConfigurations) {
        // update default configuration at TENANT_TEMPLATE_PORTAL level:
        transactionTemplate.execute(
                new UpdateConfigurationInTransactionForAllTenants(jdbcTemplate, dbVendor, bonitaConfigurations,
                        ConfigurationType.TENANT_TEMPLATE_PORTAL));
        // Also update default configuration at TENANT_PORTAL level for all existing tenants:
        transactionTemplate
                .execute(new UpdateConfigurationInTransactionForAllTenants(jdbcTemplate, dbVendor, bonitaConfigurations,
                        ConfigurationType.TENANT_PORTAL));
    }

    @Override
    public void storePlatformPortalConf(List<BonitaConfiguration> bonitaConfigurations) {
        storeConfiguration(bonitaConfigurations, ConfigurationType.PLATFORM_PORTAL, NON_TENANT_RESOURCE);

    }

    @Override
    public List<BonitaConfiguration> getTenantTemplatePortalConf() {
        return getNonTenantResource(ConfigurationType.TENANT_TEMPLATE_PORTAL);
    }

    @Override
    public List<BonitaConfiguration> getTenantPortalConf(long tenantId) {
        return getBonitaConfigurations(ConfigurationType.TENANT_PORTAL, tenantId);
    }

    @Override
    public BonitaConfiguration getTenantPortalConfiguration(long tenantId, String file) {
        return getBonitaConfiguration(ConfigurationType.TENANT_PORTAL, tenantId, file);
    }

    @Override
    public List<File> writeAllConfigurationToFolder(File configurationFolder, File licenseFolder) throws PlatformException {
        FolderResolver folderResolver = new FolderResolver(configurationFolder.toPath(), licenseFolder.toPath());
        List<File> writtenFiles = new ArrayList<>();
        for (FullBonitaConfiguration fullBonitaConfiguration : getAllConfiguration()) {
            File confFile = new File(folderResolver.getFolder(fullBonitaConfiguration), fullBonitaConfiguration.getResourceName());
            writtenFiles.add(confFile);
            LOGGER.debug(String.format("writing file %s to folder %s", confFile.getName(), confFile.getParentFile().getAbsolutePath()));
            try (FileOutputStream output = new FileOutputStream(confFile);) {
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

    private void storeConfiguration(File configurationRootFolder, ConfigurationType type, long tenantId) throws PlatformException {
        final Path path = configurationRootFolder.toPath();
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        ConfigurationResourceVisitor configurationResourceVisitor = new ConfigurationResourceVisitor(bonitaConfigurations);
        try {
            Files.walkFileTree(path, configurationResourceVisitor);
            storeConfiguration(bonitaConfigurations, type, tenantId);
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    private void storeConfiguration(List<BonitaConfiguration> bonitaConfigurations, ConfigurationType type, long tenantId) {
        transactionTemplate.execute(new StoreConfigurationInTransaction(jdbcTemplate, dbVendor, bonitaConfigurations, type, tenantId));
    }

    private void cleanAndStoreConfiguration(List<BonitaConfiguration> bonitaConfigurations, ConfigurationType type, long tenantId) {
        transactionTemplate.execute(new CleanAndStoreConfigurationInTransaction(jdbcTemplate, dbVendor, bonitaConfigurations, type, tenantId));
    }

    List<BonitaConfiguration> getNonTenantResource(ConfigurationType configurationType) {
        return getBonitaConfigurations(configurationType, NON_TENANT_RESOURCE);
    }

    @Override
    public List<BonitaConfiguration> getTenantEngineConf(long tenantId) {
        return getBonitaConfigurations(TENANT_ENGINE, tenantId);
    }

    private List<BonitaConfiguration> getBonitaConfigurations(ConfigurationType type, long tenantId) {
        return transactionTemplate.execute(new GetConfigurationsInTransaction(jdbcTemplate, tenantId, type));
    }

    @Override
    public List<BonitaConfiguration> getTenantSecurityScripts(long tenantId) {
        return getBonitaConfigurations(ConfigurationType.TENANT_SECURITY_SCRIPTS, tenantId);
    }

    private BonitaConfiguration getBonitaConfiguration(ConfigurationType type, long tenantId, String resourceName) {
        return transactionTemplate.execute(new GetConfigurationInTransaction(jdbcTemplate, tenantId, type, resourceName));
    }

    @Override
    public void storeLicenses(File licensesFolder) throws PlatformException {
        final Path path = licensesFolder.toPath();
        List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
        LicensesResourceVisitor licensesResourceVisitor = new LicensesResourceVisitor(bonitaConfigurations);
        try {
            Files.walkFileTree(path, licensesResourceVisitor);
            cleanAndStoreConfiguration(bonitaConfigurations, LICENSES, NON_TENANT_RESOURCE);
        } catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    @Override
    public List<BonitaConfiguration> getLicenses() throws PlatformException {
        return getNonTenantResource(LICENSES);
    }

    @Override
    public void deleteTenantConfiguration(long tenantId) {
        if (tenantId <= 0) {
            throw new IllegalArgumentException("tenantId value " + tenantId + " is not allowed");
        }
        transactionTemplate.execute(new DeleteTenantConfigurationInTransaction(jdbcTemplate, dbVendor, tenantId));
    }

    @Override
    public void deleteAllConfiguration() {
        transactionTemplate.execute(new DeleteAllConfigurationInTransaction(jdbcTemplate));
    }

    @Override
    public List<LightBonitaConfiguration> getMandatoryStructureConfiguration() {
        return transactionTemplate.execute(new GetMandatoryStructureConfiguration(jdbcTemplate));
    }
}
