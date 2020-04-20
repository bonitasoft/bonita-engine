/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.session;

import static org.junit.Assert.*;

import java.util.Date;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.platform.session.PlatformSessionService;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class PlatformSessionServiceIT extends CommonBPMServicesTest {

    private static PlatformSessionService sessionService;

    @Before
    public void before() {
        sessionService = getPlatformAccessor().getPlatformSessionService();
    }

    @Test
    public void testCreateSession() throws Exception {
        final String username = "platformAdmin";
        final Date before = new Date();
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        assertNotNull(session.getCreationDate());
        assertTrue(before.getTime() <= session.getCreationDate().getTime());
        assertTrue(session.getDuration() > 0);
        assertEquals(sessionService.getSessionsDuration(), session.getDuration());
        assertEquals(session.getLastRenewDate().getTime() + session.getDuration(),
                session.getExpirationDate().getTime());
        assertEquals(username, session.getUserName());
    }

    @Test
    public void testIsValid() throws Exception {
        final String username = "platformAdmin";
        long sessionsDuration = sessionService.getSessionsDuration();
        try {
            sessionService.setSessionDuration(1000);
            final SPlatformSession session = sessionService.createSession(username);
            assertNotNull(session);
            assertEquals(1000, session.getDuration());
            assertTrue(sessionService.isValid(session.getId()));
            Thread.sleep(session.getDuration() + 1);
            assertFalse(sessionService.isValid(session.getId()));
        } finally {
            sessionService.setSessionDuration(sessionsDuration);
        }
    }

    @Test
    public void testGetSession() throws Exception {
        final String username = "platformAdmin";
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        final SPlatformSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteInvalidSession() throws Exception {
        sessionService.deleteSession(System.currentTimeMillis());
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteSession() throws Exception {
        final String username = "platformAdmin";
        final SPlatformSession session = sessionService.createSession(username);
        assertNotNull(session);
        SPlatformSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);

        sessionService.deleteSession(session.getId());
        sessionService.getSession(session.getId());
    }

    @Test
    public void testrenewSession() throws Exception {
        final String username = "matti";
        final SPlatformSession session = sessionService.createSession(username);
        Thread.sleep(100);

        sessionService.renewSession(session.getId());

        final SPlatformSession session2 = sessionService.getSession(session.getId());
        assertTrue(session2.getExpirationDate().after(session.getExpirationDate()));
        assertTrue(session2.getLastRenewDate().after(session.getLastRenewDate()));
        assertEquals(session2.getLastRenewDate().getTime() + session2.getDuration(),
                session2.getExpirationDate().getTime());
    }

}
