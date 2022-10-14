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
package org.bonitasoft.console.common.server.auth;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.ServletException;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.credentials.Credentials;

/**
 * Interface to implement in order to delegate the authentication to an external provider
 *
 * @author Ruiheng Fan, Anthony Birembaut
 */
public interface AuthenticationManager {

    /**
     * Redirection URL parameter name
     */
    String REDIRECT_URL = "redirectUrl";

    /**
     * the URL param for the login page after logout
     */
    String LOGIN_URL_PARAM_NAME = "loginUrl";

    /**
     * the URL param to indicate if we should redirect after login/logout (true by default)
     */
    String REDIRECT_AFTER_LOGIN_PARAM_NAME = "redirect";

    /**
     * the URL of the default login page
     */
    String LOGIN_PAGE = "/login.jsp";

    /**
     * The default redirect URL.
     */
    String DEFAULT_DIRECT_URL = "apps/appDirectoryBonita";

    /**
     * indicate wheather the HTTP session should be invalidated and re created upon login
     */
    String INVALIDATE_SESSION = "authentication.session.invalidate";

    /**
     * Get Login Page URL
     *
     * @param redirectURL
     *        redirect url
     * @return new redirect url
     */
    String getLoginPageURL(final HttpServletRequestAccessor requestAccessor, final String redirectURL)
            throws ServletException;

    /**
     * Authenticate the user (If no exception is thrown, an engine login will then be performed with the credentials)
     *
     * @param credentials
     *        credentials extracted from the request or from the auto-login config
     * @return a map of credentials which if not empty (and containing more than just the key
     *         "authentication.session.invalidate") will be used to login on the engine. Otherwise, the username and
     *         password contained in the
     *         credentials will be used
     * @throws AuthenticationFailedException
     * @throws ServletException
     */
    Map<String, Serializable> authenticate(final HttpServletRequestAccessor requestAccessor,
            final Credentials credentials)
            throws AuthenticationFailedException, ServletException;

    /**
     * Get Logout Page URL
     * If the LoginManager implementation of this method is to return null the default login page will be displayed
     *
     * @param redirectURL
     *        redirect url
     * @return new redirect url
     */
    String getLogoutPageURL(final HttpServletRequestAccessor requestAccessor, final String redirectURL)
            throws ServletException;
}
