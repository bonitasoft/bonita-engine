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
package org.bonitasoft.platform.setup.command.configure;

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
public class TomcatBundleConfiguratorTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private TomcatBundleConfigurator configurator;

    private TomcatBundleConfigurator spy;

    private Path bundleFolder;
    private Path tomcatFolder;
    private String databaseAbsolutePath;

    public TomcatBundleConfiguratorTest() throws PlatformException {
    }

    @Before
    public void setupTempConfFolder() throws Exception {
        final File temporaryFolderRoot = temporaryFolder.newFolder();
        bundleFolder = temporaryFolderRoot.toPath().toRealPath();
        tomcatFolder = bundleFolder.resolve("server");
        configurator = new TomcatBundleConfigurator(bundleFolder);
        FileUtils.copyDirectory(Paths.get("src/test/resources/tomcat_conf").toFile(), temporaryFolderRoot);
        System.setProperty(BONITA_SETUP_FOLDER, bundleFolder.resolve("setup").toString());
        databaseAbsolutePath = BundleConfigurator.convertWindowsBackslashes(bundleFolder.resolve("h2_database").normalize().toString());
        spy = spy(configurator);
    }

    @Test
    public void configureApplicationServer_should_update_setEnv_file() throws Exception {
        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        checkFileContains(tomcatFolder.resolve("bin").resolve("setenv.sh"), "-Dsysprop.bonita.db.vendor=postgres", "-Dsysprop.bonita.bdm.db.vendor=oracle");
        checkFileContains(tomcatFolder.resolve("bin").resolve("setenv.bat"), "-Dsysprop.bonita.db.vendor=postgres", "-Dsysprop.bonita.bdm.db.vendor=oracle");
    }

    private int numberOfBackups(String file) {
        final String[] backupFiles = bundleFolder.resolve("setup").resolve("tomcat-backups").toFile().list(new RegexFileFilter(file + "\\.[0-9-_hms]*"));
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
    public void configureApplicationServer_should_update_bonita_xml_file() throws Exception {
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bonita_xml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost").resolve("bonita.xml");
        checkFileContains(bonita_xml,
                "validationQuery=\"SELECT 1\"", "username=\"bonita\"", "password=\"bpm\"", "driverClassName=\"org.postgresql.Driver\"",
                "url=\"jdbc:postgresql://localhost:5432/bonita\"");
        checkFileContains(bonita_xml,
                "validationQuery=\"SELECT 1 FROM DUAL\"", "username=\"bizUser\"", "password=\"bizPwd\"", "driverClassName=\"oracle.jdbc.OracleDriver\"",
                "url=\"jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL\"");

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
    }

    @Test
    public void configureApplicationServer_should_update_bitronix_resources_file() throws Exception {
        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bitronixFile = tomcatFolder.resolve("conf").resolve("bitronix-resources.properties");
        checkFileContains(bitronixFile, "resource.ds1.className=org.postgresql.xa.PGXADataSource", "resource.ds1.driverProperties.user=bonita",
                "resource.ds1.driverProperties.password=bpm", "resource.ds1.driverProperties.serverName=localhost",
                "resource.ds1.driverProperties.portNumber=5432", "resource.ds1.driverProperties.databaseName=bonita", "resource.ds1.testQuery=SELECT 1");
        checkFileContains(bitronixFile, "resource.ds2.className=oracle.jdbc.xa.client.OracleXADataSource",
                "resource.ds2.driverProperties.user=bizUser", "resource.ds2.driverProperties.password=bizPwd",
                "resource.ds2.driverProperties.URL=jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL", "resource.ds2.testQuery=SELECT 1 FROM DUAL");

        assertThat(numberOfBackups("bitronix-resources.properties")).isEqualTo(1);
    }

    @Test
    public void configureApplicationServer_should_support_H2_replacements_for_Bonita_database() throws Exception {
        // given:
        System.setProperty("db.vendor", "h2");
        System.setProperty("db.database.name", "internal_database.db");
        System.setProperty("db.user", "myUser");
        System.setProperty("db.password", "myPwd");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bitronixFile = tomcatFolder.resolve("conf").resolve("bitronix-resources.properties");
        final Path bonita_xml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost").resolve("bonita.xml");

        checkFileContains(bitronixFile, "resource.ds1.className=org.h2.jdbcx.JdbcDataSource", "resource.ds1.driverProperties.user=myUser",
                "resource.ds1.driverProperties.password=myPwd", "resource.ds1.driverProperties.URL=jdbc:h2:file:" + databaseAbsolutePath
                        + "/internal_database.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;",
                "resource.ds1.testQuery=SELECT 1");
        checkFileContains(bitronixFile, "resource.ds2.className=oracle.jdbc.xa.client.OracleXADataSource",
                "resource.ds2.driverProperties.user=bizUser", "resource.ds2.driverProperties.password=bizPwd",
                "resource.ds2.driverProperties.URL=jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL", "resource.ds2.testQuery=SELECT 1 FROM DUAL");

        checkFileContains(bonita_xml, "validationQuery=\"SELECT 1\"", "username=\"myUser\"", "password=\"myPwd\"", "driverClassName=\"org.h2.Driver\"",
                "url=\"jdbc:h2:file:" + databaseAbsolutePath + "/internal_database.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;\"");
        checkFileContains(bonita_xml, "validationQuery=\"SELECT 1 FROM DUAL\"", "username=\"bizUser\"", "password=\"bizPwd\"",
                "driverClassName=\"oracle.jdbc.OracleDriver\"", "url=\"jdbc:oracle:thin:@ora1.rd.lan:1521:ORCL\"");
    }

    @Test
    public void configureApplicationServer_should_support_H2_replacements_for_BDM() throws Exception {
        // given:
        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");

        // when:
        configurator.configureApplicationServer();

        // then:
        final Path bitronixFile = tomcatFolder.resolve("conf").resolve("bitronix-resources.properties");
        final Path bonita_xml = tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost").resolve("bonita.xml");

        checkFileContains(bitronixFile, "resource.ds1.className=org.postgresql.xa.PGXADataSource", "resource.ds1.driverProperties.user=bonita",
                "resource.ds1.driverProperties.password=bpm", "resource.ds1.driverProperties.serverName=localhost",
                "resource.ds1.driverProperties.portNumber=5432", "resource.ds1.driverProperties.databaseName=bonita", "resource.ds1.testQuery=SELECT 1");

        checkFileContains(bitronixFile, "resource.ds2.className=org.h2.jdbcx.JdbcDataSource", "resource.ds2.driverProperties.user=sa",
                "resource.ds2.driverProperties.password=", "resource.ds2.driverProperties.URL=jdbc:h2:file:" + databaseAbsolutePath
                        + "/business_data.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;",
                "resource.ds2.testQuery=SELECT 1");

        checkFileContains(bonita_xml, "validationQuery=\"SELECT 1\"", "username=\"sa\"", "password=\"\"", "driverClassName=\"org.h2.Driver\"",
                "url=\"jdbc:h2:file:" + databaseAbsolutePath + "/business_data.db;MVCC=TRUE;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;\"");
    }

    private void checkFileContains(Path file, String... expectedTexts) throws IOException {
        final String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        for (String text : expectedTexts) {
            assertThat(content).contains(text);
        }
    }

    @Test
    public void getDriverFilter_should_detect_Oracle_drivers() throws Exception {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("oracle");

        // then:
        assertThat(driverFilter.accept(new File("myFavoriteOjdbc1.4drivers.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("OraCLE-4.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("OraCLE.zip"))).isTrue();
        assertThat(driverFilter.accept(new File("OJdbc-1.4.2.ZIP"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_Postgres_drivers() throws Exception {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("postgres");

        // then:
        assertThat(driverFilter.accept(new File("postgres.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("POSTGRESsql-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_postgres.LAST.zip"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_SQLSERVER_drivers() throws Exception {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("sqlserver");

        // then:
        assertThat(driverFilter.accept(new File("sqlserver.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("SQLSERVER-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_SQLServer.zip"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc4.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc41.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc42.jar"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_MySQL_drivers() throws Exception {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("mysql");

        // then:
        assertThat(driverFilter.accept(new File("MySQL.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("mySQL-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_mysql.zIp"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_H2_drivers() throws Exception {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("h2");

        // then:
        assertThat(driverFilter.accept(new File("h2-1.4.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers-H2.ZIP"))).isTrue();
        assertThat(driverFilter.accept(new File("my-custom-h2_package.jar"))).isTrue();
    }

    @Test
    public void should_copy_both_drivers_if_not_the_same_dbVendor_for_bdm() throws Exception {
        // when:
        spy.configureApplicationServer();

        // then:
        verify(spy).copyDriverFile(any(Path.class), any(Path.class), eq("postgres"));
        verify(spy).copyDriverFile(any(Path.class), any(Path.class), eq("oracle"));
    }

    @Test
    public void should_not_copy_drivers_again_if_same_dbVendor_for_bdm() throws Exception {
        final String bdmDbVendor = "postgres";
        System.setProperty("bdm.db.vendor", bdmDbVendor);

        // when:
        spy.configureApplicationServer();

        // then:
        verify(spy, times(1)).copyDriverFile(any(Path.class), any(Path.class), anyString());
    }

    @Test
    public void should_fail_if_tomcat_mandatory_file_not_present() throws Exception {
        final Path confFile = tomcatFolder.resolve("conf").resolve("bitronix-resources.properties");
        FileUtils.deleteQuietly(confFile.toFile());

        // then:
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("File bitronix-resources.properties is mandatory but is not found");

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
    public void exception_in_configure_should_restore_previous_configuration() throws Exception {
        // given:
        doThrow(PlatformException.class).when(spy).copyDatabaseDriversIfNecessary(any(Path.class), any(Path.class), eq("oracle"));

        // when:
        try {
            spy.configureApplicationServer();
            fail("Should have thrown exception");
        } catch (PlatformException e) {
            // then:
            verify(spy).restorePreviousConfiguration(any(Path.class), any(Path.class), any(Path.class), any(Path.class));
        }
    }

    @Test
    public void should_not_make_backup_if_content_is_the_same() throws Exception {
        // given:
        configurator.configureApplicationServer();

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("bitronix-resources.properties")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);

        // when:
        configurator.configureApplicationServer();

        // then:
        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("bitronix-resources.properties")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);
    }

    @Test
    public void should_make_new_backup_if_configuration_changes() throws Exception {
        // given:
        configurator.configureApplicationServer();

        assertThat(numberOfBackups("bonita.xml")).isEqualTo(1);
        assertThat(numberOfBackups("bitronix-resources.properties")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(1);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(1);

        System.setProperty("bdm.db.vendor", "h2");
        System.setProperty("bdm.db.database.name", "business_data.db");
        System.setProperty("bdm.db.user", "sa");
        System.setProperty("bdm.db.password", "");

        // so that horodated file has different
        Thread.sleep(1020);

        // when:
        new TomcatBundleConfigurator(bundleFolder).configureApplicationServer();

        // then:
        assertThat(numberOfBackups("bonita.xml")).isEqualTo(2);
        assertThat(numberOfBackups("bitronix-resources.properties")).isEqualTo(2);
        assertThat(numberOfBackups("setenv.bat")).isEqualTo(2);
        assertThat(numberOfBackups("setenv.sh")).isEqualTo(2);
    }

    @Test
    public void escapeWindowsBackslashesIfAny_should_double_backslashes() throws Exception {
        // when:
        final String windowsValue = configurator.convertWindowsBackslashes("C:\\Windows");

        // then:
        assertThat(windowsValue).isEqualTo("C:/Windows");
    }

    @Test
    public void xml_chars_in_URLs_should_be_escaped_before_replacing() throws Exception {
        // given:
        String url = "jdbc:mysql://${bdm.db.server.name}:${bdm.db.server.port}/${bdm.db.database.name}?dontTrackOpenResources=true&useUnicode=true&characterEncoding=UTF-8";

        // when:
        final String escapedXmlCharacters = configurator.escapeXmlCharacters(url);

        // then:
        assertThat(escapedXmlCharacters)
                .isEqualTo(
                        "jdbc:mysql://${bdm.db.server.name}:${bdm.db.server.port}/${bdm.db.database.name}?dontTrackOpenResources=true&amp;useUnicode=true&amp;characterEncoding=UTF-8");
    }
}
