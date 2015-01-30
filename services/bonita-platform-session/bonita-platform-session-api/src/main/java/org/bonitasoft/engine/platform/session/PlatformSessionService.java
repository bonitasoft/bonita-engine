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
package org.bonitasoft.engine.platform.session;

import org.bonitasoft.engine.platform.session.model.SPlatformSession;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface PlatformSessionService {

    /**
     * Create a new session for the given user;
     * 
     * @param username
     *            user name
     * @return a new session
     * @throws SSessionException
     *             if some error arrives while creating the session
     * @@since 6.0
     */
    SPlatformSession createSession(String username) throws SSessionException;

    /**
     * Delete a session having the given id
     * 
     * @param sessionId
     *            the session's id
     * @throws SSessionNotFoundException
     *             if no session exists for the given id
     * @@since 6.0
     */
    void deleteSession(long sessionId) throws SSessionNotFoundException;

    /**
     * Verify if a session is valid
     * 
     * @param sessionId
     *            the session's id
     * @return true if the session is valid, false otherwise
     * @since 6.0
     */
    boolean isValid(long sessionId) throws SSessionNotFoundException;

    /**
     * Retrieve a session by its id
     * 
     * @param sessionId
     *            the session's id
     * @return the session associated to the given id
     * @throws SSessionNotFoundException
     *             if no session exists for the given id
     * @since 6.0
     */
    SPlatformSession getSession(long sessionId) throws SSessionNotFoundException;

    /**
     * Define how long new created sessions will be valid. This does not affect already created session
     * 
     * @param duration
     *            session's duration
     * @since 6.0
     */
    void setSessionDuration(long duration);

    /**
     * Retrieve the default sessions's duration
     * 
     * @return the default sessions's duration
     * @since 6.0
     */
    long getDefaultSessionDuration();

    /**
     * Retrieve the duration of new created sessions. If no duration was specified, the default duration will be used
     * 
     * @return the duration of new created sessions
     * @since 6.0
     */
    long getSessionsDuration();

    /**
     * Update the expiration and the last update dates of the session.
     * 
     * @param sessionId
     *            the session's id
     * @throws SSessionException
     * @since 6.0
     */
    void renewSession(long sessionId) throws SSessionException;

}
