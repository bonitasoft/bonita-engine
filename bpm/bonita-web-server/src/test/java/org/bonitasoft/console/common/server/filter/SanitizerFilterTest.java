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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.web.toolkit.client.common.json.JSonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Test class SanitizerFilter
 *
 * @author Vincent Hemery
 */
public class SanitizerFilterTest {

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

    @Before
    public void setUp() throws Exception {
        initMocks(this);
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
    public void shouldNotSanitizeWhenDisabled() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
        when(sanitizerFilter.isSanitizerEnabled()).thenReturn(false);

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        verify(sanitizerFilter, never()).sanitize(any(JsonNode.class));
        verify(chain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    public void shouldNotAffectAttributeValue() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
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
    public void shouldNotAffectParameterValues() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/json");
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
    public void shouldNotAffectFileUpload() throws Exception {
        when(httpRequest.getContentType()).thenReturn("text/xml");
        final String body = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n" +
                "<organization:Organization xmlns:organization=\"http://documentation.bonitasoft.com/organization-xml-schema/1.1\">%n"
                +
                "  <users>%n" +
                "  </users>%n" +
                "</organization:Organization>");
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(new ServletInputStream() {

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
        });

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        var updatedBody = new String(r.getInputStream().readAllBytes());
        assertThat(updatedBody).isEqualTo(body);
    }

    @Test
    public void shouldSanitizeAttackFromBody() throws Exception {
        when(httpRequest.getContentType()).thenReturn("application/JSON");
        when(sanitizerFilter.getAttributesExcluded()).thenReturn(List.of("email", "password"));
        final String attName = "test";
        final String saneValue = "Hello <b>World</b>";
        final String attValue = saneValue
                + "<style>@keyframes slidein {}</style><xss style=\"animation-duration:1s;animation-name:slidein;animation-iteration-count:2\" onwebkitanimationiteration=\"alert(1)\"></xss>";
        final String body = String.format("{%n" +
                "  \"key1\": \"value1\",%n" +
                "  \"%s\": \"%s\",%n" +
                "  \"email\": \"walter.bates@bonitasoft.com\"%n" +
                "}", attName, JSonUtil.escape(attValue));
        var is = new ByteArrayInputStream(body.getBytes());

        when(httpRequest.getInputStream()).thenReturn(new ServletInputStream() {

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
        });

        sanitizerFilter.init(filterConfig);
        sanitizerFilter.doFilter(httpRequest, httpResponse, chain);

        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(requestCaptor.capture(), any(ServletResponse.class));

        ServletRequest r = requestCaptor.getValue();
        try (ServletInputStream inputStream = r.getInputStream()) {
            var stringBody = IOUtils.toString(inputStream, r.getCharacterEncoding());
            ObjectMapper mapper = new ObjectMapper();
            var json = mapper.readTree(stringBody);
            assertThat(json.get("key1").asText()).isEqualTo("value1");
            assertThat(json.get("email").asText()).isEqualTo("walter.bates@bonitasoft.com");
            var hackValue = json.get(attName).asText();
            assertThat(hackValue).isEqualTo(saneValue);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
