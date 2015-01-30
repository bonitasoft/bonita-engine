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
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionNotFoundException;

/**
 * The LoginAPI allows to log in (and out) onto the Engine. This is a mandatory step to go further using the Engine APIs.
 * Other Engine APIs are only accessible through the returned APISession.
 * 
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 * @see APISession
 */
public interface LoginAPI {

    /**
     * Connects the user in order to use API methods of the default tenant.
     * 
     * @param userName
     *            the user name
     * @param password
     *            the password
     * @return the session to use with other tenant API methods
     * @throws LoginException
     *             occurs when an exception is thrown during login
     */
    @NoSessionRequired
    APISession login(String userName, String password) throws LoginException;

    /**
     * Connects the user in order to use API methods of the default tenant.
     * 
     * @param credentials
     *            the properties to use to login
     * @return the session to use with other tenant API methods
     * @throws LoginException
     *             occurs when an exception is thrown during login
     */
    @NoSessionRequired
    APISession login(Map<String, Serializable> credentials) throws LoginException;

    /**
     * Disconnects the logged user on a tenant according to the given session.
     * 
     * @param session
     *            the tenant session
     * @throws SessionNotFoundException
     *             if the given session is not found on the server side. This may occurs when the session has expired.
     * @throws LogoutException
     *             occurs when an exception is thrown during the logout
     */
    @NoSessionRequired
    void logout(APISession session) throws SessionNotFoundException, LogoutException;

}
