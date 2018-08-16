/**
 * Copyright (C) 2018 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.internal.servlet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class HttpAPIServletCallTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void should_manage_login_request() throws Exception {
        //given:
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("serverAPI/com.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream>" +
                        "  <map/>" +
                        "</object-stream>")
                .param("classNameParameters", "<object-stream>" +
                        "  <java.util.Arrays_-ArrayList>" +
                        "    <a class=\"string-array\">" +
                        "      <string>java.lang.String</string>" +
                        "      <string>java.lang.String</string>" +
                        "    </a>" +
                        "  </java.util.Arrays_-ArrayList>" +
                        "</object-stream>")
                .param("parametersValues", "<object-stream>" +
                        "  <object-array>" +
                        "    <string>install</string>" +
                        "    <string>install</string>" +
                        "  </object-array>" +
                        "</object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();

        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        APISessionImpl apiSession = new APISessionImpl(-2241174137745053814L, date("2018-06-07T15:10:09.132Z"),
                3600000, "install", -1, "default", 1L);
        apiSession.setTechnicalUser(true);
        when(serverAPI.invokeMethod(new HashMap<>(), "com.bonitasoft.engine.api.LoginAPI", "login",
                asList(String.class.getName(), String.class.getName()) //
                , new Object[] { "install", "install" })) //
                        .thenReturn(apiSession);

        //when:
        httpAPIServletCall.doPost();

        //then:
        assertThat(response.getStatus()).as("Response status").isEqualTo(200);
        assertThat(response.getContentType()).as("Response content type").isEqualTo("application/xml;charset=UTF-8");
        assertThat(response.getContentAsString()).as("Response content").isXmlEqualTo("<object-stream>" +
                "  <org.bonitasoft.engine.session.impl.APISessionImpl>" +
                "    <id>-2241174137745053814</id>" +
                "    <creationDate>2018-06-07 15:10:09.132 UTC</creationDate>" +
                "    <duration>3600000</duration>" +
                "    <userName>install</userName>" +
                "    <userId>-1</userId>" +
                "    <technicalUser>true</technicalUser>" +
                "    <tenantName>default</tenantName>" +
                "    <tenantId>1</tenantId>" +
                "  </org.bonitasoft.engine.session.impl.APISessionImpl>" +
                "</object-stream>");
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static Date date(String date) {
        return Date.from(Instant.parse(date));
    }

}
