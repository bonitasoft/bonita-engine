package org.bonitasoft.engine.session.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.impl.SSessionImpl;
import org.junit.Test;

public class SessionProviderImplTest {

    private final SessionProvider sessionProvider = new SessionProviderImpl();

    @Test
    public void testAddSession() throws Exception {
        sessionProvider.addSession(new SSessionImpl(12, 2, "john", "TEST", 12));
        assertNotNull(sessionProvider.getSession(12));
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testRemoveSession() throws Exception {
        sessionProvider.addSession(new SSessionImpl(13, 2, "john", "TEST", 12));
        sessionProvider.removeSession(12);
        sessionProvider.getSession(12);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testGetUnexistingSession() throws Exception {
        sessionProvider.getSession(10l);
    }

    @Test
    public void testDeleteSessionsOfTenant() throws Exception {
        sessionProvider.addSession(new SSessionImpl(54, 3, "john", "TEST", 12));
        sessionProvider.addSession(new SSessionImpl(55, 4, "john", "TEST", 12));
        sessionProvider.deleteSessionsOfTenant(3);
        sessionProvider.getSession(55);
        try {
            sessionProvider.getSession(54);
            fail("session 54 should be deleted because it is on tenant 3");
        } catch (SSessionNotFoundException e) {

        }

    }

}
