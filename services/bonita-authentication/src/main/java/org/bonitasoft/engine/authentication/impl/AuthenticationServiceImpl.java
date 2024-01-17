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
package org.bonitasoft.engine.authentication.impl;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.bonitasoft.engine.authentication.GenericAuthenticationService;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Julien Reboul
 * @author Celine Souchet
 */

@ConditionalOnProperty(value = "authentication.service.ref.name", havingValue = "authenticationService", matchIfMissing = true)
@Component("authenticationService")
public class AuthenticationServiceImpl implements GenericAuthenticationService {

    private Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final IdentityService identityService;

    public AuthenticationServiceImpl(final IdentityService identityService) {
        this.identityService = identityService;
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
            if (logger.isTraceEnabled()) {
                logger.trace(
                        LogUtil.getLogBeforeMethod(this.getClass(), methodName));
            }
            final SUser user = identityService.getUserByUserName(userName);
            if (identityService.checkCredentials(user, password)) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            LogUtil.getLogAfterMethod(this.getClass(), methodName));
                }
                return userName;
            }
            if (logger.isTraceEnabled()) {
                logger.trace(
                        LogUtil.getLogAfterMethod(this.getClass(), methodName));
            }
        } catch (final SUserNotFoundException sunfe) {
            if (logger.isTraceEnabled()) {
                logger.trace(
                        LogUtil.getLogOnExceptionMethod(this.getClass(), methodName, sunfe));
            }
        }
        return null;
    }

}
