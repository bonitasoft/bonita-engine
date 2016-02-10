/**
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
 **/

package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.execution.state.ExecutingAutomaticActivityStateImpl;
import org.bonitasoft.engine.execution.state.ExecutingCallActivityStateImpl;
import org.bonitasoft.engine.execution.state.ExecutingThrowEventStateImpl;
import org.bonitasoft.engine.execution.state.InitializingActivityStateImpl;
import org.bonitasoft.engine.execution.state.InitializingActivityWithBoundaryEventsStateImpl;
import org.bonitasoft.engine.execution.state.InitializingAndExecutingFlowNodeStateImpl;
import org.bonitasoft.engine.execution.state.InitializingBoundaryEventStateImpl;
import org.bonitasoft.engine.execution.state.InitializingLoopActivityStateImpl;
import org.bonitasoft.engine.execution.state.InitializingMultiInstanceActivityStateImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodeStateManagerImplTest {

    @InjectMocks
    private FlowNodeStateManagerImpl stateManager;

    @Mock
    private BPMInstancesCreator creator;

    @Test
    public void getFirstState_for_sub_process_should_ExecutingCallActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.SUB_PROCESS);

        //then
        assertThat(firstState).isInstanceOf(ExecutingCallActivityStateImpl.class);
    }


    @Test
    public void getFirstState_for_automatic_task_return_ExecutingAutomaticActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.AUTOMATIC_TASK);

        //then
        assertThat(firstState).isInstanceOf(ExecutingAutomaticActivityStateImpl.class);
    }

    @Test
    public void getFirstState_for_boundary_should_return_InitializingBoundaryEventStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.BOUNDARY_EVENT);

        //then
        assertThat(firstState).isInstanceOf(InitializingBoundaryEventStateImpl.class);
    }


    @Test
    public void getFirstState_for_call_activity_should_InitializingActivityWithBoundaryEventsStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.CALL_ACTIVITY);

        //then
        assertThat(firstState).isInstanceOf(InitializingActivityWithBoundaryEventsStateImpl.class);
    }


    @Test
    public void getFirstState_for_end_event_should_return_ExecutingThrowEventStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.END_EVENT);

        //then
        assertThat(firstState).isInstanceOf(ExecutingThrowEventStateImpl.class);
    }

    @Test
    public void getFirstState_for_gateway_should_return_InitializingAndExecutingFlowNodeStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.GATEWAY);

        //then
        assertThat(firstState).isInstanceOf(InitializingAndExecutingFlowNodeStateImpl.class);
    }


    @Test
    public void getFirstState_for_intermadiate_catch_should_return_InitializingActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.INTERMEDIATE_CATCH_EVENT);

        //then
        assertThat(firstState).isInstanceOf(InitializingActivityStateImpl.class);
    }


    @Test
    public void getFirstState_for_intermediate_throw_event_should_return_ExecutingThrowEventStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.INTERMEDIATE_THROW_EVENT);

        //then
        assertThat(firstState).isInstanceOf(ExecutingThrowEventStateImpl.class);
    }

    @Test
    public void getFirstState_for_loop_activity_should_return_InitializingLoopActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.LOOP_ACTIVITY);

        //then
        assertThat(firstState).isInstanceOf(InitializingLoopActivityStateImpl.class);
    }


    @Test
    public void getFirstState_for_manual_task_should_return_InitializingActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.MANUAL_TASK);

        //then
        assertThat(firstState).isInstanceOf(InitializingActivityStateImpl.class);
    }


    @Test
    public void getFirstState_for_multi_instance_should_return_InitializingMultiInstanceActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.MULTI_INSTANCE_ACTIVITY);

        //then
        assertThat(firstState).isInstanceOf(InitializingMultiInstanceActivityStateImpl.class);
    }


    @Test
    public void getFirstState_for_receive_task_should_return_InitializingActivityWithBoundaryEventsStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.RECEIVE_TASK);

        //then
        assertThat(firstState).isInstanceOf(InitializingActivityWithBoundaryEventsStateImpl.class);
    }

    @Test
    public void getFirstState_send_task_should_return_ExecutingAutomaticActivityStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.SEND_TASK);

        //then
        assertThat(firstState).isInstanceOf(ExecutingAutomaticActivityStateImpl.class);
    }

    @Test
    public void getFirstState_for_start_event_should_return_InitializingAndExecutingFlowNodeStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.START_EVENT);

        //then
        assertThat(firstState).isInstanceOf(InitializingAndExecutingFlowNodeStateImpl.class);
    }

    @Test
    public void getFirstState_for_user_task_should_return_InitializingActivityWithBoundaryEventsStateImpl() throws Exception {
        //when
        FlowNodeState firstState = stateManager.getFirstState(SFlowNodeType.USER_TASK);

        //then
        assertThat(firstState).isInstanceOf(InitializingActivityWithBoundaryEventsStateImpl.class);
    }

}