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
package org.bonitasoft.console.common.server.login.filter;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.bonitasoft.console.common.server.filter.ExcludingPatternFilter;
import org.bonitasoft.console.common.server.filter.MultiReadHttpServletRequest;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paul AMAR
 */
public class TokenValidatorFilter extends ExcludingPatternFilter {

    public static final String BEARER_HEADER_VERIFIED_ATTRIBUTE = "bearerVerified";

    private static final String CSRF_TOKEN_PARAM = "CSRFToken";
    private static final String CSRF_TOKEN_HEADER = "X-Bonita-API-Token";

    /**
     * Upper bound on the bytes buffered for the CSRFToken form field (real tokens are UUID-sized).
     * The cap is applied on the buffered field only, NOT on the parser (a request or per-item limit
     * would reject the legitimate large file uploads this filter wraps). An oversized crafted field
     * gets truncated, mismatches the session token and ends on the 401 path with bounded memory.
     * Note: the cap only bounds the token-decode step; the surrounding MultiReadHttpServletRequest
     * wrapper still buffers the whole request body for re-reading (pre-existing behavior).
     */
    private static final int MAX_CSRF_TOKEN_FIELD_BYTES = 4096;

    protected static final String TOKEN_VALIDATOR_FILTER_EXCLUDED_PAGES_PATTERN = "^/(bonita/)?((apps/.+/)|(portal/resource/.+/))?(API|APIToolkit)/system/(i18ntranslation|feature|session)";

    /**
     * Logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(TokenValidatorFilter.class.getName());

    @Override
    public void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (isCsrfProtectionEnabled() && !isSafeMethod(httpServletRequest.getMethod()) &&
                !isBearerVerifiedRequest(httpServletRequest.getSession())) {
            //we need to use a MultiReadHttpServletRequest wrapper in order to be able to get the inputstream twice (in the filter and in the API servlet)
            MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(
                    httpServletRequest);
            String headerFromRequest = getCSRFToken(multiReadHttpServletRequest);
            String apiToken = (String) multiReadHttpServletRequest.getSession().getAttribute("api_token");

            if (headerFromRequest == null || !headerFromRequest.equals(apiToken)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Token Validation failed, expected: " + apiToken + ", received: " + headerFromRequest);
                }
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.flushBuffer();
            } else {
                chain.doFilter(multiReadHttpServletRequest, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    protected boolean isBearerVerifiedRequest(HttpSession httpSession) {
        boolean isBearerVerified = false;
        Object bearerSessionAttribute = httpSession.getAttribute(BEARER_HEADER_VERIFIED_ATTRIBUTE);
        if (bearerSessionAttribute != null) {
            isBearerVerified = Boolean.parseBoolean((String) bearerSessionAttribute);
            httpSession.removeAttribute(BEARER_HEADER_VERIFIED_ATTRIBUTE);
        }
        return isBearerVerified;
    }

    // protected for testing
    protected boolean isCsrfProtectionEnabled() {
        return PropertiesFactory.getSecurityProperties().isCSRFProtectionEnabled();
    }

    /**
     * Get CSRF token from request following order as below
     * - In 'X-Bonita-API-Token' header
     * - In 'CSRFToken' parameter
     * - In 'CSRFToken' multipart body parameter
     * <p>
     * The multipart case deliberately does NOT use Spring's StandardServletMultipartResolver (the
     * documented CommonsMultipartResolver replacement): this filter runs outside any Spring MVC
     * DispatcherServlet, that resolver delegates to the container getParts() - which requires a
     * multipart-config on every servlet behind this filter and parses/buffers the whole request to
     * disk - while here a streaming iterator only reads up to the CSRFToken field, and the
     * MultiReadHttpServletRequest wrapper keeps the body re-readable downstream.
     */
    private String getCSRFToken(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(CSRF_TOKEN_HEADER);
        if (isBlank(token)) {
            token = httpRequest.getParameter(CSRF_TOKEN_PARAM);
        }
        if (isBlank(token) && isFormData(httpRequest.getContentType())) {
            try {
                // no parser-level size limit on purpose: getItemIterator streams without buffering
                // file content, and a request or per-item cap would reject the legitimate large file
                // uploads this filter wraps (see MAX_CSRF_TOKEN_FIELD_BYTES for the buffered-read cap)
                FileItemInputIterator itemIterator = new JakartaServletFileUpload<>().getItemIterator(httpRequest);
                while (itemIterator.hasNext()) {
                    FileItemInput item = itemIterator.next();
                    if (item.isFormField() && CSRF_TOKEN_PARAM.equals(item.getFieldName())) {
                        try (var stream = item.getInputStream()) {
                            token = new String(stream.readNBytes(MAX_CSRF_TOKEN_FIELD_BYTES),
                                    getFormFieldEncoding(httpRequest));
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                // fail-closed: no token extracted means the request gets a 401. Log the cause at
                // info WITHOUT the stack trace (unauthenticated entry point - a full trace per
                // malformed body would make log flooding cheap); full trace at debug only
                LOGGER.info("Unable to read the CSRF token from the multipart request body: {}",
                        e.getMessage());
                LOGGER.debug("Unable to read the CSRF token from the multipart request body", e);
            }
        }
        return token;
    }

    private Charset getFormFieldEncoding(HttpServletRequest httpRequest) {
        String characterEncoding = httpRequest.getCharacterEncoding();
        if (characterEncoding != null) {
            try {
                return Charset.forName(characterEncoding);
            } catch (IllegalArgumentException e) {
                // covers both IllegalCharsetNameException and UnsupportedCharsetException
                LOGGER.info("Unsupported request character encoding {}, falling back to UTF-8", characterEncoding);
            }
        }
        return StandardCharsets.UTF_8;
    }

    private boolean isFormData(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("multipart/form-data");
    }

    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    public String getDefaultExcludedPages() {
        return TOKEN_VALIDATOR_FILTER_EXCLUDED_PAGES_PATTERN;
    }

}
