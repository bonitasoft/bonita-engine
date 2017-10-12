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
package org.bonitasoft.engine.core.platform.login;

import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface PlatformLoginService {

    /**
     * login to the platform by userName and password
     * 
     * @param userName
     *            name of user
     * @param password
     *            password of user
     * @return an SPlatformSession object
     * @see SPlatformSession
     * @throws SPlatformLoginException
     */
    SPlatformSession login(String userName, String password) throws SPlatformLoginException, SInvalidPlatformCredentialsException;

    /**
     * logout the platform by sessionId
     * 
     * @param sessionId
     *            identifier of platform session
     * @throws SSessionNotFoundException
     */
    void logout(final long sessionId) throws SSessionNotFoundException;

    /**
     * Verify if a session is valid
     * 
     * @param sessionId
     *            identifier of platform session
     * @return true if session is valid, false otherwise.
     */
    boolean isValid(final long sessionId);

}
