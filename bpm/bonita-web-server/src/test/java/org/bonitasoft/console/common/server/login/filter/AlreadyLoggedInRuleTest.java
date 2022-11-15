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
package org.bonitasoft.console.common.server.login.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.servlet.PlatformLoginServlet;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.web.rest.model.user.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Created by Vincent Elcrin
 * Date: 30/08/13
 * Time: 15:00
 */
public class AlreadyLoggedInRuleTest {

    @Mock
    private APISession apiSession;

    @Mock
    private HttpSession httpSession;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse response;

    @Spy
    AlreadyLoggedInRule rule;

    private HttpServletRequestAccessor request;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(httpSession).when(httpServletRequest).getSession();
        request = new HttpServletRequestAccessor(httpServletRequest);
    }

    @Test
    public void testIfRuleAuthorizeAlreadyLoggedUser() throws Exception {
        doReturn("/apps").when(httpServletRequest).getServletPath();
        doReturn("/app/home").when(httpServletRequest).getPathInfo();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        // ensure we won't recreate user session
        doReturn("").when(httpSession).getAttribute(SessionUtil.USER_SESSION_PARAM_KEY);

        final boolean authorization = rule.doAuthorize(request, response);

        assertThat(authorization, is(true));
    }

    @Test
    public void testIfRuleAuthorizeAlreadyLoggedUserWhithNullPathInfo() throws Exception {
        doReturn("/apps").when(httpServletRequest).getServletPath();
        doReturn(null).when(httpServletRequest).getPathInfo();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        // ensure we won't recreate user session
        doReturn("").when(httpSession).getAttribute(SessionUtil.USER_SESSION_PARAM_KEY);

        final boolean authorization = rule.doAuthorize(request, response);

        assertThat(authorization, is(true));
    }

    @Test
    public void testIfRuleDoesntAuthorizeNullSession() throws Exception {
        doReturn("/apps").when(httpServletRequest).getServletPath();
        doReturn("/app/home").when(httpServletRequest).getPathInfo();

        final boolean authorization = rule.doAuthorize(request, response);

        assertFalse(authorization);
    }

    @Test
    public void testIfRuleAuthorizeAlreadyLoggedUserIfPlatform() throws Exception {
        doReturn("/API").when(httpServletRequest).getServletPath();
        doReturn("/platform/tenant/unusedId").when(httpServletRequest).getPathInfo();
        doReturn(mock(PlatformSession.class)).when(httpSession)
                .getAttribute(PlatformLoginServlet.PLATFORM_SESSION_PARAM_KEY);

        final boolean authorization = rule.doAuthorize(request, response);

        assertThat(authorization, is(true));
    }

    @Test
    public void testIfUserSessionIsRecreatedWhenMissing() throws Exception {
        doReturn("/apps").when(httpServletRequest).getServletPath();
        doReturn("/app/home").when(httpServletRequest).getPathInfo();
        doReturn(apiSession).when(httpSession).getAttribute(SessionUtil.API_SESSION_PARAM_KEY);

        doReturn(null).when(httpSession).getAttribute(SessionUtil.USER_SESSION_PARAM_KEY);
        // configure user that will be created
        doReturn(new Locale("en")).when(httpServletRequest).getLocale();
        doReturn("myUser").when(apiSession).getUserName();

        rule.doAuthorize(request, response);

        verify(httpSession).setAttribute(
                eq(SessionUtil.USER_SESSION_PARAM_KEY),
                argThat(new UserMatcher("myUser", "en")));
    }

    // private static class UserMatcher implements ArgumentMatcher<User> {
    class UserMatcher implements ArgumentMatcher<User> {

        private final String username;
        private final String local;

        UserMatcher(final String username, final String local) {
            this.username = username;
            this.local = local;
        }

        @Override
        public boolean matches(User argument) {
            final User user = argument;
            return username.equals(user.getUsername())
                    && local.equals(user.getLocale());
        }
    }
}
