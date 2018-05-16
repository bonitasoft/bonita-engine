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

package org.bonitasoft.platform.setup;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.PLATFORM_ENGINE;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.TENANT_TEMPLATE_PORTAL;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.configuration.util.AllConfigurationResourceVisitor;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.bonitasoft.platform.util.ConfigurationFolderUtil;
import org.bonitasoft.platform.version.VersionService;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * @author Baptiste Mesta
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        PlatformSetupApplication.class
})
public class PlatformSetupIT {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Rule
    public final ClearSystemProperties clearSystemProperties = new ClearSystemProperties(BONITA_SETUP_FOLDER);

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Value("${db.vendor}")
    String dbVendor;

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PlatformSetup platformSetup;

    @Autowired
    VersionService versionService;

    private ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();

    @After
    public void after() throws Exception {
        System.clearProperty(BONITA_SETUP_FOLDER);
        platformSetup.destroy();
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void init_method_should_init_table_and_insert_conf() throws Exception {
        //when
        platformSetup.init();

        //then
        final Integer sequences = jdbcTemplate.queryForObject("select count(*) from sequence", Integer.class);
        assertThat(sequences).isGreaterThan(1);
        final int platformRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "platform");
        assertThat(platformRows).isEqualTo(1);
        final int configurationFiles = JdbcTestUtils.countRowsInTable(jdbcTemplate, "configuration");
        assertThat(configurationFiles).isGreaterThan(1);
    }

    @Test
    public void init_method_should_init_configuration_from_folder_if_exists() throws Exception {
        //given
        File setupFolder = temporaryFolder.newFolder("conf");
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.getAbsolutePath());
        FileUtils.write(setupFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("platform_init_engine")
                .resolve("whatever.properties").toFile(), "custom content", Charset.defaultCharset());
        configurationFolderUtil.buildSqlFolder(setupFolder.toPath(), dbVendor);
        systemOutRule.clearLog();

        //when
        platformSetup.init();

        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM CONFIGURATION WHERE resource_name = 'whatever.properties'");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("resource_content")).isEqualTo("custom content".getBytes());
        assertThat(systemOutRule.getLog())
                .contains("Database will be initialized with configuration files from folder: "
                        + setupFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial").toString());
    }

    @Test
    public void init_method_should_store_tenant_portal_resources_from_classpath() throws Exception {
        //when
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM CONFIGURATION WHERE content_type= '"
                        + ConfigurationType.TENANT_TEMPLATE_PORTAL + "' ORDER BY resource_name");
        assertThat(rows).hasSize(14);
        int rowId = 0;
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("authenticationManager-config.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("autologin-v6.json");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("compound-permissions-mapping-custom.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME"))
                .isEqualTo("compound-permissions-mapping-internal.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("compound-permissions-mapping.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("console-config.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("custom-permissions-mapping.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("dynamic-permissions-checks-custom.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("dynamic-permissions-checks.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("forms-config.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("resources-permissions-mapping-custom.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME"))
                .isEqualTo("resources-permissions-mapping-internal.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("resources-permissions-mapping.properties");
        assertThat(rows.get(rowId++).get("RESOURCE_NAME")).isEqualTo("security-config.properties");
    }

    @Test
    public void init_method_should_store_platform_portal_resources_from_classpath() throws Exception {
        //when
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM CONFIGURATION WHERE content_type= '" + ConfigurationType.PLATFORM_PORTAL
                        + "' ORDER BY resource_name");
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).get("RESOURCE_NAME")).isEqualTo("cache-config.xml");
        assertThat(rows.get(1).get("RESOURCE_NAME")).isEqualTo("platform-tenant-config.properties");
        assertThat(rows.get(2).get("RESOURCE_NAME")).isEqualTo("security-config.properties");
    }

    @Test
    public void init_method_should_store_sample_security_script_from_classpath() throws Exception {
        //when
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList(
                        "SELECT * FROM CONFIGURATION WHERE content_type= '"
                                + ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS + "' ORDER BY resource_name");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("RESOURCE_NAME")).isEqualTo("SamplePermissionRule.groovy.sample");
    }

    @Test
    public void init_method_should_store_security_scripts_from_folder_if_exists() throws Exception {
        //when
        final Path confFolder = temporaryFolder.newFolder().toPath();
        configurationFolderUtil.buildInitialFolder(confFolder);
        configurationFolderUtil.buildSqlFolder(confFolder, dbVendor);

        System.setProperty(BONITA_SETUP_FOLDER, confFolder.toString());

        FileUtils.write(confFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("tenant_template_security_scripts")
                .resolve("SomeCustomScript.groovy").toFile(), "custom content", Charset.defaultCharset());
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList(
                        "SELECT * FROM CONFIGURATION WHERE content_type= '"
                                + ConfigurationType.TENANT_TEMPLATE_SECURITY_SCRIPTS + "' ORDER BY resource_name");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("RESOURCE_NAME")).isEqualTo("SomeCustomScript.groovy");
    }

