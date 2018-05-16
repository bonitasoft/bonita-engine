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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;
import static org.bonitasoft.platform.setup.ScriptExecutor.FAIL_ON_ERROR;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * author Laurent Leseigneur
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        PlatformSetupApplication.class
})
public class ScriptExecutorIT {
    @Rule
    public TestRule clean = new ClearSystemProperties(BONITA_SETUP_FOLDER);

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Value("${db.vendor}")
    String dbVendor;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        removeSetupFolderProperty();
    }

    @After
    public void after() throws Exception {
        removeSetupFolderProperty();
        scriptExecutor.deleteTables();
    }

    private void removeSetupFolderProperty() {
        System.clearProperty(BONITA_SETUP_FOLDER);
    }

    @Test
    public void should_be_able_to_create_platform_tables() throws Exception {
        scriptExecutor.createAndInitializePlatformIfNecessary();
        final Integer sequences = jdbcTemplate.queryForObject("select count(*) from sequence", Integer.class);
        assertThat(sequences).isEqualTo(6);

        final int platformRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "platform");
        assertThat(platformRows).isEqualTo(1);
        final Map<String, Object> row = jdbcTemplate.queryForMap("select * from platform");
        assertThat("" + row.get("id")).isEqualTo("1"); // convert to String as not all RDBMS convert the same way (long, int, bigDecimal...)
        assertThat(row.get("createdby")).isEqualTo("platformAdmin");
    }

    @Test
    public void should_executeSQLResource_use_filesystem() throws Exception {
        //given
        final File confFolder = temporaryFolder.newFolder();
        final Path createTableScript = confFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor).resolve("myScript.sql");
        final Path dropTableScript = confFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor).resolve("cleanup.sql");
        Files.createDirectories(createTableScript.getParent());
        final String createTable = "CREATE TABLE " +
                dbVendor +
                "_configuration (" +
                "  tenant_id BIGINT NOT NULL," +
                "  content_type VARCHAR(50) NOT NULL," +
                "  resource_name VARCHAR(120) NOT NULL," +
                "  resource_content LONGBLOB NOT NULL" +
                ")";
        Files.write(createTableScript, createTable.getBytes());
        Files.write(dropTableScript, ("DROP TABLE " + dbVendor + "_configuration").getBytes());
        System.setProperty(BONITA_SETUP_FOLDER, confFolder.getAbsolutePath());

        //when
        scriptExecutor.executeSQLResource("myScript.sql", FAIL_ON_ERROR);

        //then
        final int platformRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, dbVendor +
                "_configuration");
        scriptExecutor.executeSQLResource("cleanup.sql", FAIL_ON_ERROR);
        assertThat(platformRows).as("should create empty table from filesystem ").isEqualTo(0);
    }

    @Test
    public void should_executeSQLResource_fail_when_script_is_missing() throws Exception {
        //given
        final File confFolder = temporaryFolder.newFolder();
        final Path sqlScript = confFolder.toPath().resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve(dbVendor).resolve("missingScript.sql");
        System.setProperty(BONITA_SETUP_FOLDER, confFolder.getAbsolutePath());

        //expect
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("SQL resource file not found in filesystem: " + sqlScript.toFile().getAbsolutePath());

        //when
        scriptExecutor.executeSQLResource("missingScript.sql", FAIL_ON_ERROR);
    }
}
