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
package org.bonitasoft.engine.service.impl;

import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;

/**
 * @author Lu Kai
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 */
public class PlatformAuthenticationChecker implements PlatformAuthenticationService {

    @Override
    public void checkUserCredentials(final String userName, final String password) throws SInvalidUserException, SInvalidPasswordException {
        try {
            final Properties properties = BonitaHomeServer.getInstance().getPlatformProperties();
            final String userProperty = properties.getProperty("platformAdminUsername");
            if (userProperty == null || !userProperty.equals(userName)) {
                throw new SInvalidUserException("Invalid user: " + userName);
            }
            final String passProperty = properties.getProperty("platformAdminPassword");
            if (passProperty == null || !passProperty.equals(password)) {
                throw new SInvalidPasswordException("Invalid password");
            }
        } catch (final BonitaHomeNotSetException bhnse) {
            throw new SInvalidUserException(bhnse);
        } catch (final IOException ioe) {
            throw new SInvalidUserException(ioe);
        }
    }
}
