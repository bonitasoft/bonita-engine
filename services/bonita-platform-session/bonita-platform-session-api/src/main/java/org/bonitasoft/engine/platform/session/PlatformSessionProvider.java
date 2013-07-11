package org.bonitasoft.engine.platform.session;

import org.bonitasoft.engine.platform.session.model.SPlatformSession;

public interface PlatformSessionProvider {

    public abstract void addSession(SPlatformSession session) throws SSessionAlreadyExistsException;

    public abstract void removeSession(long sessionId) throws SSessionNotFoundException;

    public abstract SPlatformSession getSession(long sessionId) throws SSessionNotFoundException;

    public abstract void updateSession(SPlatformSession session) throws SSessionNotFoundException;

}
