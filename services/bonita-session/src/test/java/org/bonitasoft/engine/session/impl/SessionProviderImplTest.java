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
package org.bonitasoft.engine.session.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionProvider;
import org.bonitasoft.engine.session.model.SSession;
import org.junit.Before;
import org.junit.Test;

public class SessionProviderImplTest {

    private final SessionProvider sessionProvider = new SessionProviderImpl();

    @Before
    public void cleanSession() {
        //session provider have a static map of sessions
        sessionProvider.removeSessions();
    }

    @Test
    public void testAddSession() throws Exception {
        sessionProvider.addSession(SSession.builder().id(12L).userName("john").userId(12).build());
        assertNotNull(sessionProvider.getSession(12));
    }

    @Test
    public void testRemoveSession() throws Exception {
        sessionProvider.addSession(SSession.builder().id(12L).userName("john").userId(12).build());
        sessionProvider.removeSession(12L);
        assertThatThrownBy(() -> sessionProvider.getSession(12L)).isInstanceOf(SSessionNotFoundException.class);
    }

    @Test
    public void get_non_existing_session_should_throw_exception() {
        assertThatThrownBy(() -> sessionProvider.getSession(10L)).isInstanceOf(SSessionNotFoundException.class);
    }

    @Test
    public void should_deleteSessions_keep_technical_sessions() throws Exception {
        sessionProvider.removeSessions();
        sessionProvider.addSession(SSession.builder().id(54L).userName("john").userId(12).build());
        sessionProvider.addSession(SSession.builder().id(55L).userName("john").userId(12).technicalUser(true).build());
        sessionProvider.deleteSessions(true /* keep technical sessions */);
        sessionProvider.getSession(55);
        assertThatThrownBy(() -> sessionProvider.getSession(54L)).isInstanceOf(SSessionNotFoundException.class);
    }

    @Test
    public void should_delete_all_sessions() throws Exception {
        sessionProvider.removeSessions();
        sessionProvider.addSession(SSession.builder().id(55L).userName("tech").userId(13).technicalUser(true).build());
        sessionProvider.addSession(SSession.builder().id(56L).userName("john").userId(14).build());
        sessionProvider.deleteSessions(false /* keep technical sessions */);

        assertThatThrownBy(() -> sessionProvider.getSession(55L)).isInstanceOf(SSessionNotFoundException.class);
        assertThatThrownBy(() -> sessionProvider.getSession(56L)).isInstanceOf(SSessionNotFoundException.class);
    }

}
