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
public class RestRequestURIParserTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletMapping httpServletMapping;

    @InjectMocks
    private RestRequestURIParser restRequestURIParser;

    @Before
    public void before() {
        doReturn(httpServletMapping).when(httpServletRequest).getHttpServletMapping();
        doReturn("").when(httpServletRequest).getServletPath();
    }

    @Test
    public void should_parsePath_request_info_with_id() {
        doReturn("/bpm/case/15").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("15");
        assertThat(result.getResourceName()).isEqualTo("case");
        assertThat(result.getApiName()).isEqualTo("bpm");
    }

    @Test
    public void should_parsePath_request_info() {
        doReturn("/bpm/case").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getResourceQualifiers()).isNull();
        assertThat(result.getResourceName()).isEqualTo("case");
        assertThat(result.getApiName()).isEqualTo("bpm");
    }

    @Test
    public void should_parsePath_when_exact_match_servlet_mapped_under_API() {
        // When an exact-match servlet is mapped to /API/documentDownload,
        // servletPath = "/API/documentDownload" and pathInfo = null.
        // "API" is included as apiName to match permission entry "GET|API/documentDownload".
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/API/documentDownload").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("API");
        assertThat(result.getResourceName()).isEqualTo("documentDownload");
        assertThat(result.getResourceQualifiers()).isNull();
    }

    @Test
    public void should_throw_when_pathInfo_is_null_and_servletPath_has_only_API() {
        // When servletPath is just "/API" with no resource at all, parsing should fail.
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/API").when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host/API");
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestURIParser.parse())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API or resource name in request URL");
    }

    @Test
    public void should_parsePath_when_exact_match_servlet_under_portal() {
        // When an exact-match servlet is mapped to /portal/imageUpload,
        // there is no "API" segment. Parse from index 1 so that
        // apiName = "portal" and resourceName = "imageUpload",
        // matching permission entry "POST|portal/imageUpload".
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/portal/imageUpload").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("portal");
        assertThat(result.getResourceName()).isEqualTo("imageUpload");
        assertThat(result.getResourceQualifiers()).isNull();
    }

    @Test
    public void should_parsePath_when_wildcard_servlet_under_services() {
        // The RestAPIAuthorizationFilter is also mapped to /services/*.
        // servletPath = "/services" and pathInfo = "/something".
        doReturn("/something").when(httpServletRequest).getPathInfo();
        doReturn("/services").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("services");
        assertThat(result.getResourceName()).isEqualTo("something");
        assertThat(result.getResourceQualifiers()).isNull();
    }

    @Test
    public void should_parsePath_spring_mvc_request_info() {
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/API/system/maintenance").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getResourceQualifiers()).isNull();
        assertThat(result.getResourceName()).isEqualTo("maintenance");
        assertThat(result.getApiName()).isEqualTo("system");
    }

    @Test
    public void should_parsePath_spring_mvc_request_info_with_id() {
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/API/system/maintenance").when(httpServletRequest).getServletPath();
        doReturn("/1").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("1");
        assertThat(result.getResourceName()).isEqualTo("maintenance");
        assertThat(result.getApiName()).isEqualTo("system");
    }

    @Test
    public void should_parsePath_spring_mvc_custom_page_api_extension() {
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/portal/custom-page/API/extension").when(httpServletRequest).getServletPath();
        doReturn("/my-rest-api").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getResourceQualifiers()).isNull();
        assertThat(result.getApiName()).isEqualTo("extension");
        assertThat(result.getResourceName()).isEqualTo("my-rest-api");
    }

    @Test
    public void should_throw_when_spring_mvc_request_has_no_resource_name() {
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        var path = "/API";
        doReturn(path).when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host" + path);
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestURIParser.parse())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API or resource name in request URL")
                .hasMessageNotContainingAny(requestUrl.toString(), path);
    }

    @Test
    public void should_parsePath_when_no_servlet_mapped_to_API_wildcard() {
        // When no servlet is mapped to /API/*, the default servlet handles the request.
        // In that case pathInfo is null and servletPath contains the full path including /API.
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/API/living/application").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("living");
        assertThat(result.getResourceName()).isEqualTo("application");
        assertThat(result.getResourceQualifiers()).isNull();
    }

    @Test
    public void should_parsePath_when_no_servlet_mapped_to_API_wildcard_with_id() {
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/API/bpm/case/42").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("bpm");
        assertThat(result.getResourceName()).isEqualTo("case");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("42");
    }

    @Test
    public void should_parsePath_when_no_servlet_mapped_to_APIToolkit_path() {
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/APIToolkit/bpm/process").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("bpm");
        assertThat(result.getResourceName()).isEqualTo("process");
        assertThat(result.getResourceQualifiers()).isNull();
    }

    @Test
    public void should_parsePath_when_servlet_mapped_to_specific_API_subpath() {
        // When a servlet is mapped to a specific sub-path like /API/avatars/*,
        // servletPath = "/API/avatars" and pathInfo = "/17" (just the ID).
        // "API" must be included as apiName to match permission entry "GET|API/avatars".
        doReturn("/17").when(httpServletRequest).getPathInfo();
        doReturn("/API/avatars").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("API");
        assertThat(result.getResourceName()).isEqualTo("avatars");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("17");
    }

    @Test
    public void should_parsePath_when_servlet_mapped_to_portal_custom_page_API_subpath() {
        // When a servlet is mapped to /portal/custom-page/API/avatars/*,
        // servletPath includes the full prefix and pathInfo is just the ID.
        doReturn("/17").when(httpServletRequest).getPathInfo();
        doReturn("/portal/custom-page/API/avatars").when(httpServletRequest).getServletPath();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("portal");
        assertThat(result.getResourceName()).isEqualTo("custom-page");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("API");
        assertThat(result.getResourceQualifiers().getPart(1)).isEqualTo("avatars");
        assertThat(result.getResourceQualifiers().getPart(2)).isEqualTo("17");
    }

    @Test
    public void should_parsePath_spring_mvc_custom_page_api_extension_with_qualifier() {
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/portal/custom-page/API/extension").when(httpServletRequest).getServletPath();
        doReturn("/my-rest-api/resource1").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("extension");
        assertThat(result.getResourceName()).isEqualTo("my-rest-api");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("resource1");
    }

    @Test
    public void should_parsePath_when_APIToolkit_servlet_with_pathInfo() {
        // BonitaRestAPIServlet is mapped to /APIToolkit/*.
        // servletPath = "/APIToolkit" and pathInfo = "/bpm/case/15".
        doReturn("/APIToolkit").when(httpServletRequest).getServletPath();
        doReturn("/bpm/case/15").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("bpm");
        assertThat(result.getResourceName()).isEqualTo("case");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("15");
    }

    @Test
    public void should_parsePath_spring_mvc_with_APISpringInternal_rewritten_url() {
        // When UrlRewriteFilter rewrites /API/bpm/userTask/3/execution to
        // /APISpringInternal/bpm/userTask/3/execution and forwards to Spring MVC.
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/APISpringInternal").when(httpServletRequest).getServletPath();
        doReturn("/bpm/userTask/3/execution").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("bpm");
        assertThat(result.getResourceName()).isEqualTo("userTask");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("3");
        assertThat(result.getResourceQualifiers().getPart(1)).isEqualTo("execution");
    }

    @Test
    public void should_throw_when_url_path_too_short_and_no_API_segment() {
        // A path with fewer than 3 segments and no API marker should fail
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn("/x").when(httpServletRequest).getServletPath();
        var requestUrl = new StringBuffer("http://my-host/x");
        doReturn(requestUrl).when(httpServletRequest).getRequestURL();

        assertThatThrownBy(() -> restRequestURIParser.parse())
                .isInstanceOf(APIMalformedUrlException.class)
                .hasMessage("Missing API path segment in request URL");
    }

    @Test
    public void should_parsePath_spring_mvc_with_direct_wildcard_mapping() {
        // When Spring MVC has a direct wildcard mapping like /API/bpm/activityVariable/*,
        // servletPath = "/API/bpm/activityVariable" and pathInfo = "/3/myVar".
        doReturn(RestRequestURIParser.SPRING_REST_SERVLET_NAME).when(httpServletMapping).getServletName();
        doReturn("/API/bpm/activityVariable").when(httpServletRequest).getServletPath();
        doReturn("/3/myVar").when(httpServletRequest).getPathInfo();

        ParsedRestRequestURI result = restRequestURIParser.parse();

        assertThat(result.getApiName()).isEqualTo("bpm");
        assertThat(result.getResourceName()).isEqualTo("activityVariable");
        assertThat(result.getResourceQualifiers().getPart(0)).isEqualTo("3");
        assertThat(result.getResourceQualifiers().getPart(1)).isEqualTo("myVar");
    }

}
