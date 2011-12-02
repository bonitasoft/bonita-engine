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

import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.authentication.InvalidPasswordException;
import org.bonitasoft.engine.authentication.UserNotFoundException;
import org.bonitasoft.engine.core.login.LoginException;
import org.bonitasoft.engine.core.login.LoginService;
import org.bonitasoft.engine.core.login.LoginServiceImpl;
import org.bonitasoft.engine.session.SessionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class SecuredLoginServiceImpl extends LoginServiceImpl implements LoginService {

    private final AuthenticationService authenticationService;

    public SecuredLoginServiceImpl(final AuthenticationService authenticationService, final SessionService sessionService) {
        super(sessionService);
        this.authenticationService = authenticationService;
    }

    @Override
    public SSession login(final long tenantId, final String userName, final String password) throws LoginException {
        try {
            authenticationService.checkUserCredentials(userName, password);
            return getSessionService().createSession(tenantId, userName);
        } catch (UserNotFoundException e) {
            throw new LoginException(e);
        } catch (InvalidPasswordException e) {
            throw new LoginException(e);
        } catch (SessionException e) {
            throw new LoginException(e);
        }
    }

}
