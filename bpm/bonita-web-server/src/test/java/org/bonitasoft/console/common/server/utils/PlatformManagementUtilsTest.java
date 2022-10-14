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
package org.bonitasoft.console.common.server.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformManagementUtilsTest {

    @Mock
    private PlatformAPI platformAPI;
    @Mock
    private PlatformLoginAPI platformLoginAPI;
    @Spy
    private PlatformManagementUtils platformManagementUtils;
    @Rule
    public TestRule clean = new ClearSystemProperties("org.bonitasoft.platform.username",
            "org.bonitasoft.platform.password");
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() throws Exception {
        doNothing().when(platformManagementUtils).platformLogout(any(PlatformSession.class));
        doReturn(platformAPI).when(platformManagementUtils).getPlatformAPI(any(PlatformSession.class));
        doReturn(platformLoginAPI).when(platformManagementUtils).getPlatformLoginAPI();
    }

    @Test
    public void should_updateConfigurationFile_call_engine() throws Exception {
        //when
        doReturn(new PlatformSessionImpl(1231, new Date(), 54325423, "testUser", 75463)).when(platformManagementUtils)
                .platformLogin();
        doReturn(1L).when(platformManagementUtils).getDefaultTenantId();
        platformManagementUtils.updateConfigurationFile("myFile", "theNewContent".getBytes());
        //then
        InOrder inOrder = inOrder(platformManagementUtils, platformAPI);
        inOrder.verify(platformManagementUtils).platformLogin();
        inOrder.verify(platformAPI).updateClientTenantConfigurationFile(1L, "myFile", "theNewContent".getBytes());
        inOrder.verify(platformManagementUtils).platformLogout(any(PlatformSession.class));
    }

    @Test
    public void should_login_locally_when_connection_is_local() throws Exception {
        //given
        doReturn(true).when(platformManagementUtils).isLocal();
        doReturn(new PlatformSessionImpl(1231, new Date(), 54325423, "testUser", 75463)).when(platformManagementUtils)
                .platformLogin();
        //when
        PlatformSession platformSession = platformManagementUtils.platformLogin();
        //then
        verify(platformLoginAPI, never()).login(anyString(), anyString());
        assertThat(platformSession).isNotNull();
    }

    @Test
    public void should_login_using_system_property_when_connection_is_not_local() throws Exception {
        //given
        doReturn(false).when(platformManagementUtils).isLocal();
        System.setProperty("org.bonitasoft.platform.username", "john");
        System.setProperty("org.bonitasoft.platform.password", "bpm");
        //when
        platformManagementUtils.platformLogin();
        //then
        verify(platformLoginAPI).login("john", "bpm");
    }

    @Test
    public void should_throw_an_exception_with_comprehensive_message_when_system_property_to_connect_to_the_platform_is_not_set_properly()
            throws Exception {
        doReturn(false).when(platformManagementUtils).isLocal();
        doThrow(new InvalidPlatformCredentialsException("wrong credentials")).when(platformLoginAPI).login(null, null);

        expectedException.expect(InvalidPlatformCredentialsException.class);
        expectedException.expectMessage(
                "The portal is not able to login to the engine because system properties org.bonitasoft.platform.username and org.bonitasoft.platform.password are not set correctly");
        platformManagementUtils.platformLogin();
    }
}
