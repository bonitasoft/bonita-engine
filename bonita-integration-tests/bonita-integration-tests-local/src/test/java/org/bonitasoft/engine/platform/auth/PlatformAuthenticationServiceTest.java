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
package org.bonitasoft.engine.platform.auth;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.platform.authentication.PlatformAuthenticationService;
import org.bonitasoft.engine.platform.authentication.SInvalidPasswordException;
import org.bonitasoft.engine.platform.authentication.SInvalidUserException;
import org.junit.Test;

public class PlatformAuthenticationServiceTest extends CommonBPMServicesTest {

    private PlatformAuthenticationService platformAuthService;

    public PlatformAuthenticationServiceTest() {
        platformAuthService = getPlatformAccessor().getPlatformAuthenticationService();
    }

    @Test
    public void testCheckValidUser() throws Exception {
        final String username = "platformAdmin";
        final String password = "platform";

        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, password);
        //getTransactionService().complete();
    }

    @Test(expected = SInvalidPasswordException.class)
    public void testCheckUserWithWrongPassword() throws Exception {
        final String username = "platformAdmin";
        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, "wrong");
        //getTransactionService().complete();
    }

    @Test(expected = SInvalidUserException.class)
    public void testCheckNonExistentUser() throws Exception {
        final String username = "anonyme";
        final String password = "bpm";
        //getTransactionService().begin();
        platformAuthService.checkUserCredentials(username, password);
        //getTransactionService().complete();

    }
}
