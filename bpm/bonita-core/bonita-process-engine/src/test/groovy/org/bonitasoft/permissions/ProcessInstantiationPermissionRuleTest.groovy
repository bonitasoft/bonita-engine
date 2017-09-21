/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.permissions

import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.doReturn

import org.assertj.core.api.Assertions
import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessInstantiationPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    @Mock
    def ProcessAPI processAPI
    @Mock
    def IdentityAPI identityAPI
    @Mock
    def User user
    def long currentUserId = 16l
    def ProcessInstantiationPermissionRule rule = new ProcessInstantiationPermissionRule()

    @Before
    public void before() {

        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(identityAPI).when(apiAccessor).getIdentityAPI()
        doReturn(user).when(identityAPI).getUser(currentUserId)
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    def havingResourceId(boolean isInvolvedIn) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn(true).when(apiCallContext).isPOST()
        doReturn("process").when(apiCallContext).getResourceName()
        doReturn("45/instantiation").when(apiCallContext).getResourceId()
        doReturn(Arrays.asList("45", "instantiation")).when(apiCallContext).getCompoundResourceId()
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 45l);
    }

    @Test
    public void should_check_verify_can_start_is_true() {
        havingResourceId(true)
        doReturn(new SearchResultImpl<User>(1, [user])).when(processAPI).searchUsersWhoCanStartProcessDefinition(eq(45l), any(SearchOptions.class));

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();

    }

    @Test
    public void should_check_verify_can_start_on_post_is_false() {
        havingResourceId(false)
        doReturn(new SearchResultImpl<User>(0, [])).when(processAPI).searchUsersWhoCanStartProcessDefinition(eq(45l), any(SearchOptions.class));

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }
}
