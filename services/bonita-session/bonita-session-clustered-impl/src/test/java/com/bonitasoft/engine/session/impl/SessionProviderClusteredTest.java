package com.bonitasoft.engine.session.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@RunWith(MockitoJUnitRunner.class)
public class SessionProviderClusteredTest {

    private Manager manager;

    private HazelcastInstance hazelcastInstance;

    private SessionProviderClustered sessionProviderClustered;

    @Before
    public void setup() {
        manager = mock(Manager.class);
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);

        hazelcastInstance = buildHazelcastInstance();
        sessionProviderClustered = new SessionProviderClustered(manager, hazelcastInstance);
    }

    private HazelcastInstance buildHazelcastInstance() {
        Config config = new Config();
        // disable all networking
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Test
    public void testAddSession() throws Exception {
        SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.addSession(session);
        assertThat(sessionProviderClustered.getSession(123l), is(session));
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSession() throws Exception {
        SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.addSession(session);
        sessionProviderClustered.removeSession(123l);

        // This call will throw an exception
        sessionProviderClustered.getSession(123l);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSessionNotFound() throws Exception {
        sessionProviderClustered.removeSession(123l);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testGetSessionNotFound() throws Exception {
        sessionProviderClustered.getSession(123l);
    }

    @Test
    public void testUpdateSession() throws Exception {
        SSessionImpl session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        session.setLastRenewDate(new Date(System.currentTimeMillis() - 100));
        sessionProviderClustered.addSession(session);

        SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "BPM", 12);
        Date lastRenewDate = new Date(System.currentTimeMillis());
        sessionUpdated.setLastRenewDate(lastRenewDate);

        sessionProviderClustered.updateSession(sessionUpdated);
        assertThat(sessionProviderClustered.getSession(123l).getLastRenewDate(), is(lastRenewDate));
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testUpdateNoExistingSessionSession() throws Exception {
        SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.updateSession(sessionUpdated);
    }

    @Ignore
    @Test
    public void testCleanInvalidSessions() {
        // verifyZeroInteractions(map);
        // How to test it ?
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSessions() throws Exception {
        SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.addSession(session);

        sessionProviderClustered.removeSessions();

        // This call will throw an exception
        sessionProviderClustered.getSession(123l);
    }

}
