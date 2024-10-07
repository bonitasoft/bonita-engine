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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.command.configure.BundleConfiguratorTest.checkFileContains;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class TomcatBundleConfiguratorTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private TomcatBundleConfigurator configurator;

    private TomcatBundleConfigurator spy;

    private Path bundleFolder;
    private Path tomcatFolder;
    private String databaseAbsolutePath;

    @Before
    public void setupTempConfFolder() throws Exception {
        final File temporaryFolderRoot = temporaryFolder.newFolder();
        bundleFolder = temporaryFolderRoot.toPath().toRealPath();
        tomcatFolder = bundleFolder.resolve("server");
        configurator = new TomcatBundleConfigurator(bundleFolder);
        FileUtils.copyDirectory(Paths.get("src/test/resources/tomcat_conf").toFile(), temporaryFolderRoot);
        System.setProperty(BONITA_SETUP_FOLDER, bundleFolder.resolve("setup").toString());
        databaseAbsolutePath = BundleConfigurator
                .convertWindowsBackslashes(bundleFolder.resolve("h2_database").normalize().toString());
        spy = spy(configurator);
    }

    @Test
    public void should_delete_h2_jar_from_classpath_if_h2_is_not_used() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.database.name", "bonita");
        System.setProperty("db.user", "bonita");
        System.setProperty("db.password", "bpm");

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.database.name", "business_data");
        System.setProperty("bdm.db.user", "bizUser");
        System.setProperty("bdm.db.password", "bizPwd");

        // given:
        final Path h2jarPath = tomcatFolder.resolve("lib").resolve("bonita").resolve("h2.jar");

        assertThat(h2jarPath).exists();

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2jarPath).doesNotExist();
    }

    @Test
    public void should_not_delete_h2_jar_from_classpath_if_h2_is_used_for_bdm() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.database.name", "bonita");
        System.setProperty("db.user", "bonita");
        System.setProperty("db.password", "bpm");

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");

        // given:
        final Path h2jarPath = tomcatFolder.resolve("lib").resolve("bonita").resolve("h2.jar");

        assertThat(h2jarPath).exists();

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2jarPath).exists();
    }

    @Test
    public void should_not_delete_h2_jar_from_classpath_if_h2_is_used_for_bonita() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "internal_database.db");
        System.setProperty("db.user", "myUser");
        System.setProperty("db.password", "myPwd");

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.database.name", "business_data");
        System.setProperty("bdm.db.user", "bizUser");
        System.setProperty("bdm.db.password", "bizPwd");

        // given:
        final Path h2jarPath = tomcatFolder.resolve("lib").resolve("bonita").resolve("h2.jar");

        assertThat(h2jarPath).exists();

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2jarPath).exists();
    }

    @Test
    public void should_not_delete_h2_jar_from_classpath_if_h2_is_used_for_bonita_and_for_BDM() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "internal_database.db");
        System.setProperty("db.user", "myUser");
        System.setProperty("db.password", "myPwd");

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");

        // given:
        final Path h2jarPath = tomcatFolder.resolve("lib").resolve("bonita").resolve("h2.jar");

        assertThat(h2jarPath).exists();

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(h2jarPath).exists();
    }

    @Test
    public void should_not_fail_if_bonitaXml_file_does_not_pre_exist() throws Exception {
        // given:
        final Path bonitaXmlPath = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost")
                .resolve("bonita.xml");
        PathUtils.deleteFile(bonitaXmlPath);
        assertThat(bonitaXmlPath).doesNotExist();

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(bonitaXmlPath).exists();
    }

    @Test
    public void configureApplicationServer_should_update_setEnv_file() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("bdm.db.vendor", "postgres");

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        checkFileContains(tomcatFolder.resolve("bin").resolve("setenv.sh"), "-Dsysprop.bonita.db.vendor=postgres",
                "-Dsysprop.bonita.bdm.db.vendor=postgres");
        checkFileContains(tomcatFolder.resolve("bin").resolve("setenv.bat"), "-Dsysprop.bonita.db.vendor=postgres",
                "-Dsysprop.bonita.bdm.db.vendor=postgres");
    }

    private int numberOfBackups(String file) {
        final String[] backupFiles = bundleFolder.resolve("setup").resolve("tomcat-backups").toFile()
                .list(new RegexFileFilter(file + "\\.[0-9-_hms]*"));
        return backupFiles == null ? 0 : backupFiles.length;
    }

    @Test
    public void configureApplicationServer_should_fail_if_no_driver_folder() throws Exception {
        // given:
        final Path driverFolder = bundleFolder.resolve("setup").resolve("lib");
        FileUtils.deleteDirectory(driverFolder.toFile());

        // when - then:
        assertThatExceptionOfType(PlatformException.class).isThrownBy(configurator::configureApplicationServer)
                .withMessage("Drivers folder not found: " + driverFolder
                        + ". Make sure it exists and put a jar or zip file containing drivers there.");
    }

    @Test
    public void configureApplicationServer_should_update_bonitaXml_file() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.server.name", "db.localhost");
        System.setProperty("db.server.port", "5432");
        System.setProperty("db.database.name", "bonita");
        System.setProperty("db.user", "bonita");
        System.setProperty("db.password", "bpm");

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.server.name", "biz.localhost");
        System.setProperty("bdm.db.server.port", "5433");
        System.setProperty("bdm.db.database.name", "business_data");
        System.setProperty("bdm.db.user", "bizUser");
        System.setProperty("bdm.db.password", "bizPwd");

        System.setProperty("connection-pool.maxTotal", "200");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bonitaXml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost")
                .resolve("bonita.xml");
        checkFileContains(bonitaXml,
                "validationQuery=\"SELECT 1\"", "username=\"bonita\"", "password=\"bpm\"",
                "serverName=\"db.localhost\"", "portNumber=\"5432\"", "port=\"5432\"", "databaseName=\"bonita\"",
                "url=\"jdbc:postgresql://db.localhost:5432/bonita\"");
        checkFileContains(bonitaXml,
                "validationQuery=\"SELECT 1\"", "username=\"bizUser\"", "password=\"bizPwd\"",
                "serverName=\"biz.localhost\"", "portNumber=\"5433\"", "port=\"5433\"",
                "databaseName=\"business_data\"",
                "url=\"jdbc:postgresql://biz.localhost:5433/business_data\"");
        checkFileContains(bonitaXml, "driverClassName=\"org.postgresql.Driver\"",
                "type=\"org.postgresql.xa.PGXADataSource\"", "class=\"org.postgresql.xa.PGXADataSource\"",
                "factory=\"org.postgresql.xa.PGXADataSourceFactory\"");
        checkFileContains(bonitaXml, "initialSize=\"8\"",
                // maxTotal value overridden in database.properties
                "maxTotal=\"200\"",
                "minIdle=\"8\"",
                "maxIdle=\"16\"");

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
    }

    @Test
    public void configureApplicationServer_should_support_H2_replacements_for_Bonita_database_and_BDM()
            throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "internal_database.db");
        System.setProperty("db.user", "myUser");
        System.setProperty("db.password", "myPwd");

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "internal_business_data.db");
        System.setProperty("bdm.db.user", "bizUser");
        System.setProperty("bdm.db.password", "bizPwd");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bonitaXml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost")
                .resolve("bonita.xml");

        checkFileContains(bonitaXml, "validationQuery=\"SELECT 1\"", "username=\"myUser\"", "password=\"myPwd\"",
                "driverClassName=\"org.h2.Driver\"",
                "url=\"jdbc:h2:file:" + databaseAbsolutePath
                        + "/internal_database.db;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;\"");
        checkFileContains(bonitaXml, "validationQuery=\"SELECT 1\"", "username=\"bizUser\"", "password=\"bizPwd\"",
                "driverClassName=\"org.h2.Driver\"",
                "url=\"jdbc:h2:file:" + databaseAbsolutePath
                        + "/internal_business_data.db;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;\"");
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
        final Path bonitaXml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost")
                .resolve("bonita.xml");

        checkFileContains(bonitaXml, "validationQuery=\"SELECT 1\"", "username=\"_bonita_with$dollar\\andBackSlash\"",
                "password=\"bpm_With$dollar\\andBackSlash\"", "driverClassName=\"org.h2.Driver\"",
                "url=\"jdbc:h2:file:" + databaseAbsolutePath
                        + "/bonita_bdm_with$dollarXXX.db;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;\"");
    }

    @Test
    public void configureApplicationServer_should_support_special_characters_for_database() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.server.name", "localhost");
        System.setProperty("db.server.port", "5432");
        System.setProperty("db.database.name", "bonita_with$dollarXXX\\myInstance.of.bonita&perf=good");
        System.setProperty("db.user", "_bonita_with$dollar\\andBackSlash");
        System.setProperty("db.password", "bpm_With$dollar\\andBackSlash");

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.server.name", "localhost");
        System.setProperty("bdm.db.server.port", "5432");
        System.setProperty("bdm.db.database.name",
                "bonita_bdm_with$dollarXXX\\myInstance.of.bdm&perf=good?host.net.disableOob=true");
        System.setProperty("bdm.db.user", "_bdmWith$dollar\\andBackSlash");
        System.setProperty("bdm.db.password", "bdm_bpm_With$dollar\\andBackSlash");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bonitaXml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost")
                .resolve("bonita.xml");

        checkFileContains(bonitaXml, "validationQuery=\"SELECT 1\"",
                "username=\"_bonita_with$dollar\\andBackSlash\"", "password=\"bpm_With$dollar\\andBackSlash\"",
                "driverClassName=\"org.postgresql.Driver\"",
                "url=\"jdbc:postgresql://localhost:5432/bonita_with$dollarXXX\\myInstance.of.bonita&amp;perf=good\"",
                "url=\"jdbc:postgresql://localhost:5432/bonita_bdm_with$dollarXXX\\myInstance.of.bdm&amp;perf=good?host.net.disableOob=true\"");
    }

    @Test
    public void should_copy_both_drivers_if_not_the_same_dbVendor_for_bdm() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("bdm.db.vendor", "postgres");

        // when:
        spy.configureApplicationServer();

        // then:
        verify(spy).copyDriverFile(any(Path.class), any(Path.class), eq("h2"));
        verify(spy).copyDriverFile(any(Path.class), any(Path.class), eq("postgres"));
    }

    @Test
    public void should_not_copy_drivers_again_if_same_dbVendor_for_bdm() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("bdm.db.vendor", "postgres");

        // when:
        spy.configureApplicationServer();

        // then:
        verify(spy, times(1)).copyDriverFile(any(Path.class), any(Path.class), anyString());
    }

    @Test
    public void should_fail_if_tomcat_mandatory_file_not_present() throws Exception {
        final Path confFile = tomcatFolder.resolve("bin").resolve("setenv.sh");
        FileUtils.delete(confFile.toFile());

        // when - then:
        assertThatExceptionOfType(PlatformException.class).isThrownBy(configurator::configureApplicationServer)
                .withMessage("File setenv.sh is mandatory but is not found");
    }

    @Test
    public void configureApplicationServer_should_fail_if_drivers_not_found() throws Exception {
        // given:
        final String dbVendor = "postgres";
        System.setProperty("db.vendor", dbVendor);
        final Path libFolder = bundleFolder.resolve("setup").resolve("lib");
        final Path driverJar = libFolder.resolve("postgres9.2-drivers.jar");
        FileUtils.delete(driverJar.toFile());

        // when - then:
        assertThatExceptionOfType(PlatformException.class).isThrownBy(configurator::configureApplicationServer)
                .withMessage("No " + dbVendor + " drivers found in folder " + libFolder
                        + ". Make sure to put a jar or zip file containing drivers there.");
    }

    @Test
    public void exception_in_configure_should_restore_previous_configuration() throws Exception {
        // given:
        doThrow(PlatformException.class).when(spy).copyDatabaseDriversIfNecessary(any(Path.class), any(Path.class),
                eq("h2"));

        // when - then:
        assertThatExceptionOfType(PlatformException.class).isThrownBy(spy::configureApplicationServer);
        verify(spy).restorePreviousConfiguration(any(Path.class), any(Path.class), any(Path.class));
    }

    @Test
    public void should_not_make_backup_if_content_is_the_same() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.database.name", "bonita");
        System.setProperty("db.user", "bonita");
        System.setProperty("db.password", "bpm");

        configurator.configureApplicationServer();

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);
    }

    @Test
    public void should_make_new_backup_if_configuration_changes() throws Exception {
        // given:
        System.setProperty("db.vendor", "postgres");
        System.setProperty("db.database.name", "bonita");
        System.setProperty("db.user", "bonita");
        System.setProperty("db.password", "bpm");

        configurator.configureApplicationServer();

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);

        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "bonita_journal.db");
        System.setProperty("db.user", "sa");
        System.setProperty("db.password", "");

        // so that horodated file has different
        Thread.sleep(1020);

        // when:
        new TomcatBundleConfigurator(bundleFolder).configureApplicationServer();

        // then:
        assertThat(numberOfBackups("bonita.xml")).isEqualTo(2);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(2);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(2);
    }

}
