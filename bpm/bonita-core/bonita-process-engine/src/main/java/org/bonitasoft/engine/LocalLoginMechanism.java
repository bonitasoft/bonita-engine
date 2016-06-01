/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine;

import java.util.Date;

import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.session.impl.PlatformSessionImpl;

/**
 * Use to login on the platform when inside the same JVM
 * 
 * @author Baptiste Mesta
 */
//Do not remove
//used by reflection when on the same JVM
//see org.bonitasoft.console.common.server.utils.PlatformManagementUtils.platformLogin() in bonita-web
public class LocalLoginMechanism {

    public PlatformSession login() throws PlatformLoginException {
        try {
            PlatformServiceAccessor platformAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            final PlatformSessionService platformSessionService = platformAccessor.getPlatformSessionService();
            SPlatformSession platformSession = platformSessionService.createSession("local");
            final Date creationDate = platformSession.getCreationDate();
            return new PlatformSessionImpl(platformSession.getId(), creationDate, platformSession.getDuration(), "local", platformSession.getUserId());
        } catch (final Exception e) {
            throw new PlatformLoginException(e);
        }

    }
}
