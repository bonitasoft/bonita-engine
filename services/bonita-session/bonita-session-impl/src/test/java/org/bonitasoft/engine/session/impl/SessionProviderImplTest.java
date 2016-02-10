/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
    public void testDeleteSessionsOfTenantKeepTechnical() throws Exception {
        sessionProvider.removeSessions();
        sessionProvider.addSession(new SSessionImpl(54, 3, "john", "TEST", 12));
        SSessionImpl technicalSession = new SSessionImpl(55, 3, "technicalUser", "TEST", 13);
        technicalSession.setTechnicalUser(true);
        sessionProvider.addSession(technicalSession);
        sessionProvider.addSession(new SSessionImpl(56, 4, "john", "TEST", 14));
        sessionProvider.deleteSessionsOfTenant(3, true /* keep technical sessions */);
        sessionProvider.getSession(55);
        sessionProvider.getSession(56);
        try {
            sessionProvider.getSession(54);
            fail("session 54 should be deleted because it is on tenant 3");
        } catch (SSessionNotFoundException e) {

        }
    }

    @Test
    public void testDeleteSessionsOfTenant() throws Exception {
        sessionProvider.removeSessions();
        sessionProvider.addSession(new SSessionImpl(54, 3, "john", "TEST", 12));
        SSessionImpl technicalSession = new SSessionImpl(55, 3, "technicalUser", "TEST", 13);
        technicalSession.setTechnicalUser(true);
        sessionProvider.addSession(technicalSession);
        sessionProvider.addSession(new SSessionImpl(56, 4, "john", "TEST", 14));
        sessionProvider.deleteSessionsOfTenant(3, false /* keep technical sessions */);
        sessionProvider.getSession(56);
        try {
            sessionProvider.getSession(55);
            fail("session 55 should be deleted because it is on tenant 3");
        } catch (SSessionNotFoundException e) {

        }
        try {
            sessionProvider.getSession(54);
            fail("session 54 should be deleted because it is on tenant 3");
        } catch (SSessionNotFoundException e) {

        }
    }

}
