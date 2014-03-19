package org.bonitasoft.engine.session;

import org.bonitasoft.engine.session.model.SSession;

public interface SessionProvider {

    void updateSession(SSession session) throws SSessionNotFoundException;

    void cleanInvalidSessions();

    void removeSessions();

    SSession getSession(final long sessionId) throws SSessionNotFoundException;

    void removeSession(final long sessionId) throws SSessionNotFoundException;

    void addSession(final SSession session) throws SSessionAlreadyExistsException, SSessionAlreadyExistsException;

    void deleteSessionsOfTenant(long tenantId, boolean keepTechnicalSessions);

}
