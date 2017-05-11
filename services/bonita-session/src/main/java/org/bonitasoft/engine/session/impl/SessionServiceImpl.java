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

import java.util.Date;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.session.model.builder.SSessionBuilderFactory;
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
        final long id = SessionIdGenerator.getNextId();
        final long duration = getSessionDuration();

        final SSession session = BuilderFactory.get(SSessionBuilderFactory.class)
                .createNewInstance(id, tenantId, duration, userName, applicationName, userId).technicalUser(isTechnicalUser).done();
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
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "DeleteSession with sessionId = <" + sessionId + ">");
        }
    }

    @Override
    public boolean isValid(final long sessionId) throws SSessionNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "isValid"));
        }
        SSession session = null;
        try {
            session = sessionProvider.getSession(sessionId);
        } catch (final SSessionNotFoundException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Session with id = <" + sessionId + "> is invalid, because it does not exist.");
            }
            throw e;
        }
        final Date now = new Date();
        final boolean isValid = session.getExpirationDate().getTime() > now.getTime();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "isValid"));
        }
        return isValid;
    }

    @Override
    public SSession getSession(final long sessionId) throws SSessionNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSession"));
        }
        final SSession session = sessionProvider.getSession(sessionId);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSession"));
        }
        return BuilderFactory.get(SSessionBuilderFactory.class).copy(session);
    }

    @Override
    public long getLoggedUserFromSession(ReadSessionAccessor sessionAccessor) {
        try {
            long sessionId = sessionAccessor.getSessionId();
            return sessionProvider.getSession(sessionId).getUserId();
        } catch (SessionIdNotSetException e) {
            return -1;
        } catch (SSessionNotFoundException e) {
            return -1;
        }
    }

    @Override
    public void setSessionDuration(final long duration) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "setSessionDuration"));
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("The duration must be greater then 0");
        }
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "setSessionDuration"));
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
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "renewSession"));
        }
        final SSession session = getSession(sessionId);
        try {
            ClassReflector.invokeSetter(session, "setLastRenewDate", Date.class, new Date());
            sessionProvider.updateSession(session);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "renewSession"));
            }
        } catch (final Exception e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "renewSession", e));
            }
            throw new SSessionException(e);
        }
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
