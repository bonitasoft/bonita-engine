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

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.*

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.document.Document
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.exception.BonitaException
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class DocumentPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def PermissionRule rule = new DocumentPermissionRule()
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
    public void should_check_verify_filters_on_GET_with_user_not_involved_nor_supervisor() {
        //given
        havingFilters([caseId: "46"])
        def processInstance = mock(ProcessInstance.class)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(processInstance).when(processAPI).getProcessInstance(46l)
        doReturn(false).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_filters_on_GET_with_user_not_involved_but_supervisor() {
        //given
        havingFilters([caseId: "46"])
        def processInstance = mock(ProcessInstance.class)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(processInstance).when(processAPI).getProcessInstance(46l)
        doReturn(true).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    def havingFilters(Map filters) {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(filters).when(apiCallContext).getFilters()
        doReturn(true).when(processAPI).isInvolvedInProcessInstance(currentUserId, 45l);
    }

    def havingResourceId(boolean isInvolvedIn, boolean isInvolvedAsManager) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn("77").when(apiCallContext).getResourceId()
        def document = mock(Document.class)
        doReturn(document).when(processAPI).getDocument(77L)
        doReturn(123L).when(document).getProcessInstanceId()
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(123L);
        doReturn(2048L).when(instance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(2048L, currentUserId)
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 123L);
        doReturn(isInvolvedAsManager).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 123L);
    }

    @Test
    public void should_check_verify_filters_on_GET_with_user_involved() {
        //given
        havingFilters([caseId: "45"])
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(45L);
        doReturn(1024L).when(instance).getProcessDefinitionId()
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_allow_user_involved_for_himself_on_GET() {
        //given
        havingResourceId(true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_allow_user_involved_as_manager_on_GET() {
        //given
        havingResourceId(false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_not_allow_user_not_involved_and_not_supervisor() {
        //given
        havingResourceId(false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_allow_user_when_isManager_throws_exception() {
        //given
        havingResourceId(false, false)
        doThrow(new BonitaException("cause")).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 123L);
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_filters_on_GET_no_filter() {
        //given
        havingFilters([plop: "16"])
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_can_start_on_post_is_true() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "caseId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(true).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(154L);
        doReturn(1024L).when(instance).getProcessDefinitionId()

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();

    }

    @Test
    public void should_check_verify_can_start_on_post_is_false() {
        doReturn(true).when(apiCallContext).isPOST()
        doReturn('''
            {
                "caseId":"154",
                "other":"sample"
            }
        ''').when(apiCallContext).getBody()
        doReturn(false).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(154L);
        doReturn(1024L).when(instance).getProcessDefinitionId()

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
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
        doReturn(true).when(processAPI).isInvolvedInProcessInstance(currentUserId, 154l);

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();

    }
}
