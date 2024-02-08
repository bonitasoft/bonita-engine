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

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.login.PortalCookies;
import org.bonitasoft.console.common.server.login.filter.TokenGenerator;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.session.PlatformSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ruiheng Fan, Haojie Yuan
 */
public class PlatformLoginServlet extends HttpServlet {

    /**
     * engine PlatformSession atribute name in HTTP session
     */
    public static final String PLATFORM_SESSION_PARAM_KEY = "platformSession";
    /**
     * the request param for the username
     */
    protected static final String USERNAME_PARAM = "username";
    /**
     * the request param for the password
     */
    protected static final String PASSWORD_PARAM = "password";
    /**
     * login fail message
     */
    protected static final String LOGIN_FAIL_MESSAGE = "loginFailMessage";

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5326931127638029215L;
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLoginServlet.class.getName());

    /**
     * the URL of the login page
     */
    // FIXME: page does not exist:
    protected static final String LOGIN_PAGE = "/platformLogin.jsp";

    // FIXME: page does not exist:
    protected static final String PLATFORM_PAGE = "platform/BonitaPlatform.html#?_p=Platform";

    public static final String ERROR_MESSAGE = "Error while logging in to the platform";

    protected final TokenGenerator tokenGenerator = new TokenGenerator();
    protected final PortalCookies portalCookies = new PortalCookies();

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        PlatformSession platformSession;
        PlatformLoginAPI platformLoginAPI;
        final String username = request.getParameter(USERNAME_PARAM);
        final String password = request.getParameter(PASSWORD_PARAM);

        String redirectStr = request.getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        boolean redirectAfterLogin = Boolean.parseBoolean(redirectStr);

        try {
            platformLoginAPI = getPlatformLoginAPI();
            platformSession = platformLoginAPI.login(username, password);
            request.getSession().setAttribute(PLATFORM_SESSION_PARAM_KEY, platformSession);
            String csrfToken = tokenGenerator.createOrLoadToken(request.getSession());
            portalCookies.addCSRFTokenCookieToResponse(request, response, csrfToken);

            if (redirectAfterLogin) {
                response.sendRedirect(PLATFORM_PAGE);
            }
        } catch (final InvalidPlatformCredentialsException e) {
            LOGGER.trace("Wrong username or password", e);
            if (redirectAfterLogin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Wrong username or password");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (final Exception e) {
            LOGGER.error(ERROR_MESSAGE, e);
            if (redirectAfterLogin) {
                try {
                    request.setAttribute(LOGIN_FAIL_MESSAGE, LOGIN_FAIL_MESSAGE);
                    getServletContext().getRequestDispatcher(LOGIN_PAGE).forward(request, response);
                } catch (final IOException ioe) {
                    LOGGER.error("Error while redirecting to login.jsp", ioe);
                    throw new ServletException(ERROR_MESSAGE, e);
                }
            } else {
                throw new ServletException(ERROR_MESSAGE, e);
            }
        }

    }

    PlatformLoginAPI getPlatformLoginAPI()
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return PlatformAPIAccessor.getPlatformLoginAPI();
    }

}
