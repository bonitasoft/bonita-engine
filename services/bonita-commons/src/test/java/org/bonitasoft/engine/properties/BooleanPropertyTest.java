/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.properties;

import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Emmanuel Duchastenier
 */
class BooleanPropertyTest {

    @Test
    public void booleanProperty_should_take_System_property_if_set() throws Exception {
        restoreSystemProperties(() -> {
            final String propertyKey = "my.feature.enable";
            System.setProperty(propertyKey, "false");
            Boolean enabled = withEnvironmentVariable(propertyKey, "true")
                    .execute(() -> new BooleanProperty("Some feature", propertyKey, true).isEnabled());
            assertThat(enabled).isFalse();
        });
    }

    @Test
    public void booleanProperty_should_take_envVar_if_no_System_property_if_set() throws Exception {
        final String systemPropertyKey = "my.super-cool.feature.enabled";
        final String envPropertyKey = "MY_SUPERCOOL_FEATURE_ENABLED";
        Boolean enabled = withEnvironmentVariable(envPropertyKey, "false")
                .execute(() -> new BooleanProperty("Some feature", systemPropertyKey, true).isEnabled());
        assertThat(enabled).isFalse();
    }

    @Test
    public void booleanProperty_should_take_default_value_if_no_System_property_nor_env_variable_if_set() {
        assertThat(new BooleanProperty("Some feature", "some.key", false).isEnabled()).isFalse();
    }
}
