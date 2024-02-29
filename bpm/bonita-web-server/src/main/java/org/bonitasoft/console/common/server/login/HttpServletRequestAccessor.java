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
package org.bonitasoft.console.common.server.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;

/**
 * @author Vincent Elcrin
 */
public class HttpServletRequestAccessor {

    protected static final String REDIRECT_URL = AuthenticationManager.REDIRECT_URL;

    public static final String USERNAME_PARAM = "username";

    public static final String PASSWORD_PARAM = "password";

    private final static String OAUTH_VERIFIER = "oauth_verifier";

    private final static String OAUTH_TOKEN = "oauth_token";

    private final HttpServletRequest httpServletRequest;

    public HttpServletRequestAccessor(final HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String getUsername() {
        return httpServletRequest.getParameter(USERNAME_PARAM);
    }

    public String getPassword() {
        return httpServletRequest.getParameter(PASSWORD_PARAM);
    }

    public HttpSession getHttpSession() {
        return httpServletRequest.getSession();
    }

    public String getRedirectUrl() {
        return httpServletRequest.getParameter(REDIRECT_URL);
    }

    public String getOAuthToken() {
        return httpServletRequest.getParameter(OAUTH_TOKEN);
    }

    public String getOAuthVerifier() {
        return httpServletRequest.getParameter(OAUTH_VERIFIER);
    }

    public APISession getApiSession() {
        return (APISession) getHttpSession().getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameterMap() {
        return httpServletRequest.getParameterMap();
    }

    public String getRequestedUrl() {
        return httpServletRequest.getRequestURL().toString();
    }

    public String getRequestedUri() {
        return httpServletRequest.getRequestURI();
    }

    public HttpServletRequest asHttpServletRequest() {
        return httpServletRequest;
    }

    public String getUserAgent() {
        return httpServletRequest.getHeader("User-Agent");
    }

    public String getLocale() {
        return LocaleUtils.getUserLocaleAsString(httpServletRequest);
    }
}
