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
package org.bonitasoft.engine.platform.session.impl;

import java.util.Date;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.LogUtil;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.session.PlatformSessionProvider;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.platform.session.model.builder.SPlatformSessionBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class PlatformSessionServiceImpl implements PlatformSessionService {

    private static final long DEFAULT_SESSION_DURATION = 3600000;

    private final PlatformSessionProvider platformSessionProvider;

    private final TechnicalLoggerService logger;

    private long sessionDuration = DEFAULT_SESSION_DURATION;

    public PlatformSessionServiceImpl(final PlatformSessionProvider platformSessionProvider,
            final TechnicalLoggerService logger) {
        this.platformSessionProvider = platformSessionProvider;
        this.logger = logger;
    }

    @Override
    public SPlatformSession createSession(final String username) throws SSessionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "createSession"));
        }
        final long sessionId = PlatformSessionIdGenerator.getNextId();
        final long duration = getSessionsDuration();
        final SPlatformSession session = BuilderFactory.get(SPlatformSessionBuilderFactory.class).createNewInstance(sessionId, duration, username).done();
        platformSessionProvider.addSession(session);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "createSession"));
        }
        return session;
    }

    @Override
    public void deleteSession(final long sessionId) throws SSessionNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "deleteSession"));
        }
        platformSessionProvider.removeSession(sessionId);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "deleteSession"));
        }
    }

    @Override
    public boolean isValid(final long sessionId) throws SSessionNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "isValid"));
        }
        final SPlatformSession session = platformSessionProvider.getSession(sessionId);
        final Date now = new Date();
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "isValid"));
        }
        return session.getExpirationDate().after(now);
    }

    @Override
    public SPlatformSession getSession(final long sessionId) throws SSessionNotFoundException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "getSession"));
        }
        final SPlatformSession session = platformSessionProvider.getSession(sessionId);
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "getSession"));
        }
        return BuilderFactory.get(SPlatformSessionBuilderFactory.class).copy(session);
    }

    @Override
    public void setSessionDuration(final long duration) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "setSessionDuration"));
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("The duration must be greater then 0");
        }
        sessionDuration = duration;
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "setSessionDuration"));
        }
    }

    @Override
    public long getDefaultSessionDuration() {
        return DEFAULT_SESSION_DURATION;
    }

    @Override
    public long getSessionsDuration() {
        return sessionDuration;
    }

    @Override
    public void renewSession(final long sessionId) throws SSessionException {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogBeforeMethod(this.getClass(), "renewSession"));
        }
        final SPlatformSession session = getSession(sessionId);
        try {
            ClassReflector.invokeSetter(session, "setLastRenewDate", Date.class, new Date());
            platformSessionProvider.updateSession(session);
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogAfterMethod(this.getClass(), "renewSession"));
            }
        } catch (final SReflectException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, LogUtil.getLogOnExceptionMethod(this.getClass(), "renewSession", e));
            }
            throw new SSessionException(e);
        }
    }

}
