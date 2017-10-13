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
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException
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
public class CaseContextPermissionRuleTest {

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
    def CaseContextPermissionRule rule = new CaseContextPermissionRule()

    @Before
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_check_verify_resourceId_isInvolved_on_GET() {
        //given
        havingResourceId(true, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_resourceId_isInvolved_as_manager_on_GET() {
        //given
        havingResourceId(false, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_resourceId_not_isInvolved_on_GET() {
        //given
        havingResourceId(false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse()
    }


    @Test
    public void should_check_verify_resourceId_not_isInvolved_on_GET_but_supervisor() {
        //given
        havingResourceId(false, false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_resourceId_archived_isInvolved_on_GET() {
        //given
        havingArchivedResourceId(true, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET_and_not_supervisor() {
        //given
        havingArchivedResourceId(false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET_but_supervisor() {
        //given
        havingArchivedResourceId(false, false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }
    
    @Test
    public void should_check_verify_resourceId_archived_isInvolved_as_manager_on_GET() {
        //given
        havingArchivedResourceId(false, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_check_verify_resourceId_archived_not_isInvolved_on_GET() {
        //given
        havingArchivedResourceId(false, false, false)
        doThrow(new ArchivedProcessInstanceNotFoundException(new Exception())).when(processAPI).getArchivedProcessInstance(45l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_allow_when_isManager_throws_Exception_on_GET() {
        //given
        havingArchivedResourceId(false, false, false)
        doThrow(new SearchException(new Exception())).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 34l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        Assertions.assertThat(isAuthorized).isTrue()
    }

    def havingResourceId(boolean isInvolvedIn, boolean isManagerOfInvolvedIn, boolean isProcessSupervisor) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn("case").when(apiCallContext).getResourceName()
        doReturn(Arrays.asList("45", "context")).when(apiCallContext).getCompoundResourceId()
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 45l)
        doReturn(isManagerOfInvolvedIn)when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 45l)
        def processInstance = mock(ProcessInstance.class)
        doReturn(processInstance).when(processAPI).getProcessInstance(45l)
        doReturn(1024l).when(processInstance).getProcessDefinitionId()
        doReturn(isProcessSupervisor).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
    }

    def havingArchivedResourceId(boolean isInvolvedIn, boolean isManagerOfInvolvedIn, boolean isProcessSupervisor) {
        doReturn(currentUserId).when(apiSession).getUserId()
        doReturn("archivedCase").when(apiCallContext).getResourceName()
        doReturn(Arrays.asList("45", "context")).when(apiCallContext).getCompoundResourceId()
        def archivedInstance = mock(ArchivedProcessInstance.class)
        doReturn(archivedInstance).when(processAPI).getArchivedProcessInstance(45l)
        doReturn(34l).when(archivedInstance).getSourceObjectId()
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 34l)
        doReturn(isManagerOfInvolvedIn)when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 34l)
        doReturn(1024l).when(archivedInstance).getProcessDefinitionId()
        doReturn(isProcessSupervisor).when(processAPI).isUserProcessSupervisor(1024l, currentUserId)
    }
}
