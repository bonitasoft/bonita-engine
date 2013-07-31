package org.bonitasoft.engine.session;

import org.bonitasoft.engine.session.model.SSession;

public interface SessionProvider {

    public abstract void updateSession(SSession session) throws SSessionNotFoundException;

    public abstract void cleanInvalidSessions();

    public abstract void removeSessions();

    public abstract SSession getSession(final long sessionId) throws SSessionNotFoundException;

    public abstract void removeSession(final long sessionId) throws SSessionNotFoundException;

    public abstract void addSession(final SSession session) throws SSessionAlreadyExistsException, SSessionAlreadyExistsException;

}
