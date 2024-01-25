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
package org.bonitasoft.engine.platform.session.impl;

import java.util.Date;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.exceptions.SReflectException;
import org.bonitasoft.engine.platform.session.PlatformSessionProvider;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.platform.session.model.builder.SPlatformSessionBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */

@Component
public class PlatformSessionServiceImpl implements PlatformSessionService {

    private Logger logger = LoggerFactory.getLogger(PlatformSessionServiceImpl.class);
    private static final long DEFAULT_SESSION_DURATION = 3600000;

    private final PlatformSessionProvider platformSessionProvider;

    private long sessionDuration = DEFAULT_SESSION_DURATION;

    public PlatformSessionServiceImpl(
            PlatformSessionProvider platformSessionProvider) {
        this.platformSessionProvider = platformSessionProvider;
    }

    @Override
    public SPlatformSession createSession(final String username) throws SSessionException {
        final long sessionId = PlatformSessionIdGenerator.getNextId();
        final long duration = getSessionsDuration();
        final SPlatformSession session = BuilderFactory.get(SPlatformSessionBuilderFactory.class)
                .createNewInstance(sessionId, duration, username).done();
        platformSessionProvider.addSession(session);
        return session;
    }

    @Override
    public void deleteSession(final long sessionId) throws SSessionNotFoundException {
        platformSessionProvider.removeSession(sessionId);
    }

    @Override
    public boolean isValid(final long sessionId) throws SSessionNotFoundException {
        final SPlatformSession session = platformSessionProvider.getSession(sessionId);
        final Date now = new Date();
        return session.getExpirationDate().after(now);
    }

    @Override
    public SPlatformSession getSession(final long sessionId) throws SSessionNotFoundException {
        final SPlatformSession session = platformSessionProvider.getSession(sessionId);
        return BuilderFactory.get(SPlatformSessionBuilderFactory.class).copy(session);
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
    public long getSessionsDuration() {
        return sessionDuration;
    }

    @Override
    public void renewSession(final long sessionId) throws SSessionException {
        final SPlatformSession session = getSession(sessionId);
        try {
            ClassReflector.invokeSetter(session, "setLastRenewDate", Date.class, new Date());
            platformSessionProvider.updateSession(session);
        } catch (final SReflectException e) {
            throw new SSessionException(e);
        }
    }

}
