/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.authentication.impl;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.bonitasoft.engine.authentication.AuthenticationException;
import org.bonitasoft.engine.authentication.AuthenticationService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Elias Ricken de Medeiros
 */
public class JAASAuthenticationServiceImpl implements AuthenticationService {

    private static final String LOGIN_CONTEXT_PREFIX = "BonitaAuthentication";

    private final TechnicalLoggerService logger;

    private final ReadSessionAccessor sessionAccessor;

    public JAASAuthenticationServiceImpl(final TechnicalLoggerService logger, final ReadSessionAccessor sessionAccessor) {
        this.logger = logger;
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public boolean checkUserCredentials(final String username, final String password) throws AuthenticationException {
        LoginContext loginContext = null;
        try {
            loginContext = new LoginContext(getLoginContext(), new AuthenticationCallbackHandler(username, password));
        } catch (final Exception e) {
            throw new AuthenticationException(e);
        }
        try {
            loginContext.login();
        } catch (final LoginException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "", e);
            }
            return false;
        }
        try {
            loginContext.logout();
        } catch (final LoginException e) {
            throw new AuthenticationException(e);
        }
        return true;
    }

    private String getLoginContext() throws TenantIdNotSetException {
        return LOGIN_CONTEXT_PREFIX + "-" + sessionAccessor.getTenantId();
    }
}
