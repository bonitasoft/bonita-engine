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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.setup.command.configure.BundleConfiguratorTest.checkFileContains;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Baptiste Mesta
 */
public class PlatformSetupDistributionIT {

    private File setupFolder;

    @Rule
    public TestRule clean = new ClearSystemProperties("db.admin.user", "sysprop.bonita.db.vendor", "db.user",
            "db.password", "db.vendor", "db.server.name",
            "db.admin.password", "sysprop.bonita.bdm.db.vendor", "db.server.port", "db.database.name");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        setupFolder = temporaryFolder.newFolder();
        PlatformSetupTestUtils.extractDistributionTo(setupFolder);
    }

    @Test
    public void setupSh_should_work_with_init_on_h2_and_prevent_pushing_deletion() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("init");
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(setupFolder);
        executor.setStreamHandler(PlatformSetupTestUtils.getExecuteStreamHandler("yes"));

        //when
        int iExitValue = executor.execute(oCmdLine);

        //then
        assertThat(iExitValue).isZero();
        Connection jdbcConnection = PlatformSetupTestUtils.getJdbcConnection(setupFolder);
        Statement statement = jdbcConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS nb FROM CONFIGURATION");
        resultSet.next();
        assertThat(resultSet.getInt("nb")).isPositive();

        oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("pull");
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isZero();

        final Path platformEngine = setupFolder.toPath().resolve("platform_conf").resolve("current")
                .resolve("platform_engine");
        FileUtils.deleteDirectory(platformEngine.toFile());

        oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("push");
        executor.setExitValue(1);
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isEqualTo(1);

        oCmdLine.addArgument("--force");
        executor.setExitValue(0);
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isZero();
    }

    @Test
    public void setupSh_should_work_with_init_on_h2_with_overridden_system_property() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArguments("init -Ddb.user=myUser");
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(setupFolder);
        executor.setStreamHandler(PlatformSetupTestUtils.getExecuteStreamHandler("Y"));
        //when
        int iExitValue = executor.execute(oCmdLine);
        //then
        assertThat(iExitValue).isZero();
        Connection jdbcConnection = PlatformSetupTestUtils.getJdbcConnection(setupFolder, "myUser");
        Statement statement = jdbcConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS nb FROM CONFIGURATION");
        resultSet.next();
        assertThat(resultSet.getInt("nb")).isPositive();
    }

    @Test
    public void setupSh_should_have_error_with_no_argument() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        DefaultExecutor oDefaultExecutor = PlatformSetupTestUtils.createExecutor(setupFolder);
        oDefaultExecutor.setExitValue(1);
        //when
        int iExitValue = oDefaultExecutor.execute(oCmdLine);
        //then
        assertThat(iExitValue).isEqualTo(1);
    }

    @Test
    public void setupSh_should_work_on_postgres_database() throws Exception {
        //given
        File dbFolder = temporaryFolder.newFolder();
        Server pgServer = Server.createPgServer("-baseDir", dbFolder.getAbsolutePath(), "-ifNotExists");
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArguments("init");
        try {
            //server must be started to have a valid port
            pgServer.start();
            DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(setupFolder);
            //when
            Path databaseProperties = setupFolder.toPath().resolve("database.properties");
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(Files.readAllBytes(databaseProperties)));
            properties.setProperty("db.vendor", "postgres");
            properties.setProperty("db.server.name", "localhost");
            properties.setProperty("db.server.port", String.valueOf(pgServer.getPort()));
            properties.setProperty("db.database.name", "bonita");
            properties.setProperty("db.password", "bpm"); // Because Postgres does not allow to have empty password
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            properties.store(out, "");
            Files.write(databaseProperties, out.toByteArray());
            int iExitValue = executor.execute(oCmdLine);
            //then
            assertThat(iExitValue).isZero();
        } finally {
            pgServer.shutdown();
        }
    }

    @Test
    public void commandLine_should_support_H2_path_with_space_characters() throws Exception {
        //setup
        final File temporaryFolderRoot = temporaryFolder.newFolder();
        Path bundleFolder = temporaryFolderRoot.toPath().toRealPath();
        Path tomcatFolder = bundleFolder.resolve("server");
        PathUtils.copyDirectory(Paths.get("../resources/test/tomcat_conf/server").toAbsolutePath(), tomcatFolder);
        final File newSetupFolder = bundleFolder.resolve("setup").toFile();
        FileUtils.copyDirectory(setupFolder, newSetupFolder);
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArguments("configure");
        oCmdLine.addArguments("-Dh2.database.dir=\"my h2 path\"", false);
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(newSetupFolder);
        executor.setStreamHandler(PlatformSetupTestUtils.getExecuteStreamHandler("yes"));

        //when
        int iExitValue = executor.execute(oCmdLine);

        //then
        assertThat(iExitValue).isZero();
        checkFileContains(tomcatFolder.resolve("conf").resolve("Catalina").resolve("localhost").resolve("bonita.xml"),
                "/my h2 path/");
    }
}
