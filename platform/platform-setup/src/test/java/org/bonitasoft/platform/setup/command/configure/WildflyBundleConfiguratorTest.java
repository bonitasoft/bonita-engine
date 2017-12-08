/**
 * Copyright (C) 2016-2017 Bonitasoft S.A.
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
package org.bonitasoft.platform.setup.command.configure;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.command.configure.BundleConfiguratorTest.checkFileContains;
import static org.bonitasoft.platform.setup.command.configure.BundleConfiguratorTest.checkFileDoesNotContain;
import static org.mockito.Mockito.*;

import java.io.File;
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
import org.mockito.junit.MockitoJUnitRunner;

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

    private Path bundleFolder;
    private Path wildflyFolder;
    private String databaseAbsolutePath;

    @Before
    public void setupTempConfFolder() throws Exception {
        final File temporaryFolderRoot = temporaryFolder.newFolder();
        bundleFolder = temporaryFolderRoot.toPath().toRealPath();
        wildflyFolder = bundleFolder.resolve("server");
        FileUtils.copyDirectory(Paths.get("src/test/resources/wildfly_conf").toFile(), temporaryFolderRoot);
        System.setProperty(BONITA_SETUP_FOLDER, bundleFolder.resolve("setup").toString());
        configurator = new WildflyBundleConfigurator(bundleFolder);
        databaseAbsolutePath = BundleConfigurator.convertWindowsBackslashes(bundleFolder.resolve("h2_database").normalize().toString());
        spy = spy(configurator);
    }

    private int numberOfBackups(String file) {
        final String[] backupFiles = bundleFolder.resolve("setup").resolve("wildfly-backups").toFile().list(new RegexFileFilter(file + "\\.[0-9-_hms]*"));
        return backupFiles == null ? 0 : backupFiles.length;
    }

    @Test
    public void configureApplicationServer_should_fail_if_no_driver_folder() throws Exception {
        // given:
        final Path driverFolder = bundleFolder.resolve("setup").resolve("lib");
        FileUtils.deleteDirectory(driverFolder.toFile());

        // then:
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Drivers folder not found: " + driverFolder.toString());

        // when:
        configurator.configureApplicationServer();
    }

    @Test
    public void configureApplicationServer_should_escape_xml_special_char_on_mysql() throws Exception {
        // when:
        System.setProperty("db.vendor", "mysql");
        System.setProperty("db.database.name", "mysql_database");
        System.setProperty("db.server.name", "mysql_servidor");
        System.setProperty("db.server.port", "9876");

        System.setProperty("bdm.db.vendor", "mysql");
        System.setProperty("bdm.db.database.name", "mysql_database_bdm");
        System.setProperty("bdm.db.server.name", "mysql_servidor_bdm");
        System.setProperty("bdm.db.server.port", "4321");
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileContains(configFile,
                "<connection-url>jdbc:mysql://mysql_servidor:9876/mysql_database?dontTrackOpenResources=true&amp;useUnicode=true&amp;characterEncoding=UTF-8</connection-url>",
                "<connection-url>jdbc:mysql://mysql_servidor_bdm:4321/mysql_database_bdm?dontTrackOpenResources=true&amp;useUnicode=true&amp;characterEncoding=UTF-8</connection-url>");
    }

    @Test
    public void configureApplicationServer_should_update_standalone_xml_file() throws Exception {
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
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
                "<xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>");

        checkFileContains(configFile,
                "<driver>oracle</driver>", "<user-name>bizUser</user-name>", "<password>bizPwd</password>",
                "<xa-datasource-property name=\"URL\">jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL_with\\backslash</xa-datasource-property>",
                "<connection-url>jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL_with\\backslash</connection-url>",
                "<check-valid-connection-sql>SELECT 1 FROM DUAL</check-valid-connection-sql>");
        checkFileDoesNotContain(configFile, "<xa-datasource-property name=\"ServerName\">ora1.rd.lan</xa-datasource-property>",
                "<xa-datasource-property name=\"portNumber\">1521</xa-datasource-property>",
                "<xa-datasource-property name=\"DatabaseName\">ORCL</xa-datasource-property>");

        // BDM Drivers part:
        checkFileContains(configFile, "<driver name=\"oracle\" module=\"com.oracle\">",
                "<xa-datasource-class>oracle.jdbc.xa.client.OracleXADataSource</xa-datasource-class>");
        checkFileDoesNotContain(configFile, "BDM_DRIVER_TEMPLATE"); // both dbVendors are different, so we should have replace both declarations

        assertThat(numberOfBackups("standalone.xml")).isEqualTo(1);
    }


    @Test
    public void should_set_isSameRmOverride_to_false_on_oracle() throws Exception {
        System.setProperty("db.vendor", "oracle");
        System.setProperty("bdm.db.vendor", "oracle");
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileContains(configFile,
                "<is-same-rm-override>false</is-same-rm-override>");
    }

    @Test
    public void should_not_set_isSameRmOverride_when_not_oracle() throws Exception {
        System.setProperty("db.vendor", "postgres");
        System.setProperty("bdm.db.vendor", "postgres");
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileDoesNotContain(configFile,
                "<is-same-rm-override>");
    }

    @Test
    public void should_set_noTxSeparatePools_to_true_on_oracle() throws Exception {
        System.setProperty("db.vendor", "oracle");
        System.setProperty("bdm.db.vendor", "oracle");
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileContains(configFile,
                "<no-tx-separate-pools>true</no-tx-separate-pools>");
    }

    @Test
    public void should_not_set_noTxSeparatePools_when_not_oracle() throws Exception {
        System.setProperty("db.vendor", "postgres");
        System.setProperty("bdm.db.vendor", "postgres");
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        checkFileDoesNotContain(configFile,
                "<no-tx-separate-pools>");
    }

    @Test
    public void configureWildfly_should_copy_module_file_in_right_folder() throws Exception {
        // given:
        final Path postgresModule = wildflyFolder.resolve("modules").resolve("org").resolve("postgresql").resolve("main").resolve("module.xml");
        final Path oracleModule = wildflyFolder.resolve("modules").resolve("com").resolve("oracle").resolve("main").resolve("module.xml");

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
        final Path moduleFolder = wildflyFolder.resolve("modules").resolve("com").resolve("h2database").resolve("h2").resolve("main");
        final Path h2Module = moduleFolder.resolve("module.xml");
        final Path h2JarFile = moduleFolder.resolve("drivers-h2-2.12.117.jar");

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2Module.toFile()).doesNotExist();
        assertThat(h2JarFile.toFile()).doesNotExist();
    }

    @Test
    public void should_fail_if_wildfly_mandatory_file_not_present() throws Exception {
        final Path confFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
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
        new WildflyBundleConfigurator(bundleFolder).configureApplicationServer();

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

    @Test
    public void configureApplicationServer_should_support_special_characters_for_h2_database() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "bonita_with$dollarXXX.db");
        System.setProperty("db.user", "_bonita_with$dollar\\andBackSlash");
        System.setProperty("db.password", "bpm_With$dollar\\andBackSlash");

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "bonita_bdm_with$dollarXXX.db");
        System.setProperty("bdm.db.user", "_bdmWith$dollar\\andBackSlash");
        System.setProperty("bdm.db.password", "bdm_bpm_With$dollar\\andBackSlash");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        final String bonitaJdbcUrl = "jdbc:h2:file:" + databaseAbsolutePath
                + "/bonita_with$dollarXXX.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;";
        checkFileContains(configFile,
                "<connection-url>" + bonitaJdbcUrl + "</connection-url>",
                "<user-name>_bonita_with$dollar\\andBackSlash</user-name>",
                "<password>bpm_With$dollar\\andBackSlash</password>",
                "<xa-datasource-property name=\"URL\">" + bonitaJdbcUrl + "</xa-datasource-property>");

        final String bdmJdbcUrl = "jdbc:h2:file:" + databaseAbsolutePath
                + "/bonita_bdm_with$dollarXXX.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;";
        checkFileContains(configFile, "<connection-url>" + bdmJdbcUrl + "</connection-url>",
                "<user-name>_bdmWith$dollar\\andBackSlash</user-name>",
                "<password>bdm_bpm_With$dollar\\andBackSlash</password>",
                "<xa-datasource-property name=\"URL\">" + bdmJdbcUrl + "</xa-datasource-property>");
    }

    @Test
    public void configureApplicationServer_should_support_special_characters_for_oracle_database() throws Exception {
        // given:
        System.setProperty("db.vendor", "oracle");
        System.setProperty("db.database.name", "bonita_with$dollarXXX\\myInstance.of.bonita&perf=good");
        System.setProperty("db.user", "_bonita_with$dollar\\andBackSlash");
        System.setProperty("db.password", "bpm_With$dollar\\andBackSlash");

        System.setProperty("bdm.db.vendor", "oracle");
        System.setProperty("bdm.db.database.name", "bonita_bdm_with$dollarXXX\\myInstance.of.bdm&perf=good");
        System.setProperty("bdm.db.user", "_bdmWith$dollar\\andBackSlash");
        System.setProperty("bdm.db.password", "bdm_bpm_With$dollar\\andBackSlash");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");
        final String bonitaJdbcUrl = "jdbc:oracle:thin:@localhost:5432:bonita_with$dollarXXX\\myInstance.of.bonita&amp;perf=good";
        checkFileContains(configFile,
                "<connection-url>" + bonitaJdbcUrl + "</connection-url>",
                "<user-name>_bonita_with$dollar\\andBackSlash</user-name>",
                "<password>bpm_With$dollar\\andBackSlash</password>",
                "<xa-datasource-property name=\"URL\">" + bonitaJdbcUrl + "</xa-datasource-property>");

        final String bdmJdbcUrl = "jdbc:oracle:thin:@ora1.rd.lan:1521:bonita_bdm_with$dollarXXX\\myInstance.of.bdm&amp;perf=good";
        checkFileContains(configFile, "<connection-url>" + bdmJdbcUrl + "</connection-url>",
                "<user-name>_bdmWith$dollar\\andBackSlash</user-name>",
                "<password>bdm_bpm_With$dollar\\andBackSlash</password>",
                "<xa-datasource-property name=\"URL\">" + bdmJdbcUrl + "</xa-datasource-property>");
    }

    @Test
    public void configureApplicationServer_should_support_special_characters_for_postgre_specific_properties()
            throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.database.name", "bonita_with$dollarXXX.db");
        System.setProperty("db.user", "user");
        System.setProperty("db.password", "password");

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.database.name", "bonita_bdm_with$dollarXXX.db");
        System.setProperty("bdm.db.user", "bdm_user");
        System.setProperty("bdm.db.password", "bdm_password");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path configFile = wildflyFolder.resolve("standalone").resolve("configuration").resolve("standalone.xml");

        checkFileContains(configFile,
                "<xa-datasource-property name=\"DatabaseName\">bonita_with$dollarXXX.db</xa-datasource-property>",
                "<xa-datasource-property name=\"DatabaseName\">bonita_bdm_with$dollarXXX.db</xa-datasource-property>");
    }

}
