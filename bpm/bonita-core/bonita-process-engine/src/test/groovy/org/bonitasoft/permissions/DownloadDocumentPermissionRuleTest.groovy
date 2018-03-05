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

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.document.Document
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

@RunWith(MockitoJUnitRunner.class)
public class DownloadDocumentPermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def PermissionRule rule = new DownloadDocumentPermissionRule()
    @Mock
    def ProcessAPI processAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {

        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_deny_on_GET_with_user_not_involved_nor_supervisor() {
        //given
        havingDocumentParameter("46", false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_allow_on_GET_with_user_not_involved_but_supervisor() {
        //given
        havingDocumentParameter("46", true, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_user_involved() {
        //given
        havingDocumentParameter("46", false, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_manager_of_involved_user() {
        //given
        havingDocumentParameter("46", false, false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }
    
    @Test
    public void should_allow_on_GET_with_user_involved_and_supervisor() {
        //given
        havingDocumentParameter("46", true, true, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }


    def havingDocumentParameter(String documentId, boolean isSupervisor, boolean isInvolvedIn, boolean isInvolvedAsManager) {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([document: documentId]).when(apiCallContext).getParameters()
        def document = mock(Document.class)
        doReturn(document).when(processAPI).getDocument(Long.valueOf(documentId))
        doReturn(123L).when(document).getProcessInstanceId()
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(123L)
        doReturn(2048L).when(instance).getProcessDefinitionId()
        doReturn(isSupervisor).when(processAPI).isUserProcessSupervisor(2048L, currentUserId)
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 123L)
        doReturn(isInvolvedAsManager).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 123L)
    }
}
