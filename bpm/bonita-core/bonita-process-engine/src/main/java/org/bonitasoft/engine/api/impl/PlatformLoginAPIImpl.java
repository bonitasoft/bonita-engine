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
package org.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.impl.transaction.CustomTransactions;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
public class PlatformLoginAPIImpl extends AbstractLoginApiImpl implements PlatformLoginAPI {

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public PlatformSession login(final String userName, final String password) throws PlatformLoginException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            e.printStackTrace();// no logger available yet
            throw new PlatformLoginException(e.getMessage());
        }
        final PlatformLoginService platformLoginService = platformAccessor.getPlatformLoginService();
//        PlatformService platformService = platformAccessor.getPlatformService(); // TO UNCOMMENT lvaills

        // first call before create session: put the platform in cache if necessary
//        putPlatformInCacheIfNecessary(platformAccessor, platformService); // TO UNCOMMENT lvaills

        try {
            final SPlatformSession platformSession = platformLoginService.login(userName, password);
            final long id = platformSession.getId();
            final Date creationDate = platformSession.getCreationDate();
            final long duration = platformSession.getDuration();
            final long userId = platformSession.getUserId();
            return new PlatformSessionImpl(id, creationDate, duration, userName, userId);
        } catch (final SBonitaException e) {
            final TechnicalLoggerService technicalLoggerService = platformAccessor.getTechnicalLoggerService();
            if (technicalLoggerService.isLoggable(this.getClass(), TechnicalLogSeverity.WARNING)) {
                technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.WARNING, e);
            }
            throw new PlatformLoginException(e.getMessage());
        }
    }

    @Override
    @CustomTransactions
    @AvailableOnStoppedNode
    public void logout(final PlatformSession session) throws PlatformLogoutException, SessionNotFoundException {
        PlatformServiceAccessor platformAccessor;
        try {
            platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            e.printStackTrace();// no logger available yet
            throw new PlatformLogoutException(e.getMessage());
        }
        final PlatformLoginService platformLoginService = platformAccessor.getPlatformLoginService();
        try {
            platformLoginService.logout(session.getId());
        } catch (final SSessionNotFoundException e) {
            throw new SessionNotFoundException(e);
        }
    }

}
