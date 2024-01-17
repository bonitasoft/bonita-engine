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
package org.bonitasoft.console.common.server.login.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerFactory;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.login.credentials.UserLogger;
import org.bonitasoft.console.common.server.login.utils.LoginUrl;
import org.bonitasoft.console.common.server.login.utils.RedirectUrlBuilder;
import org.bonitasoft.console.common.server.login.utils.RedirectUrlHandler;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to logout from the applications
 *
 * @author Zhiheng Yang, Chong Zhao
 */
public class LogoutServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 739607235407639011L;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        logout(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        logout(request, response);
    }

    /**
     * Console logout
     */
    protected void logout(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {
        final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(request);
        final HttpSession session = requestAccessor.getHttpSession();
        final APISession apiSession = requestAccessor.getApiSession();
        try {
            engineLogout(apiSession);
            SessionUtil.sessionLogout(session);

            if (RedirectUrlHandler.shouldRedirectAfterLogout(request)) {
                final String loginPage = getURLToRedirectTo(requestAccessor);
                response.sendRedirect(loginPage);
            }
        } catch (final Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while performing the logout", e);
            }
            throw new ServletException(e);
        }
    }

    // protected for test stubbing
    protected AuthenticationManager getAuthenticationManager() throws ServletException {
        try {
            return AuthenticationManagerFactory.getAuthenticationManager();
        } catch (final AuthenticationManagerNotFoundException e) {
            throw new ServletException(e);
        }
    }

    protected String getURLToRedirectTo(final HttpServletRequestAccessor requestAccessor) throws ServletException {
        final AuthenticationManager authenticationManager = getAuthenticationManager();
        final HttpServletRequest request = requestAccessor.asHttpServletRequest();

        final String redirectURL = createRedirectUrl(requestAccessor);

        final String logoutPage = authenticationManager.getLogoutPageURL(requestAccessor, redirectURL);
        String redirectionPage;
        if (logoutPage != null) {
            redirectionPage = logoutPage;
        } else {
            final String loginPageURLFromRequest = request.getParameter(AuthenticationManager.LOGIN_URL_PARAM_NAME);
            if (loginPageURLFromRequest != null) {
                redirectionPage = sanitizeLoginPageUrl(loginPageURLFromRequest);
            } else {
                LoginUrl loginPageURL = new LoginUrl(authenticationManager, redirectURL, requestAccessor);
                redirectionPage = loginPageURL.getLocation();
            }
        }
        return redirectionPage;
    }

    protected String createRedirectUrl(final HttpServletRequestAccessor requestAccessor)
            throws ServletException {
        return RedirectUrlHandler.retrieveRedirectUrl(requestAccessor);
    }

    protected String sanitizeLoginPageUrl(final String loginURL) {
        return new RedirectUrlBuilder(new URLProtector().protectRedirectUrl(loginURL)).build().getUrl();
    }

    protected void engineLogout(final APISession apiSession) throws LoginFailedException {
        if (apiSession != null) {
            getUserLogger().doLogout(apiSession);
        }
    }

    protected UserLogger getUserLogger() {
        return new UserLogger();
    }
}
