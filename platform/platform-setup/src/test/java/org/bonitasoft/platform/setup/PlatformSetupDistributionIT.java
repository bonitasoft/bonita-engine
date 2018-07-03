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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Baptiste Mesta
 */
public class PlatformSetupDistributionIT {

    //
    // WARNING !!!! This IT test class must be launched after a MAVEN build has been run:
    // See PlatformSetupTestUtils.extractDistributionTo() to better understand why.
    //

    private File distFolder;

    @Rule
    public TestRule clean = new ClearSystemProperties("db.admin.user", "sysprop.bonita.db.vendor", "db.user",
            "db.password", "db.vendor", "db.server.name",
            "db.admin.password", "sysprop.bonita.bdm.db.vendor", "db.server.port", "db.database.name");

    @Rule
    public EnvironmentVariables envVar = new EnvironmentVariables();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        distFolder = temporaryFolder.newFolder();
        PlatformSetupTestUtils.extractDistributionTo(distFolder);
    }

    @Test
    public void setupSh_should_work_with_init_on_h2_and_prevent_pushing_deletion() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("init");
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(distFolder);
        executor.setStreamHandler(PlatformSetupTestUtils.getExecuteStreamHandler("yes"));

        //when
        int iExitValue = executor.execute(oCmdLine);

        //then
        assertThat(iExitValue).isEqualTo(0);
        Connection jdbcConnection = PlatformSetupTestUtils.getJdbcConnection(distFolder);
        Statement statement = jdbcConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS nb FROM CONFIGURATION");
        resultSet.next();
        assertThat(resultSet.getInt("nb")).isGreaterThan(0);

        oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("pull");
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isEqualTo(0);

        final Path platform_engine = distFolder.toPath().resolve("platform_conf").resolve("current")
                .resolve("platform_engine");
        FileUtils.deleteDirectory(platform_engine.toFile());

        oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArgument("push");
        executor.setExitValue(1);
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isEqualTo(1);

        oCmdLine.addArgument("--force");
        executor.setExitValue(0);
        iExitValue = executor.execute(oCmdLine);
        assertThat(iExitValue).isEqualTo(0);
    }

    @Test
    public void setupSh_should_work_with_init_on_h2_with_overridden_system_property() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArguments("init -Ddb.user=myUser");
        DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(distFolder);
        executor.setStreamHandler(PlatformSetupTestUtils.getExecuteStreamHandler("Y"));
        //when
        int iExitValue = executor.execute(oCmdLine);
        //then
        assertThat(iExitValue).isEqualTo(0);
        Connection jdbcConnection = PlatformSetupTestUtils.getJdbcConnection(distFolder, "myUser");
        Statement statement = jdbcConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS nb FROM CONFIGURATION");
        resultSet.next();
        assertThat(resultSet.getInt("nb")).isGreaterThan(0);
    }

    @Test
    public void setupSh_should_have_error_with_no_argument() throws Exception {
        //given
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        DefaultExecutor oDefaultExecutor = PlatformSetupTestUtils.createExecutor(distFolder);
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
        Server pgServer = Server.createPgServer("-baseDir", dbFolder.getAbsolutePath());
        CommandLine oCmdLine = PlatformSetupTestUtils.createCommandLine();
        oCmdLine.addArguments("init");
        try {
            //server must be started to have a valid port
            pgServer.start();
            DefaultExecutor executor = PlatformSetupTestUtils.createExecutor(distFolder);
            //when
            Path databaseProperties = distFolder.toPath().resolve("database.properties");
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
            assertThat(iExitValue).isEqualTo(0);
        } finally {
            pgServer.shutdown();
        }
    }
}
