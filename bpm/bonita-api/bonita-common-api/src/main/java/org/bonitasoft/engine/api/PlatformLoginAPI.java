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

import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformLogoutException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;

/**
 * <b>Manage the login on the platform.</b>
 * <p>
 * Using this API you can obtain a {@link PlatformSession} that can be used to retreive a PlatformAPIAccessor.
 * <p>
 * <code>PlatformSession</code> gives access to platform APIs only:
 * <ul>
 * <li>{@link PlatformAPI}</li>
 * <li>{@link PlatformCommandAPI}</li>
 * </ul>
 * 
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public interface PlatformLoginAPI {

    /**
     * Login with username and password of the platform administrator defined in {@code bonita-platform.properties}
     * 
     * @param userName
     *            the platform administrator name
     * @param password
     *            the platform administrator password
     * @return
     *         the session created for you, can be used to retrieve platform APIs
     * @throws PlatformLoginException
     *             occurs when an exception is thrown during login the platform
     */
    @NoSessionRequired
    PlatformSession login(String userName, String password) throws PlatformLoginException;

    /**
     * Logout from a platform.
     * 
     * @param session
     *            the platform session to logout from.
     * @throws PlatformLogoutException
     *             occurs when an exception is thrown during logout the platform
     * @throws SessionNotFoundException
     *             if the session is not found on the server side. This may occurs when the session has expired.
     */
    @NoSessionRequired
    void logout(PlatformSession session) throws PlatformLogoutException, SessionNotFoundException;

}
