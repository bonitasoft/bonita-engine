/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.auth.impl.standard;

import static org.junit.Assert.fail;

import org.bonitasoft.console.common.server.auth.AuthenticationFailedException;
import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.credentials.StandardCredentials;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.test.toolkit.server.MockHttpServletRequest;
import org.bonitasoft.web.test.AbstractJUnitWebTest;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class StandardAuthenticationManagerImplIT extends AbstractJUnitWebTest {

    private static final String TECHNICAL_USER_USERNAME = "install";

    private static final String TECHNICAL_USER_PASSWORD = "install";

    @Override
    public void webTestSetUp() {
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void testLogin() {
        final HttpServletRequestAccessor request = new HttpServletRequestAccessor(new MockHttpServletRequest());
        try {
            new StandardAuthenticationManagerImpl()
                    .authenticate(request,
                            new StandardCredentials(TECHNICAL_USER_USERNAME, TECHNICAL_USER_PASSWORD));
        } catch (final AuthenticationFailedException e) {
            fail("Cannot login " + e);
        }

    }

}
