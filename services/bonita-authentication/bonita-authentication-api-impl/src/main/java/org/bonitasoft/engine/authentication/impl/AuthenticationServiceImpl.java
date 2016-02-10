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
package org.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Julien Reboul
 * @author Celine Souchet
 */
public class AuthenticationServiceImpl implements GenericAuthenticationService {

    private final IdentityService identityService;

    private final TechnicalLoggerService logger;

    public AuthenticationServiceImpl(final IdentityService identityService, final TechnicalLoggerService logger) {
        this.identityService = identityService;
        this.logger = logger;
    }

    /**
     * @see org.bonitasoft.engine.authentication.GenericAuthenticationService#checkUserCredentials(java.util.Map)
     */
    @Override
    public String checkUserCredentials(Map<String, Serializable> credentials) {
        final String methodName = "checkUserCredentials";
        try {
            final String password = String.valueOf(credentials.get(AuthenticationConstants.BASIC_PASSWORD));
            final String userName = String.valueOf(credentials.get(AuthenticationConstants.BASIC_USERNAME));
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), methodName));
            }
            final SUser user = identityService.getUserByUserName(userName);
            if (identityService.chechCredentials(user, password)) {
                if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                    logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), methodName));
                }
                return userName;
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), methodName));
            }
        } catch (final SUserNotFoundException sunfe) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, sunfe));
            }
        }
        return null;
    }

}
