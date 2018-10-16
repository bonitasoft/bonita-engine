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
package org.bonitasoft.engine.session.impl;

import static java.util.Collections.emptyList;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SessionServiceImpl implements SessionService {

    private static final long DEFAULT_SESSION_DURATION = 3600000;

    private long sessionDuration = DEFAULT_SESSION_DURATION;

    private final SessionProvider sessionProvider;

    private final String applicationName;

    private final TechnicalLoggerService logger;

    public SessionServiceImpl(final SessionProvider sessionProvider, final String applicationName, final TechnicalLoggerService logger) {
        this.sessionProvider = sessionProvider;
        this.applicationName = applicationName;
        this.logger = logger;
    }

    @Override
    public SSession createSession(final long tenantId, final String userName) throws SSessionException {
        return this.createSession(tenantId, -1, userName, false);
    }

    @Override
    public SSession createSession(final long tenantId, final long userId, final String userName, final boolean isTechnicalUser) throws SSessionException {
        return createSession(tenantId, userId, userName, isTechnicalUser, emptyList());
    }

    @Override
    public SSession createSession(final long tenantId, final long userId, final String userName, final boolean isTechnicalUser, List<String> profiles) throws SSessionException {
        final long id = SessionIdGenerator.getNextId();
        Date now = new Date();
        SSession session = SSession.builder()
                .id(id)
                .tenantId(tenantId)
                .duration(sessionDuration)
                .userName(userName)
                .applicationName(applicationName)
                .userId(userId)
                .technicalUser(isTechnicalUser)
                .creationDate(now)
                .lastRenewDate(now)
                .profiles(profiles)
                .build();
        sessionProvider.addSession(session);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "CreateSession with tenantId = <" + tenantId + ">, username = <" + userName + ">, id = <"
                    + id + ">");
        }
        return session;
    }

    @Override
    public void deleteSession(final long sessionId) throws SSessionNotFoundException {
        sessionProvider.removeSession(sessionId);
    }

    @Override
    public boolean isValid(final long sessionId) throws SSessionNotFoundException {
        return sessionProvider.getSession(sessionId).getExpirationDate().getTime() > new Date().getTime();
    }

    @Override
    public SSession getSession(final long sessionId) throws SSessionNotFoundException {
        return sessionProvider.getSession(sessionId).toBuilder().build();
    }

    @Override
    public long getLoggedUserFromSession(ReadSessionAccessor sessionAccessor) {
        try {
            long sessionId = sessionAccessor.getSessionId();
            return sessionProvider.getSession(sessionId).getUserId();
        } catch (SessionIdNotSetException | SSessionNotFoundException e) {
            return -1;
        }
    }

    @Override
    public void setSessionDuration(final long duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("The duration must be greater then 0");
        }
        sessionDuration = duration;
    }

    @Override
    public long getDefaultSessionDuration() {
        return DEFAULT_SESSION_DURATION;
    }

    @Override
    public long getSessionDuration() {
        return sessionDuration;
    }

    @Override
    public void renewSession(final long sessionId) throws SSessionException {
        final SSession session = getSession(sessionId);
        SSession updatedSession = session.toBuilder().lastRenewDate(new Date()).build();
        sessionProvider.updateSession(updatedSession);
    }

    @Override
    public void cleanInvalidSessions() {
        sessionProvider.cleanInvalidSessions();
    }

    @Override
    public void deleteSessionsOfTenant(final long tenantId) {
        sessionProvider.deleteSessionsOfTenant(tenantId, false/* don't keep technical user */);
    }

    @Override
    public void deleteSessionsOfTenantExceptTechnicalUser(final long tenantId) {
        sessionProvider.deleteSessionsOfTenant(tenantId, true/* keep technical user */);
    }

    @Override
    public void deleteSessions() {
        sessionProvider.removeSessions();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Sessions were deleted.");
        }
    }

}
