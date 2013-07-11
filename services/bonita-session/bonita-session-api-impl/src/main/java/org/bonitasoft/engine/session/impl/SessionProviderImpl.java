/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.session.SSessionAlreadyExistsException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.SSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public final class SessionProviderImpl implements SessionProvider {

    private static Map<Long, SSession> sessions;
    static {
        sessions = new HashMap<Long, SSession>();

    }

    public SessionProviderImpl() {
    }

    @Override
    public synchronized void addSession(final SSession session) throws SSessionAlreadyExistsException {
        final long id = session.getId();
        if (sessions.containsKey(id)) {
            throw new SSessionAlreadyExistsException("A session wih id \"" + id + "\" already exists");
        }
        sessions.put(id, session);
    }

    @Override
    public void removeSession(final long sessionId) throws SSessionNotFoundException {
        final SSession session = sessions.remove(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
    }

    @Override
    public SSession getSession(final long sessionId) throws SSessionNotFoundException {
        final SSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        return session;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.engine.session.impl.SessionProvider#updateSession(org.bonitasoft.engine.session.model.SSession)
     */
    @Override
    public void updateSession(final SSession session) throws SSessionNotFoundException {
        final long id = session.getId();
        if (!sessions.containsKey(id)) {
            throw new SSessionNotFoundException("No session found with id \"" + id + "\"");
        }
        sessions.put(id, session);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.engine.session.impl.SessionProvider#cleanInvalidSessions()
     */
    @Override
    public synchronized void cleanInvalidSessions() {
        final List<Long> invalidSessionIds = new ArrayList<Long>();
        for (final SSession session : sessions.values()) {
            if (!session.isValid()) {
                invalidSessionIds.add(session.getId());
            }
        }
        for (final Long invalidSessionId : invalidSessionIds) {
            sessions.remove(invalidSessionId);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.engine.session.impl.SessionProvider#removeSessions()
     */
    @Override
    public synchronized void removeSessions() {
        sessions.clear();
    }

}
