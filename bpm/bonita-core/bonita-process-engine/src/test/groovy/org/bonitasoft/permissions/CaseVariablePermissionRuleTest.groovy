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
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.bpm.process.ProcessInstance
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
class CaseVariablePermissionRuleTest {

    @Mock
    def APISession apiSession
    @Mock
    def APICallContext apiCallContext
    @Mock
    def APIAccessor apiAccessor
    @Mock
    def Logger logger

    def CaseVariablePermissionRule rule = new CaseVariablePermissionRule()

    @Mock
    def ProcessAPI processAPI
    @Mock
    def IdentityAPI identityAPI
    @Mock
    def User user
    def long currentUserId = 16l

    @Before
    void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    def havingProcessInstance(boolean isSupervisor) {
        def instance = mock(ProcessInstance.class)
        doReturn(425l).when(instance).getProcessDefinitionId()
        doReturn(instance).when(processAPI).getProcessInstance(158l)
        doReturn(isSupervisor).when(processAPI).isUserProcessSupervisor(425l, currentUserId)
    }

    def havingFilters(Map filters) {
        doReturn(true).when(apiCallContext).isGET()
        doReturn(filters).when(apiCallContext).getFilters()
    }

    @Test
    void should_check_return_false_on_delete() {
        doReturn(true).when(apiCallContext).isDELETE()

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()

    }

    @Test
    void should_check_return_true_on_get() {
        //given
        doReturn(true).when(apiCallContext).isGET()
        doReturn("158/myData").when(apiCallContext).getResourceId()
        havingProcessInstance(true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()

    }

    @Test
    void should_check_return_false_on_get_if_not_process_owner() {
        //given
        doReturn(true).when(apiCallContext).isGET()
        doReturn("158/myData").when(apiCallContext).getResourceId()
        havingProcessInstance(false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()

    }

    @Test
    void should_check_return_true_on_put() {
        //given
        doReturn(true).when(apiCallContext).isPUT()
        doReturn("158/myData").when(apiCallContext).getResourceId()
        havingProcessInstance(true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()

    }

    @Test
    void should_check_return_true_on_search_if_supervisor() {
        havingFilters([case_id: "158"])
        havingProcessInstance(true)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()

    }

    @Test
    void should_check_return_false_on_search_if_not_supervisor() {
        havingFilters([case_id: "158"])
        havingProcessInstance(false)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()

    }


}
