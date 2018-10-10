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
package org.bonitasoft.engine.execution.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SBoundaryEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SCallActivityInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteFlowNodesTest {

    private FlowNodeStateManager flownodeStateManager;

    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private final ProcessDefinitionService processDefinitionService = mock(ProcessDefinitionService.class);
    @Mock
    private final GatewayInstanceService gatewayInstanceService = mock(GatewayInstanceService.class);
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private BPMInstancesCreator bpmInstancesCreator;
    @Mock
    private WorkService workService;
    private BPMWorkFactory workFactory =new BPMWorkFactory();
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Before
    public void before() {
        flownodeStateManager = new FlowNodeStateManagerImpl(null, null, null, null, null, null, bpmInstancesCreator, null, null,
                null, null, null, null, null, null, null, null);
        when(tenantServiceAccessor.getActivityInstanceService()).thenReturn(activityInstanceService);
        when(tenantServiceAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantServiceAccessor.getGatewayInstanceService()).thenReturn(gatewayInstanceService);
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(logger);
        when(tenantServiceAccessor.getWorkService()).thenReturn(workService);
        when(tenantServiceAccessor.getBPMWorkFactory()).thenReturn(workFactory);
        when(tenantServiceAccessor.getFlowNodeStateManager()).thenReturn(flownodeStateManager);
        when(tenantServiceAccessor.getBPMWorkFactory()).thenReturn(new BPMWorkFactory());
    }

    private ExecuteFlowNodes createExecutorWith(final SFlowNodeInstance... flowNodes) throws Exception {
        ArrayList<Long> nodes = new ArrayList<Long>();
        for (SFlowNodeInstance node : flowNodes) {
            nodes.add(node.getId());
            when(activityInstanceService.getFlowNodeInstance(node.getId())).thenReturn(node);
        }
        return new ExecuteFlowNodes(tenantServiceAccessor, nodes.iterator());
    }

    @Test
    public final void execute_with_terminal_flow_node_should_register_notify_work() throws Exception {
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(createTask(123l, true));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(work -> work.getType().equals("FINISH_FLOWNODE")));

    }

    private SAutomaticTaskInstanceImpl createTask(final long id, final boolean terminal) {
        SAutomaticTaskInstanceImpl sAutomaticTaskInstanceImpl = new SAutomaticTaskInstanceImpl();
        sAutomaticTaskInstanceImpl.setId(id);
        sAutomaticTaskInstanceImpl.setTerminal(terminal);
        sAutomaticTaskInstanceImpl.setLogicalGroup(3, 456l);
        return sAutomaticTaskInstanceImpl;
    }

    @Test
    public final void execute_with_non_terminal_flow_node_should_register_execute_work() throws Exception {
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(createTask(123l, false));

        executeFlowNodes.call();

        verify(workService).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public final void executeFlownodeShouldNotCreateWorkIfNotApplicable() throws Exception {
        SAutomaticTaskInstanceImpl autoTask = createTask(123l, false);
        ExecuteFlowNodes executeFlowNodes = spy(createExecutorWith(autoTask));
        when(executeFlowNodes.shouldExecuteFlownode(autoTask)).thenReturn(false);
        executeFlowNodes.call();

        verify(executeFlowNodes, times(0)).createExecuteFlowNodeWork(workService, logger, autoTask);

    }

    @Test
    public final void execute_21_flow_node_only_execute_20() throws Exception {
        ArrayList<SFlowNodeInstance> list = new ArrayList<SFlowNodeInstance>();
        for (int i = 1; i <= 21; i++) {
            list.add(createTask(123 + i, false));
        }
        ExecuteFlowNodes executeFlowNodes = createExecutorWith(list.toArray(new SFlowNodeInstance[] {}));

        executeFlowNodes.call();

        assertThat(list.size()).isEqualTo(21);
        verify(workService, times(20)).registerWork(argThat(work -> work.getType().equals("EXECUTE_FLOWNODE")));
    }

    @Test
    public void shouldExecuteFlownodeIfNotGateway() throws Exception {
        SUserTaskInstanceImpl gatewayInstance = new SUserTaskInstanceImpl();
        gatewayInstance.setId(17L);
        ExecuteFlowNodes executeFlowNodes = new ExecuteFlowNodes(tenantServiceAccessor, null);

        boolean shouldExecuteFlownode = executeFlowNodes.shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
        verifyNoMoreInteractions(processDefinitionService);
        verifyNoMoreInteractions(gatewayInstanceService);
    }

    @Test
    public void shouldExecuteFlownodeForGatewayWithMatchingMergeCondition() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstanceImpl();
        doReturn(true).when(gatewayInstanceService).checkMergingCondition(nullable(SProcessDefinition.class), eq(gatewayInstance));

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
    }

    @Test
    public void shouldNotExecuteFlownodeForGatewayWithNonMatchingMergeCondition() throws Exception {
        SGatewayInstance gatewayInstance = new SGatewayInstanceImpl();

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_CANCELLING_when_state_is_stable_but_not_in_the_same_stateCategory() throws Exception {
        SBoundaryEventInstanceImpl boundaryEventInstance = new SBoundaryEventInstanceImpl();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, flownodeStateManager.getState(10)/* waiting state */);

        assertThat(new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_CANCELLING_when_state_is_stable_but_in_the_same_stateCategory() throws Exception {
        SCallActivityInstanceImpl boundaryEventInstance = new SCallActivityInstanceImpl();
        boundaryEventInstance.setStateCategory(SStateCategory.CANCELLING);
        setState(boundaryEventInstance, flownodeStateManager.getState(19)/* cancelling call activity */);

        assertThat(new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(boundaryEventInstance)).isFalse();
    }

    @Test
    public void should_execute_flow_node_in_ABORTING_when_state_is_stable_but_not_in_the_same_stateCategory() throws Exception {
        SBoundaryEventInstanceImpl boundaryEventInstance = new SBoundaryEventInstanceImpl();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, flownodeStateManager.getState(10)/* waiting state */);

        assertThat(new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(boundaryEventInstance)).isTrue();
    }

    @Test
    public void should_not_execute_flow_node_in_ABORTING_when_state_is_stable_but_in_the_same_stateCategory() throws Exception {
        SBoundaryEventInstanceImpl boundaryEventInstance = new SBoundaryEventInstanceImpl();
        boundaryEventInstance.setStateCategory(SStateCategory.ABORTING);
        setState(boundaryEventInstance, flownodeStateManager.getState(20)/* aborting call activity */);

        assertThat(new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(boundaryEventInstance)).isFalse();
    }

    private void setState(SFlowNodeInstanceImpl boundaryEventInstance, FlowNodeState state) {
        boundaryEventInstance.setStateId(state.getId());
        boundaryEventInstance.setStable(state.isStable());
        boundaryEventInstance.setTerminal(state.isTerminal());
    }

   @Test
    public void should_execute_gateway_when_stateCategory_is_ABORTING() throws Exception {
        SGatewayInstanceImpl gatewayInstance = new SGatewayInstanceImpl();
        gatewayInstance.setStateCategory(SStateCategory.ABORTING);

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
    }

    @Test
    public void should_execute_gateway_when_stateCategory_is_CANCELLING() throws Exception {
        SGatewayInstanceImpl gatewayInstance = new SGatewayInstanceImpl();
        gatewayInstance.setStateCategory(SStateCategory.CANCELLING);

        boolean shouldExecuteFlownode = new ExecuteFlowNodes(tenantServiceAccessor, null).shouldExecuteFlownode(gatewayInstance);

        assertThat(shouldExecuteFlownode).isTrue();
    }

}
