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

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.session.impl.SessionServiceImpl;
import org.bonitasoft.engine.session.model.builder.SSessionBuilders;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 * 
 */
public class SessionServiceImplTest {

    @Mock
    private SSessionBuilders sessionModelBuilder;

    @Mock
    private PlatformService platformService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private SessionServiceImpl sessionServiceImpl;

    @Before
    public void setUp() throws Exception {
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
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#getSession(long)}.
     */
    @Test
    public final void getSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#createSession(long, java.lang.String)}.
     */
    @Test
    public final void createSessionByTenantIdAndUsername() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#createSession(long, long, java.lang.String, boolean)}.
     */
    @Test
    public final void createSessionByTenantIdAndUserIdAndUsernameAndIsTechnicalUser() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#deleteSession(long)}.
     */
    @Test
    public final void deleteSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#isValid(long)}.
     */
    @Test
    public final void isValid() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#isAllowed(long, java.lang.String)}.
     */
    @Test
    public final void isAllowed() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#setSessionDuration(long)}.
     */
    @Test
    public final void setSessionDuration() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#renewSession(long)}.
     */
    @Test
    public final void renewSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#cleanInvalidSessions()}.
     */
    @Test
    public final void cleanInvalidSessions() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.session.impl.SessionServiceImpl#deleteSessions()}.
     */
    @Test
    public final void deleteSessions() {
        // TODO : Not yet implemented
    }

}
