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
package org.bonitasoft.engine.platform.authentication.impl;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Celine Souchet
 * 
 */
public class PlatformAuthenticationServiceImplTest {

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private PlatformAuthenticationServiceImpl platformAuthenticationServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.platform.authentication.impl.PlatformAuthenticationServiceImpl#checkUserCredentials(java.lang.String, java.lang.String)}.
     */
    @Test
    public final void checkUserCredentials() throws SInvalidUserException, SInvalidPasswordException {
        platformAuthenticationServiceImpl.checkUserCredentials("platformAdmin", "platform");
    }

    @Test(expected = SInvalidUserException.class)
    public final void checkUserCredentialsWithBadUserName() throws SInvalidUserException, SInvalidPasswordException {
        platformAuthenticationServiceImpl.checkUserCredentials("plop", "platform");
    }

    @Test(expected = SInvalidPasswordException.class)
    public final void checkUserCredentialsWithBadPassword() throws SInvalidUserException, SInvalidPasswordException {
        platformAuthenticationServiceImpl.checkUserCredentials("platformAdmin", "plop");
    }

}
