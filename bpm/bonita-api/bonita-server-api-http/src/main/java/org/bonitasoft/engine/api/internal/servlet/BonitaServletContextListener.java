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
package org.bonitasoft.engine.api.internal.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Used to initialize the spring context if not yet initialized.
 * Also, starts the node if the platform exists.
 * 
 * @author Baptiste Mesta
 */
public class BonitaServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            PlatformServiceAccessor platformAccessor = getPlatformAccessor();
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            PlatformSessionService platformSessionService = platformAccessor.getPlatformSessionService();
            SPlatformSession createSession = platformSessionService.createSession("john");
            sessionAccessor.setSessionInfo(createSession.getId(), -1);
            PlatformAPIImpl platformAPI = new PlatformAPIImpl();
            if (platformAPI.isPlatformCreated()) {
                platformAPI.startNode();
            }
            platformSessionService.deleteSession(createSession.getId());
            sessionAccessor.deleteSessionId();
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    protected PlatformServiceAccessor getPlatformAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {

    }

}
