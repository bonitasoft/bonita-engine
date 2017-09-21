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

import static org.mockito.Mockito.*

import org.assertj.core.api.Assertions
import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.exception.SearchException
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class CommentPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def CommentPermissionRule rule = new CommentPermissionRule()
    @Mock
    def ProcessAPI processAPI
    @Mock
    def IdentityAPI identityAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_check_verify_filters_on_GET_with_different_user_involved() {
        //given
        havingFilters([user_id: "15"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    def havingFilters(Map filters) {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(filters).when(apiCallContext).getFilters()
    }

    @Test
    public void should_check_verify_filters_on_GET_with_different_user_started() {
        //given
        havingFilters([team_manager_id: "15"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_filters_on_GET_with_same_user_involved() {
        //given
        havingFilters([user_id: "16"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_filters_on_GET_with_same_user_started() {
        //given
        havingFilters([team_manager_id: "16"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_filters_on_GET_nofilter_on_user() {
        //given
        havingFilters([plop: "16"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_allow_if_isInvolved_as_manager_on_POST() {
        // given
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''{ "processInstanceId":"154" }''').when(apiCallContext).getBody()
        def processInstance = mock(ProcessInstance.class)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(processInstance).when(processAPI).getProcessInstance(154l)
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        doReturn(false).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);
        doReturn(true).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 154l);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    def havingResourceId(boolean isInvolvedIn, boolean isInvolvedAsManager) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn(true).when(apiCallContext).isGET()
        doReturn(["processInstanceId": "45"]).when(apiCallContext).getFilters()
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(45l);
        doReturn(1024l).when(instance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 45l);
        doReturn(isInvolvedAsManager).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l);
    }

    @Test
    public void should_allow_if_isInvolved_himself_on_get() {
        // given
        havingResourceId(true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_allow_if_isInvolved_as_manager_on_get() {
        // given
        havingResourceId(false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_not_allow_if_isNotInvolved_on_get() {
        // given
        havingResourceId(false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_allow_if_isManager_throws_exception_on_get() {
        // given
        havingResourceId(false, false)
        doThrow(new SearchException(new Exception())).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_can_start_on_post_is_true() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "processInstanceId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(true).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();

    }

    @Test
    public void should_check_verify_can_start_on_post_is_false() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "processInstanceId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        def processInstance = mock(ProcessInstance.class)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(processInstance).when(processAPI).getProcessInstance(154l)
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        doReturn(false).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_can_start_on_post_is_false_but_is_supervisor() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "processInstanceId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        def processInstance = mock(ProcessInstance.class)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(processInstance).when(processAPI).getProcessInstance(154l)
        doReturn(true).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        doReturn(false).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_can_start_on_post_with_bad_body_is_false() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "unknown":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(true).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();

    }


}
