/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.identity;

import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.LoginException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.AccessorUtil;
import org.bonitasoft.engine.util.PlatformAccessorUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class UserTestSP {

    private static final String DEFAULT_TENANT = "default";

    @BeforeClass
    public static void beforeClass() throws BonitaException, BonitaHomeNotSetException {
        final PlatformLoginAPI platformLoginAPI = PlatformAccessorUtil.getPlatformLoginAPI();
        PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAccessorUtil.getPlatformAPI(session);
        platformAPI.createPlatform();
        platformAPI.startPlatform();
        platformAPI.createTenant(DEFAULT_TENANT);
        platformAPI.activateTenant(DEFAULT_TENANT);
        platformAPI.createDefaultUsers(DEFAULT_TENANT);

        platformLoginAPI.logout(session);
    }

    @AfterClass
    public static void afterClass() throws BonitaException, BonitaHomeNotSetException {
        final PlatformLoginAPI platformLoginAPI = PlatformAccessorUtil.getPlatformLoginAPI();
        PlatformSession session = platformLoginAPI.login("platformAdmin", "platform");
        final PlatformAPI platformAPI = PlatformAccessorUtil.getPlatformAPI(session);
        platformAPI.deactiveTenant(DEFAULT_TENANT);
        platformAPI.stopPlatform();
        platformAPI.deleteTenant(DEFAULT_TENANT);
        platformAPI.deletePlaftorm();
        platformLoginAPI.logout(session);

    }

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongUser() throws BonitaException, BonitaHomeNotSetException {
        final String userName = "hannu";
        final String password = "revontuli";
        final LoginAPI loginAPI = AccessorUtil.getLoginAPI();
        loginAPI.login(DEFAULT_TENANT, userName, password);
    }

    @Test(expected = LoginException.class)
    public void loginFailsUsingWrongPassword() throws BonitaException, BonitaHomeNotSetException {
        final String userName = "matti";
        final String password = "suomi";
        final LoginAPI loginAPI = AccessorUtil.getLoginAPI();
        loginAPI.login(DEFAULT_TENANT, userName, password);
    }

}
