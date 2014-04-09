/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform.session.impl;

import org.bonitasoft.engine.platform.session.PlatformSessionProvider;
import org.bonitasoft.engine.platform.session.SSessionAlreadyExistsException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Baptiste Mesta
 */
public final class PlatformSessionProviderClustered implements PlatformSessionProvider {

    private static final String SESSION_MAP = "SESSION_MAP_PLATFORM";

    private final IMap<Long, SPlatformSession> sessions;

    public PlatformSessionProviderClustered(final HazelcastInstance hazelcastInstance) {
        this(Manager.getInstance(), hazelcastInstance);
    }

    public PlatformSessionProviderClustered(final Manager manager, final HazelcastInstance hazelcastInstance) {
        if (!manager.isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        sessions = hazelcastInstance.getMap(SESSION_MAP);
    }

    @Override
    public synchronized void addSession(final SPlatformSession session) throws SSessionAlreadyExistsException {
        final long id = session.getId();
        if (sessions.containsKey(id)) {
            throw new SSessionAlreadyExistsException("A session wih id \"" + id + "\" already exists");
        }
        sessions.put(id, session);
    }

    @Override
    public void removeSession(final long sessionId) throws SSessionNotFoundException {
        final SPlatformSession session = sessions.remove(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
    }

    @Override
    public SPlatformSession getSession(final long sessionId) throws SSessionNotFoundException {
        final SPlatformSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        return session;
    }

    @Override
    public void updateSession(final SPlatformSession session) throws SSessionNotFoundException {
        final long id = session.getId();
        if (!sessions.containsKey(id)) {
            throw new SSessionNotFoundException("No session found with id \"" + id + "\"");
        }
        sessions.put(id, session);
    }

}
