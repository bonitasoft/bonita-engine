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
package org.bonitasoft.engine.platform.authentication.impl;

import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformAuthenticationServiceImpl implements PlatformAuthenticationService {

    private static final String USERNAME = "platformAdmin";

    private static final String PASSWORD = "platform";

    private final TechnicalLoggerService logger;

    public PlatformAuthenticationServiceImpl(final TechnicalLoggerService logger) {
        super();
        this.logger = logger;
    }

    @Override
    public void checkUserCredentials(final String username, final String password) throws SInvalidUserException, SInvalidPasswordException {
        final String methodName = "checkUserCredentials";
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
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
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), methodName));
        }
    }

    private void logOnExceptionMethod(final String username, final String methodName) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE,
                    LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, "Invalid user : " + username));
        }
    }

}
