/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.session.impl;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bonitasoft.engine.session.SSessionAlreadyExistsException;
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.SSession;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Baptiste Mesta
 */
public final class SessionProviderClustered implements SessionProvider {

    private static final String SESSION_MAP = "SESSION_MAP";

    private final IMap<Long, SSession> sessions;

    public SessionProviderClustered(final HazelcastInstance hazelcastInstance) {
        this(Manager.getInstance(), hazelcastInstance);
    }

    public SessionProviderClustered(final Manager manager, final HazelcastInstance hazelcastInstance) {
        if (!manager.isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        // --- Hard coded configuration for Hazelcast
        MapConfig mapConfig = hazelcastInstance.getConfig().getMapConfig(SESSION_MAP);
        mapConfig.setTimeToLiveSeconds(0); // eternal. We specify the ttl when we put the session in the map.
        // ---
        sessions = hazelcastInstance.getMap(SESSION_MAP);
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

    @Override
    public void updateSession(final SSession session) throws SSessionNotFoundException {
        final long sessionId = session.getId();
        if (!sessions.containsKey(sessionId)) {
            throw new SSessionNotFoundException("No session found with id \"" + sessionId + "\"");
        }
        sessions.put(sessionId, session);
    }

    @Override
    public synchronized void cleanInvalidSessions() {
        // do nothing since sessions are cleaned whith a TTL
    }

    @Override
    public synchronized void removeSessions() {
        sessions.clear();
    }

    @Override
    public synchronized void deleteSessionsOfTenant(long tenantId) {
        Iterator<Entry<Long, SSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Long, SSession> sSession = iterator.next();
            if (tenantId == sSession.getValue().getTenantId()) {
                sessions.remove(sSession.getKey());
            }
        }
    }

}
