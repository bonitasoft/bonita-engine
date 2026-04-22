/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.internal.servlet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.InvalidClassException;
import java.lang.reflect.InaccessibleObjectException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import com.thoughtworks.xstream.security.ForbiddenClassException;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
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
                .post("http://localhost/serverAPI/com.bonitasoft.engine.api.LoginAPI/login")
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
        assertThat(response.getContentAsString()).as("Response content").isEqualToIgnoringWhitespace("<object-stream>" +
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

    @Test
    public void should_sanitize_error_response_when_xstream_deny_list_blocks_type() throws Exception {
        // given: a ForbiddenClassException from XStream deny list (simulated via mock to avoid
        // Java module access issues with XStream's FieldDictionary on Java 17+)
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        ForbiddenClassException forbidden = new ForbiddenClassException(java.net.URL.class);
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any()))
                .thenThrow(new BonitaRuntimeException("Unable to deserialize object", forbidden));

        // when:
        httpAPIServletCall.doPost();

        // then: response should contain a generic error, not security mechanism details
        // Note: status code assertion omitted — ServletCall.error() flushes the output before
        // calling setStatus(), so MockHttpServletResponse commits with 200 before the 500 is set.
        // In production, the real servlet container buffers the response so 500 is effective.
        String content = response.getContentAsString();
        assertThat(content).contains("Invalid request");
        assertThat(content).doesNotContain("ForbiddenClassException");
        assertThat(content).doesNotContain("java.net.URL");
        // NoPermission is the XStream security error type name — must not leak to clients
        assertThat(content).doesNotContain("NoPermission");
    }

    @Test
    public void should_return_full_error_response_for_non_security_exceptions() throws Exception {
        // given: a request that triggers a non-security exception from the server API
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any()))
                .thenThrow(new BonitaRuntimeException("Something went wrong"));

        // when:
        httpAPIServletCall.doPost();

        // then: response should contain the real exception, not the sanitized "Invalid request"
        String content = response.getContentAsString();
        assertThat(content).contains("BonitaRuntimeException");
        assertThat(content).contains("Something went wrong");
        assertThat(content).doesNotContain("Invalid request");
    }

    @Test
    public void should_sanitize_error_response_when_jep290_filter_rejects_class() throws Exception {
        // given: a request where the JEP 290 ObjectInputFilter rejects a class during deserialization
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        InvalidClassException ice = new InvalidClassException("com.evil.Payload", "filter status: REJECTED");
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("deserialization failed", ice));

        // when:
        httpAPIServletCall.doPost();

        // then: response should be sanitized
        String content = response.getContentAsString();
        assertThat(content).contains("Invalid request");
        assertThat(content).doesNotContain("com.evil.Payload");
        assertThat(content).doesNotContain("filter status: REJECTED");
    }

    @Test
    public void should_sanitize_error_response_when_security_exception_is_deeply_nested() throws Exception {
        // given: ForbiddenClassException wrapped two levels deep in the cause chain
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        ForbiddenClassException forbidden = new ForbiddenClassException(java.net.URL.class);
        RuntimeException nested = new RuntimeException("conversion failed",
                new RuntimeException("inner wrapper", forbidden));
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any())).thenThrow(nested);

        // when:
        httpAPIServletCall.doPost();

        // then: response should be sanitized despite the deep nesting
        String content = response.getContentAsString();
        assertThat(content).contains("Invalid request");
        assertThat(content).doesNotContain("ForbiddenClassException");
        assertThat(content).doesNotContain("java.net.URL");
    }

    @Test
    public void should_sanitize_error_response_when_security_exception_wrapped_in_ServerWrappedException()
            throws Exception {
        // given: a ForbiddenClassException wrapped in ServerWrappedException (production wire-transport path)
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        ForbiddenClassException forbidden = new ForbiddenClassException(java.net.URL.class);
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any()))
                .thenThrow(new ServerWrappedException(forbidden));

        // when:
        httpAPIServletCall.doPost();

        // then: response should be sanitized despite ServerWrappedException wrapping
        String content = response.getContentAsString();
        assertThat(content).contains("Invalid request");
        assertThat(content).doesNotContain("ForbiddenClassException");
        assertThat(content).doesNotContain("java.net.URL");
    }

    @Test
    public void should_not_sanitize_error_response_for_non_jep290_InvalidClassException() throws Exception {
        // given: an InvalidClassException caused by serialVersionUID mismatch (not a security rejection)
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/login")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        InvalidClassException ice = new InvalidClassException("com.example.MyClass",
                "local class incompatible: stream classdesc serialVersionUID = 123, local class serialVersionUID = 456");
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("deserialization failed", ice));

        // when:
        try {
            httpAPIServletCall.doPost();
        } catch (InaccessibleObjectException e) {
            // On Java 17+, XStream fails to serialize IOException-derived exceptions
            // due to module access restrictions (same issue documented in toResponse()).
            // This is expected and not what we're testing.
        }

        // then: response should NOT contain the sanitized "Invalid request" message,
        // proving isDeserializationSecurityException correctly returned false
        String content = response.getContentAsString();
        assertThat(content).doesNotContain("Invalid request");
    }

    @Test
    public void should_write_empty_body_when_output_object_is_null() throws Exception {
        // BPA-443 defence-in-depth: output(Object) is the sibling of output(String) and
        // carries the same null hazard — object.toString() NPEs if object is null. Same guard.
        // given:
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/org.bonitasoft.engine.api.LoginAPI/logout")
                .buildRequest(new MockServletContext());
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpAPIServletCall httpAPIServletCall = new HttpAPIServletCall(request, response);

        // when:
        httpAPIServletCall.output((Object) null);

        // then:
        assertThat(response.getContentAsString())
                .as("Null Object must produce empty body, not NPE or literal \"null\"")
                .isEmpty();
    }

    @Test
    public void should_write_empty_body_when_server_api_returns_null_for_void_method() throws Exception {
        // BPA-443: when a void API method is invoked (e.g. logout, retryTask), the server-side
        // invokeMethod returns null. Writing "null" via PrintWriter.print(null) yields an NPE
        // inside response wrappers that track content length (e.g. Spring Session's
        // SaveContextPrintWriter). Guard against this at the source: null → empty body.
        // given:
        MockHttpServletRequest request = MockMvcRequestBuilders
                .post("http://localhost/serverAPI/com.bonitasoft.engine.api.LoginAPI/logout")
                .param("options", "<object-stream><map/></object-stream>")
                .buildRequest(new MockServletContext());

        MockHttpServletResponse response = new MockHttpServletResponse();

        HttpAPIServletCall httpAPIServletCall = spy(new HttpAPIServletCall(request, response));
        ServerAPI serverAPI = mock(ServerAPI.class);
        doReturn(serverAPI).when(httpAPIServletCall).getServerAPI();
        when(serverAPI.invokeMethod(any(), any(), any(), any(), any())).thenReturn(null);

        // when:
        httpAPIServletCall.doPost();

        // then:
        assertThat(response.getStatus()).as("Response status").isEqualTo(200);
        assertThat(response.getContentAsString())
                .as("Response body for void method must be empty, not the literal string \"null\"")
                .isEmpty();
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static Date date(String date) {
        return Date.from(Instant.parse(date));
    }

}
