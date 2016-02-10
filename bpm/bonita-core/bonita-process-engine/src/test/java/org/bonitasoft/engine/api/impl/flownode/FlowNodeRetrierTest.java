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

package org.bonitasoft.engine.api.impl.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.engine.api.impl.connector.ConnectorResetStrategy;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.FlowNodeExecutor;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.state.CompletedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FailedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.InitializingActivityStateImpl;
import org.bonitasoft.engine.execution.state.ReadyActivityStateImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodeRetrierTest {

    public static final String FLOW_NODE_NAME = "step1";
    @Mock
    private FlowNodeExecutor flowNodeExecutor;

    @Mock
    private ContainerRegistry registry;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    private FlowNodeStateManager flowNodeStateManager;

    @Mock
    private SFlowNodeInstance flowNodeInstance;

    @Mock
    ConnectorResetStrategy strategy;

    private static long FLOW_NODE_INSTANCE_ID = 10;

    private static long PROCESS_INSTANCE_ID = 1;

    private static long PROCESS_DEFINITION_ID = 20;

    private static int STATE_ID = 29;

    private static int PREVIOUS_STATE_ID = 30;

    private FlowNodeRetrier retrier;

    @Before
    public void setUp() throws Exception {
        retrier = new FlowNodeRetrier(registry, flowNodeExecutor, activityInstanceService, flowNodeStateManager, strategy);

        given(flowNodeInstance.getId()).willReturn(FLOW_NODE_INSTANCE_ID);
        given(flowNodeInstance.getStateId()).willReturn(STATE_ID);
        given(flowNodeInstance.getPreviousStateId()).willReturn(PREVIOUS_STATE_ID);
        given(flowNodeInstance.getProcessDefinitionId()).willReturn(PROCESS_DEFINITION_ID);
        given(flowNodeInstance.getParentProcessInstanceId()).willReturn(PROCESS_INSTANCE_ID);
        given(flowNodeInstance.getName()).willReturn(FLOW_NODE_NAME);

    }

    @Test
    public void retry_should_reset_connectors_set_the_previous_state_and_execute_flow_node() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);

        given(flowNodeStateManager.getState(STATE_ID)).willReturn(new FailedActivityStateImpl());
        given(flowNodeStateManager.getState(PREVIOUS_STATE_ID)).willReturn(new InitializingActivityStateImpl(mock(StateBehaviors.class)));

        //when
        retrier.retry(FLOW_NODE_INSTANCE_ID);

        //then
        verify(strategy).resetConnectorsOf(FLOW_NODE_INSTANCE_ID);
        verify(flowNodeExecutor).setStateByStateId(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, PREVIOUS_STATE_ID);
        verify(registry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, FLOW_NODE_INSTANCE_ID, null, null);
    }

    @Test
    public void retry_should_not_execute_flow_node_when_previous_state_is_terminal() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);

        given(flowNodeStateManager.getState(STATE_ID)).willReturn(new FailedActivityStateImpl());
        given(flowNodeStateManager.getState(PREVIOUS_STATE_ID)).willReturn(new CompletedActivityStateImpl());

        //when
        retrier.retry(FLOW_NODE_INSTANCE_ID);

        //then
        verify(registry, never()).executeFlowNode(anyLong(), anyLong(), anyLong(), any(SExpressionContext.class), Matchers.<List<SOperation>> any());
        verify(flowNodeExecutor).setStateByStateId(PROCESS_DEFINITION_ID, FLOW_NODE_INSTANCE_ID, PREVIOUS_STATE_ID);
    }

    @Test(expected = ActivityExecutionException.class)
    public void retryTask_should_throw_ActivityExecutionException_when_activityInstanceService_throws_exception() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willThrow(new SFlowNodeReadException(""));

        //when
        retrier.retry(FLOW_NODE_INSTANCE_ID);

        //then exception
    }

    @Test(expected = ActivityInstanceNotFoundException.class)
    public void retryTask_should_throw_ActivityInstanceNotFoundException_when_activityInstanceService_throws_SFlowNodeNotFoundException() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willThrow(new SFlowNodeNotFoundException(FLOW_NODE_INSTANCE_ID));

        //when
        retrier.retry(FLOW_NODE_INSTANCE_ID);

        //then exception
    }

    @Test
    public void retry_should_throw_exception_when_flowNode_is_not_in_failed_state() throws Exception {
        //given
        given(activityInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);
        given(flowNodeStateManager.getState(STATE_ID)).willReturn(new ReadyActivityStateImpl(mock(StateBehaviors.class)));

        try {
            //when
            retrier.retry(FLOW_NODE_INSTANCE_ID);
            fail("Exception expected");
        } catch (ActivityExecutionException e) {
            //then
            assertThat(e.getMessage()).isEqualTo(
                    "Unable to retry the flow node instance [name=" + FLOW_NODE_NAME + ", id="
                            + FLOW_NODE_INSTANCE_ID + "] because it is not in failed state. The current state for this flow node instance is '"
                            + ActivityStates.READY_STATE + "'");
        }

    }

}
