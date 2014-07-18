/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform.session.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.bonitasoft.engine.platform.session.model.impl.SPlatformSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

@RunWith(MockitoJUnitRunner.class)
public class PlatformSessionProviderClusteredTest {

    private Manager manager;

    private HazelcastInstance hazelcastInstance;

    private PlatformSessionProviderClustered sessionProviderClustered;

    private IMap<Long, SPlatformSession> map;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        manager = mock(Manager.class);
        hazelcastInstance = mock(HazelcastInstance.class);
        map = mock(IMap.class);
        when(hazelcastInstance.<Long, SPlatformSession> getMap(anyString())).thenReturn(map);
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);
        sessionProviderClustered = new PlatformSessionProviderClustered(manager, hazelcastInstance);
    }

    @Test
    public void testAddSession() throws Exception {
        SPlatformSessionImpl session = new SPlatformSessionImpl(123l, "john");
        sessionProviderClustered.addSession(session);
        verify(map).put(123l, session);
    }

    @Test
    public void testRemoveSession() throws Exception {
        SPlatformSessionImpl session = new SPlatformSessionImpl(123l, "john");
        when(map.remove(123l)).thenReturn(session);
        sessionProviderClustered.removeSession(123l);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSessionNotFound() throws Exception {
        sessionProviderClustered.removeSession(123l);
    }

    @Test
    public void testGetSession() throws Exception {
        SPlatformSessionImpl session = new SPlatformSessionImpl(123l, "john");
        when(map.get(123l)).thenReturn(session);
        assertEquals(session, sessionProviderClustered.getSession(123l));
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testGetSessionNotFound() throws Exception {
        sessionProviderClustered.getSession(123l);
    }

    @Test
    public void testUpdateSession() throws Exception {
        SPlatformSessionImpl session = new SPlatformSessionImpl(123l, "john");
        session.setLastRenewDate(new Date(System.currentTimeMillis() - 100));
        SPlatformSessionImpl sessionUpdated = new SPlatformSessionImpl(123l, "john");
        sessionUpdated.setLastRenewDate(new Date(System.currentTimeMillis()));
        when(map.containsKey(123l)).thenReturn(true);
        sessionProviderClustered.updateSession(sessionUpdated);
        verify(map).put(123l, sessionUpdated);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testUpdateNoExistingSessionSession() throws Exception {
        SPlatformSessionImpl sessionUpdated = new SPlatformSessionImpl(123l, "john");
        sessionProviderClustered.updateSession(sessionUpdated);
    }

}