    @Test
    public void should_extract_configuration() throws Exception {
        final File destinationFolder = temporaryFolder.newFolder("setup");
        //given
        platformSetup.init();

        //when
        System.setProperty(BONITA_SETUP_FOLDER, destinationFolder.getAbsolutePath());
        platformSetup.pull();

        //then
        File folderContainingResultOfGet = destinationFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME)
                .resolve("current").toFile();
        assertThat(folderContainingResultOfGet).as("should retrieve config files")
                .exists()
                .isDirectory();

        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        AllConfigurationResourceVisitor allConfigurationResourceVisitor = new AllConfigurationResourceVisitor(
                configurations);
        Files.walkFileTree(destinationFolder.toPath(), allConfigurationResourceVisitor);

        assertThat(configurations).extracting("resourceName").containsOnly(
                "bonita-platform-community-custom.properties",
                "bonita-platform-custom.xml",
                "bonita-platform-init-custom.xml",
                "cache-config.xml",
                "platform-tenant-config.properties",
                "security-config.properties",
                "bonita-tenant-community-custom.properties",
                "bonita-tenants-custom.xml",
                "authenticationManager-config.properties",
                "compound-permissions-mapping.properties",
                "compound-permissions-mapping-custom.properties",
                "compound-permissions-mapping-internal.properties",
                "console-config.properties",
                "custom-permissions-mapping.properties",
                "dynamic-permissions-checks.properties",
                "dynamic-permissions-checks-custom.properties",
                "forms-config.properties",
                "resources-permissions-mapping.properties",
                "resources-permissions-mapping-custom.properties",
                "resources-permissions-mapping-internal.properties",
                "SamplePermissionRule.groovy.sample",
                "autologin-v6.json");
    }

    @Test
    public void init_method_should_log_when_created() throws Exception {
        //given
        assertThat(platformSetup.isPlatformAlreadyCreated()).isFalse();

        //when
        systemOutRule.clearLog();
        platformSetup.init();

        //then
        assertThat(platformSetup.isPlatformAlreadyCreated()).isTrue();

        final String log = systemOutRule.getLogWithNormalizedLineSeparator();
        final String[] split = log.split("\n");
        assertThat(log).as("should setup log message").doesNotContain("Platform is already created. Nothing to do.");
        assertThat(split).as("should setup log message").isNotEmpty();
        assertThat(log).as("should create platform and log message").contains("Platform created.");
        assertThat(split[split.length - 1]).as("should push Initial configuration and log message")
                .contains("INFO")
                .endsWith("Initial configuration files successfully pushed to database");
    }

    @Test
    public void init_method_should_upgrade_default_configuration_when_already_created() throws Exception {
        //given
        platformSetup.init();

        //when
        systemOutRule.clearLog();
        platformSetup.init();

        //then
        assertThat(platformSetup.isPlatformAlreadyCreated()).isTrue();

        final String log = systemOutRule.getLogWithNormalizedLineSeparator();
        assertThat(log).doesNotContain("Platform created.");
        assertThat(log).contains("Platform is already created.");
        assertThat(log).contains("Upgrading default configuration");

    }

    @Test
    public void push_method_should_log_when_created() throws Exception {
        // given
        platformSetup.init();
        File setupFolder = temporaryFolder.newFolder("conf");
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.getAbsolutePath());
        configurationFolderUtil.buildCurrentFolder(setupFolder.toPath());

        // when
        systemOutRule.clearLog();
        platformSetup.forcePush();

        final String log = systemOutRule.getLogWithNormalizedLineSeparator();
        final String[] split = log.split("\n");

        // then
        assertThat(split[split.length - 1]).as("should push new configuration and log message").contains("INFO")
                .endsWith(
                        "Configuration files successfully pushed to database. You can now restart Bonita to reflect your changes.");
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void push_should_fail_if_required_folder_would_be_deleted() throws Exception {
        // on windows, the test fails to delete the 'platform init engine' directory
        // so do not run it for now
        assumeFalse(IS_OS_WINDOWS);

        // given
        platformSetup.init();
        File setupFolder = temporaryFolder.newFolder("conf");
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.getAbsolutePath());
        platformSetup.pull();
        final Path platform_init_engine = setupFolder.toPath().resolve("platform_conf").resolve("current")
                .resolve("platform_init_engine");
        FileUtils.deleteDirectory(platform_init_engine.toFile());

        // then
        exception.expect(PlatformException.class);
        exception.expectMessage("You are trying to remove a protected folder from configuration");
        exception.expectMessage(platform_init_engine.toString());
        exception.expectMessage("To restore the deleted folders");

        // when
        platformSetup.push();
    }

    @Test
    public void push_should_throw_exception_when_platform_is_not_created() throws Exception {
        //given
        assertThat(platformSetup.isPlatformAlreadyCreated()).isFalse();

        //expect
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Platform is not created. Run 'setup init' first.");

        //when
        platformSetup.push();
    }

    @Test
    public void clean_method_should_delete_and_log() throws Exception {
        //given
        final Path path = temporaryFolder.newFolder("afterClean").toPath();
        final Path licensePath = temporaryFolder.newFolder("licenses").toPath();
        platformSetup.init();

        //when
        systemOutRule.clearLog();
        platformSetup.clean();

        //then
        final String log = systemOutRule.getLogWithNormalizedLineSeparator();
        final String[] split = log.split("\n");
        assertThat(split).as("should log message").isNotEmpty();
        assertThat(split[split.length - 1]).as("should log message").contains("DEBUG")
                .endsWith("Execute DeleteAllConfigurationInTransaction transaction.");

        platformSetup.pull(path, licensePath);
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        Files.walkFileTree(path, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should remove all files").isEmpty();

        Files.walkFileTree(licensePath, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should remove all files").isEmpty();
    }

    @Test
    public void push_method_should_clean_previous_config() throws Exception {
        //given
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        final Path initPath = temporaryFolder.newFolder("init").toPath();
        final Path pushPath = temporaryFolder.newFolder("push").toPath();
        final Path checkPath = temporaryFolder.newFolder("check").toPath();
        final Path licensesPath = temporaryFolder.newFolder("lic").toPath();

        FileUtils.writeByteArrayToFile(
                initPath.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                        .resolve(PLATFORM_ENGINE.name().toLowerCase()).resolve("initial.properties")
                        .toFile(),
                "key1=value1".getBytes());

        FileUtils.writeByteArrayToFile(
                pushPath.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("current")
                        .resolve(TENANT_TEMPLATE_PORTAL.name().toLowerCase())
                        .resolve("current.properties").toFile(),
                "key2=value2".getBytes());

        System.setProperty(BONITA_SETUP_FOLDER, initPath.toString());
        configurationFolderUtil.buildSqlFolder(initPath.toFile().toPath(), dbVendor);
        platformSetup.init();

        //when
        System.setProperty(BONITA_SETUP_FOLDER, pushPath.toString());
        platformSetup.forcePush();

        //then
        platformSetup.pull(checkPath, licensesPath);
        Files.walkFileTree(checkPath, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should remove all files").hasSize(1)
                .extracting("resourceName").containsOnly("current.properties");
    }

    @Test
    public void push_method_should_throw_exception_if_no_current_folder() throws Exception {
        //given
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        final Path confFolder = temporaryFolder.newFolder().toPath();
        configurationFolderUtil.buildInitialFolder(confFolder);
        configurationFolderUtil.buildSqlFolder(confFolder, dbVendor);

        System.setProperty(BONITA_SETUP_FOLDER, confFolder.toString());
        platformSetup.init();
        Path current = confFolder.resolve("platform_conf").resolve("current");

        //when
        try {
            platformSetup.push();
            fail();
        } catch (PlatformException ignored) {
            assertThat(ignored.getMessage()).isEqualTo("Unable to push configuration from " +
                    current +
                    ", as directory does not exists. To modify your configuration, run 'setup pull', update your configuration files from "
                    +
                    current +
                    " folder, and then push your new configuration.");
            //ok
        }
        //then
        platformSetup.pull();
        Files.walkFileTree(current, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should have kept old files").hasSize(1)
                .extracting("resourceName").containsOnly("initialConfig.properties");
    }

    @Test
    public void should_push_check_platform_version() throws Exception {
        //given
        platformSetup.init();
        jdbcTemplate.execute("UPDATE platform SET version='bad version'");

        //then
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage(
                "Platform version [bad version] is not supported by current platform setup version ["
                        + versionService.getPlatformSetupVersion() + "]");

        //when
        final Path confFolder = temporaryFolder.newFolder().toPath();
        configurationFolderUtil.buildCurrentFolder(confFolder);
        System.setProperty(BONITA_SETUP_FOLDER, confFolder.toFile().getAbsolutePath());
        platformSetup.push();
    }

    @Test
    public void should_pull_check_platform_version() throws Exception {
        //given
        platformSetup.init();
        jdbcTemplate.execute("UPDATE platform SET version='bad version'");

        //then
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage(
                "Platform version [bad version] is not supported by current platform setup version ["
                        + versionService.getPlatformSetupVersion() + "]");

        //when
        platformSetup.pull();

    }

    @Test
    public void pushLicences_should_pass_if_licence_folder_does_not_exists() throws Exception {
        platformSetup.initProperties();
        platformSetup.preventFromPushingZeroLicense();
    }

    @Test
    public void should_not_fail_when_pulling_twice_in_the_same_jvm() throws Exception {
        platformSetup.init();
        platformSetup.pull();
        platformSetup.pull();
    }

    @Test
    public void pushLicences_should_fail_if_licence_folder_exists_but_is_empty() throws Exception {
        final Path setupFolder = temporaryFolder.newFolder().toPath();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platform_conf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);

        final Path licenseFolder = platform_conf.resolve("licenses");
        Files.createDirectories(licenseFolder);

        expectedException.expect(PlatformException.class);
        expectedException
                .expectMessage("No license (.lic file) found." + LINE_SEPARATOR
                        + "This would prevent Bonita Platform subscription edition"
                        + " to start normally." + LINE_SEPARATOR + "Place your license file");

        platformSetup.initProperties();
        platformSetup.preventFromPushingZeroLicense();
    }

    @Test
    public void pushLicences_should_fail_if_no_license_file_with_lic_extension_exists() throws Exception {
        final Path setupFolder = temporaryFolder.newFolder().toPath();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platform_conf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);

        final Path licenseFolder = platform_conf.resolve("licenses");
        Files.createDirectories(licenseFolder);
        Files.createFile(licenseFolder.resolve("bonita-file.renamed"));

        expectedException.expect(PlatformException.class);
        expectedException
                .expectMessage("No license (.lic file) found." + LINE_SEPARATOR
                        + "This would prevent Bonita Platform subscription edition"
                        + " to start normally." + LINE_SEPARATOR + "Place your license file");

        platformSetup.initProperties();
        platformSetup.preventFromPushingZeroLicense();
    }

    @Test
    public void init_method_should_update_configuration_files() throws Exception {
        //given
        File setupFolder = temporaryFolder.newFolder("conf");
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.getAbsolutePath());
        final File permissionFile = setupFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("tenant_template_portal")
                .resolve("resources-permissions-mapping.properties").toFile();
        FileUtils.write(permissionFile, "default 7.5.4 content", Charset.defaultCharset());
        configurationFolderUtil.buildSqlFolder(setupFolder.toPath(), dbVendor);
        platformSetup.init();

        // Simulate new 7.6.0 configuration file content:
        final String new_7_6_0_content = "default 7.6.0 content";
        FileUtils.write(permissionFile, new_7_6_0_content, Charset.defaultCharset());

        //when
        platformSetup.init();

        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList(
                        "SELECT * FROM CONFIGURATION WHERE resource_name = 'resources-permissions-mapping.properties'");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("resource_content")).isEqualTo(new_7_6_0_content.getBytes());
    }
}
