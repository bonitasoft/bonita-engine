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

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class WildflyBundleConfiguratorTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private WildflyBundleConfigurator configurator;

    private WildflyBundleConfigurator spy;

    private Path newFolderPath;

    public WildflyBundleConfiguratorTest() throws PlatformException {
    }

    @Before
    public void setupTempConfFolder() throws Exception {
        final File temporaryFolderRoot = temporaryFolder.getRoot();
        newFolderPath = temporaryFolderRoot.toPath();
        FileUtils.copyDirectory(Paths.get("src/test/resources/wildfly_conf").toFile(), temporaryFolderRoot);
        System.setProperty(BONITA_SETUP_FOLDER, newFolderPath.resolve("setup").toString());
        configurator = new WildflyBundleConfigurator(newFolderPath);
        spy = spy(configurator);
    }

    private int numberOfBackups(String file) {
        final String[] backupFiles = newFolderPath.resolve("setup").resolve("wildfly-backups").toFile().list(new RegexFileFilter(file + "\\.[0-9-_hms]*"));
        return backupFiles == null ? 0 : backupFiles.length;
    }

    @Test
    public void configureApplicationServer_should_fail_if_no_driver_folder() throws Exception {
        // given:
        final Path driverFolder = newFolderPath.resolve("setup").resolve("lib");
        FileUtils.deleteDirectory(driverFolder.toFile());

        // then:
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Drivers folder not found: " + driverFolder.toString());

        // when:
        configurator.configureApplicationServer();
    }

    @Test
    public void configureApplicationServer_should_update_standalone_xml_file() throws Exception {
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = newFolderPath.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileContains(configFile,
                "<driver>postgres</driver>", "<user-name>bonita</user-name>", "<password>bpm</password>",
                "<xa-datasource-property name=\"ServerName\">localhost</xa-datasource-property>",
                "<xa-datasource-property name=\"portNumber\">5432</xa-datasource-property>",
                "<xa-datasource-property name=\"DatabaseName\">bonita</xa-datasource-property>",
                "<connection-url>jdbc:postgresql://localhost:5432/bonita</connection-url>",
                "<check-valid-connection-sql>SELECT 1</check-valid-connection-sql>");
        checkFileDoesNotContain(configFile, "<xa-datasource-property name=\"URL\">jdbc:postgresql://localhost:5432/bonita</xa-datasource-property>");

        // Bonita internal Drivers part:
        checkFileContains(configFile, "<driver name=\"postgres\" module=\"org.postgresql\">",
                "<xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>",
                "<datasource-class>org.postgresql.Driver</datasource-class>");

        checkFileContains(configFile,
                "<driver>oracle</driver>", "<user-name>bizUser</user-name>", "<password>bizPwd</password>",
                "<xa-datasource-property name=\"URL\">jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL</xa-datasource-property>",
                "<connection-url>jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL</connection-url>",
                "<check-valid-connection-sql>SELECT 1 FROM DUAL</check-valid-connection-sql>");
        checkFileDoesNotContain(configFile, "<xa-datasource-property name=\"ServerName\">ora1.rd.lan</xa-datasource-property>",
                "<xa-datasource-property name=\"portNumber\">1521</xa-datasource-property>",
                "<xa-datasource-property name=\"DatabaseName\">ORCL</xa-datasource-property>");

        // BDM Drivers part:
        checkFileContains(configFile, "<driver name=\"oracle\" module=\"com.oracle\">",
                "<xa-datasource-class>oracle.jdbc.xa.client.OracleXADataSource</xa-datasource-class>",
                "<datasource-class>oracle.jdbc.OracleDriver</datasource-class>");
        checkFileDoesNotContain(configFile, "BDM_DRIVER_TEMPLATE"); // both dbVendors are different, so we should have replace both declarations

        assertThat(numberOfBackups("standalone.xml")).isEqualTo(1);
    }

    @Test
    public void configureWildfly_should_copy_module_file_in_right_folder() throws Exception {
        // given:
        final Path postgresModule = newFolderPath.resolve("modules").resolve("org").resolve("postgresql").resolve("main").resolve("module.xml");
        final Path oracleModule = newFolderPath.resolve("modules").resolve("com").resolve("oracle").resolve("main").resolve("module.xml");

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(postgresModule.toFile()).exists();
        checkFileContains(postgresModule, "<module xmlns=\"urn:jboss:module:1.0\" name=\"org.postgresql\">",
                "<resource-root path=\"postgres9.2-drivers.jar\" />");

        assertThat(oracleModule.toFile()).exists();
        checkFileContains(oracleModule, "<module xmlns=\"urn:jboss:module:1.0\" name=\"com.oracle\">",
                "<resource-root path=\"ojdbc-6.jar\" />");
    }

    @Test
    public void configureWildfly_should_not_copy_module_file_for_H2_nor_jar_file() throws Exception {
        // given:
        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");
        final Path moduleFolder = newFolderPath.resolve("modules").resolve("com").resolve("h2database").resolve("h2").resolve("main");
        final Path h2Module = moduleFolder.resolve("module.xml");
        final Path h2JarFile = moduleFolder.resolve("drivers-h2-2.12.117.jar");

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2Module.toFile()).doesNotExist();
        assertThat(h2JarFile.toFile()).doesNotExist();
    }

    private void checkFileContains(Path file, String... expectedTexts) throws IOException {
        final String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        for (String text : expectedTexts) {
            assertThat(content).contains(text);
        }
    }

    private void checkFileDoesNotContain(Path file, String... expectedTexts) throws IOException {
        final String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        for (String text : expectedTexts) {
            assertThat(content).doesNotContain(text);
        }
    }

    @Test
    public void should_fail_if_wildfly_mandatory_file_not_present() throws Exception {
        final Path confFile = newFolderPath.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        FileUtils.deleteQuietly(confFile.toFile());

        // then:
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("File standalone.xml is mandatory but is not found");

        // when:
        configurator.configureApplicationServer();
    }

    @Test
    public void configureApplicationServer_should_fail_if_drivers_not_found() throws Exception {
        // given:
        final String dbVendor = "sqlserver";
        System.setProperty("db.vendor", dbVendor);

        // then:
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("No " + dbVendor + " drivers found in folder");

        // when:
        configurator.configureApplicationServer();
    }

    @Test
    public void should_not_make_backup_if_content_is_the_same() throws Exception {
        // given:
        configurator.configureApplicationServer();

        assertThat(numberOfBackups("standalone.xml")).isEqualTo(1);

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(numberOfBackups("standalone.xml")).isEqualTo(1);
    }

    @Test
    public void should_make_new_backup_if_configuration_changes() throws Exception {
        // given:
        configurator.configureApplicationServer();

        assertThat(numberOfBackups("standalone.xml")).isEqualTo(1);

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");

        // so that horodated file has different
        Thread.sleep(1020);

        // when:
        new WildflyBundleConfigurator(newFolderPath).configureApplicationServer();

        // then:
        assertThat(numberOfBackups("standalone.xml")).isEqualTo(2);
    }

    @Test
    public void exception_in_configure_should_restore_previous_configuration() throws Exception {
        // given:
        doThrow(PlatformException.class).when(spy).copyDatabaseDriversIfNecessary(any(Path.class), any(Path.class), eq("oracle"));

        // when:
        try {
            spy.configureApplicationServer();
            fail("Should have thrown exception");
        } catch (PlatformException e) {
            // then:
            verify(spy).restorePreviousConfiguration(any(Path.class));
        }
    }

}
