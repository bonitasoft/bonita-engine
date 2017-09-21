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
import static org.mockito.Matchers.any
import static org.mockito.Mockito.doReturn

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.api.permission.PermissionRule
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.search.SearchOptions
import org.bonitasoft.engine.search.SearchResult
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class ConnectorInstancePermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger
    def PermissionRule rule = new ConnectorInstancePermissionRule()
    @Mock
    def ProcessAPI processAPI
    @Mock
    def FlowNodeInstance flownodeInstance
    @Mock
    def SearchResult<ArchivedFlowNodeInstance> archivedFlowNodeInstanceSearchResult
    @Mock
    def ArchivedFlowNodeInstance archivedFlowNodeInstance
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_check_verify_get_is_true_when_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(
                [
                        "containerId": "2"
                ]
        ).when(apiCallContext).getFilters()
        doReturn("connectorInstance").when(apiCallContext).getResourceName()
        doReturn(flownodeInstance).when(processAPI).getFlowNodeInstance(2l)
        doReturn(1l).when(flownodeInstance).getProcessDefinitionId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(1l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_get_is_false_when_not_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(
                [
                        "containerId": "2"
                ]
        ).when(apiCallContext).getFilters()
        doReturn("connectorInstance").when(apiCallContext).getResourceName()
        doReturn(flownodeInstance).when(processAPI).getFlowNodeInstance(2l)
        doReturn(1l).when(flownodeInstance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(1l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_get_for_archived_is_true_when_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(
                [
                        "containerId": "2"
                ]
        ).when(apiCallContext).getFilters()
        doReturn("archivedConnectorInstance").when(apiCallContext).getResourceName()
        doReturn(archivedFlowNodeInstanceSearchResult).when(processAPI).searchArchivedFlowNodeInstances(any(SearchOptions.class))
        doReturn([archivedFlowNodeInstance]).when(archivedFlowNodeInstanceSearchResult).getResult()
        doReturn(1l).when(archivedFlowNodeInstance).getProcessDefinitionId()
        doReturn(true).when(processAPI).isUserProcessSupervisor(1l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void should_check_verify_get_for_archived_is_false_when_not_process_owner() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(
                [
                        "containerId": "2"
                ]
        ).when(apiCallContext).getFilters()
        doReturn("archivedConnectorInstance").when(apiCallContext).getResourceName()
        doReturn(archivedFlowNodeInstanceSearchResult).when(processAPI).searchArchivedFlowNodeInstances(any(SearchOptions.class))
        doReturn([archivedFlowNodeInstance]).when(archivedFlowNodeInstanceSearchResult).getResult()
        doReturn(1l).when(archivedFlowNodeInstance).getProcessDefinitionId()
        doReturn(false).when(processAPI).isUserProcessSupervisor(1l, currentUserId)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void should_check_verify_get_is_false_when_no_process_id() {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(
                [
                        "other": "sample"
                ]
        ).when(apiCallContext).getFilters()

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse();
    }
}
