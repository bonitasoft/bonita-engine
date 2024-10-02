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

import static java.lang.System.lineSeparator;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.PLATFORM_ENGINE;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.TENANT_TEMPLATE_PORTAL;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.configuration.util.AllConfigurationResourceVisitor;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.bonitasoft.platform.util.ConfigurationFolderUtil;
import org.bonitasoft.platform.version.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * @author Baptiste Mesta
 */
@ExtendWith({ SpringExtension.class, OutputCaptureExtension.class })
@SpringBootTest(classes = {
        PlatformSetupApplication.class
})
class PlatformSetupIT {

    @Value("${db.vendor}")
    private String dbVendor;

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PlatformSetup platformSetup;

    @Autowired
    VersionService versionService;

    private final ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();

    @BeforeEach
    void setUp() throws Exception {
        System.clearProperty(BONITA_SETUP_FOLDER);
        platformSetup.destroy();
    }

    @Test
    @Tag("community-only")
    void init_method_should_init_table_and_insert_conf() throws Exception {
        //when
        platformSetup.init();

        //then
        final Integer sequences = jdbcTemplate.queryForObject("select count(*) from sequence", Integer.class);
        assertThat(sequences).isGreaterThan(1);
        final List<String> platformRows = jdbcTemplate.queryForList("SELECT information FROM platform", String.class);
        assertThat(platformRows).hasSize(1);
        assertThat(platformRows.get(0)).isNotBlank(); // In Community, should contain the initial case counter value for information
        final int tenantRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "tenant");
        assertThat(tenantRows).isEqualTo(1);
        final int configurationFiles = JdbcTestUtils.countRowsInTable(jdbcTemplate, "configuration");
        assertThat(configurationFiles).isGreaterThan(1);
    }

    @Test
    void init_method_should_init_configuration_from_folder_if_exists(@TempDir Path setupFolder,
            CapturedOutput capturedOutput) throws Exception {
        //given
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toAbsolutePath().toString());
        FileUtils.write(setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("platform_engine")
                .resolve("whatever.properties").toFile(), "custom content", Charset.defaultCharset());
        configurationFolderUtil.buildSqlFolder(setupFolder, dbVendor);

        //when
        platformSetup.init();

        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM configuration WHERE resource_name = 'whatever.properties'");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsEntry("resource_content", "custom content".getBytes());
        assertThat(capturedOutput.getOut())
                .contains("Database will be initialized with configuration files from folder: "
                        + setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial"));
    }

    @Test
    @Tag("community-only")
    void init_method_should_store_tenant_portal_resources_from_classpath() throws Exception {
        //when
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM configuration WHERE content_type= '"
                        + ConfigurationType.TENANT_TEMPLATE_PORTAL + "' ORDER BY resource_name");
        assertThat(rows).hasSize(9);
        int rowId = 0;
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "compound-permissions-mapping-custom.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME",
                "compound-permissions-mapping-internal.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "compound-permissions-mapping.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "console-config.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "custom-permissions-mapping.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "resources-permissions-mapping-custom.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME",
                "resources-permissions-mapping-internal.properties");
        assertThat(rows.get(rowId++)).containsEntry("RESOURCE_NAME", "resources-permissions-mapping.properties");
        assertThat(rows.get(rowId)).containsEntry("RESOURCE_NAME", "security-config.properties");
    }

    @Test
    void init_method_should_store_platform_portal_resources_from_classpath() throws Exception {
        //when
        platformSetup.init();
        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList("SELECT * FROM configuration WHERE content_type= '" + ConfigurationType.PLATFORM_PORTAL
                        + "' ORDER BY resource_name");
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsEntry("RESOURCE_NAME", "cache-config.xml");
        assertThat(rows.get(1)).containsEntry("RESOURCE_NAME", "platform-tenant-config.properties");
        assertThat(rows.get(2)).containsEntry("RESOURCE_NAME", "security-config.properties");
    }

    @Test
    @Tag("community-only")
    void should_extract_configuration(@TempDir Path setupFolder) throws Exception {
        //given
        platformSetup.init();

        //when
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toAbsolutePath().toString());
        platformSetup.pull();

        //then
        File folderContainingResultOfGet = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME)
                .resolve("current").toFile();
        assertThat(folderContainingResultOfGet).as("should retrieve config files")
                .exists()
                .isDirectory();

        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        AllConfigurationResourceVisitor allConfigurationResourceVisitor = new AllConfigurationResourceVisitor(
                configurations);
        Files.walkFileTree(setupFolder, allConfigurationResourceVisitor);

        assertThat(configurations).extracting("resourceName").containsOnly(
                "bonita-platform-community-custom.properties",
                "bonita-platform-custom.xml",
                "cache-config.xml",
                "platform-tenant-config.properties",
                "security-config.properties",
                "bonita-tenant-community-custom.properties",
                "bonita-tenants-custom.xml",
                "compound-permissions-mapping.properties",
                "compound-permissions-mapping-custom.properties",
                "compound-permissions-mapping-internal.properties",
                "console-config.properties",
                "custom-permissions-mapping.properties",
                "resources-permissions-mapping.properties",
                "resources-permissions-mapping-custom.properties",
                "resources-permissions-mapping-internal.properties");
    }

    @Test
    void init_method_should_log_when_created(CapturedOutput capturedOutput) throws Exception {
        //given
        assertThat(platformSetup.isPlatformAlreadyCreated()).isFalse();

        //when
        platformSetup.init();

        //then
        assertThat(platformSetup.isPlatformAlreadyCreated()).isTrue();

        assertThat(capturedOutput.getOut()).as("should setup log message")
                .doesNotContain("Platform is already created. Nothing to do.")
                .contains("Platform created.")
                .contains("Initial configuration files successfully pushed to database");
    }

    @Test
    void init_method_should_upgrade_default_configuration_when_already_created(CapturedOutput capturedOutput)
            throws Exception {
        //given
        platformSetup.init();

        //when
        platformSetup.init();

        //then
        assertThat(platformSetup.isPlatformAlreadyCreated()).isTrue();

        assertThat(capturedOutput.getOut())
                .containsOnlyOnce("Platform created.")
                .containsOnlyOnce("Platform is already created.")
                .containsOnlyOnce("Upgrading default configuration");
    }

    @Test
    void push_method_should_log_when_created_and_create_backup(@TempDir File setupFolder,
            CapturedOutput capturedOutput) throws Exception {
        // given
        platformSetup.init();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.getAbsolutePath());
        configurationFolderUtil.buildCurrentFolder(setupFolder.toPath());

        // when
        platformSetup.forcePush();

        // then
        assertThat(setupFolder.listFiles()).hasSize(1);
        List<File> backupDirectory = Arrays.stream(setupFolder.listFiles()[0].listFiles())
                .filter(it -> it.getName().contains("backup")).toList();
        assertThat(backupDirectory).hasSize(1);
        assertThat(capturedOutput.getOut())
                .contains("Backup directory created:")
                .as("should push new configuration and log message")
                .contains("Configuration files successfully pushed to database. " +
                        "You can now restart Bonita to reflect your changes.");
    }

    @Test
    void push_should_fail_if_required_folder_would_be_deleted(@TempDir Path temporaryFolder) throws Exception {
        // on windows, the test fails to delete the 'platform init engine' directory
        // so do not run it for now
        assumeFalse(IS_OS_WINDOWS);

        // given
        platformSetup.init();
        Path setupFolder = Files.createDirectory(temporaryFolder.resolve("conf"));
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toAbsolutePath().toString());
        platformSetup.pull();
        final Path platformEngine = setupFolder.resolve("platform_conf").resolve("current")
                .resolve("platform_engine");
        FileUtils.deleteDirectory(platformEngine.toFile());

        // when - then
        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::push)
                .withMessageStartingWith("You are trying to remove a protected folder from configuration")
                .withMessageContaining(platformEngine.toString())
                .withMessageContaining("To restore the deleted folders");
    }

    @Test
    void push_should_throw_exception_when_platform_is_not_created() {
        //given
        assertThat(platformSetup.isPlatformAlreadyCreated()).isFalse();

        // when - then
        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::push)
                .withMessage("Platform is not created. Run 'setup init' first.");
    }

    @Test
    void clean_method_should_delete_and_log(@TempDir Path temporaryFolder, CapturedOutput capturedOutput)
            throws Exception {
        //given
        final Path path = Files.createDirectory(temporaryFolder.resolve("afterClean"));
        final Path licensePath = Files.createDirectory(temporaryFolder.resolve("licenses"));
        platformSetup.init();

        //when
        platformSetup.clean();

        //then
        assertThat(capturedOutput.getOut()).as("should log message")
                .isNotEmpty()
                .contains("Execute DeleteAllConfigurationInTransaction transaction.");

        platformSetup.pull(path, licensePath);
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        Files.walkFileTree(path, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should remove all files").isEmpty();

        Files.walkFileTree(licensePath, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should remove all files").isEmpty();
    }

    @Test
    void push_method_should_clean_previous_config(@TempDir Path temporaryFolder) throws Exception {
        //given
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        final Path initPath = Files.createDirectory(temporaryFolder.resolve("init"));
        final Path pushPath = Files.createDirectory(temporaryFolder.resolve("push"));
        final Path checkPath = Files.createDirectory(temporaryFolder.resolve("check"));
        final Path licensesPath = Files.createDirectory(temporaryFolder.resolve("lic"));

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
    void push_method_should_throw_exception_if_no_current_folder(@TempDir Path temporaryFolder) throws Exception {
        //given
        List<FullBonitaConfiguration> configurations = new ArrayList<>();
        final Path confFolder = Files.createDirectory(temporaryFolder.resolve(UUID.randomUUID().toString()));
        configurationFolderUtil.buildPlatformEngineFolder(confFolder);
        configurationFolderUtil.buildSqlFolder(confFolder, dbVendor);

        System.setProperty(BONITA_SETUP_FOLDER, confFolder.toString());
        platformSetup.init();
        Path current = confFolder.resolve("platform_conf").resolve("current");

        //when - then
        assertThatExceptionOfType(PlatformException.class).isThrownBy(platformSetup::push)
                .withMessage("Unable to push configuration from %1$s, as directory does not exists. " +
                        "To modify your configuration, run 'setup pull', " +
                        "update your configuration files from %1$s folder, " +
                        "and then push your new configuration.", current);

        platformSetup.pull();
        Files.walkFileTree(current, new AllConfigurationResourceVisitor(configurations));
        assertThat(configurations).as("should have kept old files").hasSize(1)
                .extracting("resourceName").containsOnly("initialConfig.properties");
    }

    @Test
    void should_push_check_platform_version(@TempDir Path temporaryFolder) throws Exception {
        //given
        platformSetup.init();
        jdbcTemplate.execute("UPDATE platform SET version='bad version'");

        final Path confFolder = Files.createDirectory(temporaryFolder.resolve(UUID.randomUUID().toString()));
        configurationFolderUtil.buildCurrentFolder(confFolder);
        System.setProperty(BONITA_SETUP_FOLDER, confFolder.toFile().getAbsolutePath());

        // when - then
        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::push)
                .withMessage("The version of the platform (binaries) you are running ["
                        + versionService.getPlatformSetupVersion() + "] only support database schema in version ["
                        + versionService.getSupportedDatabaseSchemaVersion() + "]" +
                        " but the current database schema version is [bad version]." +
                        " You might need to migrate your platform or use a different version of the binaries.");
    }

    @Test
    void should_pull_check_platform_version() throws Exception {
        //given
        platformSetup.init();
        jdbcTemplate.execute("UPDATE platform SET version='bad version'");

        // when - then
        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::pull)
                .withMessage("The version of the platform (binaries) you are running ["
                        + versionService.getPlatformSetupVersion() + "] only support database schema in version ["
                        + versionService.getSupportedDatabaseSchemaVersion() + "]" +
                        " but the current database schema version is [bad version]." +
                        " You might need to migrate your platform or use a different version of the binaries.");
    }

    @Test
    void pushLicences_should_pass_if_licence_folder_does_not_exists() throws Exception {
        platformSetup.initProperties();
        assertThatNoException().isThrownBy(platformSetup::preventFromPushingZeroLicense);
    }

    @Test
    void should_not_fail_when_pulling_twice_in_the_same_jvm(@TempDir Path temporaryFolder) throws Exception {
        final Path setupFolder = Files.createDirectory(temporaryFolder.resolve(UUID.randomUUID().toString()));
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        configurationFolderUtil.buildPlatformEngineFolder(setupFolder);
        configurationFolderUtil.buildSqlFolder(setupFolder, dbVendor);
        platformSetup.init();
        platformSetup.pull();
        assertThatNoException().isThrownBy(platformSetup::pull);
    }

    @Test
    void pushLicences_should_fail_if_licence_folder_exists_but_is_empty(@TempDir Path temporaryFolder)
            throws Exception {
        final Path setupFolder = Files.createDirectory(temporaryFolder.resolve(UUID.randomUUID().toString()));
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platformConf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);

        final Path licenseFolder = platformConf.resolve("licenses");
        Files.createDirectories(licenseFolder);

        platformSetup.initProperties();

        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::preventFromPushingZeroLicense)
                .withMessageStartingWith("No license (.lic file) found." + lineSeparator()
                        + "This would prevent Bonita Platform subscription edition"
                        + " to start normally." + lineSeparator() + "Place your license file");
    }

    @Test
    void pushLicences_should_fail_if_no_license_file_with_lic_extension_exists(@TempDir Path temporaryFolder)
            throws Exception {
        final Path setupFolder = Files.createDirectory(temporaryFolder.resolve(UUID.randomUUID().toString()));
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platformConf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);

        final Path licenseFolder = platformConf.resolve("licenses");
        Files.createDirectories(licenseFolder);
        Files.createFile(licenseFolder.resolve("bonita-file.renamed"));

        platformSetup.initProperties();

        assertThatExceptionOfType(PlatformException.class)
                .isThrownBy(platformSetup::preventFromPushingZeroLicense)
                .withMessageStartingWith("No license (.lic file) found." + lineSeparator()
                        + "This would prevent Bonita Platform subscription edition"
                        + " to start normally." + lineSeparator() + "Place your license file");
    }

    @Test
    void init_method_should_update_configuration_files(@TempDir Path temporaryFolder) throws Exception {
        //given
        Path setupFolder = Files.createDirectory(temporaryFolder.resolve("conf"));
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toAbsolutePath().toString());
        final File permissionFile = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("tenant_template_portal")
                .resolve("resources-permissions-mapping.properties").toFile();
        FileUtils.write(permissionFile, "default 7.5.4 content", Charset.defaultCharset());
        configurationFolderUtil.buildSqlFolder(setupFolder, dbVendor);
        platformSetup.init();

        // Simulate new 7.6.0 configuration file content:
        final String new_7_6_0_content = "default 7.6.0 content";
        FileUtils.write(permissionFile, new_7_6_0_content, Charset.defaultCharset());

        //when
        platformSetup.init();

        //then
        List<Map<String, Object>> rows = jdbcTemplate
                .queryForList(
                        "SELECT * FROM configuration WHERE resource_name = 'resources-permissions-mapping.properties'");
        assertThat(rows).hasSize(2)
                .allSatisfy(row -> assertThat(row).containsEntry("RESOURCE_CONTENT", new_7_6_0_content.getBytes()));
    }

    @Test
    void init_on_existing_platform_should_add_new_config_files(CapturedOutput capturedOutput) throws Exception {
        //given
        platformSetup.init();
        final String countConfigFile = "SELECT * FROM configuration WHERE resource_name = 'bonita-tenant-community-custom.properties'";
        assertThat(jdbcTemplate.queryForList(countConfigFile)).hasSize(2)
                .anyMatch(map -> map.get("CONTENT_TYPE").equals("TENANT_ENGINE"))
                .anyMatch(map -> map.get("CONTENT_TYPE").equals("TENANT_TEMPLATE_ENGINE"));

        // Delete it to check that init method adds it again:
        jdbcTemplate
                .update("DELETE from configuration WHERE resource_name = 'bonita-tenant-community-custom.properties'");

        assertThat(jdbcTemplate.queryForList(countConfigFile)).isEmpty();

        //when
        platformSetup.init();

        //then
        assertThat(jdbcTemplate.queryForList(countConfigFile)).hasSize(2)
                .anyMatch(map -> map.get("CONTENT_TYPE").equals("TENANT_ENGINE"))
                .anyMatch(map -> map.get("CONTENT_TYPE").equals("TENANT_TEMPLATE_ENGINE"));
        assertThat(capturedOutput.getOut())
                .contains("New configuration file detected 'bonita-tenant-community-custom.properties'");
    }
}
