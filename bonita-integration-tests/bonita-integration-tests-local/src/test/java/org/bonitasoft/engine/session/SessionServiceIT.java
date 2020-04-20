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
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros, Yanyan Liu
 */
public class SessionServiceIT extends CommonBPMServicesTest {

    private SessionService sessionService;

    @Before
    public void setup() {
        sessionService = getTenantAccessor().getSessionService();
        sessionService.setSessionDuration(sessionService.getDefaultSessionDuration());
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());
        getSessionAccessor().deleteSessionId();
    }

    @Test
    public void testSessionUserIdForUnknownUser() throws Exception {
        final String username = "DonaldDuck";
        final SSession session = createSession(username);
        assertNotNull(session);
        assertEquals(-1, session.getUserId());
    }

    @Test
    public void testCreateSession() throws Exception {
        final String username = "john";
        final Date before = new Date();
        final SSession session = createSession(username);
        assertNotNull(session);
        assertNotNull(session.getCreationDate());
        assertTrue(before.getTime() <= session.getCreationDate().getTime());
        assertTrue(session.getDuration() > 0);
        assertEquals(sessionService.getSessionDuration(), session.getDuration());
        assertEquals(session.getLastRenewDate().getTime() + session.getDuration(),
                session.getExpirationDate().getTime());
        assertEquals(getDefaultTenantId(), session.getTenantId());
        assertEquals(username, session.getUserName());
    }

    private SSession createSession(final String username) throws SBonitaException {
        return createSession(username, getDefaultTenantId());
    }

    private SSession createSession(final String username, final long tenantId) throws SBonitaException {
        getTransactionService().begin();
        final SSession session = sessionService.createSession(tenantId, username);
        getTransactionService().complete();
        return session;
    }

    @Test
    public void testIsValid() throws Exception {
        final String username = "john";
        sessionService.setSessionDuration(1000);
        final SSession session = createSession(username);
        assertNotNull(session);
        assertTrue(sessionService.isValid(session.getId()));
        Thread.sleep(session.getDuration() + 1);
        assertFalse(sessionService.isValid(session.getId()));
    }

    @Test
    public void testRenewSession() throws Exception {
        final String username = "matti";
        final SSession session = createSession(username);
        Thread.sleep(10);
        // getTransactionService().begin();
        sessionService.renewSession(session.getId());
        // getTransactionService().complete();

        // getTransactionService().begin();
        final SSession session2 = sessionService.getSession(session.getId());
        // getTransactionService().complete();
        assertTrue(session2.getExpirationDate().after(session.getExpirationDate()));
        assertTrue(session2.getLastRenewDate().after(session.getLastRenewDate()));
        assertEquals(session2.getLastRenewDate().getTime() + session2.getDuration(),
                session2.getExpirationDate().getTime());
    }

    @Test
    public void testGetSession() throws Exception {
        final String username = "john";
        final SSession session = createSession(username);
        assertNotNull(session);
        final SSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testCleanInvalidSessions() throws Exception {
        final String username = "john";
        sessionService.setSessionDuration(1);
        final SSession invalidSession = createSession(username);
        assertNotNull(invalidSession);
        // the session will expires
        Thread.sleep(10);
        sessionService.cleanInvalidSessions();
        // throw exception
        sessionService.getSession(invalidSession.getId());
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteSession() throws Exception {
        final String username = "john";
        final SSession session = createSession(username);
        assertNotNull(session);
        SSession retrievedSession = sessionService.getSession(session.getId());
        assertEquals(session, retrievedSession);
        sessionService.deleteSession(session.getId());
        try {
            sessionService.getSession(session.getId());
        } finally {
            // restore deleted session:
            createSession(username, getDefaultTenantId());
        }
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testDeleteWrongSession() throws Exception {
        sessionService.deleteSession(System.currentTimeMillis());
    }
}
