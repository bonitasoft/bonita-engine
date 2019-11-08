/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodesTest {


    private static final int ABORTING_STATE_ID = 1111;
    private static final int NORMAL_STATE_ID = 2222;
    private static final int CANCELLING_STATE_ID = 33333;

    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private WorkService workService;
    @Mock
    private BPMWorkFactory workFactory;
    @Mock
    private GatewayInstanceService gatewayInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private FlowNodeStateManager flowNodeStateManager;
    @Mock
    private FlowNodeState abortingFlowNodeState;
    @Mock
    private FlowNodeState normalFlowNodeState;
    @Mock
    private FlowNodeState cancellingFlowNodeState;
    @Mock
    private WorkDescriptor finishFlowNodeWorkDescriptor;
    @Mock
    private WorkDescriptor executeFlowNodeWorkDescriptor;
    @InjectMocks
    private ExecuteFlowNodes executeFlowNodes;



    @Before
    public void before() {
        doReturn(abortingFlowNodeState).when(flowNodeStateManager).getState(ABORTING_STATE_ID);
        doReturn(normalFlowNodeState).when(flowNodeStateManager).getState(NORMAL_STATE_ID);
        doReturn(cancellingFlowNodeState).when(flowNodeStateManager).getState(CANCELLING_STATE_ID);
        doReturn(SStateCategory.ABORTING).when(abortingFlowNodeState).getStateCategory();
        doReturn(SStateCategory.NORMAL).when(normalFlowNodeState).getStateCategory();
        doReturn(SStateCategory.CANCELLING).when(cancellingFlowNodeState).getStateCategory();
        doReturn(finishFlowNodeWorkDescriptor).when(workFactory).createNotifyChildFinishedWorkDescriptor(any());
        doReturn(executeFlowNodeWorkDescriptor).when(workFactory).createExecuteFlowNodeWorkDescriptor(any());
    }

    private Iterator<Long> flowNodeIds(final SFlowNodeInstance... flowNodes) throws Exception {
        ArrayList<Long> nodes = new ArrayList<>();
        for (SFlowNodeInstance node : flowNodes) {
            nodes.add(node.getId());
            when(activityInstanceService.getFlowNodeInstance(node.getId())).thenReturn(node);
        }
        return nodes.iterator();
    }

    @Test
    public final void execute_with_terminal_flow_node_should_register_notify_work() throws Exception {

        executeFlowNodes.execute(flowNodeIds(createTask(123l, true)));

        verify(workService).registerWork(finishFlowNodeWorkDescriptor);

    }

    private SAutomaticTaskInstance createTask(final long id, final boolean terminal) {
        SAutomaticTaskInstance sAutomaticTaskInstance = new SAutomaticTaskInstance();
        sAutomaticTaskInstance.setId(id);
        sAutomaticTaskInstance.setTerminal(terminal);
        sAutomaticTaskInstance.setLogicalGroup(3, 456L);
        return sAutomaticTaskInstance;
    }

    @Test
    public final void execute_with_non_terminal_flow_node_should_register_execute_work() throws Exception {

        executeFlowNodes.execute(flowNodeIds(createTask(123L, false)));

        verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

    @Test
    public final void should_execute_flownode_that_is_aborting_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category() throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.ABORTING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(NORMAL_STATE_ID);

        executeFlowNodes.execute(flowNodeIds(autoTask));

        verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

    @Test
    public final void should_execute_flownode_that_is_cancelling_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category() throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.CANCELLING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(NORMAL_STATE_ID);

        executeFlowNodes.execute(flowNodeIds(autoTask));

        verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

    @Test
    public final void should_not_execute_flownode_that_is_aborting_non_terminal_and_stable_when_the_FlowNodeState_is_in_the_same_state_category() throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.ABORTING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(ABORTING_STATE_ID);

        executeFlowNodes.execute(flowNodeIds(autoTask));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public final void should_not_execute_flownode_that_is_cancelling_non_terminal_and_stable_when_the_FlowNodeState_is_in_the_same_state_category() throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.CANCELLING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(CANCELLING_STATE_ID);

        executeFlowNodes.execute(flowNodeIds(autoTask));

        verify(workService, never()).registerWork(any());
    }


    @Test
    public final void execute_21_flow_node_only_execute_20() throws Exception {
        ArrayList<SFlowNodeInstance> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123 + i, false));
        }

        executeFlowNodes.execute(flowNodeIds(list.toArray(new SFlowNodeInstance[]{})));

        assertThat(list.size()).isEqualTo(21);
        verify(workService, times(20)).registerWork(executeFlowNodeWorkDescriptor);
    }


    @Test
    public void should_execute_gateway_when_merging_condition_is_true() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        doReturn(true).when(gatewayInstanceService).checkMergingCondition(any(), eq(gatewayInstance));

        executeFlowNodes.execute(flowNodeIds(gatewayInstance));

        verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

    @Test
    public void should_not_execute_gateway_when_merging_condition_is_false() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);

        executeFlowNodes.execute(flowNodeIds(gatewayInstance));

        verify(workService, never()).registerWork(any());
    }


   @Test
    public void should_execute_gateway_when_stateCategory_is_ABORTING() throws Exception {
       SGatewayInstance gatewayInstance = new SGatewayInstance();
       gatewayInstance.setId(333L);
       gatewayInstance.setStateCategory(SStateCategory.ABORTING);

       executeFlowNodes.execute(flowNodeIds(gatewayInstance));

       verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_CANCELLING() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setStateCategory(SStateCategory.CANCELLING);

        executeFlowNodes.execute(flowNodeIds(gatewayInstance));

        verify(workService).registerWork(executeFlowNodeWorkDescriptor);
    }

}
