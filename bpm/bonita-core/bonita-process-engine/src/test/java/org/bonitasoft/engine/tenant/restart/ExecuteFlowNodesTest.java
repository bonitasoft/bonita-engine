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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.archive.BPMArchiverService;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodesTest {

    private static final int ABORTING_STATE_ID = 1111;
    private static final int NORMAL_STATE_ID = 61;
    private static final int CANCELLING_STATE_ID = 33333;
    public static final int BATCH_RESTART_SIZE = 10;
    private FlowNodeStateManager flownodeStateManager;

    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private WorkService workService;
    @Mock
    private GatewayInstanceService gatewayInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private BPMInstancesCreator bpmInstancesCreator;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private BPMArchiverService bpmArchiverService;

    private BPMWorkFactory workFactory = new BPMWorkFactory();
    private ExecuteFlowNodes executeFlowNodes;
    private List<SFlowNodeInstance> allFlowNodes = new ArrayList<>();

    @Before
    public void before() throws Exception {
        flownodeStateManager = new FlowNodeStateManagerImpl(null, null, null, null, bpmInstancesCreator,
                null, null,
                null, null, null, bpmArchiverService);
        executeFlowNodes = new ExecuteFlowNodes(workService, new TechnicalLoggerSLF4JImpl(), activityInstanceService,
                gatewayInstanceService,
                processDefinitionService, flownodeStateManager, workFactory, userTransactionService,
                BATCH_RESTART_SIZE);
        doAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call()).when(userTransactionService)
                .executeInTransaction(any());
        when(activityInstanceService.getFlowNodeInstancesByIds(any())).thenAnswer(invocationOnMock -> {
            List<Long> ids = invocationOnMock.getArgument(0);
            return allFlowNodes.stream().filter(f -> ids.contains(f.getId())).collect(Collectors.toList());
        });
    }

    @Test
    public final void execute_with_terminal_flow_node_should_register_notify_work() throws Exception {

        executeFlowNodes.executeFlowNodes(flowNodeIds(createTask(123l, true)));

        verify(workService).registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")));

    }

    @Test
    public final void execute_with_non_terminal_flow_node_should_register_execute_work() throws Exception {

        executeFlowNodes.executeFlowNodes(flowNodeIds(createTask(123L, false)));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_create_work_for_all_works() throws Exception {
        List<SFlowNodeInstance> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123L + i, false));
        }

        executeFlowNodes.executeFlowNodes(flowNodeIds(list.toArray(new SFlowNodeInstance[] {})));

        assertThat(list.size()).isEqualTo(21);
        verify(workService, times(21)).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }

    @Test
    public void should_continue_to_restart_flow_node_event_if_one_batch_failed() throws Exception {
        doThrow(new UnsupportedOperationException("current batch failed, sorry ¯\\_(ツ)_/¯"))
                .doAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call())
                .when(userTransactionService).executeInTransaction(any());
        List<SFlowNodeInstance> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123L + i, false));
        }

        executeFlowNodes.executeFlowNodes(flowNodeIds(list.toArray(new SFlowNodeInstance[] {})));

        verify(workService, times(11)).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }

    @Test
    public void should_execute_flownode_that_is_aborting_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.ABORTING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(NORMAL_STATE_ID);

        executeFlowNodes.executeFlowNodes(flowNodeIds(autoTask));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public final void should_execute_flownode_that_is_cancelling_non_terminal_and_stable_when_the_FlowNodeState_is_not_in_the_same_state_category()
            throws Exception {
        SAutomaticTaskInstance autoTask = createTask(123L, false);
        autoTask.setStateCategory(SStateCategory.CANCELLING);
        autoTask.setTerminal(false);
        autoTask.setStable(true);
        autoTask.setStateId(NORMAL_STATE_ID);

        executeFlowNodes.executeFlowNodes(flowNodeIds(autoTask));

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

        executeFlowNodes.executeFlowNodes(flowNodeIds(autoTask));

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

        executeFlowNodes.executeFlowNodes(flowNodeIds(autoTask));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public void should_execute_gateway_when_merging_condition_is_true() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        doReturn(true).when(gatewayInstanceService).checkMergingCondition(any(), eq(gatewayInstance));

        executeFlowNodes.executeFlowNodes(flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_continue_batch_if_one_flow_node_fails() throws Exception {
        SGatewayInstance gatewayInstance1 = new SGatewayInstance();
        gatewayInstance1.setId(333L);
        SGatewayInstance gatewayInstance2 = new SGatewayInstance();
        gatewayInstance2.setId(344L);
        doThrow(SBonitaReadException.class).when(gatewayInstanceService).checkMergingCondition(any(),
                eq(gatewayInstance1));
        doReturn(true).when(gatewayInstanceService).checkMergingCondition(any(), eq(gatewayInstance2));

        executeFlowNodes.executeFlowNodes(flowNodeIds(gatewayInstance1, gatewayInstance2));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")
                && work.getParameter("flowNodeInstanceId").equals(344L)));
        verifyNoMoreInteractions(workService);
    }

    @Test
    public void should_not_execute_gateway_when_merging_condition_is_false() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);

        executeFlowNodes.executeFlowNodes(flowNodeIds(gatewayInstance));

        verify(workService, never()).registerWork(any());
    }

    @Test
    public void should_not_execute_gateway_that_is_not_finished() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();

        boolean shouldExecuteFlownode = executeFlowNodes.shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_CANCELLING_when_state_is_stable_but_not_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, flownodeStateManager.getState(10)/* waiting state */);

        assertThat(executeFlowNodes.shouldExecuteFlownode(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_CANCELLING_when_state_is_stable_but_in_the_same_stateCategory()
            throws Exception {
        SCallActivityInstance boundaryEventInstance = new SCallActivityInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, flownodeStateManager.getState(19)/* cancelling call activity */);

        assertThat(executeFlowNodes.shouldExecuteFlownode(boundaryEventInstance)).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_ABORTING_when_state_is_stable_but_not_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, flownodeStateManager.getState(10)/* waiting state */);

        assertThat(executeFlowNodes.shouldExecuteFlownode(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_ABORTING_when_state_is_stable_but_in_the_same_stateCategory()
            throws Exception {
        SBoundaryEventInstance boundaryEventInstance = new SBoundaryEventInstance();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, flownodeStateManager.getState(20)/* aborting call activity */);

        assertThat(executeFlowNodes.shouldExecuteFlownode(boundaryEventInstance)).isFalse();
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_ABORTING() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setStateCategory(SStateCategory.ABORTING);

        executeFlowNodes.executeFlowNodes(flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_CANCELLING() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstance();
        gatewayInstance.setId(333L);
        gatewayInstance.setStateCategory(SStateCategory.CANCELLING);

        executeFlowNodes.executeFlowNodes(flowNodeIds(gatewayInstance));

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    private void setState(SFlowNodeInstance boundaryEventInstance, FlowNodeState state) {
        boundaryEventInstance.setStateId(state.getId());
        boundaryEventInstance.setStable(state.isStable());
        boundaryEventInstance.setTerminal(state.isTerminal());
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
