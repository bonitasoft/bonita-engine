/**
 * Copyright (C) 2025 Bonitasoft S.A.
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

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringPropertyTest {

    @BeforeEach
    void setUp() {
        // Reset the static cache of already logged properties before each test
        StringProperty.clearLoggedProperties();
    }

    @Test
    void initialization_message_should_be_logged_once_only() throws Exception {
        String log = tapSystemOut(() -> new StringProperty("my property", "my.property", "default value"));
        assertThat(log).contains(
                "my property default value, you may set it using env property MY_PROPERTY or System property -Dmy.property");

        // should not log again:
        log = tapSystemOut(() -> new StringProperty("my property", "my.property", "default value"));
        assertThat(log).doesNotContain("my.property");
    }
}
