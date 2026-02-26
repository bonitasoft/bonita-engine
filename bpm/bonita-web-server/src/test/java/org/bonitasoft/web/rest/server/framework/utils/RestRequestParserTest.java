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
package org.bonitasoft.web.rest.server.framework.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestRequestParserTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletMapping httpServletMapping;

    @InjectMocks
    private RestRequestParser restRequestParser;

    @Before
    public void before() {
        doReturn(httpServletMapping).when(httpServletRequest).getHttpServletMapping();
    }

    @Test
    public void should_parsePath_request_info_with_id() {
        doReturn("/bpm/case/15").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers().getPart(0)).isEqualTo("15");
        assertThat(restRequestParser.getResourceName()).isEqualTo("case");
        assertThat(restRequestParser.getApiName()).isEqualTo("bpm");
    }

    @Test
    public void should_parsePath_request_info() {
        doReturn("/bpm/case").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers()).isNull();
        assertThat(restRequestParser.getResourceName()).isEqualTo("case");
        assertThat(restRequestParser.getApiName()).isEqualTo("bpm");
    }

    @Test
    public void should_parsePath_with_bad_request() {
        doReturn("/bpm").when(httpServletRequest).getPathInfo();
        doReturn("/API").when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host/API/bpm");
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestParser.invoke())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API or resource name in request URL")
                .hasMessageNotContainingAny(requestUrl.toString(), "/API", "/bpm");
    }

    @Test
    public void should_parsePath_spring_mvc_request_info() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/API/system/maintenance").when(httpServletRequest).getServletPath();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers()).isNull();
        assertThat(restRequestParser.getResourceName()).isEqualTo("maintenance");
        assertThat(restRequestParser.getApiName()).isEqualTo("system");
    }

    @Test
    public void should_parsePath_spring_mvc_request_info_with_id() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/API/system/maintenance").when(httpServletRequest).getServletPath();
        doReturn("/1").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers().getPart(0)).isEqualTo("1");
        assertThat(restRequestParser.getResourceName()).isEqualTo("maintenance");
        assertThat(restRequestParser.getApiName()).isEqualTo("system");
    }

    @Test
    public void should_parsePath_spring_mvc_custom_page_api_extension() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/portal/custom-page/API/extension").when(httpServletRequest).getServletPath();
        doReturn("/my-rest-api").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getResourceQualifiers()).isNull();
        assertThat(restRequestParser.getApiName()).isEqualTo("extension");
        assertThat(restRequestParser.getResourceName()).isEqualTo("my-rest-api");
    }

    @Test
    public void should_throw_when_spring_mvc_request_has_no_API_segment() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        var path = "/not/a/valid/path";
        doReturn(path).when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host" + path);
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestParser.invoke())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API segment in request URL")
                .hasMessageNotContainingAny(requestUrl.toString(), path);
    }

    @Test
    public void should_throw_when_spring_mvc_request_has_no_resource_name() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        var path = "/API";
        doReturn(path).when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host" + path);
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestParser.invoke())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API or resource name in request URL")
                .hasMessageNotContainingAny(requestUrl.toString(), path);
    }

    @Test
    public void should_parsePath_spring_mvc_custom_page_api_extension_with_qualifier() {
        doReturn(RestRequestParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/portal/custom-page/API/extension").when(httpServletRequest).getServletPath();
        doReturn("/my-rest-api/resource1").when(httpServletRequest).getPathInfo();

        restRequestParser.invoke();

        assertThat(restRequestParser.getApiName()).isEqualTo("extension");
        assertThat(restRequestParser.getResourceName()).isEqualTo("my-rest-api");
        assertThat(restRequestParser.getResourceQualifiers().getPart(0)).isEqualTo("resource1");
    }

}
