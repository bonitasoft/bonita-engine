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

package org.bonitasoft.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.ABORTING;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.SkippedFlowNodeStateImpl;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.work.WorkDescriptor;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeExecutorImplTest {

    private static final long PROCESS_INSTANCE_ID = 343332L;
    @Mock
    private WorkService workService;
    private BPMWorkFactory workFactory = new BPMWorkFactory();
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private ContainerRegistry containerRegistry;
    @Mock
    private FlowNodeStateManager flowNodeStateManager;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceInterruptor processInstanceInterruptor;
    @Mock
    private ArchiveService archiveService;
    @Mock
    private StateBehaviors stateBehaviors;
    @Captor
    private ArgumentCaptor<WorkDescriptor> workDescriptorArgumentCaptor;
    private FlowNodeExecutorImpl flowNodeExecutor;
    private SkippedFlowNodeStateImpl skippedFlowNodeState;

    @Before
    public void before() throws Exception {
        flowNodeExecutor = new FlowNodeExecutorImpl(flowNodeStateManager, activityInstanceService, archiveService,
                null, containerRegistry, processDefinitionService, null, null, null, null, workService, workFactory,
                null,processInstanceInterruptor);
        skippedFlowNodeState = new SkippedFlowNodeStateImpl();
        doReturn(skippedFlowNodeState).when(flowNodeStateManager).getState(SkippedFlowNodeStateImpl.ID);
        doReturn(stateBehaviors).when(flowNodeStateManager).getStateBehaviors();
    }

    @Test
    public void should_abort_children_when_setting_activity_to_a_terminal_state() throws Exception {
        SUserTaskInstanceImpl flowNodeInstance = aTask(1L, true);
        flowNodeInstance.setTokenCount(2);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(processInstanceInterruptor).interruptChildrenOfFlowNodeInstance(flowNodeInstance, ABORTING);
    }

    @Test
    public void should_interrupt_boundary_when_setting_activity_to_a_terminal_state() throws Exception {
        SUserTaskInstanceImpl sUserTaskInstance = aTask(1L, true);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(stateBehaviors).interruptAttachedBoundaryEvent(any(),eq(sUserTaskInstance),eq(ABORTING));
    }

    @Test
    public void should_register_notifyFinish_when_setting_activity_to_a_terminal_state_with_no_children() throws Exception {
        aTask(1L, true);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(workService).registerWork(workDescriptorArgumentCaptor.capture());
        assertThat(workDescriptorArgumentCaptor.getValue().getType())
                .isEqualTo("FINISH_FLOWNODE");
    }

    @Test
    public void should_set_the_state_on_the_activity() throws Exception {
        SUserTaskInstanceImpl aTask = aTask(1L, true);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(activityInstanceService).setState(aTask, skippedFlowNodeState);
    }

    private SUserTaskInstanceImpl aTask(long id, boolean stable) throws SFlowNodeReadException, SFlowNodeNotFoundException {
        SUserTaskInstanceImpl sUserTaskInstance = new SUserTaskInstanceImpl();
        sUserTaskInstance.setId(id);
        sUserTaskInstance.setParentContainerId(PROCESS_INSTANCE_ID);
        sUserTaskInstance.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        sUserTaskInstance.setStable(stable);
        doReturn(sUserTaskInstance).when(activityInstanceService).getFlowNodeInstance(id);
        return sUserTaskInstance;
    }

}
