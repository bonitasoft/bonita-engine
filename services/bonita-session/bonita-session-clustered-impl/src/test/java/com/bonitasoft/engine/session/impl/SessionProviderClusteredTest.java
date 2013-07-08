package com.bonitasoft.engine.session.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@RunWith(MockitoJUnitRunner.class)
public class SessionProviderClusteredTest {

    private Manager manager;

    private HazelcastInstance hazelcastInstance;

    private SessionProviderClustered sessionProviderClustered;

    private IMap<Long, SSession> map;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        manager = mock(Manager.class);
        hazelcastInstance = mock(HazelcastInstance.class);
        map = mock(IMap.class);
        when(hazelcastInstance.<Long, SSession> getMap(anyString())).thenReturn(map);
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);
        sessionProviderClustered = new SessionProviderClustered(manager, hazelcastInstance);
    }

    @Test
    public void testAddSession() throws Exception {
        SSessionImpl session = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        sessionProviderClustered.addSession(session);
        verify(map).put(123l, session);
    }

    @Test
    public void testRemoveSession() throws Exception {
        SSessionImpl session = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        when(map.remove(123l)).thenReturn(session);
        sessionProviderClustered.removeSession(123l);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSessionNotFound() throws Exception {
        sessionProviderClustered.removeSession(123l);
    }

    @Test
    public void testGetSession() throws Exception {
        SSessionImpl session = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        when(map.get(123l)).thenReturn(session);
        assertEquals(session, sessionProviderClustered.getSession(123l));
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testGetSessionNotFound() throws Exception {
        sessionProviderClustered.getSession(123l);
    }

    @Test
    public void testUpdateSession() throws Exception {
        SSessionImpl session = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        session.setLastRenewDate(new Date(System.currentTimeMillis() - 100));
        SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        sessionUpdated.setLastRenewDate(new Date(System.currentTimeMillis()));
        when(map.containsKey(123l)).thenReturn(true);
        sessionProviderClustered.updateSession(sessionUpdated);
        verify(map).put(123l, sessionUpdated);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testUpdateNoExistingSessionSession() throws Exception {
        SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "6.0", "BPM", 12);
        sessionProviderClustered.updateSession(sessionUpdated);
    }

    @Test
    public void testCleanInvalidSessions() throws Exception {
        verifyZeroInteractions(map);

    }

    @Test
    public void testRemoveSessions() throws Exception {
        sessionProviderClustered.removeSessions();
        verify(map).clear();
    }

}
