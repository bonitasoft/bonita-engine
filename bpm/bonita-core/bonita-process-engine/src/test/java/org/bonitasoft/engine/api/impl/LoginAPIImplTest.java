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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.core.login.TechnicalUser;
import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.model.STenant;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class LoginAPIImplTest {

    private STenant sTenant = new STenant();

    private LoginAPIImpl loginAPI = new LoginAPIImpl();

    @Test
    public void checkThatWeCanLogin_should_allow_technical_user() throws Exception {
        //given
        sTenant.setStatus(STenant.PAUSED);

        //expected no exception
        loginAPI.checkThatWeCanLogin("install", sTenant, new TechnicalUser("install", "install"));

    }

    @Test(expected = LoginException.class)
    public void checkThatWeCanLogin_should_throw_exception_when_tenant_is_not_activated() throws Exception {
        //given
        sTenant.setStatus(STenant.DEACTIVATED);

        //expected LoginException
        loginAPI.checkThatWeCanLogin("joe", sTenant, new TechnicalUser("techUser", "techPass"));

    }

    @Test(expected = TenantStatusException.class)
    public void checkThatWeCanLogin_should_refuse_non_technical_user() throws Exception {
        //given
        sTenant.setStatus(STenant.PAUSED);

        LoginAPIImpl loginAPI = new LoginAPIImpl();

        //expected TenantStatusException
        loginAPI.checkThatWeCanLogin("joe", sTenant, new TechnicalUser("techUser", "techPass"));
    }

}
