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

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Baptiste Mesta
 */
public class SessionInfos {

    private final String username;

    private final long userId;

    public SessionInfos(final String username, final long userId) {
        this.username = username;
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public static SessionInfos getSessionInfos() {
        SSession session = getSession();
        if (session != null) {
            return new SessionInfos(session.getUserName(), session.getUserId());
        }
        return new SessionInfos(SessionService.SYSTEM, -1);
    }

    public static SSession getSession() {
        SSession session;
        try {
            final long sessionId = getSessionAccessor().getSessionId();
            session = getSessionService().getSession(sessionId);
        } catch (final SessionIdNotSetException e) {
            return null;
        } catch (SSessionNotFoundException e) {
            return null;
        }
        return session;
    }

    private static SessionService getSessionService() {
        try {
            final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
            return platformServiceAccessor.getTenantServiceAccessor(getSessionAccessor().getTenantId()).getSessionService();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    private static SessionAccessor getSessionAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createSessionAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    public static long getUserIdFromSession() {
        return getSessionService().getLoggedUserFromSession(getSessionAccessor());
    }

    public static String getUserNameFromSession() {
        SSession session = getSession();
        if (session == null) {
            // system
            return SessionService.SYSTEM;
        }
        return session.getUserName();
    }

}
