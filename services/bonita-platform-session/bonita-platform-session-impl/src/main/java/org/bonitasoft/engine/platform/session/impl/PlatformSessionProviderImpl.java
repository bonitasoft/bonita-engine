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

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.platform.session.PlatformSessionProvider;
import org.bonitasoft.engine.platform.session.SSessionAlreadyExistsException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public final class PlatformSessionProviderImpl implements PlatformSessionProvider {

    private static Map<Long, SPlatformSession> platformSessions;

    static {
        platformSessions = new HashMap<Long, SPlatformSession>();
    }

    public PlatformSessionProviderImpl() {
    }

    @Override
    public synchronized void addSession(final SPlatformSession session) throws SSessionAlreadyExistsException {
        final long id = session.getId();
        if (platformSessions.containsKey(id)) {
            throw new SSessionAlreadyExistsException("A session wih id \"" + id + "\" already exists");
        }
        platformSessions.put(id, session);
    }

    @Override
    public void removeSession(final long sessionId) throws SSessionNotFoundException {
        if (!platformSessions.containsKey(sessionId)) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        platformSessions.remove(sessionId);
    }

    @Override
    public SPlatformSession getSession(final long sessionId) throws SSessionNotFoundException {
        if (!platformSessions.containsKey(sessionId)) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        return platformSessions.get(sessionId);
    }

    @Override
    public void updateSession(final SPlatformSession session) throws SSessionNotFoundException {
        final long id = session.getId();
        if (!platformSessions.containsKey(id)) {
            throw new SSessionNotFoundException("No session found with id \"" + id + "\"");
        }
        platformSessions.put(id, session);
    }

}
