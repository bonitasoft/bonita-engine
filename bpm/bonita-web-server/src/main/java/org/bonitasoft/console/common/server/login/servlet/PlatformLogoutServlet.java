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
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.PlatformSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to logout from the applications
 *
 * @author Ruiheng Fan
 */
public class PlatformLogoutServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 739607235407639011L;

    /**
     * the URL of the login page
     */
    // FIXME: page does not exist:
    protected final String PLATFORM_LOGIN_PAGE = "platformLogin.jsp";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformLogoutServlet.class.getName());

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
            throws ServletException, IOException {
        final HttpSession session = request.getSession();

        final PlatformSession platformSession = (PlatformSession) session
                .getAttribute(PlatformLoginServlet.PLATFORMSESSION);
        if (platformSession != null) {
            try {
                final PlatformLoginAPI platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
                platformLoginAPI.logout(platformSession);
            } catch (final BonitaException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while performing the logout", e);
                }
            }
        }
        session.removeAttribute(PlatformLoginServlet.PLATFORMSESSION);
        session.invalidate();

        String redirectStr = request.getParameter(AuthenticationManager.REDIRECT_AFTER_LOGIN_PARAM_NAME);
        if (Boolean.parseBoolean(redirectStr)) {
            response.sendRedirect(PLATFORM_LOGIN_PAGE);
        }
    }
}
