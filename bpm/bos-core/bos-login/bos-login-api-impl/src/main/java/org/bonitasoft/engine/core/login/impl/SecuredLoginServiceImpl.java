/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.login.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.authentication.InvalidPasswordException;
import org.bonitasoft.engine.authentication.UserNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.login.LoginServiceImpl;
import org.bonitasoft.engine.core.login.SLoginException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.io.PropertiesManager;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SecuredLoginServiceImpl extends LoginServiceImpl implements LoginService {

    private final AuthenticationService authenticationService;

    private final SessionAccessor sessionAccesor;

    public SecuredLoginServiceImpl(final AuthenticationService authenticationService, final SessionService sessionService,
            final SessionAccessor sessionAccessor, final IdentityService identityService) {
        super(sessionService, identityService);
        this.authenticationService = authenticationService;
        sessionAccesor = sessionAccessor;
    }

    @Override
    public SSession login(final long tenantId, final String userName, final String password) throws SLoginException {
        sessionAccesor.setSessionInfo(-1, tenantId); // necessary to check user credentials
        long userId = -1;
        try {
            userId = authenticationService.checkUserCredentials(userName, password).getId();
        } catch (final UserNotFoundException e) {
            handleInvalidUser(tenantId, userName, password, e);
        } catch (final InvalidPasswordException e) {
            handleInvalidUser(tenantId, userName, password, e);
        }
        try {
            final SSession createSession = getSessionService().createSession(tenantId, userName);
            getSessionService().setCurrentUserId(createSession, userId);
            return createSession;
        } catch (final SSessionException se) {
            throw new SLoginException(se);
        }
    }

    private void handleInvalidUser(final long tenantId, final String userName, final String password, final SBonitaException originalException)
            throws SLoginException {
        try {
            final boolean isTechnicalUser = checkTechnicalUserCredentials(tenantId, userName, password);
            if (!isTechnicalUser) {
                throw new SLoginException(originalException);
            }
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new SLoginException(bhnse);
        } catch (final IOException ioe) {
            throw new SLoginException(ioe);
        }
    }

    private boolean checkTechnicalUserCredentials(final long tenantId, final String userName, final String password) throws BonitaHomeNotSetException,
            SLoginException, IOException {
        final String technicalUserPropertiesPath = BonitaHomeServer.getInstance().getTenantConfFolder(tenantId) + File.separator + "technical-user.properties";
        final Properties properties = PropertiesManager.getPropertiesFromFile(new String(technicalUserPropertiesPath));
        final String techinicalUser = (String) properties.get("userName");
        final String techinicalPassword = (String) properties.get("userPassword");
        return techinicalUser != null && techinicalUser.equals(userName) && techinicalPassword != null && techinicalPassword.equals(password);
    }

}
