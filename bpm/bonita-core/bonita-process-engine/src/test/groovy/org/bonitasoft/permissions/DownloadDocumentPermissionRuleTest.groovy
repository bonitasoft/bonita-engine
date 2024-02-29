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
import org.bonitasoft.engine.bpm.document.ArchivedDocument
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.search.impl.SearchResultImpl
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException
import org.bonitasoft.engine.bpm.document.ArchivedDocumentNotFoundException
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.doThrow
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
    public void should_allow_on_GET_document_not_found() {
        //given
        def documentId = "45"
        doReturn(true).when(apiCallContext).isGET()
        doReturn([document: [documentId] as String[]]).when(apiCallContext).getParameters()
        doThrow(new DocumentNotFoundException("")).when(processAPI).getDocument(Long.valueOf(documentId))
        doThrow(new ArchivedDocumentNotFoundException(new Exception(""))).when(processAPI).getArchivedVersionOfProcessDocument(Long.valueOf(documentId))
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        //let the download servlet handle the 404
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_deny_on_GET_with_user_not_involved_nor_supervisor() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(false, false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_deny_on_GET_with_user_not_involved_nor_supervisor_on_archived_case() {
        //given
        havingDocumentParameter()
        havingInvolvementInArchivedProcessInstance(false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_deny_on_GET_content_storage_id_with_user_not_involved_nor_supervisor() {
        //given
        havingContentStorageIdParameter()
        havingInvolvementInProcessInstance(false, false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_deny_on_GET_content_storage_id_with_user_not_involved_nor_supervisor_on_archived_case() {
        //given
        havingContentStorageIdParameterForArchivedDocument()
        havingInvolvementInArchivedProcessInstance(false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_allow_on_GET_with_user_not_involved_but_supervisor() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(true, false, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_user_involved() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(false, true, false, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_user_involved_on_archived_case() {
        //given
        havingDocumentParameter()
        havingInvolvementInArchivedProcessInstance(false, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_manager_of_involved_user() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(false, false, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_user_involved_and_supervisor() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(true, true, true, false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_GET_with_user_involved_in_process_instance_as_subprocess() {
        //given
        havingDocumentParameter()
        havingInvolvementInProcessInstance(false, false, false, true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    def havingDocumentParameter() {
        def documentId = "46"
        doReturn(true).when(apiCallContext).isGET()
        doReturn([document: [documentId] as String[]]).when(apiCallContext).getParameters()
        def document = mock(Document.class)
        doReturn(document).when(processAPI).getDocument(Long.valueOf(documentId))
        doReturn(123L).when(document).getProcessInstanceId()
    }

    def havingDocumentParameterForArchivedDocument() {
        def documentId = "46"
        doReturn(true).when(apiCallContext).isGET()
        doReturn([document: [documentId] as String[]]).when(apiCallContext).getParameters()
        doThrow(new DocumentNotFoundException("")).when(processAPI).getDocument(Long.valueOf(documentId))
        def archivedDocument = mock(ArchivedDocument.class)
        doReturn(archivedDocument).when(processAPI).getArchivedVersionOfProcessDocument(Long.valueOf(documentId))
        doReturn(123L).when(archivedDocument).getProcessInstanceId()
    }

    def havingContentStorageIdParameter() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([contentStorageId: ["45"] as String[], fileName: ["test.txt"] as String[]]).when(apiCallContext).getParameters()
        def document = mock(Document.class)
        doReturn(new SearchResultImpl(1, [document])).when(processAPI).searchDocuments(any(SearchOptions.class))
        doReturn(123L).when(document).getProcessInstanceId()
    }

    def havingContentStorageIdParameterForArchivedDocument() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn([contentStorageId: ["45"] as String[], fileName: ["test.txt"] as String[]]).when(apiCallContext).getParameters()
        doReturn(new SearchResultImpl(0, [])).when(processAPI).searchDocuments(any(SearchOptions.class))
        def document = mock(ArchivedDocument.class)
        doReturn(new SearchResultImpl(1, [document])).when(processAPI).searchArchivedDocuments(any(SearchOptions.class))
        doReturn(123L).when(document).getProcessInstanceId()
    }

    def havingInvolvementInProcessInstance(boolean isSupervisor, boolean isInvolvedIn, boolean isInvolvedAsManager, boolean isInvolvedInForSubprocesses) {
        def instance = mock(ProcessInstance.class)
        doReturn(instance).when(processAPI).getProcessInstance(123L)
        doReturn(2048L).when(instance).getProcessDefinitionId()
        doReturn(isSupervisor).when(processAPI).isUserProcessSupervisor(2048L, currentUserId)
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 123L)
        doReturn(isInvolvedAsManager).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 123L)
        def searchResult = mock(SearchResult.class)
        if (isInvolvedInForSubprocesses) {
            doReturn(1L).when(searchResult).getCount()
        }
        doReturn(searchResult).when(processAPI).searchMyAvailableHumanTasks(eq(currentUserId), any(SearchOptions.class))
    }

    def havingInvolvementInArchivedProcessInstance(boolean isSupervisor, boolean isInvolvedIn, boolean isInvolvedAsManager) {
        def archivedInstance = mock(ArchivedProcessInstance.class)
        doThrow(new ProcessInstanceNotFoundException("")).when(processAPI).getProcessInstance(123L)
        doReturn(archivedInstance).when(processAPI).getFinalArchivedProcessInstance(123L)
        doReturn(2048L).when(archivedInstance).getProcessDefinitionId()
        doReturn(isSupervisor).when(processAPI).isUserProcessSupervisor(2048L, currentUserId)
        doReturn(isInvolvedIn).when(processAPI).isInvolvedInProcessInstance(currentUserId, 123L)
        doReturn(isInvolvedAsManager).when(processAPI).isManagerOfUserInvolvedInProcessInstance(currentUserId, 123L)
        def searchResult = mock(SearchResult.class)
        doReturn(searchResult).when(processAPI).searchMyAvailableHumanTasks(eq(currentUserId), any(SearchOptions.class))
    }
}
