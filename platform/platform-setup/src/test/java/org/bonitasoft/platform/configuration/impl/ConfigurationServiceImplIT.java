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
package org.bonitasoft.platform.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.jdbc.datasource.init.ScriptUtils.*;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.configuration.util.FolderComparator;
import org.bonitasoft.platform.setup.PlatformSetupApplication;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * @author laurent Leseigneur
 */
@RunWith(SpringJUnit4ClassRunner.class)

//keep order
@SpringApplicationConfiguration(classes = {
        PlatformSetupApplication.class})
@ComponentScan(basePackages = {"org.bonitasoft.platform.setup", "org.bonitasoft.platform.configuration"})
@PropertySource("classpath:/application.properties")
@Component
public class ConfigurationServiceImplIT {

    public static final long TENANT_ID_1 = 1L;
    public static final long TENANT_ID_5 = 5L;
    public static final long TENANT_ID_12 = 12L;

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${db.vendor}")
    String dbVendor;

    @Autowired
    ConfigurationServiceImpl configurationService;

    @Autowired
    DataSource dataSource;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public TestRule clean = new ClearSystemProperties("db.admin.user", "sysprop.bonita.db.vendor", "db.user", "db.password", "db.vendor", "db.server.name=",
            "db.admin.password", "sysprop.bonita.bdm.db.vendor", "db.server.port", "db.database.name");

    @Before
    public void setUpDb() throws Exception {
        dropTables();
        createTables();
    }

    @After
    public void cleanUpDB() throws Exception {
        dropTables();
    }

