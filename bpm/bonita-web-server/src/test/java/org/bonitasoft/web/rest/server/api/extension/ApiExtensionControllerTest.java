/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.extension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.servlet.http.Cookie;

import org.bonitasoft.console.common.server.page.PageMappingService;
import org.bonitasoft.console.common.server.page.RestApiRenderer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class ApiExtensionControllerTest extends AbstractControllerTest<ApiExtensionController> {

    private static final String API_URL = "/API/extension/myResource";

    @Mock
    private RestApiRenderer restApiRenderer;

    @Mock
    private PageMappingService pageMappingService;

    @Override
    protected ApiExtensionController createController() {
        return spy(new ApiExtensionController(restApiRenderer, pageMappingService));
    }

    @Override
    protected void configureMocks(ApiExtensionController controller) {
        // No additional mock configuration needed
    }

    @Test
    void should_return_response_body() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("{'key':'value'}").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{'key':'value'}"));
    }

    @Test
    void should_return_empty_response() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void should_return_null_body() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_500_when_response_is_null() throws Exception {
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(null);

        mockMvc.perform(get(API_URL)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_return_500_on_bonita_exception() throws Exception {
        when(restApiRenderer.handleRestApiCall(any(), any())).thenThrow(new BonitaException("error message"));

        mockMvc.perform(get(API_URL)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_pass_through_content_range_header() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withAdditionalHeader("Content-Range", "1-10/100")
                .withResponse("[]")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Range", "1-10/100"));
    }

    @Test
    void should_pass_through_location_header() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withAdditionalHeader("Location", "https://documentation.bonitasoft.com/bonita/7.7/")
                .withResponse("")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "https://documentation.bonitasoft.com/bonita/7.7/"));
    }

    @Test
    void should_pass_through_content_disposition_header() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withAdditionalHeader("Content-Disposition", "attachment; filename=\"Mon Fichier.png\"")
                .withResponse("")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Mon Fichier.png\""));
    }

    @Test
    void should_return_custom_status_code() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withResponseStatus(201)
                .withResponse("{}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(post(API_URL)
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void should_handle_post_request() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("{'created':true}").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(post(API_URL)
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{'created':true}"));
    }

    @Test
    void should_handle_put_request() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("{'updated':true}").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(put(API_URL)
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{'updated':true}"));
    }

    @Test
    void should_handle_delete_request() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(delete(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void should_handle_patch_request() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("{'patched':true}").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(patch(API_URL)
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{'patched':true}"));
    }

    @Test
    void should_return_404_status_from_extension() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withResponseStatus(404)
                .withResponse("{\"error\":\"not found\"}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"not found\"}"));
    }

    @Test
    void should_set_cookies_from_response() throws Exception {
        var cookie = new Cookie("myCookie", "myValue");
        cookie.setPath("/");
        var restApiResponse = new RestApiResponseBuilder()
                .withAdditionalCookie(cookie)
                .withResponse("")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().value("myCookie", "myValue"));
    }

    @Test
    void handleExtensionCall_should_use_default_utf8_charset_with_json_media_type() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withResponse("{}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    void handleExtensionCall_should_use_custom_charset_when_specified() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withCharacterSet("ISO-8859-1")
                .withResponse("{}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=ISO-8859-1"));
    }

    @Test
    void handleExtensionCall_should_use_custom_media_type_with_default_charset() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withMediaType("text/plain")
                .withResponse("hello")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void handleExtensionCall_should_not_append_charset_when_already_present_in_media_type() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withMediaType("application/json;charset=ISO-8859-1")
                .withResponse("{}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=ISO-8859-1"));
    }

    @Test
    void handleExtensionCall_should_fallback_to_utf8_when_charset_is_unsupported() throws Exception {
        var restApiResponse = new RestApiResponseBuilder()
                .withCharacterSet("UNSUPPORTED-CHARSET")
                .withResponse("{}")
                .build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get(API_URL)
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

    @Test
    void handleExtensionCall_should_handle_portal_custom_page_extension_url() throws Exception {
        var restApiResponse = new RestApiResponseBuilder().withResponse("{'key':'value'}").build();
        when(restApiRenderer.handleRestApiCall(any(), any())).thenReturn(restApiResponse);

        mockMvc.perform(get("/portal/custom-page/API/extension/myResource")
                .sessionAttrs(sessionAttributes))
                .andExpect(status().isOk())
                .andExpect(content().string("{'key':'value'}"));
    }
}
