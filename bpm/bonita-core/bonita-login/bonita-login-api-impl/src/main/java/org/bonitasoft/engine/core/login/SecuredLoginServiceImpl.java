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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SecuredLoginServiceImpl implements LoginService {

    private AuthenticationService authenticationService = null;

    private GenericAuthenticationService genericAuthenticationService = null;

    private final SessionService sessionService;

    private final SessionAccessor sessionAccessor;

    private final IdentityService identityService;

    @Deprecated
    public SecuredLoginServiceImpl(@SuppressWarnings("deprecation") final AuthenticationService authenticationService, final SessionService sessionService,
            final SessionAccessor sessionAccessor, final IdentityService identityService) {
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.identityService = identityService;
    }

    public SecuredLoginServiceImpl(final GenericAuthenticationService genericAuthenticationService, final SessionService sessionService,
            final SessionAccessor sessionAccessor, final IdentityService identityService) {
        this.genericAuthenticationService = genericAuthenticationService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.identityService = identityService;
    }

    @Override
    @Deprecated
    public SSession login(final long tenantId, final String userName, final String password) throws SLoginException {
        final Map<String, Serializable> credentials = new HashMap<String, Serializable>();
        credentials.put(AuthenticationConstants.BASIC_TENANT_ID, String.valueOf(tenantId));
        credentials.put(AuthenticationConstants.BASIC_PASSWORD, password);
        credentials.put(AuthenticationConstants.BASIC_USERNAME, userName);
        return this.login(credentials);

    }

    @Override
    public SSession login(final Map<String, Serializable> credentials) throws SLoginException {
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
                isTechnicalUser = true;
                userId = -1;
            } else {
                userName = loginChoosingAppropriateAuthenticationService(credentials);
                if (StringUtils.isNotBlank(userName)) {
                    final SUser user = identityService.getUserByUserName(userName);
                    userId = user.getId();
                } else {
                    // now we are sure authentication Failed
                    authenticationFailed();
                }
            }
        } catch (final AuthenticationException ae) {
            throw new SLoginException(ae);
        } catch (final SUserNotFoundException e) {
            throw new SLoginException("Unable to found user " + userName);
        } finally {
            // clean session accessor
            sessionAccessor.deleteSessionId();
        }
        try {
            return sessionService.createSession(tenantId, userId, userName, isTechnicalUser);
        } catch (final SSessionException e) {
            throw new SLoginException(e);
        }
    }

    /**
     * process the failed SpringTenantFileSystemBeanAccessor.java:127authentication behaviour
     * 
     * @param authenticationException
     *            the authentication that may have risen from authentication service
     * @throws SLoginException
     *             the appropriate exception
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
     * login to the internal authentication service if it is not null or to the Generic Authentication Service
     * 
     * @since 6.3
     *        Ensure backward compatibility with previous version
     * 
     * @param credentials
     *            the credentials to use to login
     * @return the username of the logged in user
     */
    protected String loginChoosingAppropriateAuthenticationService(final Map<String, Serializable> credentials) throws AuthenticationException, SLoginException {
        if (authenticationService != null) {
            final String userName = retrieveUsernameFromCredentials(credentials);
            final String password = retrievePasswordFromCredentials(credentials);
            if (authenticationService.checkUserCredentials(userName, password)) {
                return userName;
            }
            return null;
        } else if (genericAuthenticationService != null) {
            return genericAuthenticationService.checkUserCredentials(credentials);
        }
        throw new AuthenticationException("no implementation of authentication supplied");
    }

    /**
     * retrieve password from credentials assuming it is stored under the {@link AuthenticationConstants.BASIC_PASSWORD} key
     * 
     * @param credentials
     *            the credentials to check
     * @return the password
     * @throws SLoginException
     *             if password is absent or if credentials is null
     */
    protected String retrievePasswordFromCredentials(final Map<String, Serializable> credentials) throws SLoginException {
        if (credentials == null || !credentials.containsKey(AuthenticationConstants.BASIC_PASSWORD)
                || credentials.get(AuthenticationConstants.BASIC_PASSWORD) == null) {
            throw new SLoginException("invalid credentials, password is absent");
        }
        return String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD));
    }

    /**
     * retrieve username from credentials assuming it is stored under the {@link AuthenticationConstants.BASIC_USERNAME} key
     * 
     * @param credentials
     *            the credentials to check
     * @return the username
     * @throws SLoginException
     *             if username is absent, blank or if credentials is null
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
        } catch (final BonitaHomeNotSetException e) {
            throw new SLoginException(e);
        } catch (final IOException e) {
            throw new SLoginException(e);
        }
    }

}
