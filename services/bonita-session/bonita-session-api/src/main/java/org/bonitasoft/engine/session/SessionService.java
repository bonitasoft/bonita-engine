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
package org.bonitasoft.engine.session;

import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

/**
 * @author Elias Ricken de Medeiros
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface SessionService {

    String SYSTEM = "system";

    /**
     * Create a new session for the given user;
     *
     * @param tenantId
     * @param userName
     *            userName
     * @return a new session
     * @throws SSessionException
     *             if some error arrives while creating the session
     * @since 6.0
     */
    SSession createSession(long tenantId, String userName) throws SSessionException;

    SSession createSession(long tenantId, long userId, String userName, boolean technicalUser) throws SSessionException;

    /**
     * Delete a session having the given id
     *
     * @param sessionId
     *            the session's id
     * @throws SSessionNotFoundException
     *             if no session exists for the given id
     * @since 6.0
     */
    void deleteSession(final long sessionId) throws SSessionNotFoundException;

    /**
     * Delete all invalid sessions
     *
     * @since 6.0
     */
    void cleanInvalidSessions();

    /**
     * Verify if a session is valid
     *
     * @param sessionId
     *            the session's id
     * @return true if the session is valid, false otherwise
     * @throws SSessionNotFoundException
     *             if no session exists for the given id
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
    SSession getSession(long sessionId) throws SSessionNotFoundException;

    /**
     *
     * @param sessionAccessor
     *          the sessionAccessor that contains the current session
     * @return the logged user or -1 if there is no session
     * @since 6.4
     */
    long getLoggedUserFromSession(ReadSessionAccessor sessionAccessor);

    /**
     * Define how long, in milliseconds, the created sessions will be valid. This does not affect already created session
     *
     * @param duration
     * @since 6.0
     */
    void setSessionDuration(long duration);

    /**
     * Retrieve the default sessions's duration, in milliseconds.
     *
     * @return the default sessions's duration
     * @since 6.0
     */
    long getDefaultSessionDuration();

    /**
     * Retrieve the duration, in milliseconds, of new created session. If no duration was specified, the default duration will be used
     *
     * @return the duration of new created session.
     * @since 6.0
     */
    long getSessionDuration();

    /**
     * Update the expiration and the last update dates of the session.
     *
     * @param sessionId
     *            the session id
     * @throws SSessionException
     *             if some error arrives while creating the session
     * @since 6.0
     */
    void renewSession(long sessionId) throws SSessionException;

    /**
     * Deletes all the sessions.
     */
    void deleteSessions();

    /**
     * Delete all sessions of a tenant
     *
     * @param tenantId
     */
    void deleteSessionsOfTenant(long tenantId);

    /**
     * Delete all sessions of a tenant except the one of the technical user
     *
     * @param tenantId
     */
    void deleteSessionsOfTenantExceptTechnicalUser(long tenantId);

}
