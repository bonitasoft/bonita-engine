/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.session.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.session.impl.AbstractSessionProvider;
import org.bonitasoft.engine.session.model.SSession;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Baptiste Mesta
 */
public final class SessionProviderClustered extends AbstractSessionProvider {

    static final String SESSION_MAP = "SESSION_MAP";

    private final IMap<Long, SSession> sessions;

    public SessionProviderClustered(final HazelcastInstance hazelcastInstance) {
        this(Manager.getInstance(), hazelcastInstance);
    }

    SessionProviderClustered(final Manager manager, final HazelcastInstance hazelcastInstance) {
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
    public synchronized void cleanInvalidSessions() {
        // do nothing since sessions are cleaned whith a TTL
    }

    @Override
    protected Map<Long, SSession> getSessions() {
        return sessions;
    }

    @Override
    protected SSession putSession(final SSession session, final long id) {
        return sessions.put(id, session, session.getDuration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public final synchronized void deleteSessionsOfTenant(final long tenantId, final boolean keepTechnicalSessions) {
        Iterator<Entry<Long, SSession>> iterator = getSessions().entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Long, SSession> entry = iterator.next();
            SSession session = entry.getValue();
            if (tenantId == session.getTenantId() && (!keepTechnicalSessions || !session.isTechnicalUser())) {
                getSessions().remove(entry.getKey());
            }
        }
    }
}
