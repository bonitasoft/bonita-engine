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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.session.SSessionAlreadyExistsException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Baptiste Mesta
 * 
 */
public abstract class AbstractSessionProvider implements SessionProvider {

    protected abstract Map<Long, SSession> getSessions();

    protected SSession putSession(final SSession session, final long id) {
        return getSessions().put(id, session);
    }

    @Override
    public synchronized void addSession(final SSession session) throws SSessionAlreadyExistsException {
        final long id = session.getId();
        if (getSessions().containsKey(id)) {
            throw new SSessionAlreadyExistsException("A session wih id \"" + id + "\" already exists");
        }
        putSession(session, id);
    }

    @Override
    public synchronized void removeSession(final long sessionId) throws SSessionNotFoundException {
        final SSession session = getSessions().remove(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
    }

    @Override
    public synchronized SSession getSession(final long sessionId) throws SSessionNotFoundException {
        final SSession session = getSessions().get(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        return session;
    }

    @Override
    public synchronized void updateSession(final SSession session) throws SSessionNotFoundException {
        final long id = session.getId();
        if (!getSessions().containsKey(id)) {
            throw new SSessionNotFoundException("No session found with id \"" + id + "\"");
        }
        putSession(session, id);
    }

    @Override
    public synchronized void cleanInvalidSessions() {
        final List<Long> invalidSessionIds = new ArrayList<Long>();
        for (final SSession session : getSessions().values()) {
            if (!session.isValid()) {
                invalidSessionIds.add(session.getId());
            }
        }
        for (final Long invalidSessionId : invalidSessionIds) {
            getSessions().remove(invalidSessionId);
        }
    }

    @Override
    public synchronized void removeSessions() {
        getSessions().clear();
    }

    @Override
    public synchronized void deleteSessionsOfTenant(final long tenantId, final boolean keepTechnicalSessions) {
        Iterator<SSession> iterator = getSessions().values().iterator();
        while (iterator.hasNext()) {
            SSession sSession = iterator.next();
            if (tenantId == sSession.getTenantId() && (!keepTechnicalSessions || !sSession.isTechnicalUser())) {
                iterator.remove();
            }
        }
    }

}
