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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.filter.ExcludingPatternFilter;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.web.rest.server.framework.utils.RestRequestParser;
import org.bonitasoft.web.toolkit.client.common.i18n.model.I18nLocaleDefinition;
import org.bonitasoft.web.toolkit.client.common.session.SessionDefinition;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Zhiheng Yang, Chong Zhao
 * @author Baptiste Mesta
 * @author Anthony Birembaut
 */
public class RestAPIAuthorizationFilter extends ExcludingPatternFilter {

    private static final String PLATFORM_API_URI_REGEXP = ".*(API|APIToolkit)/platform/.*";

    protected static final String PLATFORM_SESSION_PARAM_KEY = "platformSession";
    protected static final String AUTHORIZATION_FILTER_EXCLUDED_PAGES_PATTERN = "^/(bonita/)?((apps/.+/)|(portal/resource/.+/))?(API|APIToolkit)/system/(i18ntranslation|feature)";

    /**
     * Logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(RestAPIAuthorizationFilter.class.getName());

    @Override
    public void proceedWithFiltering(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException {
        try {
            //we need to use a MultiReadHttpServletRequest wrapper in order to be able to get the inputstream twice (in the filter and in the API servlet)
            MultiReadHttpServletRequest httpServletRequest = new MultiReadHttpServletRequest(
                    (HttpServletRequest) request);
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            boolean isAuthorized;
            if (httpServletRequest.getRequestURI().matches(PLATFORM_API_URI_REGEXP)) {
                isAuthorized = platformAPIsCheck(httpServletRequest, httpServletResponse);
            } else {
                isAuthorized = tenantAPIsCheck(httpServletRequest, httpServletResponse);
            }
            if (isAuthorized) {
                chain.doFilter(httpServletRequest, response);
            }
        } catch (final Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
            throw new ServletException(e);
        }
    }

    protected boolean tenantAPIsCheck(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
            throws ServletException, IOException {
        final APISession apiSession = (APISession) httpRequest.getSession()
                .getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        try {
            if (apiSession == null) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.flushBuffer();
                return false;
            } else if (!checkPermissions(httpRequest)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.flushBuffer();
                return false;
            } else {
                return true;
            }
        } catch (InvalidSessionException e) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Invalid Bonita engine session.", e);
            }
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            SessionUtil.sessionLogout(httpRequest.getSession());
            httpResponse.flushBuffer();
            return false;
        }
    }

    protected boolean platformAPIsCheck(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
        final PlatformSession platformSession = (PlatformSession) httpRequest.getSession()
                .getAttribute(PLATFORM_SESSION_PARAM_KEY);
        if (platformSession != null) {
            return true;
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    protected boolean checkPermissions(final HttpServletRequest request) throws ServletException {
        final RestRequestParser restRequestParser = new RestRequestParser(request).invoke();
        return checkPermissions(request, restRequestParser.getApiName(), restRequestParser.getResourceName(),
                restRequestParser.getResourceQualifiers());
    }

    protected boolean checkPermissions(final HttpServletRequest request, final String apiName,
            final String resourceName, final APIID resourceQualifiers)
            throws ServletException {
        final String method = request.getMethod();
        final HttpSession session = request.getSession();
        // userPermissions are of type: "organization_visualization"
        //        @SuppressWarnings("unchecked") final Set<String> userPermissions = (Set<String>) session.getAttribute(SessionUtil.PERMISSIONS_SESSION_PARAM_KEY);
        final APISession apiSession = (APISession) session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY);

        final boolean apiAuthorizationsCheckEnabled = isApiAuthorizationsCheckEnabled();
        if (!apiAuthorizationsCheckEnabled || apiSession.isTechnicalUser()) {
            return true;
        }
        final String resourceQualifiersAsString = resourceQualifiers != null ? resourceQualifiers.toString() : null;
        final String body = getRequestBody(request);
        final APICallContext apiCallContext = new APICallContext(method, apiName, resourceName,
                resourceQualifiersAsString, request.getQueryString(), body);
        if (isAlwaysAuthorizedResource(apiCallContext)) {
            return true;
        }
        try {
            return enginePermissionsCheck(apiCallContext, apiSession);
        } catch (BonitaException e) {
            throw new ServletException(e);
        }
    }

    protected boolean isAlwaysAuthorizedResource(final APICallContext apiCallContext) {
        return apiCallContext.isGET()
                && (isSingleResourceCall(apiCallContext, SessionDefinition.TOKEN)
                        || isSingleResourceCall(apiCallContext, I18nLocaleDefinition.TOKEN));
    }

    private boolean isSingleResourceCall(final APICallContext apiCallContext, final String authorizedResourceName) {
        return authorizedResourceName.equals(apiCallContext.getResourceName())
                && "system".equals(apiCallContext.getApiName());
    }

    protected String getRequestBody(final HttpServletRequest request) throws ServletException {
        try {
            final ServletInputStream inputStream = request.getInputStream();
            return IOUtils.toString(inputStream, request.getCharacterEncoding());
        } catch (final IOException e) {
            throw new ServletException(e);
        }
    }

    protected boolean isApiAuthorizationsCheckEnabled() {
        return PropertiesFactory.getSecurityProperties().isAPIAuthorizationsCheckEnabled();
    }

    protected boolean enginePermissionsCheck(final APICallContext apiCallContext, final APISession apiSession)
            throws ServerAPIException, BonitaHomeNotSetException, UnknownAPITypeException, ExecutionException {
        return TenantAPIAccessor.getPermissionAPI(apiSession).isAuthorized(apiCallContext);
    }

    @Override
    public String getDefaultExcludedPages() {
        return AUTHORIZATION_FILTER_EXCLUDED_PAGES_PATTERN;
    }
}
