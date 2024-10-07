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
package org.bonitasoft.web.rest.server.api.platform;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.platform.PlatformInformationAPI;
import org.bonitasoft.engine.session.APISession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemInformationControllerTest {

    @Mock
    private PlatformInformationAPI platformInformationAPI;

    @Spy
    private SystemInformationController systemInformationController;

    @Test
    void get_platform_information_should_return_raw_value_from_Engine() throws Exception {
        //given
        APISession apiSession = mock(APISession.class);
        final HttpSession httpSession = mock(HttpSession.class);
        doReturn(apiSession).when(systemInformationController).getApiSession(httpSession);
        doReturn(platformInformationAPI).when(systemInformationController).getPlatformInformationAPI(apiSession);
        final Map<String, String> expectedInfo = Map.of("key", "value");
        doReturn(expectedInfo).when(platformInformationAPI).getPlatformInformation();

        //when
        final Map<String, String> platformInfo = systemInformationController.getPlatformInfo(httpSession);

        //then
        assertThat(platformInfo).isEqualTo(expectedInfo);
    }

}
