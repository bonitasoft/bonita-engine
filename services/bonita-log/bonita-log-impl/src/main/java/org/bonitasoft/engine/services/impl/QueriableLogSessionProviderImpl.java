/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.services.impl;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.services.QueriableLogSessionProvider;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class QueriableLogSessionProviderImpl implements QueriableLogSessionProvider {

    private final SessionService sessionService;

    private final ReadSessionAccessor sessionAccessor;

    private final TechnicalLoggerService technicalLoggerService;

    private final ThreadLocal<SSession> localSession = new ThreadLocal<SSession>();

    public QueriableLogSessionProviderImpl(final SessionService sessionService, final ReadSessionAccessor sessionAccessor,
            final TechnicalLoggerService technicalLoggerService) {
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.technicalLoggerService = technicalLoggerService;
    }

    private SSession getSession() {
        SSession session = localSession.get();
        try {
            if (session == null || session.getId() != sessionAccessor.getSessionId()) {
                long sessionId;
                sessionId = sessionAccessor.getSessionId();
                session = sessionService.getSession(sessionId);
                localSession.set(session);
            }
        } catch (final SessionIdNotSetException e) {
            technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
        } catch (final SSessionNotFoundException e) {
            technicalLoggerService.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            try {
	            System.err.println("****** sessionAccessor.getTenantId()" + sessionAccessor.getTenantId());
            } catch (TenantIdNotSetException e1) {
	            e1.printStackTrace();
            }
            try {
            	sessionService.getSession(session.getId());
            	System.err.println("****** sessionService.getSession(session.getId()) " + session.getId() + "   OK");
            } catch (SSessionNotFoundException e2) {
            	System.err.println("****** sessionService.getSession(session.getId()) " + session.getId() + "   KO");
            }
        }
        return session;
    }

    @Override
    public String getUserId() {
        final SSession session = getSession();
        if (session != null) {
            return session.getUserName();
        }
        return null;
    }

    @Override
    public String getClusterNode() {
        final SSession session = getSession();
        if (session != null) {
            return session.getClusterNode();
        }
        return null;
    }

    @Override
    public String getProductVersion() {
        final SSession session = getSession();
        if (session != null) {
            return session.getProductVersion();
        }
        return null;
    }

}
