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
package org.bonitasoft.console.common.server.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.web.toolkit.client.common.json.JSonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * Test class SanitizerFilter
 *
 * @author Vincent Hemery
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class SanitizerFilterTest {

    @Mock
    private FilterChain chain;

    @Mock
    private HttpServletRequestAccessor request;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession httpSession;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private ServletContext servletContext;

    @Spy
    SanitizerFilter sanitizerFilter;

    @BeforeEach
    void setUp() {
        doReturn(httpSession).when(request).getHttpSession();
        when(request.asHttpServletRequest()).thenReturn(httpRequest);
        when(httpRequest.getMethod()).thenReturn("POST");
        when(httpRequest.getCharacterEncoding()).thenReturn("UTF-8");
        when(httpRequest.getRequestURL()).thenReturn(new StringBuffer());
        when(servletContext.getContextPath()).thenReturn("");
        when(filterConfig.getServletContext()).thenReturn(servletContext);
        when(filterConfig.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());
    }

    @Test
    void shouldNotSanitizeWhenDisabled() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(false);

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        verify(sanitizerFilter, never()).sanitize(any(JsonNode.class));
        verify(chain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    void shouldNotAffectAttributeValue() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        final String attributeName = "key";
        final String attributeValue = "value";
        when(httpRequest.getAttribute(attributeName)).thenReturn(attributeValue);

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        final String value = r.getAttribute(attributeName).toString();
        assertThat(value).isEqualTo(attributeValue);
    }

    @Test
    void shouldNotAffectParameterValues() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        final String parameterName = "key";
        final String parameterValue = "value";
        when(httpRequest.getParameter(parameterName)).thenReturn(parameterValue);

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        final String value = r.getParameter(parameterName);
        assertThat(value).isEqualTo(parameterValue);
    }

    @Test
    void shouldNotAffectFileUpload() throws Exception {
        when(httpRequest.getContentType()).thenReturn("text/xml");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        final String body = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                "<organization:Organization xmlns:organization=\"http://documentation.ofelia.com/organization-xml-schema/1.1\">%n"
                +
                "  <users>%n" +
                "  </users>%n" +
                "</organization:Organization>");
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = new String(r.getInputStream().readAllBytes());
        assertThat(updatedBody).isEqualTo(body);
    }

    @Test
    void shouldNotAffectNonHtml() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());
        final String body = "{\"key\":\"value\"}";
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = new String(r.getInputStream().readAllBytes());
        assertThat(updatedBody).isEqualTo(body);
    }

    @Test
    void shouldNotAffectPre() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());
        final String body = "{\"key\":\"<div><p>text</p><pre>value formatted</pre></div>\"}";
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = new String(r.getInputStream().readAllBytes());
        assertThat(updatedBody).isEqualTo(body);
    }

    @Test
    void shouldNotAffectLinks() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());
        final String body = "{\"key\":\"<p><a href=\\\"https://documentation.ofelia.com/bonita/latest/\\\">link text</a></p>\"}";
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = new String(r.getInputStream().readAllBytes());
        // Note: rel attribute order is non-deterministic after OWASP HTML Sanitizer removed Guava (v20240325.1+)
        // Check structure and all required rel values are present
        assertThat(updatedBody)
                .startsWith(
                        "{\"key\":\"<p><a href=\\\"https://documentation.ofelia.com/bonita/latest/\\\" rel=\\\"")
                .endsWith("\\\">link text</a></p>\"}")
                .contains("noreferrer")
                .contains("noopener")
                .contains("nofollow");
    }

    @Test
    void shouldSanitizeAttackFromBody() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/JSON");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(List.of("email", "password"));
        // Classic XSS attack in value
        final String attName1 = "test1";
        final String saneValue1 = "Hello <b>World</b>";
        final String attValue1 = saneValue1
                + "<style>@keyframes slidein {}</style><xss style=\"animation-duration:1s;animation-name:slidein;animation-iteration-count:2\" onwebkitanimationiteration=\"alert(1)\"></xss>";
        // Another XSS attack in value
        final String attName2 = "test2";
        final String saneValue2 = "<div>test</div>";
        final String attValue2 = "<div onclick=\"alert('test')\">test</div><script>alert('test')</script>";
        // XSS attack in name
        final String saneAttName3 = "test3";
        final String attName3 = saneAttName3 + "<script>alert('test')</script>";
        final String attValue3 = "value3";
        // XSS attack as exploited in IE (as seen on
        // https://github.com/OWASP/java-html-sanitizer/blob/master/docs/html-validation.md#valid-according-to-policy)
        final String attName4 = "test4";
        final String saneValue4 = "v4";
        final String attValue4 = saneValue4 + "<!--if[true]> <script>alert(1337)</script> -->";
        // XSS attack as exploited in foreign content context (as seen on
        // https://github.com/OWASP/java-html-sanitizer/blob/master/docs/html-validation.md#valid-according-to-policy)
        final String attName5 = "test5";
        final String saneValue5 = "v5";
        final String attValue5 = saneValue5 + "<![CDATA[ <!-- ]]><script>alert(1337)</script><!-- -->";
        // Don't break my heart (as seen on
        // https://github.com/OWASP/java-html-sanitizer/blob/master/docs/html-validation.md#dont-break-my-heart)
        final String attName6 = "test6";
        final String saneValue6 = "I <3 Poniez!";
        // but escaped chars are unescaped anyway...
        final String attName7 = "test7";
        final String saneValue7 = "You <3 Poniez 2!";
        final String attValue7 = "You &lt;3 Poniez 2!";
        // XSS attack escaped in value
        final String attName8 = "test8";
        final String saneValue8 = "value8";
        final String attValue8 = saneValue8 + "&lt;script>alert('test')</script>";

        final String body = String.format("{%n" +
                "  \"key1\": \"value1\",%n" +
                "  \"keyOfEmpty\": \"\",%n" +
                "  \"keyOfNull\": null,%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"email\": \"walter.bates@bonitasoft.com\"%n" +
                "}",
                attName1, JSonUtil.escape(attValue1), attName2, JSonUtil.escape(attValue2),
                attName3, JSonUtil.escape(attValue3), attName4, JSonUtil.escape(attValue4),
                attName5, JSonUtil.escape(attValue5), attName6, JSonUtil.escape(saneValue6),
                attName7, JSonUtil.escape(attValue7), attName8, JSonUtil.escape(attValue8));
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        try (ServletInputStream inputStream = r.getInputStream()) {
            var stringBody = IOUtils.toString(inputStream, r.getCharacterEncoding());
            ObjectMapper mapper = new ObjectMapper();
            var json = mapper.readTree(stringBody);
            // check normal values
            assertThat(json.get("key1").asText()).isEqualTo("value1");
            assertThat(json.get("keyOfEmpty").asText()).isEqualTo("");
            assertThat(json.get("keyOfNull").isNull()).isTrue();
            assertThat(json.get("email").asText()).isEqualTo("walter.bates@bonitasoft.com");
            // check sanitized values
            assertThat(json.get(attName1).asText()).isEqualTo(saneValue1);
            assertThat(json.get(attName2).asText()).isEqualTo(saneValue2);
            assertThat(json.get(attName3)).isNull();
            assertThat(json.get(saneAttName3).asText()).isEqualTo(attValue3);
            assertThat(json.get(attName4).asText()).isEqualTo(saneValue4);
            assertThat(json.get(attName5).asText()).isEqualTo(saneValue5);
            assertThat(json.get(attName6).asText()).isEqualTo(saneValue6);
            assertThat(json.get(attName7).asText()).isEqualTo(saneValue7);
            assertThat(json.get(attName8).asText()).isEqualTo(saneValue8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void shouldSanitizeAttackWhenRequestIsMultiReadHttpServletRequest() throws Exception {
        // Simulate the production scenario where TokenValidatorFilter or RestAPIAuthorizationFilter
        // has already wrapped the request in a MultiReadHttpServletRequest
        MultiReadHttpServletRequest multiReadRequest = mock(MultiReadHttpServletRequest.class);
        when(multiReadRequest.getMethod()).thenReturn("PUT");
        when(multiReadRequest.getCharacterEncoding()).thenReturn("UTF-8");
        when(multiReadRequest.getContentType()).thenReturn("application/json");
        when(multiReadRequest.getRequestURL()).thenReturn(new StringBuffer());
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());

        final String body = "{\"lastname\":\"<marquee onclick=\\\"alert('test')\\\">XSS</marquee>\"}";
        var is = new ByteArrayInputStream(body.getBytes());
        when(multiReadRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(multiReadRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = IOUtils.toString(r.getInputStream(), r.getCharacterEncoding());
        ObjectMapper mapper = new ObjectMapper();
        var json = mapper.readTree(updatedBody);
        // The marquee tag and onclick should be stripped by the sanitizer
        assertThat(json.get("lastname").asText()).doesNotContain("<marquee");
        assertThat(json.get("lastname").asText()).doesNotContain("onclick");
        assertThat(json.get("lastname").asText()).isEqualTo("XSS");
    }

    @Test
    public void shouldSanitizeAttackWhenReadingBodyViaGetReader() throws Exception {
        // Verify that getReader() also returns the sanitized body, not the original
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());

        final String body = "{\"name\":\"<script>alert('xss')</script>safe\"}";
        var is = new ByteArrayInputStream(body.getBytes());
        when(httpRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        // Read via getReader() instead of getInputStream()
        var readerBody = IOUtils.toString(r.getReader());
        ObjectMapper mapper = new ObjectMapper();
        var json = mapper.readTree(readerBody);
        assertThat(json.get("name").asText()).doesNotContain("<script>");
        assertThat(json.get("name").asText()).isEqualTo("safe");
    }

    @Test
    public void shouldNotWrapMultiReadHttpServletRequestWhenBodyIsUnchanged() throws Exception {
        // When the body is clean and the request is already a MultiReadHttpServletRequest,
        // the original request should be passed through without wrapping
        MultiReadHttpServletRequest multiReadRequest = mock(MultiReadHttpServletRequest.class);
        when(multiReadRequest.getMethod()).thenReturn("PUT");
        when(multiReadRequest.getCharacterEncoding()).thenReturn("UTF-8");
        when(multiReadRequest.getContentType()).thenReturn("application/json");
        when(multiReadRequest.getRequestURL()).thenReturn(new StringBuffer());
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(true);
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(Collections.emptyList());

        final String body = "{\"lastname\":\"safe value\"}";
        var is = new ByteArrayInputStream(body.getBytes());
        when(multiReadRequest.getInputStream()).thenReturn(getServletInputStream(is));

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(multiReadRequest, httpResponse, chain);

        // Verify the original MultiReadHttpServletRequest is passed through, not a wrapper
        verify(chain, times(1)).doFilter(multiReadRequest, httpResponse);
    }

    private ServletInputStream getServletInputStream(ByteArrayInputStream is) {
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return is.read();
            }

            @Override
            public boolean isFinished() {
                return is.available() == 0;
            }

            @Override
            public boolean isReady() {
                return !isFinished();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException("Unimplemented method 'setReadListener'");
            }
        };
    }
}
