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
import static org.mockito.Mockito.*

import org.assertj.core.api.Assertions
import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.exception.SearchException
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class CasePermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def CasePermissionRule rule = new CasePermissionRule()
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
        doReturn(identityAPI).when(apiAccessor).getIdentityAPI()
        doReturn(user).when(identityAPI).getUser(currentUserId)
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
        havingFilters([started_by: "15"])
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
        havingFilters([started_by: "16"])
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
    public void should_check_verify_resourceId_isInvolved_on_GET() {
        //given
        havingResourceId(true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_resourceId_isInvolved_as_manager_on_GET() {
        //given
        havingResourceId(false)
        doReturn(true).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_resourceId_not_isInvolved_on_GET() {
        //given
        havingResourceId(false)

        def processInstance = mock(ProcessInstance.class)
        doReturn(processInstance).when(processAPI).getProcessInstance(45l)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }


    @Test
    public void should_check_verify_resourceId_not_isInvolved_on_GET_but_supervisor() {
        //given
        havingResourceId(false)

        def processInstance = mock(ProcessInstance.class)
        doReturn(processInstance).when(processAPI).getProcessInstance(45l)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_resourceId_archived_isInvolved_on_GET() {
        //given
        havingArchivedResourceId(true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET_and_not_supervisor() {
        //given
        havingArchivedResourceId(false)
        doReturn(false).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l);
        def instance = mock(ArchivedProcessInstance.class)
        doReturn(instance).when(processAPI).getArchivedProcessInstance(45l);
        doReturn(1024l).when(instance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET_but_supervisor() {
        //given
        havingArchivedResourceId(false)
        def instance = mock(ArchivedProcessInstance.class)
        doReturn(instance).when(processAPI).getArchivedProcessInstance(45l);
        doReturn(1024l).when(instance).getProcessDefinitionId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET() {
        //given
        havingArchivedResourceId(false)
        doThrow(new ArchivedProcessInstanceNotFoundException(new Exception())).when(processAPI).getArchivedProcessInstance(45l);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_archived_filters_sourceObjectId_on_GET_not_involved_but_supervisor() {
        //given
        havingFilters([sourceObjectId: "123"])
        doReturn("archivedCase").when(apiCallContext).getResourceName()
        doReturn(new SearchResultImpl<ArchivedProcessInstance>(0, [])).when(processAPI).searchArchivedProcessInstancesInvolvingUser(eq(currentUserId), any(SearchOptions.class));
        def archivedInstance = mock(ArchivedProcessInstance.class)
        doReturn(archivedInstance).when(processAPI).getFinalArchivedProcessInstance(123l)
        doReturn(1024l).when(archivedInstance).getProcessDefinitionId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)

        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_allow_when_isManager_throws_Exception_on_GET() {
        //given
        havingResourceId(false)
        doThrow(new SearchException(new Exception())).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    def havingResourceId(boolean isInvolvedIn) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn(true).when(apiCallContext).isGET()
        doReturn("case").when(apiCallContext).getResourceName()
        doReturn("45").when(apiCallContext).getResourceId()
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 45l);
    }

    def havingArchivedResourceId(boolean isInvolvedIn) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn(true).when(apiCallContext).isGET()
        doReturn("archivedCase").when(apiCallContext).getResourceName()
        doReturn("45").when(apiCallContext).getResourceId()
        def instance = mock(ArchivedProcessInstance.class)
        doReturn(instance).when(processAPI).getArchivedProcessInstance(45l);
        doReturn(42l).when(instance).getSourceObjectId();
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 42l);
    }

    @Test
    public void should_check_verify_can_start_on_post_is_true() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "processDefinitionId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(new SearchResultImpl<User>(1, [user])).when(processAPI).searchUsersWhoCanStartProcessDefinition(eq(154l), any(SearchOptions.class));

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
                "processDefinitionId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(new SearchResultImpl<User>(0, [])).when(processAPI).searchUsersWhoCanStartProcessDefinition(eq(154l), any(SearchOptions.class));

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_can_start_on_post_with_bad_body_is_true() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "unknown":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(new SearchResultImpl<User>(1, [user])).when(processAPI).searchUsersWhoCanStartProcessDefinition(eq(154l), any(SearchOptions.class));

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();

    }
}
