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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.util.ConfigurationFolderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs on every supported database: how a statement cancelled by its timeout is reported depends on the driver.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { PlatformSetupApplication.class })
class ConfigurationServiceIT {

    private static final String COUNT_ROWS = "SELECT COUNT(*) FROM configuration WHERE content_type = ? AND resource_name = ?";

    @Value("${db.vendor}")
    private String dbVendor;

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PlatformSetup platformSetup;

    @TempDir
    Path setupFolder;

    private Path configurationFolder;

    @BeforeEach
    void setUp() throws Exception {
        platformSetup.destroy();
        platformSetup.init();
        configurationFolder = new ConfigurationFolderUtil().buildCurrentFolder(setupFolder).getParent();
    }

    @Test
    void storeAllConfiguration_should_replace_the_configuration_and_keep_the_licenses() throws Exception {
        //given
        insertConfiguration(ConfigurationType.PLATFORM_PORTAL, "stale.properties");
        insertConfiguration(ConfigurationType.LICENSES, "license.lic");

        //when
        serviceWithTimeout(60).storeAllConfiguration(configurationFolder);

        //then
        assertThat(countRows(ConfigurationType.PLATFORM_PORTAL, "stale.properties")).as("absent from the folder")
                .isZero();
        assertThat(countRows(ConfigurationType.PLATFORM_ENGINE, "currentConfig.properties")).as("from the folder")
                .isEqualTo(1);
        assertThat(countRows(ConfigurationType.LICENSES, "license.lic")).as("licenses are kept").isEqualTo(1);
    }

    @Test
    void storeAllConfiguration_should_fail_with_a_timeout_message_when_the_configuration_stays_locked()
            throws Exception {
        //given a row absent from the folder, and another transaction holding a lock on the configuration rows
        insertConfiguration(ConfigurationType.PLATFORM_PORTAL, "stale.properties");
        List<String> rowsBefore = allRows();
        try (Connection lockHolder = dataSource.getConnection()) {
            lockHolder.setAutoCommit(false);
            try (PreparedStatement lock = lockHolder.prepareStatement(
                    "UPDATE configuration SET resource_name = resource_name WHERE content_type <> ?")) {
                lock.setString(1, ConfigurationType.LICENSES.name());
                lock.executeUpdate();
            }
            try {
                //when-then
                assertThatThrownBy(() -> serviceWithTimeout(1).storeAllConfiguration(configurationFolder))
                        .isInstanceOf(PlatformException.class)
                        .hasMessageContaining("did not complete the write in time");
            } finally {
                lockHolder.rollback();
            }
        }
        assertThat(allRows()).as("the failed replace left the configuration unchanged").isEqualTo(rowsBefore);
    }

    private ConfigurationServiceImpl serviceWithTimeout(int seconds) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));
        transactionTemplate.setTimeout(seconds);
        return new ConfigurationServiceImpl(new JdbcTemplate(dataSource), transactionTemplate, dbVendor);
    }

    private void insertConfiguration(ConfigurationType type, String resourceName) {
        jdbcTemplate.update(
                "INSERT INTO configuration(content_type, resource_name, resource_content) VALUES (?, ?, ?)",
                type.name(), resourceName, "content".getBytes(StandardCharsets.UTF_8));
    }

    private List<String> allRows() {
        return jdbcTemplate.queryForList(
                "SELECT content_type, resource_name FROM configuration ORDER BY content_type, resource_name")
                .stream().map(row -> row.values().toString()).toList();
    }

    private int countRows(ConfigurationType type, String resourceName) {
        return jdbcTemplate.queryForObject(COUNT_ROWS, Integer.class, type.name(), resourceName);
    }
}
