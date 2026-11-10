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
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

import org.bonitasoft.engine.api.APIAccessor
import org.bonitasoft.engine.api.IdentityAPI
import org.bonitasoft.engine.api.Logger
import org.bonitasoft.engine.api.ProcessAPI
import org.bonitasoft.engine.api.permission.APICallContext
import org.bonitasoft.engine.bpm.flownode.*
import org.bonitasoft.engine.identity.User
import org.bonitasoft.engine.session.APISession
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.class)
public class TaskExecutionPermissionRuleTest {

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
    def TaskExecutionPermissionRule rule = new TaskExecutionPermissionRule()

    @Before
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI()
        doReturn(identityAPI).when(apiAccessor).getIdentityAPI()
        doReturn(currentUserId).when(apiSession).getUserId()
    }

    @Test
    public void should_get_on_a_archived_task_is_ok() {
        //given
        havingResource("archivedUserTask")
        def archivedTask = mock(ArchivedFlowNodeInstance.class)
        doReturn(FlowNodeType.USER_TASK).when(archivedTask).getType()
        doReturn(currentUserId).when(archivedTask).getExecutedBy()
        doReturn(archivedTask).when(processAPI).getArchivedFlowNodeInstance(458)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_get_on_a_archived_task_is_not_the_assignee() {
        //given
        havingResource("archivedUserTask")
        def archivedTask = mock(ArchivedFlowNodeInstance.class)
        doReturn(FlowNodeType.USER_TASK).when(archivedTask).getType()
        doReturn(58l).when(archivedTask).getExecutedBy()
        doReturn(archivedTask).when(processAPI).getArchivedFlowNodeInstance(458)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_get_on_a_archived_task_is_not_found() {
        //given
        havingResource("archivedUserTask")
        doThrow(new ArchivedFlowNodeInstanceNotFoundException(458)).when(processAPI).getArchivedFlowNodeInstance(458)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }


    @Test
    public void should_get_on_a_human_task_the_user_is_involved_in_is_ok() {
        //given — involvement (assignee / pending-actor / active delegate) is resolved engine-side
        //        by isInvolvedInHumanTaskInstance; the rule only consults that single result.
        havingResource("userTask")
        def instance = mock(UserTaskInstance.class)
        doReturn(FlowNodeType.USER_TASK).when(instance).getType()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doReturn(true).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_get_on_a_human_task_the_user_is_not_involved_in_is_not_ok() {
        //given — not involved and not supervisor
        havingResource("userTask")
        def instance = mock(UserTaskInstance.class)
        doReturn(10l).when(instance).getProcessDefinitionId()
        doReturn(FlowNodeType.USER_TASK).when(instance).getType()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        doReturn(false).when(processAPI).isUserProcessSupervisor(10l, currentUserId)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_get_on_an_unexisting_flow_node() {
        //given
        havingResource("userTask")
        doThrow(new FlowNodeInstanceNotFoundException(new Exception())).when(processAPI).getFlowNodeInstance(458)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_allow_on_a_manual_task_when_user_is_involved_in_the_parent_task() {
        //given — caller is not involved in the manual subtask itself, but is involved in its parent task.
        havingResource("userTask")
        def instance = mock(ManualTaskInstance.class)
        doReturn(FlowNodeType.MANUAL_TASK).when(instance).getType()
        doReturn(999l).when(instance).getParentContainerId()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        def parentTask = mock(HumanTaskInstance.class)
        doReturn(777l).when(parentTask).getId()
        doReturn(parentTask).when(processAPI).getHumanTaskInstance(999l)
        doReturn(true).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 777l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_deny_on_a_manual_task_when_parent_task_is_not_found() {
        //given — manual subtask not granted and its parent task cannot be loaded
        havingResource("userTask")
        def instance = mock(ManualTaskInstance.class)
        doReturn(FlowNodeType.MANUAL_TASK).when(instance).getType()
        doReturn(999l).when(instance).getParentContainerId()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        doThrow(new ActivityInstanceNotFoundException(999l)).when(processAPI).getHumanTaskInstance(999l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_deny_on_a_manual_task_when_parent_involvement_check_throws_NotFoundException() {
        //given — parent task loads, but its involvement check loses the race (task vanished); deny.
        havingResource("userTask")
        def instance = mock(ManualTaskInstance.class)
        doReturn(FlowNodeType.MANUAL_TASK).when(instance).getType()
        doReturn(999l).when(instance).getParentContainerId()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        def parentTask = mock(HumanTaskInstance.class)
        doReturn(777l).when(parentTask).getId()
        doReturn(parentTask).when(processAPI).getHumanTaskInstance(999l)
        doThrow(new ActivityInstanceNotFoundException(777l)).when(processAPI)
                .isInvolvedInHumanTaskInstance(currentUserId, 777l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_POST_on_a_task_if_supervisor_and_assigned_user_is_involved() {
        //given — caller is not involved but is supervisor; the assigned user (4) is involved,
        //        so the supervisor-with-assignedUser recursion grants access.
        havingResource("userTask")
        def instance = mock(UserTaskInstance.class)
        doReturn(10l).when(instance).getProcessDefinitionId()
        doReturn(FlowNodeType.USER_TASK).when(instance).getType()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458l)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        doReturn(true).when(processAPI).isUserProcessSupervisor(10l, currentUserId)
        doReturn([user: ["4"] as String[]]).when(apiCallContext).getParameters()
        def assigneUser = mock(User.class)
        doReturn(assigneUser).when(identityAPI).getUser(4l)
        doReturn(4l).when(assigneUser).getId()
        doReturn(true).when(processAPI).isInvolvedInHumanTaskInstance(4l, 458l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isTrue()
    }

    @Test
    public void should_not_POST_on_a_task_if_supervisor_but_assigned_user_not_involved() {
        //given — caller is supervisor but the assigned user (4) is neither involved nor supervisor
        havingResource("userTask")
        def instance = mock(UserTaskInstance.class)
        doReturn(10l).when(instance).getProcessDefinitionId()
        doReturn(FlowNodeType.USER_TASK).when(instance).getType()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458l)
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(currentUserId, 458l)
        doReturn(true).when(processAPI).isUserProcessSupervisor(10l, currentUserId)
        doReturn([user: ["4"] as String[]]).when(apiCallContext).getParameters()
        def assigneUser = mock(User.class)
        doReturn(assigneUser).when(identityAPI).getUser(4l)
        doReturn(4l).when(assigneUser).getId()
        doReturn(false).when(processAPI).isInvolvedInHumanTaskInstance(4l, 458l)
        doReturn(false).when(processAPI).isUserProcessSupervisor(10l, 4l)
        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)
        //then
        assertThat(isAuthorized).isFalse()
    }

    @Test
    public void should_return_false_when_isInvolvedInHumanTaskInstance_throws_NotFoundException() {
        //given — defensive: if the involvement check throws NotFoundException (task vanished mid-check),
        //        treat as deny rather than letting it surface as a 404-granting path.
        havingResource("userTask")
        def instance = mock(UserTaskInstance.class)
        doReturn(FlowNodeType.USER_TASK).when(instance).getType()
        doReturn(instance).when(processAPI).getFlowNodeInstance(458)
        doThrow(new ActivityInstanceNotFoundException(458l)).when(processAPI)
                .isInvolvedInHumanTaskInstance(currentUserId, 458l)

        //when
        def isAuthorized = rule.isAllowed(apiSession, apiCallContext, apiAccessor, logger)

        //then
        assertThat(isAuthorized).isFalse()
    }

    def havingResource(String resourceName) {
        doReturn(Arrays.asList("458", "execution")).when(apiCallContext).getCompoundResourceId()
        doReturn(resourceName).when(apiCallContext).getResourceName()
    }
}
