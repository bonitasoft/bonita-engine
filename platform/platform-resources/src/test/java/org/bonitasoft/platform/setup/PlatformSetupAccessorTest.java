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
import static org.bonitasoft.platform.configuration.ConfigurationService.DEFAULT_TRANSACTION_TIMEOUT_SECONDS;
import static org.bonitasoft.platform.configuration.ConfigurationService.TRANSACTION_TIMEOUT_PROPERTY;
import static org.bonitasoft.platform.setup.PlatformSetupAccessor.createJdbcTemplate;
import static org.bonitasoft.platform.setup.PlatformSetupAccessor.createTransactionTemplate;
import static org.bonitasoft.platform.setup.PlatformSetupAccessor.timeoutSeconds;
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class PlatformSetupAccessorTest {

    @Test
    public void timeoutSeconds_should_default_when_property_is_not_set() {
        assertThat(timeoutSeconds((String) null)).isEqualTo(DEFAULT_TRANSACTION_TIMEOUT_SECONDS);
        assertThat(timeoutSeconds(" ")).isEqualTo(DEFAULT_TRANSACTION_TIMEOUT_SECONDS);
    }

    @Test
    public void timeoutSeconds_should_read_seconds_when_no_unit_is_given() {
        assertThat(timeoutSeconds("90")).isEqualTo(90);
    }

    @Test
    public void timeoutSeconds_should_read_a_duration_with_unit() {
        assertThat(timeoutSeconds("90s")).isEqualTo(90);
        assertThat(timeoutSeconds("2m")).isEqualTo(120);
        assertThat(timeoutSeconds("PT30S")).isEqualTo(30);
    }

    @Test
    public void timeoutSeconds_should_reject_a_value_below_one_second() {
        for (String value : new String[] { "0", "500ms", "-5" }) {
            assertThatThrownBy(() -> timeoutSeconds(value))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(TRANSACTION_TIMEOUT_PROPERTY)
                    .hasMessageContaining(value);
        }
    }

    @Test
    public void timeoutSeconds_should_name_the_property_when_the_value_cannot_be_parsed() {
        assertThatThrownBy(() -> timeoutSeconds("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(TRANSACTION_TIMEOUT_PROPERTY)
                .hasMessageContaining("abc");
    }

    @Test
    public void timeoutSeconds_should_read_the_environment_variable() {
        assertThat(timeoutSeconds(environment(Map.of(), Map.of("BONITA_PLATFORM_SETUP_TRANSACTION_TIMEOUT", "90s"))))
                .isEqualTo(90);
    }

    @Test
    public void timeoutSeconds_should_prefer_the_system_property_over_the_environment_variable() {
        assertThat(timeoutSeconds(environment(Map.of(TRANSACTION_TIMEOUT_PROPERTY, "30s"),
                Map.of("BONITA_PLATFORM_SETUP_TRANSACTION_TIMEOUT", "90s")))).isEqualTo(30);
    }

    @Test
    public void timeoutSeconds_should_default_when_neither_is_set() {
        assertThat(timeoutSeconds(environment(Map.of(), Map.of()))).isEqualTo(DEFAULT_TRANSACTION_TIMEOUT_SECONDS);
    }

    private static StandardEnvironment environment(Map<String, Object> systemProperties,
            Map<String, Object> environmentVariables) {
        StandardEnvironment environment = new StandardEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.replace(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                new MapPropertySource(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, systemProperties));
        propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        environmentVariables));
        return environment;
    }

    @Test
    public void should_apply_the_timeout_to_statements_and_transactions() {
        //given
        DataSource dataSource = mock(DataSource.class);

        //when
        JdbcTemplate jdbcTemplate = createJdbcTemplate(dataSource, 42);
        TransactionTemplate transactionTemplate = createTransactionTemplate(dataSource, 42);

        //then
        assertThat(jdbcTemplate.getQueryTimeout()).isEqualTo(42);
        assertThat(((DataSourceTransactionManager) transactionTemplate.getTransactionManager()).getDefaultTimeout())
                .isEqualTo(42);
    }
}
