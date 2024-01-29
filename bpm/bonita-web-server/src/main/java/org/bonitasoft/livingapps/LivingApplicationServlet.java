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
package org.bonitasoft.livingapps;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerFactory;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.utils.LoginUrl;
import org.bonitasoft.console.common.server.login.utils.RedirectUrl;
import org.bonitasoft.console.common.server.login.utils.RedirectUrlBuilder;
import org.bonitasoft.console.common.server.page.CustomPageRequestModifier;
import org.bonitasoft.console.common.server.page.PageRenderer;
import org.bonitasoft.console.common.server.page.ResourceRenderer;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.livingapps.exception.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet which displays the application layout with an URL like /apps/<app-token>/*
 * Note: whenever there is an InvalidSessionException from the engine it performs an HTTP session logout and redirects
 * to the login page (this is possible because unlike in LivingApplicationPageServlet we are in the top frame).
 */
public class LivingApplicationServlet extends HttpServlet {

    private static final long serialVersionUID = -3911437607969651000L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LivingApplicationServlet.class.getName());

    protected CustomPageRequestModifier customPageRequestModifier = new CustomPageRequestModifier();

    @Override
    protected void service(final HttpServletRequest hsRequest, final HttpServletResponse hsResponse)
            throws ServletException, IOException {

        final APISession session = getSession(hsRequest);

        // Check if requested URL is missing final slash (necessary in order to be able to use relative URLs for resources)
        if (isPageUrlWithoutFinalSlash(hsRequest)) {
            customPageRequestModifier.redirectToValidPageUrl(hsRequest, hsResponse);
            return;
        }

        try {
            createApplicationRouter(session).route(hsRequest, hsResponse, session, getPageRenderer(),
                    getResourceRenderer(), new BonitaHomeFolderAccessor());
        } catch (final ApplicationPageNotFoundException | PageNotFoundException | CreationException e) {
            hsResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (final BonitaException | IllegalAccessException | InstantiationException e) {
            if (LOGGER.isWarnEnabled()) {
                final String message = "Error while trying to display application " + hsRequest.getPathInfo();
                LOGGER.warn(message, e);
            }
            if (!hsResponse.isCommitted()) {
                hsResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                throw new ServletException(e);
            }
        } catch (final InvalidSessionException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Invalid Bonita engine session.", e);
            }
            SessionUtil.sessionLogout(hsRequest.getSession());
            HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(hsRequest);
            LoginUrl loginURL = new LoginUrl(getAuthenticationManager(), makeRedirectUrl(requestAccessor).getUrl(),
                    requestAccessor);
            hsResponse.sendRedirect(loginURL.getLocation());
        }

    }

    ApplicationRouter createApplicationRouter(final APISession session)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return new ApplicationRouter(new ApplicationModelFactory(
                TenantAPIAccessor.getLivingApplicationAPI(session),
                TenantAPIAccessor.getCustomPageAPI(session),
                TenantAPIAccessor.getProfileAPI(session)));
    }

    private boolean isPageUrlWithoutFinalSlash(final HttpServletRequest request) {
        return request.getPathInfo() == null
                || request.getPathInfo().matches("/[^/]+/[^/]+")
                || request.getPathInfo().matches("/[^/]+");
    }

    APISession getSession(final HttpServletRequest hsRequest) {
        return (APISession) hsRequest.getSession().getAttribute("apiSession");
    }

    PageRenderer getPageRenderer() {
        return new PageRenderer(getResourceRenderer());
    }

    ResourceRenderer getResourceRenderer() {
        return new ResourceRenderer();
    }

    protected RedirectUrl makeRedirectUrl(final HttpServletRequestAccessor httpRequest) {
        final RedirectUrlBuilder builder = new RedirectUrlBuilder(httpRequest.getRequestedUri());
        builder.appendParameters(httpRequest.getParameterMap());
        return builder.build();
    }

    // protected for test stubbing
    protected AuthenticationManager getAuthenticationManager() throws ServletException {
        try {
            return AuthenticationManagerFactory.getAuthenticationManager();
        } catch (final AuthenticationManagerNotFoundException e) {
            throw new ServletException(e);
        }
    }
}
