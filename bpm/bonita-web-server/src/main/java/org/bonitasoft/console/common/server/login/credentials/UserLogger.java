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

import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;

/**
 * @author Vincent Elcrin
 */
public class UserLogger {

    /**
     * Overridden in SP
     */
    public APISession doLogin(Credentials credentials) throws LoginFailedException {
        try {
            return getDatastore().login(credentials.getName(),
                    credentials.getPassword());
        } catch (final BonitaException e) {
            throw new LoginFailedException(e.getMessage(), e);
        }
    }

    public APISession doLogin(Map<String, Serializable> credentials) throws LoginFailedException {
        try {
            return getDatastore().login(credentials);
        } catch (final BonitaException e) {
            throw new LoginFailedException(e.getMessage(), e);
        }
    }

    public void doLogout(final APISession apiSession) throws LoginFailedException {
        try {
            getDatastore().logout(apiSession);
        } catch (final BonitaException e) {
            throw new LoginFailedException(e.getMessage(), e);
        }
    }

    /**
     * Overridden in SP
     */
    private LoginDatastore getDatastore() {
        return new LoginDatastore();
    }

}