    @Test
    public void should_add_configuration() throws Exception {
        //given
        final List<BonitaConfiguration> bonitaConfigurations1 = new ArrayList<>();
        bonitaConfigurations1.add(new BonitaConfiguration("resource 1", "resource content1".getBytes()));
        bonitaConfigurations1.add(new BonitaConfiguration("resource 2", "resource content2".getBytes()));

        //when
        configurationService.storePlatformInitEngineConf(bonitaConfigurations1);

        //then
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "configuration")).as("should insert row").isEqualTo(2);

    }

    @Test
    public void should_store_put_resources_in_database() throws Exception {
        //when
        configurationService.storePlatformInitEngineConf(Arrays.asList(
                new BonitaConfiguration("PLATFORM_INIT_ENGINE resource 1", "resource content1".getBytes()),
                new BonitaConfiguration("PLATFORM_INIT_ENGINE resource 2", "resource content2".getBytes())));
        configurationService.storePlatformEngineConf(Collections.singletonList(
                new BonitaConfiguration("resourceOfPlatform.xml", "platform resource content".getBytes())));
        configurationService.storeTenantTemplateEngineConf(Collections.singletonList(
                new BonitaConfiguration("theResourceOfTenantTemplate.xml", "tenantTemplate resource content".getBytes())));
        configurationService.storeTenantEngineConf(getBonitaConfigurationsSample(TENANT_ID_1), TENANT_ID_1);
        configurationService.storeTenantEngineConf(getBonitaConfigurationsSample(TENANT_ID_12), TENANT_ID_12);

        //then
        assertThat(configurationService.getPlatformInitEngineConf()).containsExactly(
                new BonitaConfiguration("PLATFORM_INIT_ENGINE resource 1", "resource content1".getBytes()),
                new BonitaConfiguration("PLATFORM_INIT_ENGINE resource 2", "resource content2".getBytes()));
        assertThat(configurationService.getPlatformEngineConf()).containsExactly(
                new BonitaConfiguration("resourceOfPlatform.xml", "platform resource content".getBytes()));
        assertThat(configurationService.getTenantTemplateEngineConf()).containsExactly(
                new BonitaConfiguration("theResourceOfTenantTemplate.xml", "tenantTemplate resource content".getBytes()));
        assertThat(configurationService.getTenantEngineConf(TENANT_ID_1)).containsExactly(
                new BonitaConfiguration("resourceOfTenant.xml", "resource content in tenant 1".getBytes()));
        assertThat(configurationService.getTenantEngineConf(TENANT_ID_12)).containsExactly(
                new BonitaConfiguration("resourceOfTenant.xml", "resource content in tenant 12".getBytes()));
    }

    @Test
    public void should_store_overwrite_content() throws Exception {
        //given
        configurationService.storeTenantEngineConf(getBonitaConfigurationsSample(TENANT_ID_1), TENANT_ID_1);
        //when
        configurationService.storeTenantEngineConf(Collections.singletonList(
                new BonitaConfiguration("resourceOfTenant.xml", "resource content in tenant 1 modified".getBytes())), 1L);
        //then
        assertThat(configurationService.getTenantEngineConf(1L)).containsExactly(
                new BonitaConfiguration("resourceOfTenant.xml", "resource content in tenant 1 modified".getBytes()));
    }

    @Test
    public void should_store_configuration() throws Exception {
        //given
        final File configFolder = new File(this.getClass().getResource("/conf").getPath());

        //when
        configurationService.storePlatformConfiguration(configFolder);

        //then
        assertThat(configurationService.getPlatformEngineConf()).as("should retrieve configuration")
                .extracting("resourceName")
                .containsOnly("bonita-platform-community.properties", "bonita-platform-custom.xml");
    }

    @Test
    public void should_write_configuration_to_fileSystem() throws Exception {
        //given
        final File configFolder = new File(this.getClass().getResource("/conf").getPath());
        configurationService.storePlatformConfiguration(configFolder);

        //when
        final File destFolder = temporaryFolder.newFolder();
        final File licFolder = temporaryFolder.newFolder();

        configurationService.writeAllConfigurationToFolder(destFolder, licFolder);

        //then
        assertThat(destFolder).as("should retrieve config files")
                .exists()
                .isDirectory();
        new FolderComparator().compare(configFolder, destFolder);

    }

    @Test
    public void should_store_licenses() throws Exception {
        //given
        final File licenseFolder = new File(this.getClass().getResource("/licenses").getPath());

        //when
        configurationService.storeLicenses(licenseFolder);

        //then
        BonitaConfiguration expectedLicense1 = new BonitaConfiguration("license1.lic", "license 1 content".getBytes());
        BonitaConfiguration expectedLicense2 = new BonitaConfiguration("license2.lic", "license 2 content".getBytes());
        assertThat(configurationService.getLicenses()).as("should retrieve configuration").containsOnly(expectedLicense1, expectedLicense2);

    }

    @Test
    public void should_store_licenses_override_previous_licenses() throws Exception {
        //given
        final File givenlicensesFolder = new File(this.getClass().getResource("/licenses").getPath());
        configurationService.storeLicenses(givenlicensesFolder);

        //when
        final File newlicensesFolder = new File(this.getClass().getResource("/newLicenses").getPath());
        configurationService.storeLicenses(newlicensesFolder);

        //then
        BonitaConfiguration newLicense2 = new BonitaConfiguration("license2.lic", "new license 2 content".getBytes());
        BonitaConfiguration newLicense3 = new BonitaConfiguration("license3.lic", "license 3 content".getBytes());
        assertThat(configurationService.getLicenses()).as("should retrieve configuration").containsOnly(newLicense2, newLicense3);

    }

    @Test
    public void should_delete_tenant_configuration() {
        //given
        storeTenantConfiguration(TENANT_ID_5);
        storeTenantConfiguration(TENANT_ID_12);

        //when
        configurationService.deleteTenantConfiguration(TENANT_ID_12);

        //then
        assertThat(configurationService.getTenantSecurityScripts(TENANT_ID_5)).as("should delete only for tenant 12").hasSize(1);
        assertThat(configurationService.getTenantEngineConf(TENANT_ID_5)).as("should delete only for tenant 12").hasSize(1);
        assertThat(configurationService.getTenantPortalConf(TENANT_ID_5)).as("should delete only for tenant 12").hasSize(1);

        assertThat(configurationService.getTenantSecurityScripts(TENANT_ID_12)).as("should delete only for tenant 12").isEmpty();
        assertThat(configurationService.getTenantEngineConf(TENANT_ID_12)).as("should delete only for tenant 12").isEmpty();
        assertThat(configurationService.getTenantPortalConf(TENANT_ID_12)).as("should delete only for tenant 12").isEmpty();

    }

    @Test
    public void should_clean_configuration() throws Exception {
        //given
        final List<BonitaConfiguration> bonitaConfigurations1 = new ArrayList<>();
        bonitaConfigurations1.add(new BonitaConfiguration("resource 1", "resource content1".getBytes()));
        bonitaConfigurations1.add(new BonitaConfiguration("resource 2", "resource content2".getBytes()));
        configurationService.storePlatformInitEngineConf(bonitaConfigurations1);

        //when
        configurationService.deleteAllConfiguration();

        //then
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "configuration")).as("should delete all").isEqualTo(0);

    }

    private void storeTenantConfiguration(long tenantId) {
        configurationService.storeTenantEngineConf(getBonitaConfigurationsSample(tenantId), tenantId);
        configurationService.storeTenantPortalConf(getBonitaConfigurationsSample(tenantId), tenantId);
        configurationService.storeTenantSecurityScripts(getBonitaConfigurationsSample(tenantId), tenantId);
    }

    private List<BonitaConfiguration> getBonitaConfigurationsSample(long tenantId) {
        final String content = "resource content in tenant " + tenantId;
        return Collections.singletonList(
                new BonitaConfiguration("resourceOfTenant.xml", content.getBytes()));
    }

    private void createTables() throws Exception {
        final InputStream createTableResource = this.getClass().getResourceAsStream("/sql/" + dbVendor + "/createTables.sql");
        ScriptUtils.executeSqlScript(getConnection(),
                new EncodedResource(new InputStreamResource(createTableResource)), false, false, DEFAULT_COMMENT_PREFIX, getDefaultStatementSeparator(),
                DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
    }

    private void dropTables() throws Exception {
        final InputStream dropTablesResource = this.getClass().getResourceAsStream("/sql/" + dbVendor + "/dropTables.sql");
        ScriptUtils.executeSqlScript(getConnection(),
                new EncodedResource(new InputStreamResource(dropTablesResource)), true, true, DEFAULT_COMMENT_PREFIX, getDefaultStatementSeparator(),
                DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
    }

    private String getDefaultStatementSeparator() {
        switch (dbVendor) {
            case "sqlserver":
                return "GO";
            default:
                return DEFAULT_STATEMENT_SEPARATOR;
        }
    }

    private Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

}
