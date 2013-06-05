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
package org.bonitasoft.engine.platform.session.impl.test;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl;
import org.bonitasoft.engine.platform.session.model.builder.SPlatformSessionModelBuilder;
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
public class PlatformSessionServiceImplTest {

    @Mock
    private SPlatformSessionModelBuilder platformSessionModelBuilder;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private PlatformSessionServiceImpl platformLoginServiceImpl;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#getDefaultSessionDuration()}.
     */
    @Test
    public final void getDefaultSessionDuration() {
        assertEquals(3600000, platformLoginServiceImpl.getDefaultSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#getSessionsDuration()}.
     */
    @Test
    public final void getSessionsDuration() {
        assertEquals(3600000, platformLoginServiceImpl.getDefaultSessionDuration());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#createSession(java.lang.String)}.
     */
    @Test
    public final void createSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#deleteSession(long)}.
     */
    @Test
    public final void deleteSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#isValid(long)}.
     */
    @Test
    public final void isValid() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#setSessionDuration(long)}.
     */
    @Test
    public final void setSessionDuration() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#getSession(long)}.
     */
    @Test
    public final void getSession() {
        // TODO : Not yet implemented
    }

    /**
     * Test method for {@link org.bonitasoft.engine.platform.session.impl.PlatformSessionServiceImpl#renewSession(long)}.
     */
    @Test
    public final void renewSession() {
        // TODO : Not yet implemented
    }

}
