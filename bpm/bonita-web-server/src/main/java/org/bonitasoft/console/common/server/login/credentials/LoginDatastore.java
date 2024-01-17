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
package org.bonitasoft.console.common.server.login.credentials;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.platform.UnknownUserException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yongtao Guo
 */
public class LoginDatastore {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDatastore.class.getName());

    public APISession login(final String username, final String password) throws BonitaException {
        APISession apiSession;
        final String errorMessage = "Error while logging in the engine API.";
        try {
            if (username == null || password == null) {
                LOGGER.error(errorMessage);
                throw new LoginException(errorMessage);
            }
            apiSession = getLoginAPI().login(username, password);
        } catch (final UnknownUserException e) {
            LOGGER.error(e.getMessage());
            throw e;
        } catch (final LoginException e) {
            LOGGER.error(errorMessage);
            throw e;
        } catch (final BonitaException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return apiSession;
    }

    /**
     * login.
     *
     * @return APISession aAPISession
     * @throws BonitaException
     */
    public APISession login(final Map<String, Serializable> credentials) throws BonitaException {
        APISession apiSession;
        try {
            if (credentials == null) {
                final String errorMessage = "Error while logging in on the engine API.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage);
                }
                throw new LoginException(errorMessage);
            }
            apiSession = getLoginAPI().login(credentials);
        } catch (final LoginException e) {
            final String errorMessage = "Error while logging in on the engine API.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage);
            }
            throw new BonitaException(e);
        }
        return apiSession;
    }

    /**
     * logout .
     *
     * @throws BonitaException
     */
    public void logout(final APISession apiSession) throws BonitaException {
        if (apiSession != null) {
            try {
                getLoginAPI().logout(apiSession);
            } catch (final LogoutException e) {
                final String errorMessage = "Logout error while calling the engine API.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage);
                }
                throw new BonitaException(errorMessage, e);
            }
        }
    }

    protected LoginAPI getLoginAPI() throws BonitaException {
        try {
            return TenantAPIAccessor.getLoginAPI();
        } catch (final BonitaException e) {
            final String errorMessage = "Error while getting the loginAPI.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage);
            }
            throw new BonitaException(errorMessage, e);
        }
    }
}
