/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.SessionProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 */
public class SessionServiceImplTest {

    @Mock
    private PlatformService platformService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SessionProvider sessionProvider;

    @InjectMocks
    private SessionServiceImpl sessionServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#getDefaultSessionDuration()}.
     */
    @Test
    public final void getDefaultSessionDuration() {
        assertEquals(3600000, sessionServiceImpl.getDefaultSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#getSessionDuration()}.
     */
    @Test
    public final void getSessionDuration() {
        assertEquals(3600000, sessionServiceImpl.getSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#cleanInvalidSessions()}.
     */
    @Test
    public final void cleanInvalidSessions() {
        sessionServiceImpl.cleanInvalidSessions();
        verify(sessionProvider, times(1)).cleanInvalidSessions();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#deleteSessions()}.
     */
    @Test
    public final void deleteSessions() {
        sessionServiceImpl.deleteSessions();
        verify(sessionProvider, times(1)).removeSessions();
    }

    @Test
    public final void deleteSessionsOfTenant() {
        sessionServiceImpl.deleteSessionsOfTenant(12l);
        verify(sessionProvider, times(1)).deleteSessionsOfTenant(12l, false);
    }

    @Test
    public final void deleteSessionsOfTenantExceptTechnicalUser() {
        sessionServiceImpl.deleteSessionsOfTenantExceptTechnicalUser(12l);
        verify(sessionProvider, times(1)).deleteSessionsOfTenant(12l, true);
    }
}
