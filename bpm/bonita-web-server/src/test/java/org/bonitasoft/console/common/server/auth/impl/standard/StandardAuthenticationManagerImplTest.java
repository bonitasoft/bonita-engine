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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class StandardAuthenticationManagerImplTest {

    private MockHttpServletRequest request;
    private HttpServletRequestAccessor requestAccessor;

    private StandardAuthenticationManagerImpl standardLoginManagerImpl = spy(new StandardAuthenticationManagerImpl());

    @Before
    public void setUp() throws Exception {
        request = new MockHttpServletRequest();
        request.setContextPath("bonita");

        requestAccessor = new HttpServletRequestAccessor(request);
    }

    @Test
    public void testGetSimpleLoginpageURL() throws Exception {
        String redirectUrl = "%2Fapps%2FappDirectoryBonita";

        String loginURL = standardLoginManagerImpl.getLoginPageURL(requestAccessor, redirectUrl);

        assertThat(loginURL).isEqualToIgnoringCase("bonita/login.jsp?redirectUrl=%2Fapps%2FappDirectoryBonita");
    }

    @Test
    public void testGetLoginpageURLWithLocale() throws Exception {
        String redirectUrl = "%2Fapps%2FappDirectoryBonita";
        request.setParameter("_l", "es");

        String loginURL = standardLoginManagerImpl.getLoginPageURL(requestAccessor, redirectUrl);

        assertThat(loginURL).isEqualToIgnoringCase("bonita/login.jsp?_l=es&redirectUrl=%2Fapps%2FappDirectoryBonita");
    }

    @Test
    public void testGetLoginpageURLFromPortal() throws Exception {
        String redirectUrl = "%2Fapps%2FappDirectoryBonita";
        request.setServletPath("/portal/");

        String loginURL = standardLoginManagerImpl.getLoginPageURL(requestAccessor, redirectUrl);

        assertThat(loginURL).isEqualToIgnoringCase("bonita/login.jsp?redirectUrl=%2Fapps%2FappDirectoryBonita");
    }
}
