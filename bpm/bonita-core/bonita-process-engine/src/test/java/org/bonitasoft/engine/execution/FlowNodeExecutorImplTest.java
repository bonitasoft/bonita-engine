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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.state.SkippedFlowNodeStateImpl;
import org.bonitasoft.engine.execution.work.WrappingBonitaWork;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeExecutorImplTest {

    private static final long PROCESS_DEFINITION_ID = 302332L;
    private static final long PROCESS_INSTANCE_ID = 343332L;
    @Mock
    private WorkService workService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private ContainerRegistry containerRegistry;
    @Mock
    private FlowNodeStateManager flowNodeStateManager;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ArchiveService archiveService;
    @Captor
    private ArgumentCaptor<BonitaWork> workArgumentCaptor;
    @InjectMocks
    private FlowNodeExecutorImpl flowNodeExecutor;
    private SkippedFlowNodeStateImpl skippedFlowNodeState;

    @Before
    public void before() throws Exception {

        skippedFlowNodeState = new SkippedFlowNodeStateImpl();
        doReturn(skippedFlowNodeState).when(flowNodeStateManager).getState(SkippedFlowNodeStateImpl.ID);
        doReturn(new SProcessDefinitionImpl("process", "1.0")).when(processDefinitionService).getProcessDefinition(PROCESS_DEFINITION_ID);
    }

    @Test
    public void should_interrupt_children_when_setting_activity_to_a_terminal_state() throws Exception {
        SUserTaskInstanceImpl flowNodeInstance = aTask(1L, true);
        flowNodeInstance.setTokenCount(2);
        SUserTaskInstanceImpl task1 = aTask(2L, true);
        SUserTaskInstanceImpl task2 = aTask(3L, true);
        doReturn(Arrays.asList(task1, task2)).when(activityInstanceService).searchActivityInstances(eq(SActivityInstance.class), any(QueryOptions.class));

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(activityInstanceService).setStateCategory(task1, ABORTING);
        verify(activityInstanceService).setStateCategory(task2, ABORTING);
        verify(workService, times(2)).registerWork(workArgumentCaptor.capture());
        assertThat(getRootWorks(workArgumentCaptor.getAllValues())).extracting("description").containsOnly("ExecuteFlowNodeWork: flowNodeInstanceId: 2",
                "ExecuteFlowNodeWork: flowNodeInstanceId: 3");
    }

    @Test
    public void should_register_notifyFinish_when_setting_activity_to_a_terminal_state_with_no_children() throws Exception {
        aTask(1L, true);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(workService).registerWork(workArgumentCaptor.capture());
        assertThat(unwrap(workArgumentCaptor.getValue()).getDescription())
                .isEqualTo("NotifyChildFinishedWork: processInstanceId:" + PROCESS_INSTANCE_ID + ", flowNodeInstanceId: 1");
    }

    @Test
    public void should_set_the_state_on_the_activity() throws Exception {
        SUserTaskInstanceImpl aTask = aTask(1L, true);

        flowNodeExecutor.setStateByStateId(1L, SkippedFlowNodeStateImpl.ID);

        verify(activityInstanceService).setState(aTask, skippedFlowNodeState);
    }

    private List<BonitaWork> getRootWorks(List<BonitaWork> bonitaWorkList) {
        ArrayList<BonitaWork> bonitaWorks = new ArrayList<>();
        for (BonitaWork bonitaWork : bonitaWorkList) {
            bonitaWorks.add(unwrap(bonitaWork));
        }
        return bonitaWorks;
    }

    private BonitaWork unwrap(BonitaWork work) {
        while (work instanceof WrappingBonitaWork) {
            work = ((WrappingBonitaWork) work).getWrappedWork();
        }
        return work;
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
