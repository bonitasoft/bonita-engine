/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.WaitingEvent;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ManualTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskInstanceImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.WaitingErrorEventImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.WaitingMessageEventImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.impl.UserImpl;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author mazourd
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(ClientEventUtil.class)
public class UserTaskAPIImplTest {

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession session;

    @Mock
    private CommandAPI commandAPI;

    @InjectMocks
    private UserTaskAPIImpl userTaskAPI;

    private UserTaskAPIImpl spyUserTaskAPI;

    @Before
    public void setUp() throws Exception {
        spyUserTaskAPI = spy(userTaskAPI);
        given(spyUserTaskAPI.getProcessAPI()).willReturn(processAPI);
        given(spyUserTaskAPI.getCommandAPI()).willReturn(commandAPI);
    }

    @Test
    public void skip_should_set_state_by_name_to_skipped() throws Exception {
        //given

        //when
        spyUserTaskAPI.skipTask(2L);

        //then
        verify(processAPI).setActivityStateByName(2L, "skipped");

    }

    @Test
    public void checkNoWaitingEvent_should_return_messages() throws BonitaException {
        //given
        List<WaitingEvent> waitingEventList = new ArrayList<>();
        WaitingEvent waitingEvent1 = new WaitingErrorEventImpl();
        WaitingEvent waitingEvent2 = new WaitingMessageEventImpl();
        SearchResult<WaitingEvent> searchResult = new SearchResultImpl<>(2, waitingEventList);
        waitingEventList.add(waitingEvent1);
        waitingEventList.add(waitingEvent2);
        doReturn(searchResult).when(commandAPI).execute(anyString(), anyMap());
        //when
        List<String> messages = spyUserTaskAPI.checkNoWaitingEvent();
        //then
        assertThat(messages != null);
        assertThat(!messages.isEmpty());
    }

    @Test
    public void waitForFlowNodes_should_return_Long() throws Exception {
        //given
        PowerMockito.mockStatic(ClientEventUtil.class);
        //when
        long a = spyUserTaskAPI.waitForFlowNode(25, TestStates.FAILED, "dummy", true, 10);
        long b = spyUserTaskAPI.waitForFlowNode(25, TestStates.FAILED, "dummy", false, 10);
        //then
        assertThat(a != 0);
        assertThat(b != 0);
    }

    @Test
    public void waitForGateway_should_correctly_return_null_if_no_Gateway_Instance() throws Exception {
        //given
        PowerMockito.mockStatic(ClientEventUtil.class);
        doReturn(new AutomaticTaskInstanceImpl("dummy task", 45)).when(processAPI).getFlowNodeInstance(anyLong());
        GatewayInstance gatewayInstance1 = null;
        //when
        gatewayInstance1 = spyUserTaskAPI.waitForGateway(new ProcessInstanceImpl("dummy"), "dummy2");

        //then
        //System.out.println(gatewayInstance1.toString());
        assertThat(gatewayInstance1 == null);
    }

    @Test
    public void waitForGateway_should_correctly_return_the_Gatewayif_there_is_one() throws Exception {
        PowerMockito.mockStatic(ClientEventUtil.class);
        doReturn(new GatewayInstanceImpl("dummy task", 45)).when(processAPI).getFlowNodeInstance(anyLong());
        Object gatewayInstance1 = null;
        gatewayInstance1 = spyUserTaskAPI.waitForGateway(new ProcessInstanceImpl("dummy"), "dummy2");
        assertThat(gatewayInstance1 != null);
        assertThat(gatewayInstance1 instanceof GatewayInstance);
    }

    @Test
    public void waitForTaskToFail_should_return_ActivityInstance() throws Exception {
        //given
        PowerMockito.mockStatic(ClientEventUtil.class);
        //when
        ActivityInstance activityInstance = spyUserTaskAPI.waitForTaskToFail(new ProcessInstanceImpl("dummy"));
        //then
        assertThat(activityInstance != null);
    }

    @Test
    public void waitForUserTaskAndAssigneIt_should_assign_a_human_task() throws Exception {
        //given
        doReturn(new ManualTaskInstanceImpl("dummy task", -1, 17)).when(spyUserTaskAPI).waitForUserTaskAndGetIt(anyLong(), anyString(), anyInt());
        //when
        HumanTaskInstance humanTaskInstance = spyUserTaskAPI.waitForUserTaskAndAssigneIt(-1, "dummy task", new UserImpl(-1, "dummy user", "lalala"));
        //then
        assertThat(humanTaskInstance != null);
    }

    @Test
    public void waitForUserTaskAndExecuteIt_shoud_return_long() throws Exception {
        //given
        doReturn(5L).when(spyUserTaskAPI).waitForUserTask(anyLong(), anyString(), anyInt());
        //when
        long id = spyUserTaskAPI.waitForUserTaskAndExecuteIt(-1, "dummy", new UserImpl(-1, "dummy user", "lalala"));
        //then
        verify(spyUserTaskAPI, atLeast(2)).getProcessAPI();
        assertThat(id != 0);
    }

    @Test
    public void waitForUserTask_shoud_return_long() throws CommandExecutionException, TimeoutException, CommandParameterizationException,
            CommandNotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        //given
        PowerMockito.mockStatic(ClientEventUtil.class);
        when(ClientEventUtil.executeWaitServerCommand(eq(spyUserTaskAPI.getCommandAPI()), anyMap(), eq(50))).thenReturn(5L);
        when(ClientEventUtil.executeWaitServerCommand(eq(spyUserTaskAPI.getCommandAPI()), anyMap(), eq(2 * 60 * 1000))).thenReturn(5L);
        //when
        long a = spyUserTaskAPI.waitForUserTask(-1, "lala", -1);
        long b = spyUserTaskAPI.waitForUserTask(17, "lala", 50);
        //then
        assertThat(a == 5L);
        assertThat(b == 5L);

    }

    @Test
    public void skipTasks_should_iterate_on_all_activities() throws UpdateException {
        //given
        ActivityInstance instance1 = new CallActivityInstanceImpl("dummy", 456L);
        ActivityInstance instance2 = new UserTaskInstanceImpl("dummy2", 45L, 55L);
        List<ActivityInstance> activityInstances = new ArrayList<>();
        activityInstances.add(instance1);
        activityInstances.add(instance2);
        given(processAPI.getActivities(anyLong(), anyInt(), anyInt())).willReturn(activityInstances);
        //when
        spyUserTaskAPI.skipTasks(2L);
        //then
        verify(spyUserTaskAPI, atLeast(2)).skipTask(anyLong());
    }

    @Test
    public void waiForTaskAndGetIt_should_should_return_the_task() throws Exception {

        //given
        long processInstanceId = 1L;
        String taskName = "step1";
        int timeout = 10;

        HumanTaskInstance humanTaskInstance = mock(HumanTaskInstance.class);
        doReturn(5L).when(spyUserTaskAPI).waitForUserTask(processInstanceId, taskName, timeout);
        given(processAPI.getHumanTaskInstance(5L)).willReturn(humanTaskInstance);

        //when
        HumanTaskInstance taskInstance = spyUserTaskAPI.waitForUserTaskAndGetIt(processInstanceId, taskName, timeout);

        //then
        assertThat(taskInstance).isEqualTo(humanTaskInstance);

    }
}
