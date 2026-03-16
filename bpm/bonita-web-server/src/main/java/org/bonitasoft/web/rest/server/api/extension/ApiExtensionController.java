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

import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.console.common.server.page.PageMappingService;
import org.bonitasoft.console.common.server.page.RestApiRenderer;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.extension.rest.RestApiResponse;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ConditionalOnSingleCandidate(ApiExtensionController.class)
@RequestMapping({ "/API/extension", "/portal/custom-page/API/extension" })
public class ApiExtensionController extends AbstractRESTController {

    private final RestApiRenderer restApiRenderer;
    private final PageMappingService pageMappingService;

    public ApiExtensionController() {
        this(new RestApiRenderer(), new PageMappingService());
    }

    protected ApiExtensionController(RestApiRenderer restApiRenderer, PageMappingService pageMappingService) {
        this.restApiRenderer = restApiRenderer;
        this.pageMappingService = pageMappingService;
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleExtensionCall(HttpServletRequest request, HttpServletResponse response) {
        try {
            var resolver = new ResourceExtensionResolver(request, request.getMethod(), pageMappingService);
            var restApiResponse = restApiRenderer.handleRestApiCall(request, resolver);
            if (restApiResponse == null) {
                throw new BonitaException("error: restApiResponse is null");
            }
            return buildResponse(restApiResponse, response);
        } catch (Exception e) {
            log.error("Failed to handle API Extension call", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<String> buildResponse(RestApiResponse restApiResponse, HttpServletResponse servletResponse) {
        // Set cookies on the servlet response (ResponseEntity doesn't support cookies)
        for (Cookie cookie : restApiResponse.getAdditionalCookies()) {
            servletResponse.addCookie(cookie);
        }

        MediaType mediaType = MediaType.parseMediaType(restApiResponse.getMediaType());
        if (mediaType.getCharset() == null) {
            Charset characterSet = Charset.isSupported(restApiResponse.getCharacterSet())
                    ? Charset.forName(restApiResponse.getCharacterSet()) : RestApiResponse.DEFAULT_CHARSET;
            mediaType = new MediaType(mediaType, characterSet);
        }
        var bodyBuilder = ResponseEntity
                .status(restApiResponse.getHttpStatus())
                .contentType(mediaType);

        // Pass through all additional headers directly
        for (Map.Entry<String, String> entry : restApiResponse.getAdditionalHeaders().entrySet()) {
            bodyBuilder.header(entry.getKey(), entry.getValue());
        }

        String body = restApiResponse.getResponse() != null ? restApiResponse.getResponse().toString() : null;
        return bodyBuilder.body(body);
    }
}
