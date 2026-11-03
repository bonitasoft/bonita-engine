/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.impl.BonitaAllConfigurationPreparedStatementSetter;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.bonitasoft.platform.util.ConfigurationFolderUtil;
import org.bonitasoft.platform.version.VersionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A {@code setup pull} running while a {@code push} replaces the configuration must never read it empty, otherwise
 * it writes an empty {@code platform_conf/current} folder and loses the license file.
 * <p>
 * The rows are counted on an independent connection (as a concurrent {@code pull} would) twice: when {@code push()}
 * starts storing the new configuration, and inside the replace, once the old rows are deleted and before the new
 * ones are inserted and committed. The second sample needs a non-blocking read of uncommitted deletes (H2,
 * PostgreSQL).
 * <p>
 * The spy wraps a {@link PlatformSetup} built here rather than a {@code @SpyBean}: a bean override would start a
 * second Spring context, whose {@link MemoryJNDISetup} would fail to re-bind the JNDI datasources. This community
 * {@link PlatformSetup} does not support the subscription databases, hence the {@code community-only} tag.
 */
@Tag("community-only")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { PlatformSetupApplication.class })
class PlatformSetupConcurrencyIT {

    @Value("${db.vendor}")
    private String dbVendor;

    @Value("${bdm.db.vendor}")
    private String bdmDbVendor;

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    DataSource dataSource;

    @Autowired
    ScriptExecutor scriptExecutor;

    @Autowired
    VersionService versionService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    PlatformSetup platformSetup;

    private final ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();

    @BeforeEach
    void setUp() throws Exception {
        System.clearProperty(BONITA_SETUP_FOLDER);
        platformSetup.destroy();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(BONITA_SETUP_FOLDER);
    }

    @Test
    void push_should_not_expose_empty_configuration_to_a_concurrent_reader(@TempDir Path setupFolder)
            throws Exception {
        // given: a push driven through a spied ConfigurationService and JdbcTemplate, and a created platform
        final JdbcTemplate spiedJdbcTemplate = spy(jdbcTemplate);
        final ConfigurationService spiedConfigurationService = spy(
                new ConfigurationServiceImpl(spiedJdbcTemplate, transactionTemplate, dbVendor));
        final PlatformSetup platformSetupUnderTest = new PlatformSetup(scriptExecutor, spiedConfigurationService,
                versionService, dataSource, dbVendor, bdmDbVendor);

        platformSetupUnderTest.init();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toAbsolutePath().toString());
        configurationFolderUtil.buildCurrentFolder(setupFolder);
        seedLicenseRow();
        createLicenseFileInSetupFolder(setupFolder);

        assertThat(countConfigurationRowsOnSeparateConnection())
                .as("precondition: platform configuration is present before push")
                .isGreaterThan(0);
        assertThat(countLicenseRowsOnSeparateConnection())
                .as("precondition: a license is present before push")
                .isGreaterThan(0);

        final AtomicInteger configRowsSeenByConcurrentReader = new AtomicInteger(-1);
        final AtomicInteger licenseRowsSeenByConcurrentReader = new AtomicInteger(-1);
        doAnswer(invocation -> {
            configRowsSeenByConcurrentReader.set(countConfigurationRowsOnSeparateConnection());
            licenseRowsSeenByConcurrentReader.set(countLicenseRowsOnSeparateConnection());
            return invocation.callRealMethod();
        }).when(spiedConfigurationService).storeAllConfiguration(any());
        final AtomicInteger configRowsSeenDuringReplace = new AtomicInteger(-1);
        doAnswer(invocation -> {
            configRowsSeenDuringReplace.set(countConfigurationRowsOnSeparateConnection());
            return invocation.callRealMethod();
        }).when(spiedJdbcTemplate).batchUpdate(eq(BonitaAllConfigurationPreparedStatementSetter.INSERT_CONFIGURATION),
                any(BatchPreparedStatementSetter.class));

        // when
        platformSetupUnderTest.forcePush();

        // then
        assertThat(configRowsSeenByConcurrentReader.get())
                .as("a concurrent reader must never see an empty configuration while it is replaced")
                .isGreaterThan(0);
        assertThat(configRowsSeenDuringReplace.get())
                .as("the delete of the replace must not be visible before the insert is committed with it")
                .isGreaterThan(0);
        assertThat(licenseRowsSeenByConcurrentReader.get())
                .as("replacing the configuration must keep the licenses (they are replaced in a later transaction)")
                .isGreaterThan(0);
    }

    /**
     * Reads the configuration row count (licenses excluded) on a fresh connection from the pool, independent of any
     * transaction opened by {@code push()} - the way a concurrent {@code setup pull} process would see the database.
     */
    private int countConfigurationRowsOnSeparateConnection() throws Exception {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT COUNT(*) FROM configuration WHERE content_type <> ?")) {
            statement.setString(1, ConfigurationType.LICENSES.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** License row count on an independent connection - the way a concurrent {@code setup pull} would see it. */
    private int countLicenseRowsOnSeparateConnection() throws Exception {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT COUNT(*) FROM configuration WHERE content_type = ?")) {
            statement.setString(1, ConfigurationType.LICENSES.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /** Insert a committed LICENSES row directly, so it is present in the DB before the push under test runs. */
    private void seedLicenseRow() throws Exception {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO configuration(tenant_id, content_type, resource_name, resource_content) "
                                + "VALUES (?, ?, ?, ?)")) {
            statement.setLong(1, 0L);
            statement.setString(2, ConfigurationType.LICENSES.name());
            statement.setString(3, "bpa291-test.lic");
            statement.setBytes(4, "dummy-license-content".getBytes(StandardCharsets.UTF_8));
            statement.executeUpdate();
        }
    }

    /** Place a .lic in the setup folder so {@code push()} can re-push the seeded license and complete cleanly. */
    private void createLicenseFileInSetupFolder(Path setupFolder) throws Exception {
        Path licensesFolder = setupFolder.resolve(ConfigurationFolderUtil.PLATFORM_CONF_FOLDER_NAME)
                .resolve("licenses");
        Files.createDirectories(licensesFolder);
        Files.write(licensesFolder.resolve("bpa291-test.lic"),
                "dummy-license-content".getBytes(StandardCharsets.UTF_8));
    }
}
