/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.login;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Anthony Birembaut
 */
public class SecuredLoginServiceImpl implements LoginService {

    private GenericAuthenticationService authenticationService;

    private final SessionService sessionService;

    private final SessionAccessor sessionAccessor;

    private final IdentityService identityService;

    private final TechnicalLoggerService logger;

    public SecuredLoginServiceImpl(final GenericAuthenticationService authenticationService, final SessionService sessionService,
            final SessionAccessor sessionAccessor, final IdentityService identityService, TechnicalLoggerService tenantTechnicalLoggerService) {
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.identityService = identityService;
        this.logger = tenantTechnicalLoggerService;
    }

    @Override
    public SSession login(final Map<String, Serializable> credentials) throws SLoginException, SUserNotFoundException {
        debugLog("Loging in");
        if (credentials == null) {
            throw new SLoginException("invalid credentials, map is null");
        }
        final Long tenantId = NumberUtils.toLong(String.valueOf(credentials.get(AuthenticationConstants.BASIC_TENANT_ID)), -1);
        sessionAccessor.setSessionInfo(-1, tenantId); // necessary to check user credentials
        long userId = 0;
        boolean isTechnicalUser = false;
        String userName = null;
        try {
            final TechnicalUser technicalUser = getTechnicalUser(tenantId);

            if (credentials.containsKey(AuthenticationConstants.BASIC_USERNAME) && credentials.get(AuthenticationConstants.BASIC_USERNAME) != null) {
                userName = String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME));
            }

            if (technicalUser.getUserName().equals(userName)
                    && technicalUser.getPassword().equals(String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD)))) {
                debugLog("Authenticated as technical user");
                isTechnicalUser = true;
                userId = -1;
            } else {
                userName = authenticationService.checkUserCredentials(credentials);
                if (StringUtils.isNotBlank(userName)) {
                    debugLog("Authenticated as regular user");
                    final SUser user = identityService.getUserByUserName(userName);
                    userId = user.getId();
                } else {
                    debugLog("Authentication failed");
                    // now we are sure authentication Failed
                    authenticationFailed();
                }
            }
        } catch (final AuthenticationException ae) {
            debugLog("Unable to authenticate user with username " + userName);
            throw new SLoginException(ae);
        } catch (final SUserNotFoundException e) {
            debugLog("Unable to find user with username " + userName + " in database.");
            throw e;
        } finally {
            // clean session accessor
            sessionAccessor.deleteSessionId();
        }
        try {
            debugLog("Session creation");
            return sessionService.createSession(tenantId, userId, userName, isTechnicalUser);
        } catch (final SSessionException e) {
            throw new SLoginException(e);
        }
    }

    /**
     * Processes the failed authentication behaviour.
     * 
     * @throws SLoginException
     *         the appropriate exception
     */
    protected void authenticationFailed() throws SLoginException {
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            throw new SLoginException("User name or password is not valid!");
        }
        throw new SLoginException("User name or password is not valid!");
    }

    /**
     * retrieve password from credentials assuming it is stored under the {@link AuthenticationConstants#BASIC_PASSWORD} key
     * 
     * @param credentials
     *        the credentials to check
     * @return the password
     * @throws SLoginException
     *         if password is absent or if credentials is null
     */
    protected String retrievePasswordFromCredentials(final Map<String, Serializable> credentials) throws SLoginException {
        if (credentials == null || !credentials.containsKey(AuthenticationConstants.BASIC_PASSWORD)
                || credentials.get(AuthenticationConstants.BASIC_PASSWORD) == null) {
            throw new SLoginException("invalid credentials, password is absent");
        }
        return String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD));
    }

    /**
     * retrieve username from credentials assuming it is stored under the {@link AuthenticationConstants#BASIC_USERNAME} key
     * 
     * @param credentials
     *        the credentials to check
     * @return the username
     * @throws SLoginException
     *         if username is absent, blank or if credentials is null
     */
    protected String retrieveUsernameFromCredentials(final Map<String, Serializable> credentials) throws SLoginException {
        String userName;
        if (credentials == null || !credentials.containsKey(AuthenticationConstants.BASIC_USERNAME)
                || credentials.get(AuthenticationConstants.BASIC_USERNAME) == null
                || StringUtils.isBlank(userName = String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME)))) {
            throw new SLoginException("invalid credentials, username is blank");
        }
        return userName;
    }

    @Override
    public void logout(final long sessionId) throws SSessionNotFoundException {
        sessionService.deleteSession(sessionId);
    }

    @Override
    public boolean isValid(final long sessionId) {
        try {
            return sessionService.isValid(sessionId);
        } catch (final SSessionNotFoundException e) {
            return false;
        }
    }

    protected TechnicalUser getTechnicalUser(final long tenantId) throws SLoginException {
        try {
            final Properties properties = BonitaHomeServer.getInstance().getTenantProperties(tenantId);
            final String userName = (String) properties.get("userName");
            final String password = (String) properties.get("userPassword");
            return new TechnicalUser(userName, password);
        } catch (IOException e) {
            throw new SLoginException(e);
        }
    }

    protected void debugLog(String message) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
            logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
        }
    }
}
