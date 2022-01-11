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
package org.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.core.platform.login.SInvalidPlatformCredentialsException;
import org.bonitasoft.engine.core.platform.login.SPlatformLoginException;
import org.bonitasoft.engine.platform.InvalidPlatformCredentialsException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.PlatformLogoutException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class PlatformLoginAPIImpl implements PlatformLoginAPI {

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public PlatformSession login(final String userName, final String password) throws PlatformLoginException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new PlatformLoginException(e);
        }
        final PlatformLoginService platformLoginService = platformAccessor.getPlatformLoginService();

        final SPlatformSession platformSession;
        try {
            platformSession = platformLoginService.login(userName, password);
        } catch (SPlatformLoginException e) {
            throw new PlatformLoginException(e);
        } catch (SInvalidPlatformCredentialsException ignored) {
            throw new InvalidPlatformCredentialsException("Wrong username of password");
        }
        final long id = platformSession.getId();
        final Date creationDate = platformSession.getCreationDate();
        final long duration = platformSession.getDuration();
        final long userId = platformSession.getUserId();
        return new PlatformSessionImpl(id, creationDate, duration, userName, userId);
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void logout(final PlatformSession session) throws PlatformLogoutException, SessionNotFoundException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new PlatformLogoutException(e);
        }
        final PlatformLoginService platformLoginService = platformAccessor.getPlatformLoginService();
        try {
            platformLoginService.logout(session.getId());
        } catch (final SSessionNotFoundException e) {
            throw new SessionNotFoundException(e);
        }
    }

}
