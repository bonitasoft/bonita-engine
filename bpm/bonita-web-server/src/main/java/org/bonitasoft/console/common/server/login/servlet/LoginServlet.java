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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.filter.PathSanitizer;
import org.bonitasoft.console.common.server.login.AccountLockedException;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.login.LoginManager;
import org.bonitasoft.console.common.server.login.utils.RedirectUrlBuilder;
import org.bonitasoft.console.common.server.login.utils.RedirectUrlHandler;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.console.common.server.utils.TenantsManagementUtils;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.server.login.LoginFailureTracker;
import org.bonitasoft.web.server.login.LoginFailureTrackerAccessor;
import org.restlet.data.MediaType;
import org.restlet.engine.header.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * @author Anthony Birembaut, Ruiheng Fan, Chong Zhao, Haojie Yuan
 */
public class LoginServlet extends HttpServlet {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5326931127638029215L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginServlet.class.getName());

    /**
     * login fail message
     */
    protected static final String LOGIN_FAIL_MESSAGE = "loginFailMessage";

    /**
     * account locked message — must match the string literal in login.jsp (line 58)
     */
    protected static final String ACCOUNT_LOCKED_MESSAGE = "accountLockedMessage";

    /**
     * the URL param for the login page
     */
    protected static final String LOGIN_URL_PARAM_NAME = "loginUrl";

    /*
     * System property to allow login with GET from the development suite
     */
    public static final String ENABLE_DEV_SUITE_LOGIN = "org.bonitasoft.web.login.get.enabled";

    private LoginFailureTracker loginFailureTracker;

    @Override
    public void init() throws ServletException {
        super.init();
        loginFailureTracker = LoginFailureTrackerAccessor.getLoginFailureTracker(getServletContext());
    }

    /**
     * Necessary studio integration (username and password are passed in the URL in development mode)
     *
     * @deprecated
     *             use {@link #doPost(HttpServletRequest, HttpServletResponse)} instead
     */
    @Override
    @Deprecated(since = "8.0", forRemoval = false)
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (!Boolean.parseBoolean(System.getProperty(ENABLE_DEV_SUITE_LOGIN))) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("query string : " + dropPassword(req.getQueryString()));
        }
        doPost(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {

        // force post request body to UTF-8
        try {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            // should never appear
            throw new ServletException(e);
        }
        if (request.getContentType() != null
                && !MediaType.APPLICATION_WWW_FORM.equals(ContentType.readMediaType(request.getContentType()))) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "The only content type supported by this service is application/x-www-form-urlencoded. The content-type request header needs to be set accordingly.");
            }
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        } else {
            handleLogin(request, response);
        }
    }

    protected void handleLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {
        final boolean redirectAfterLogin = RedirectUrlHandler.shouldRedirectAfterLogin(request);
        final String redirectURL = getRedirectUrl(request, redirectAfterLogin);
        String locale = LocaleUtils.getUserLocaleAsString(request);
        try {
            doLogin(request, response);
            final APISession apiSession = (APISession) request.getSession()
                    .getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
            // if there a redirect=true or a redirectURL parameter in the request do nothing (API login), otherwise, redirect (Portal login)
            if (redirectAfterLogin) {
                if (apiSession.isTechnicalUser() || hasProfile(apiSession)) {
                    response.sendRedirect(createRedirectUrl(redirectURL, locale));
                } else {
                    request.setAttribute(LOGIN_FAIL_MESSAGE, "noProfileForUser");
                    getServletContext().getRequestDispatcher(AuthenticationManager.LOGIN_PAGE).forward(request,
                            response);
                }
            } else {
                LocaleUtils.addOrReplaceLocaleCookieResponse(response, locale);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (final AuthenticationManagerNotFoundException e) {
            final String message = "Can't get login manager";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, e);
            }
            throw new ServletException(e);
        } catch (final LoginFailedException e) {
            handleException(request, response, redirectAfterLogin, e, locale);
        } catch (final AuthenticationFailedException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Authentication failed : " + e.getMessage(), e);
            }
            handleException(request, response, redirectAfterLogin, e, locale);
        } catch (final Exception e) {
            LOGGER.error("Error while trying to log in", e);
            throw new ServletException(e);
        }
    }

    protected boolean hasProfile(final APISession apiSession)
            throws NotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantsManagementUtils.hasProfileForUser(apiSession);
    }

    private void handleException(final HttpServletRequest request, final HttpServletResponse response,
            final boolean redirectAfterLogin,
            final Exception e, final String locale) throws ServletException {
        if (redirectAfterLogin) {
            try {
                if (e instanceof AccountLockedException) {
                    request.setAttribute(LOGIN_FAIL_MESSAGE, ACCOUNT_LOCKED_MESSAGE);
                } else {
                    request.setAttribute(LOGIN_FAIL_MESSAGE, LOGIN_FAIL_MESSAGE);
                }
                String loginURL = request.getParameter(LOGIN_URL_PARAM_NAME);
                if (loginURL == null) {
                    loginURL = AuthenticationManager.LOGIN_PAGE;
                    getServletContext().getRequestDispatcher(loginURL).forward(request, response);
                } else {
                    // Defense-in-depth: sanitize user-supplied loginURL before passing
                    // to getRequestDispatcher(). Strip path parameters (semicolons) to
                    // prevent ..;-based traversal, then normalize to collapse any ../
                    String sanitizedLoginURL;
                    try {
                        sanitizedLoginURL = PathSanitizer.stripPathParameters(loginURL);
                        String normalizedPath = new URI(sanitizedLoginURL).normalize().getPath();
                        sanitizedLoginURL = normalizedPath != null ? normalizedPath
                                : AuthenticationManager.LOGIN_PAGE;
                    } catch (URISyntaxException uriEx) {
                        LOGGER.warn("Invalid loginURL [{}], falling back to default login page: {}",
                                loginURL, uriEx.getMessage());
                        sanitizedLoginURL = AuthenticationManager.LOGIN_PAGE;
                    }
                    getServletContext()
                            .getRequestDispatcher(createRedirectUrl(sanitizedLoginURL, locale))
                            .forward(request, response);
                }
            } catch (final Exception e1) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(e1.getMessage());
                }
                throw new ServletException(e1);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage());
            }
            if (e instanceof AccountLockedException) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                if (loginFailureTracker != null) {
                    response.setHeader(HttpHeaders.RETRY_AFTER,
                            String.valueOf(loginFailureTracker.getLockoutDurationSeconds()));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }

    private String getRedirectUrl(final HttpServletRequest request, final boolean redirectAfterLogin) {
        String redirectURL = request.getParameter(AuthenticationManager.REDIRECT_URL);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("redirecting to : " + redirectURL);
        }
        if (redirectAfterLogin && (redirectURL == null || redirectURL.isEmpty())) {
            redirectURL = AuthenticationManager.DEFAULT_DIRECT_URL;
        } else {
            if (redirectURL != null) {
                redirectURL = new URLProtector().protectRedirectUrl(redirectURL);
            }
        }
        return redirectURL;
    }

    private String createRedirectUrl(final String redirectURL, final String locale) {
        RedirectUrlBuilder redirectUrlBuilder = new RedirectUrlBuilder(redirectURL);
        redirectUrlBuilder.appendParameter(LocaleUtils.PORTAL_LOCALE_PARAM, locale);
        return redirectUrlBuilder.build().getUrl();
    }

    protected void doLogin(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationManagerNotFoundException, LoginFailedException, ServletException,
            AuthenticationFailedException {
        final LoginManager loginManager = getLoginManager();
        loginManager.login(request, response);
    }

    protected LoginManager getLoginManager() {
        return new LoginManager(loginFailureTracker);
    }

    static String dropPassword(final String content) {
        String tmp = content;
        if (content != null && content.contains("password")) {
            tmp = tmp.replaceAll("[&]?password=([^&|#]*)?", "");
        }
        return tmp;
    }

}
