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
package org.bonitasoft.engine.core.platform.login.impl;

import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.core.platform.login.SPlatformLoginException;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public class PlatformLoginServiceImpl implements PlatformLoginService {

    private final PlatformAuthenticationService authenticationService;

    private final PlatformSessionService sessionService;

    public PlatformLoginServiceImpl(final PlatformAuthenticationService platformAuthenticationService, final PlatformSessionService platformSessionService) {
        authenticationService = platformAuthenticationService;
        sessionService = platformSessionService;
    }

    @Override
    public SPlatformSession login(final String userName, final String password) throws SPlatformLoginException {
        try {
            authenticationService.checkUserCredentials(userName, password);
            return sessionService.createSession(userName);
        } catch (final SInvalidUserException e) {
            throw new SPlatformLoginException(e);
        } catch (final SInvalidPasswordException e) {
            throw new SPlatformLoginException(e);
        } catch (final SSessionException e) {
            throw new SPlatformLoginException(e);
        }
    }

    @Override
    public void logout(final long sessionId) throws SSessionNotFoundException {
        sessionService.deleteSession(sessionId);
    }

    @Override
    public boolean isValid(final long sessionId) {
        try {
            return sessionService.isValid(sessionId);
        } catch (final SSessionNotFoundException ssnfe) {
            return false;
        }
    }

}
