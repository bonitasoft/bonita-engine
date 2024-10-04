/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager.getProperties;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class AuthenticationManagerPropertiesTest {

    public AuthenticationManagerProperties loginManagerProperties;

    @Before
    public void setUp() throws IOException {
        loginManagerProperties = spy(new AuthenticationManagerProperties());
        doReturn(getProperties(("OAuth.serviceProvider = LinkedIn\n" +
                "OAuth.consumerKey = ove2vcdjptar\n" +
                "OAuth.consumerSecret = vdaBrCmHvkgJoYz1\n" +
                "OAuth.callbackURL = http://127.0.0.1:8888/loginservice").getBytes())).when(loginManagerProperties)
                .getTenantPropertiesOfScope();
    }

    @After
    public void tearDown() {
        loginManagerProperties = null;
    }

    @Test
    public void isLogoutDisabled_should_return_FALSE_if_not_set() {
        // given:
        final AuthenticationManagerProperties properties = AuthenticationManagerProperties.getProperties();

        // when:
        final boolean isLogoutDisabled = properties.isLogoutDisabled();

        // then:
        assertThat(isLogoutDisabled).isFalse();
    }

    @Test
    public void testGetOAuthServiceProviderName() {
        assertNotNull("Cannot get OAuth service provider name", loginManagerProperties.getOAuthServiceProviderName());
    }

    @Test
    public void testGetOAuthConsumerKey() {
        assertNotNull("Cannot get OAuth consumer key", loginManagerProperties.getOAuthConsumerKey());
    }

    @Test
    public void testGetOAuthConsumerSecret() {
        assertNotNull("Cannot get OAuth consumer secret", loginManagerProperties.getOAuthConsumerSecret());
    }

    @Test
    public void testGetOAuthCallbackURL() {
        assertNotNull("Cannot get OAuth callback URL", loginManagerProperties.getOAuthCallbackURL());
    }

}
