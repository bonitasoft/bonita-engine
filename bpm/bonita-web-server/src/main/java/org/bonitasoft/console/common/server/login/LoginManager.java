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

import java.io.Serializable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.auth.AuthenticationManager;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerFactory;
import org.bonitasoft.console.common.server.auth.AuthenticationManagerNotFoundException;
import org.bonitasoft.console.common.server.login.credentials.Credentials;
import org.bonitasoft.console.common.server.login.credentials.StandardCredentials;
import org.bonitasoft.console.common.server.login.credentials.UserLogger;
import org.bonitasoft.console.common.server.login.filter.TokenGenerator;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.user.User;
import org.bonitasoft.web.server.login.AuthenticationAuditLoggerAccessor;
import org.bonitasoft.web.server.login.LoginFailureTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the authentication, the login and initializes the HTTP session
 *
 * @author Anthony Birembaut
 */
public class LoginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginManager.class.getName());

    protected TokenGenerator tokenGenerator = new TokenGenerator();
    protected PortalCookies portalCookies = new PortalCookies();
    // Resolved from the Spring web context of the request, see getAuthenticationAuditLogger. No locking needed even
    // when the LoginManager is shared (AuthenticationRule): the lookup always returns the same immutable logger
    protected AuthenticationAuditLogger authenticationAuditLogger;
    protected LoginFailureTracker loginFailureTracker;

    /**
     * @deprecated Use {@link #LoginManager(LoginFailureTracker)} instead.
     *             This constructor disables brute-force login protection.
     */
    @Deprecated
    public LoginManager() {
        this(null);
    }

    public LoginManager(LoginFailureTracker loginFailureTracker) {
        this.loginFailureTracker = loginFailureTracker;
    }

    /**
     * Performs the login using the appropriate AuthenticationManager implementation for authentication, logging in on
     * the engine, initializing the HTTP session, and creating the cookies (tanat and CSRF)
     */
    public void login(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationManagerNotFoundException, LoginFailedException, TenantStatusException,
            AuthenticationFailedException, ServletException {
        final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(request);
        final StandardCredentials userCredentials = createUserCredentials(requestAccessor);
        loginInternal(requestAccessor, response, getUserLogger(), userCredentials);
    }

    public void loginInternal(HttpServletRequestAccessor request, HttpServletResponse response, UserLogger userLoger,
            Credentials credentials)
            throws AuthenticationFailedException, ServletException, LoginFailedException {
        loginInternal(request, response, userLoger, credentials, true);
    }

    /**
     * Same as {@link #loginInternal(HttpServletRequestAccessor, HttpServletResponse, UserLogger, Credentials)}, with
     * the possibility to keep the login out of the authentication audit log (e.g. the automatic login of the guest
     * user on public applications).
     *
     * @param audited whether the login is written in the authentication audit log
     */
    public void loginInternal(HttpServletRequestAccessor request, HttpServletResponse response, UserLogger userLoger,
            Credentials credentials, boolean audited)
            throws AuthenticationFailedException, ServletException, LoginFailedException {
        String username = credentials.getName();
        boolean shouldTrackLoginFailures = loginFailureTracker != null && StringUtils.isNotEmpty(username);
        // Note: returning 429 (locked) vs 401 (bad credentials) reveals whether a username exists.
        // This is an intentional usability trade-off — the lockout message helps legitimate users
        // understand why they cannot log in, and username enumeration is low-risk given that
        // the login page itself already exposes whether an account is valid.
        if (shouldTrackLoginFailures && loginFailureTracker.isLockedOut(username)) {
            LOGGER.warn(
                    "Login attempt rejected for user [{}]: account temporarily locked due to too many failed attempts",
                    username);
            if (audited) {
                getAuthenticationAuditLogger(request).loginFailed(request.asHttpServletRequest(), username,
                        AuthenticationAuditLogger.METHOD_STANDARD, AuthenticationAuditLogger.REASON_LOCKED_OUT);
            }
            throw new AccountLockedException("Too many failed login attempts. Please try again later.");
        }
        AuthenticationManager authenticationManager = getAuthenticationManager();
        APISession apiSession;
        boolean invalidateAndRecreateHTTPSession;
        // Declared out of the try block so that the audit events know whether it is an SSO login
        Map<String, Serializable> credentialsMap = null;
        try {
            credentialsMap = authenticationManager.authenticate(request, credentials);
            // In case of a login with the login service we invalidate the session and create a new one.
            // Otherwise, logging in with the credentials in the request (SSO) it is not mandatory it depends on the AuthenticationManager implementation used
            // some SSO mechanisms already handle it (SAML, OIDC).
            Boolean invalidateAndRecreateHTTPSessionIfSet = (Boolean) credentialsMap
                    .remove(AuthenticationManager.INVALIDATE_SESSION);
            invalidateAndRecreateHTTPSession = invalidateAndRecreateHTTPSessionIfSet == null
                    || invalidateAndRecreateHTTPSessionIfSet.booleanValue();
            if (credentialsMap.isEmpty()) {
                if (credentials.getName() == null || credentials.getName().isEmpty()) {
                    LOGGER.debug("There are no credentials in the request");
                    throw new AuthenticationFailedException("No credentials in request");
                }
            }
            apiSession = loginWithAppropriateCredentials(userLoger, credentials, credentialsMap);
        } catch (LoginFailedException | AuthenticationFailedException e) {
            if (shouldTrackLoginFailures) {
                loginFailureTracker.recordFailure(username);
            }
            // With SSO, the attempted user is the one returned by the AuthenticationManager
            final boolean ssoLogin = MapUtils.isNotEmpty(credentialsMap);
            final String attemptedUsername = ssoLogin
                    ? MapUtils.getString(credentialsMap, AuthenticationManager.USERNAME_CREDENTIAL)
                    : username;
            // A failed SSO login may have no username (e.g. a rejected CAS ticket). Otherwise, without any username,
            // the request is anonymous ("No credentials in request"), not a failed login
            if (audited && (ssoLogin || StringUtils.isNotEmpty(attemptedUsername))) {
                getAuthenticationAuditLogger(request).loginFailed(request.asHttpServletRequest(), attemptedUsername,
                        resolveAuthMethod(authenticationManager, credentialsMap),
                        resolveAuthProvider(authenticationManager, request, credentialsMap), null);
            }
            throw e;
        }
        if (shouldTrackLoginFailures) {
            loginFailureTracker.resetFailures(username);
        }
        storeCredentials(request, apiSession, invalidateAndRecreateHTTPSession);
        portalCookies.addCSRFTokenCookieToResponse(request.asHttpServletRequest(), response,
                tokenGenerator.createOrLoadToken(request.getHttpSession()));
        if (audited) {
            final String authMethod = resolveAuthMethod(authenticationManager, credentialsMap);
            final String authProvider = resolveAuthProvider(authenticationManager, request, credentialsMap);
            // Kept in the HTTP session so that the logout of an audited login is audited too, with the same values
            final HttpSession httpSession = request.getHttpSession();
            httpSession.setAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM, authMethod);
            if (authProvider != null) {
                httpSession.setAttribute(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM, authProvider);
            }
            getAuthenticationAuditLogger(request).loginSucceeded(request.asHttpServletRequest(),
                    apiSession.getUserName(), authMethod, authProvider, apiSession.getId());
        }
    }

    /**
     * Performs the logout on the engine, cleans the HTTP session and, for the audited logins, writes the logout in
     * the authentication audit log.
     *
     * @param invalidateHttpSession whether the HTTP session is invalidated, or only cleaned from the Bonita
     *        attributes (for the SSO mechanisms that still need it to log out from the identity provider)
     * @throws LoginFailedException if the engine logout fails, in which case the HTTP session is left untouched
     */
    public void logout(final HttpServletRequestAccessor request, final UserLogger userLogger,
            final boolean invalidateHttpSession) throws LoginFailedException {
        final HttpSession httpSession = request.getHttpSession();
        final APISession apiSession = request.getApiSession();
        final String auditedLoginMethod = (String) httpSession
                .getAttribute(SessionUtil.AUDITED_LOGIN_METHOD_SESSION_PARAM);
        final String auditedLoginProvider = (String) httpSession
                .getAttribute(SessionUtil.AUDITED_LOGIN_PROVIDER_SESSION_PARAM);
        if (apiSession != null) {
            userLogger.doLogout(apiSession);
        }
        SessionUtil.sessionLogout(httpSession, invalidateHttpSession);
        if (apiSession != null && auditedLoginMethod != null) {
            getAuthenticationAuditLogger(request).loggedOut(request.asHttpServletRequest(), apiSession.getUserName(),
                    auditedLoginMethod, auditedLoginProvider, apiSession.getId());
        }
    }

    protected AuthenticationAuditLogger getAuthenticationAuditLogger(final HttpServletRequestAccessor request) {
        if (authenticationAuditLogger == null) {
            authenticationAuditLogger = AuthenticationAuditLoggerAccessor
                    .getAuthenticationAuditLogger(request.asHttpServletRequest().getServletContext());
        }
        return authenticationAuditLogger;
    }

    /**
     * Logins performed with the map of credentials returned by the AuthenticationManager are SSO logins, the others
     * use the username and password of the request.
     */
    private String resolveAuthMethod(final AuthenticationManager authenticationManager,
            final Map<String, Serializable> credentialsMap) {
        return MapUtils.isEmpty(credentialsMap) ? AuthenticationAuditLogger.METHOD_STANDARD
                : authenticationManager.getAuthMethod();
    }

    /**
     * Only the SSO logins have an identity provider.
     */
    private String resolveAuthProvider(final AuthenticationManager authenticationManager,
            final HttpServletRequestAccessor request, final Map<String, Serializable> credentialsMap) {
        return MapUtils.isEmpty(credentialsMap) ? null : authenticationManager.getAuthProvider(request);
    }

    protected StandardCredentials createUserCredentials(final HttpServletRequestAccessor requestAccessor) {
        return new StandardCredentials(requestAccessor.getUsername(), requestAccessor.getPassword());
    }

    protected UserLogger getUserLogger() {
        return new UserLogger();
    }

    public AuthenticationManager getAuthenticationManager() throws ServletException {
        try {
            final AuthenticationManager authenticationManager = AuthenticationManagerFactory.getAuthenticationManager();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using the AuthenticationManager implementation: "
                        + authenticationManager.getClass().getName());
            }
            return authenticationManager;
        } catch (final AuthenticationManagerNotFoundException e) {
            throw new ServletException(e);
        }
    }

    private APISession loginWithAppropriateCredentials(UserLogger userLoger, Credentials credentials,
            Map<String, Serializable> credentialsMap)
            throws LoginFailedException {
        if (MapUtils.isEmpty(credentialsMap)) {
            LOGGER.debug("Engine login using the username and password");
            return userLoger.doLogin(credentials);
        } else {
            LOGGER.debug("Engine login using the map of credentials retrieved from the request");
            return userLoger.doLogin(credentialsMap);
        }
    }

    protected void storeCredentials(final HttpServletRequestAccessor request, final APISession session,
            boolean recreateHTTPSession) throws LoginFailedException {
        String local = LocaleUtils.getUserLocaleAsString(request.asHttpServletRequest());
        // Use APISession username instead of request parameter for SSO compatibility (OIDC, SAML, etc.)
        final User user = new User(session.getUserName(), local);
        initSession(request, session, user, recreateHTTPSession);
    }

    protected void initSession(final HttpServletRequestAccessor request, final APISession session, final User user,
            boolean recreateHTTPSession) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("HTTP session initialization");
        }
        if (recreateHTTPSession) {
            //invalidating session allows to fix session fixation security issue
            request.getHttpSession().invalidate();
        }
        //calling request.getSession() creates a new Session if no any valid exists
        SessionUtil.sessionLogin(user, session, request.getHttpSession());
    }

}
