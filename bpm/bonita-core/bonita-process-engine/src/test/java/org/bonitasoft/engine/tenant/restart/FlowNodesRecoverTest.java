/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.execution.TestFlowNodeState.stableState;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlowNodesRecoverTest {

    private static final int ABORTING_STATE_ID = 1111;
    private static final int CANCELLING_STATE_ID = 33333;

    @Mock
    private FlowNodeStateManager flownodeStateManager;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private WorkService workService;
    @Mock
    private ExecutionMonitor executionMonitor;

    private final BPMWorkFactory workFactory = new BPMWorkFactory();
    private FlowNodesRecover flowNodesRecover;
    private final List<SFlowNodeInstance> allFlowNodes = new ArrayList<>();

    private final FlowNodeState waitingState = stableState(1, SStateCategory.NORMAL);
    private final FlowNodeState abortingState = stableState(2, SStateCategory.ABORTING);
    private final FlowNodeState cancellingState = stableState(3, SStateCategory.CANCELLING);
    private final FlowNodeState normalStableState = stableState(4, SStateCategory.NORMAL);

    @Before
    public void before() throws Exception {
        ;
        flowNodesRecover = new FlowNodesRecover(workService, new TechnicalLoggerSLF4JImpl(), activityInstanceService,
                flownodeStateManager, workFactory);
        when(activityInstanceService.getFlowNodeInstancesByIds(any())).thenAnswer(invocationOnMock -> {
            List<Long> ids = invocationOnMock.getArgument(0);
            return allFlowNodes.stream().filter(f -> ids.contains(f.getId())).collect(Collectors.toList());
        });
        doReturn(waitingState).when(flownodeStateManager).getState(waitingState.getId());
        doReturn(abortingState).when(flownodeStateManager).getState(abortingState.getId());
        doReturn(cancellingState).when(flownodeStateManager).getState(cancellingState.getId());
        doReturn(normalStableState).when(flownodeStateManager).getState(normalStableState.getId());
    }

    @Test
    public final void should_register_FINISH_FLOWNODE_when_flownode_is_in_terminal_state() throws Exception {
        SAutomaticTaskInstance task = createTask(123L, true /* terminal */);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(task));

        verify(workService).registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")));

    }

    @Test
    public void should_register_EXECUTE_FLOWNODE_when_flownode_is_not_in_terminal_state() throws Exception {
        SAutomaticTaskInstance task = createTask(123L, false /* not terminal */);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(task));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_register_work_for_each_flow_nodes_executed() throws Exception {
        List<SFlowNodeInstance> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123L + i, false));
        }

        flowNodesRecover.execute(executionMonitor, flowNodeIds(list.toArray(new SFlowNodeInstance[] {})));

        assertThat(list.size()).isEqualTo(21);
        verify(workService, times(21)).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_execute_flownode_that_is_aborting_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        setState(autoTask, normalStableState);
        autoTask.setStateCategory(SStateCategory.ABORTING);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(autoTask));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public final void should_execute_flownode_that_is_cancelling_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        setState(autoTask, normalStableState);
        autoTask.setStateCategory(SStateCategory.CANCELLING);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(autoTask));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public final void should_not_execute_flownode_that_is_aborting_non_terminal_and_stable_when_the_FlowNodeState_is_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.ABORTING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(ABORTING_STATE_ID);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(autoTask));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public final void should_not_execute_flownode_that_is_cancelling_non_terminal_and_stable_when_the_FlowNodeState_is_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.CANCELLING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(CANCELLING_STATE_ID);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(autoTask));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public void should_execute_gateway_when_gateway_is_flagged_as_finished() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setHitBys("FINISH:2");

        flowNodesRecover.execute(executionMonitor, flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_not_execute_gateway_when_gateway_has_still_an_active_branch() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setHitBys("1,2");

        flowNodesRecover.execute(executionMonitor, flowNodeIds(gatewayInstance));

        verify(workService, never()).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_continue_batch_if_one_flow_node_fails() throws Exception {
        SAutomaticTaskInstance automaticTaskInstance1 = new SAutomaticTaskInstance();
        automaticTaskInstance1.setId(333L);
        automaticTaskInstance1.setTerminal(true);
        SAutomaticTaskInstance automaticTaskInstance2 = new SAutomaticTaskInstance();
        automaticTaskInstance2.setId(344L);
        automaticTaskInstance2.setTerminal(true);
        doThrow(SWorkRegisterException.class).when(workService)
                .registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")
                        && work.getParameter("flowNodeInstanceId").equals(333L)));

        flowNodesRecover.execute(executionMonitor,
                flowNodeIds(automaticTaskInstance1, automaticTaskInstance2));

        verify(workService).registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")
                && work.getParameter("flowNodeInstanceId").equals(344L)));
        verify(workService).registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")
                && work.getParameter("flowNodeInstanceId").equals(333L)));
    }

    @Test
    public void should_not_execute_gateway_when_merging_condition_is_false() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(gatewayInstance));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public void should_not_execute_gateway_that_is_not_finished() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();

        boolean shouldExecuteFlownode = flowNodesRecover.shouldBeRecovered(gatewayInstance);

        assertThat(shouldExecuteFlownode).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_CANCELLING_when_state_is_stable_but_not_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, waitingState);

        assertThat(flowNodesRecover.shouldBeRecovered(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_CANCELLING_when_state_is_stable_but_in_the_same_stateCategory()
            throws Exception {
        SCallActivityInstance boundaryEventInstance = new SCallActivityInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, cancellingState);

        assertThat(flowNodesRecover.shouldBeRecovered(boundaryEventInstance)).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_ABORTING_when_state_is_stable_but_not_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, waitingState);

        assertThat(flowNodesRecover.shouldBeRecovered(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_ABORTING_when_state_is_stable_but_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, abortingState);

        assertThat(flowNodesRecover.shouldBeRecovered(boundaryEventInstance)).isFalse();
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_ABORTING() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setStateCategory(SStateCategory.ABORTING);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_CANCELLING() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setStateCategory(SStateCategory.CANCELLING);

        flowNodesRecover.execute(executionMonitor, flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    private void setState(SFlowNodeInstance flowNodeInstance, FlowNodeState state) {
        flowNodeInstance.setStateId(state.getId());
        flowNodeInstance.setStable(state.isStable());
        flowNodeInstance.setTerminal(state.isTerminal());
    }

    private List<Long> flowNodeIds(final SFlowNodeInstance... flowNodes) {
        ArrayList<Long> nodes = new ArrayList<>();
        for (SFlowNodeInstance node : flowNodes) {
            nodes.add(node.getId());
            allFlowNodes.add(node);
        }
        return nodes;
    }

    private SAutomaticTaskInstance createTask(final long id, final boolean terminal) {
        SAutomaticTaskInstance sAutomaticTaskInstance = new SAutomaticTaskInstance();
        sAutomaticTaskInstance.setId(id);
        sAutomaticTaskInstance.setTerminal(terminal);
        sAutomaticTaskInstance.setLogicalGroup(3, 456L);
        return sAutomaticTaskInstance;
    }
}
