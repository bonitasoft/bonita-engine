/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.preferences.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.console.common.server.preferences.properties.SecurityProperties.SANITIZER_PROTECTION;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;

public class SecurityPropertiesTest {

    private final SecurityProperties securityProperties = spy(new SecurityProperties());

    @Test
    public void invalid_or_absent_sanitizer_conf_should_be_disabled() {
        doReturn(null).when(securityProperties).getPlatformProperty(SANITIZER_PROTECTION);
        assertThat(securityProperties.isSanitizerProtectionEnabled()).isFalse();

        doReturn("").when(securityProperties).getPlatformProperty(SANITIZER_PROTECTION);
        assertThat(securityProperties.isSanitizerProtectionEnabled()).isFalse();

        doReturn("false").when(securityProperties).getPlatformProperty(SANITIZER_PROTECTION);
        assertThat(securityProperties.isSanitizerProtectionEnabled()).isFalse();
    }

    @Test
    public void sanitizer_should_be_enabled_for_true_value_whatever_the_case() {
        doReturn("true").when(securityProperties).getPlatformProperty(SANITIZER_PROTECTION);
        assertThat(securityProperties.isSanitizerProtectionEnabled()).isTrue();

        doReturn("trUE").when(securityProperties).getPlatformProperty(SANITIZER_PROTECTION);
        assertThat(securityProperties.isSanitizerProtectionEnabled()).isTrue();
    }
}
