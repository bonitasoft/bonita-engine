/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.login.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.authentication.InvalidPasswordException;
import org.bonitasoft.engine.authentication.UserNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.login.LoginException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.login.LoginServiceImpl;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.session.SessionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class SecuredLoginServiceImpl extends LoginServiceImpl implements LoginService {

    private final AuthenticationService authenticationService;
    private final SessionAccessor sessionAccesor;

    public SecuredLoginServiceImpl(final AuthenticationService authenticationService, final SessionService sessionService, final SessionAccessor sessionAccessor) {
        super(sessionService);
        this.authenticationService = authenticationService;
        this.sessionAccesor = sessionAccessor;
    }

    @Override
    public SSession login(final long tenantId, final String userName, final String password) throws LoginException {
        sessionAccesor.setSessionInfo(-1, tenantId); // necessary to check user credentials
        try {
            authenticationService.checkUserCredentials(userName, password);
        } catch (final UserNotFoundException e) {
            handleInvalidUser(tenantId, userName, password, e);
        } catch (final InvalidPasswordException e) {
            handleInvalidUser(tenantId, userName, password, e);
        }  
        
        try {
            return getSessionService().createSession(tenantId, userName);
        } catch (final SessionException e) {
            throw new LoginException(e);
        }
    }

    private void handleInvalidUser(final long tenantId, final String userName, final String password, final SBonitaException originalException) throws LoginException {
        try {
            final boolean isTechnicalUser = checkTechinicalUserCredentials(tenantId, userName, password);
            if (!isTechnicalUser) {
                throw new LoginException(originalException);
            }
        } catch (final BonitaHomeNotSetException e1) {
            throw new LoginException(e1);
        } catch (final FileNotFoundException e1) {
            throw new LoginException(e1);
        } 
    }

    private boolean checkTechinicalUserCredentials(final long tenantId, final String userName, final String password) throws BonitaHomeNotSetException, FileNotFoundException, LoginException {
        final String technicalUserPropertiesPath = BonitaHomeServer.getInstance().getTenantConfFolder(tenantId) + File.separator + "technical-user.properties";
        final Properties techProp = new Properties();
        
        final FileInputStream fis = new FileInputStream(technicalUserPropertiesPath);
        
        try {
            techProp.load(fis);
            final String techinicalUser = (String) techProp.get("userName");
            final String techinicalPassword = (String) techProp.get("userPassword");
            return userName.equals(techinicalUser) && password.equals(techinicalPassword);
        } catch (final IOException e) {
            throw new LoginException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final IOException e) {
                    throw new LoginException(e);
                }
            }
        }
        
    }

}
