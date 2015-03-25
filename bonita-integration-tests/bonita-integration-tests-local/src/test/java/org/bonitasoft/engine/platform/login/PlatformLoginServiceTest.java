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
package org.bonitasoft.engine.platform.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.core.platform.login.PlatformLoginService;
import org.bonitasoft.engine.core.platform.login.SPlatformLoginException;
import org.bonitasoft.engine.platform.session.SSessionNotFoundException;
import org.bonitasoft.engine.platform.session.model.SPlatformSession;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
public class PlatformLoginServiceTest extends CommonBPMServicesTest {

    private final PlatformLoginService platformLoginService;

    public PlatformLoginServiceTest() {
        platformLoginService = getPlatformAccessor().getPlatformLoginService();
    }

    @Test
    public void testLoginLogout() throws Exception {
        final String username = "platformAdmin";
        final String password = "platform";
        final SPlatformSession session = platformLoginService.login(username, password);
        assertNotNull(session);
        assertEquals(username, session.getUserName());

        platformLoginService.logout(session.getId());
    }

    @Test(expected = SPlatformLoginException.class)
    public void testLoginBadUser() throws Exception {
        final String username = "noAdmin";
        final String password = "platform";
        platformLoginService.login(username, password);
    }

    @Test(expected = SPlatformLoginException.class)
    public void testLoginBadPassword() throws Exception {
        final String username = "platformAdmin";
        final String password = "wrong";
        platformLoginService.login(username, password);
    }

    @Test(expected = SSessionNotFoundException.class)
    public void testLogoutWrongSession() throws Exception {
        platformLoginService.logout(System.currentTimeMillis());
    }

}
