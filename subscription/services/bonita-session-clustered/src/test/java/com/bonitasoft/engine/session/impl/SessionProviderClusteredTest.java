/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.session.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        final Config config = new Config();
        // disable all networking
        final JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Test
    public void testAddSession() throws Exception {
        final SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.addSession(session);
        assertThat(sessionProviderClustered.getSession(123l)).isEqualTo(session);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSession() throws Exception {
        final SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
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
        final SSessionImpl session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        session.setLastRenewDate(new Date(System.currentTimeMillis() - 100));
        sessionProviderClustered.addSession(session);

        final SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "BPM", 12);
        final Date lastRenewDate = new Date(System.currentTimeMillis());
        sessionUpdated.setLastRenewDate(lastRenewDate);

        sessionProviderClustered.updateSession(sessionUpdated);
        assertThat(sessionProviderClustered.getSession(123l).getLastRenewDate()).isEqualTo(lastRenewDate);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testUpdateNoExistingSessionSession() throws Exception {
        final SSessionImpl sessionUpdated = new SSessionImpl(123l, 1, "john", "BPM", 12);
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
        final SSession session = new SSessionImpl(123l, 1, "john", "BPM", 12);
        sessionProviderClustered.addSession(session);

        sessionProviderClustered.removeSessions();

        // This call will throw an exception
        sessionProviderClustered.getSession(123l);
    }

    @Test
    public void testDeleteSessionsOfTenantKeepTechnical() throws Exception {
        sessionProviderClustered.removeSessions();
        sessionProviderClustered.addSession(new SSessionImpl(54, 3, "john", "TEST", 12));
        final SSessionImpl technicalSession = new SSessionImpl(55, 3, "technicalUser", "TEST", 13);
        technicalSession.setTechnicalUser(true);
        sessionProviderClustered.addSession(technicalSession);
        sessionProviderClustered.addSession(new SSessionImpl(56, 4, "john", "TEST", 14));
        sessionProviderClustered.deleteSessionsOfTenant(3, true);
        sessionProviderClustered.getSession(55);
        sessionProviderClustered.getSession(56);
        try {
            sessionProviderClustered.getSession(54);
            fail("session 54 should be deleted because it is on tenant 3");
        } catch (final SSessionNotFoundException e) {

        }
    }

    @Test
    public void testDeleteSessionsOfTenant() throws Exception {
        sessionProviderClustered.removeSessions();
        sessionProviderClustered.addSession(new SSessionImpl(54, 3, "john", "TEST", 12));
        final SSessionImpl technicalSession = new SSessionImpl(55, 3, "technicalUser", "TEST", 13);
        technicalSession.setTechnicalUser(true);
        sessionProviderClustered.addSession(technicalSession);
        sessionProviderClustered.addSession(new SSessionImpl(56, 4, "john", "TEST", 14));
        sessionProviderClustered.deleteSessionsOfTenant(3, false);
        sessionProviderClustered.getSession(56);
        try {
            sessionProviderClustered.getSession(55);
            fail("session 55 should be deleted because it is on tenant 3");
        } catch (final SSessionNotFoundException e) {

        }
        try {
            sessionProviderClustered.getSession(54);
            fail("session 54 should be deleted because it is on tenant 3");
        } catch (final SSessionNotFoundException e) {

        }
    }

}
