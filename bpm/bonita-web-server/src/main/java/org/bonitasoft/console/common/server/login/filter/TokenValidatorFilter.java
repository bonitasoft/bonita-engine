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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
     * wrapper still copies the whole request body for re-reading, until the request ends. That copy is
     * only made for a multipart POST request of a session holding a token, when the token is not in a
     * header or parameter.
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
            // wrapper created by this filter pass, if any: its copy of the body is deleted once the request is processed
            MultiReadHttpServletRequest bodyCopyOwner = null;
            try {
                String apiToken = (String) httpServletRequest.getSession().getAttribute("api_token");
                // without a token in the session (no login yet), no token sent with the request can match:
                // the request is refused without reading its body
                boolean tokenCanMatch = !isBlank(apiToken);
                HttpServletRequest checkedRequest = httpServletRequest;
                String tokenFromRequest = tokenCanMatch ? getCSRFTokenFromHeaderOrParameter(httpServletRequest) : null;
                if (tokenCanMatch && isBlank(tokenFromRequest) && isMultipartFormDataPost(httpServletRequest)) {
                    // reading the token consumes the body: we need to use a MultiReadHttpServletRequest wrapper in
                    // order to be able to get the input stream twice (in the filter and in the API servlet).
                    // A request forwarded after a first pass of this filter is already wrapped, possibly under the
                    // wrapper of another filter: reuse its copy.
                    if (!hasBodyCopy(httpServletRequest)) {
                        bodyCopyOwner = new MultiReadHttpServletRequest(httpServletRequest);
                        checkedRequest = bodyCopyOwner;
                    }
                    tokenFromRequest = getCSRFTokenFromMultipartBody(checkedRequest);
                }

                if (tokenFromRequest == null || !tokenFromRequest.equals(apiToken)) {
                    // the tokens are secrets: log why the validation failed, never their values
                    LOGGER.debug("CSRF token validation failed for {} {}: {}", httpServletRequest.getMethod(),
                            httpServletRequest.getRequestURI(), getValidationFailureReason(apiToken, tokenFromRequest));
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.flushBuffer();
                } else {
                    chain.doFilter(checkedRequest, response);
                }
            } finally {
                // the copy of a multipart body is not needed once the request has been processed, whatever the outcome.
                // The request is over when the chain returns: nothing behind this filter processes it asynchronously
                // (no startAsync in bonita-web-server)
                if (bodyCopyOwner != null) {
                    bodyCopyOwner.cleanMultipartTempContent();
                }
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
     * The 'CSRFToken' multipart body parameter is read by {@link #getCSRFTokenFromMultipartBody}, only when both are
     * blank and for a multipart POST request, as it requires a copy of the body.
     */
    private String getCSRFTokenFromHeaderOrParameter(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(CSRF_TOKEN_HEADER);
        if (isBlank(token)) {
            token = httpRequest.getParameter(CSRF_TOKEN_PARAM);
        }
        return token;
    }

    /**
     * Get CSRF token from the 'CSRFToken' multipart body parameter
     * <p>
     * The body is copied first, outside of the parsing: an I/O error while copying it (a full disk, for example)
     * propagates as a server error, while a body that cannot be parsed ends with a 401. A streaming iterator then
     * reads the copy up to the CSRFToken field, and stores none of the parts. Spring's CommonsMultipartResolver, used
     * before, parsed the whole body and wrote every part above 10 KB to the JVM temporary folder, where nothing
     * deleted it before the garbage collector.
     *
     * @param httpRequest a request whose body can be read again downstream (see {@link #hasBodyCopy})
     */
    private String getCSRFTokenFromMultipartBody(HttpServletRequest httpRequest) throws IOException {
        // forces the copy (MultiReadHttpServletRequest copies the body on its first read): its I/O errors propagate,
        // while the parse errors below end with a 401
        httpRequest.getInputStream().close();
        try {
            // no parser-level size limit on purpose: getItemIterator streams without buffering
            // file content, and a request or per-item cap would reject the legitimate large file
            // uploads this filter wraps (see MAX_CSRF_TOKEN_FIELD_BYTES for the buffered-read cap)
            FileItemIterator itemIterator = new ServletFileUpload().getItemIterator(httpRequest);
            while (itemIterator.hasNext()) {
                FileItemStream item = itemIterator.next();
                if (item.isFormField() && CSRF_TOKEN_PARAM.equals(item.getFieldName())) {
                    try (InputStream stream = item.openStream()) {
                        return new String(stream.readNBytes(MAX_CSRF_TOKEN_FIELD_BYTES),
                                getFormFieldEncoding(httpRequest));
                    }
                }
            }
        } catch (Exception e) {
            // fail-closed: no token extracted means the request gets a 401. Log the cause at
            // info WITHOUT the stack trace (any logged-in client can send a malformed body - a full
            // trace per request would make log flooding cheap); full trace at debug only
            LOGGER.info("Unable to read the CSRF token from the multipart request body: {}", e.getMessage());
            LOGGER.debug("Unable to read the CSRF token from the multipart request body", e);
        }
        return null;
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

    private static String getValidationFailureReason(String apiToken, String tokenFromRequest) {
        if (isBlank(apiToken)) {
            return "no token in the HTTP session";
        }
        return tokenFromRequest == null ? "no token in the request" : "the token of the request does not match";
    }

    /**
     * ServletFileUpload, MultiReadHttpServletRequest and FileUploadServlet only handle POST requests as multipart:
     * other methods send the token in the header or as a parameter
     */
    private boolean isMultipartFormDataPost(HttpServletRequest httpRequest) {
        return isFormData(httpRequest.getContentType()) && ServletFileUpload.isMultipartContent(httpRequest);
    }

    /**
     * @return true when the request is, or wraps, a MultiReadHttpServletRequest: its body can be read again
     */
    private boolean hasBodyCopy(HttpServletRequest httpRequest) {
        return httpRequest instanceof MultiReadHttpServletRequest
                || httpRequest instanceof ServletRequestWrapper
                        && ((ServletRequestWrapper) httpRequest).isWrapperFor(MultiReadHttpServletRequest.class);
    }

    private boolean isFormData(String contentType) {
        // media types are case-insensitive: lower-case them whatever the default locale (Turkish dotless i)
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("multipart/form-data");
    }

    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    @Override
    public String getDefaultExcludedPages() {
        return TOKEN_VALIDATOR_FILTER_EXCLUDED_PAGES_PATTERN;
    }

}
