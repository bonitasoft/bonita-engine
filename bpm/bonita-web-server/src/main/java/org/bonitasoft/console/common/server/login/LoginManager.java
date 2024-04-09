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

import org.apache.commons.collections4.MapUtils;
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

    /**
     * @deprecated use {@link LoginManager#login(HttpServletRequest, HttpServletResponse)} instead. It also deals with
     *             the cookies creation (CSRF and tenant)
     */
    @Deprecated
    public void login(HttpServletRequestAccessor request, HttpServletResponse response, UserLogger userLoger,
            Credentials credentials)
            throws AuthenticationFailedException, ServletException, LoginFailedException {
        loginInternal(request, response, userLoger, credentials);
    }

    public void loginInternal(HttpServletRequestAccessor request, HttpServletResponse response, UserLogger userLoger,
            Credentials credentials)
            throws AuthenticationFailedException, ServletException, LoginFailedException {
        AuthenticationManager authenticationManager = getAuthenticationManager();
        Map<String, Serializable> credentialsMap = authenticationManager.authenticate(request, credentials);
        // In case of a login with the login service we invalidate the session and create a new one.
        // Otherwise, logging in with the credentials in the request (SSO) it is not mandatory it depends on the AuthenticationManager implementation used
        // some SSO mechanisms already handle it (SAML, OIDC).
        Boolean invalidateAndRecreateHTTPSessionIfSet = (Boolean) credentialsMap
                .remove(AuthenticationManager.INVALIDATE_SESSION);
        boolean invalidateAndRecreateHTTPSession = invalidateAndRecreateHTTPSessionIfSet == null
                || invalidateAndRecreateHTTPSessionIfSet.booleanValue();
        if (credentialsMap.isEmpty()) {
            if (credentials.getName() == null || credentials.getName().isEmpty()) {
                LOGGER.debug("There are no credentials in the request");
                throw new AuthenticationFailedException("No credentials in request");
            }
        }
        APISession apiSession = loginWithAppropriateCredentials(userLoger, credentials, credentialsMap);
        storeCredentials(request, apiSession, invalidateAndRecreateHTTPSession);
        portalCookies.addCSRFTokenCookieToResponse(request.asHttpServletRequest(), response,
                tokenGenerator.createOrLoadToken(request.getHttpSession()));
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
        final User user = new User(request.getUsername(), local);
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
