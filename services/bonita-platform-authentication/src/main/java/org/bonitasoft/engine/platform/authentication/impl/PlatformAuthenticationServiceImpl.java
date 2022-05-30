/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform.authentication.impl;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformAuthenticationServiceImpl implements PlatformAuthenticationService {

    private Logger logger = LoggerFactory.getLogger(PlatformAuthenticationServiceImpl.class);
    private static final String USERNAME = "platformAdmin";

    private static final String PASSWORD = "platform";

    public PlatformAuthenticationServiceImpl() {
        super();
    }

    @Override
    public void checkUserCredentials(final String username, final String password)
            throws SInvalidUserException, SInvalidPasswordException {
        final String methodName = "checkUserCredentials";
        if (logger.isTraceEnabled()) {
            logger.trace(
                    LogUtil.getLogBeforeMethod(this.getClass(), methodName));
        }
        // FIXME read user and password from a configuration file
        if (!USERNAME.equals(username)) {
            logOnExceptionMethod(username, methodName);
            throw new SInvalidUserException("Invalid user : " + username);
        }
        if (!PASSWORD.equals(password)) {
            logOnExceptionMethod(username, methodName);
            throw new SInvalidPasswordException("Invalid password");
        }
        if (logger.isTraceEnabled()) {
            logger.trace(
                    LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void logOnExceptionMethod(final String username, final String methodName) {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, "Invalid user : " + username));
        }
    }

}
