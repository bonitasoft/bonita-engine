/**
 * Copyright (C) 2016-2018 Bonitasoft S.A.
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

import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.ABORTING;
import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.CANCELLING;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceInterruptorTest {

    public static final long PROCESS_DEFINITION_ID = 1231L;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @Mock
    private ContainerRegistry containerRegistry;
    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @InjectMocks
    private ProcessInstanceInterruptor processInstanceInterruptor;

    private long PROCESS_INSTANCE_ID = 5348927512390L;
    private SUserTaskInstanceImpl flownode1_stable;
    private SUserTaskInstanceImpl flownode2_unstable;
    private SUserTaskInstanceImpl flownode3_stable;

    @Before
    public void before() throws Exception {
        flownode1_stable = new SUserTaskInstanceImpl("user1", 532654L, 54336L, 5643456L, 897523454L, STaskPriority.ABOVE_NORMAL, PROCESS_DEFINITION_ID, 67547L);
        flownode1_stable.setId(9846769L);
        flownode1_stable.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        flownode1_stable.setStable(true);
        flownode2_unstable = new SUserTaskInstanceImpl("user2", 532654L, 54336L, 5643456L, 897523454L, STaskPriority.ABOVE_NORMAL, PROCESS_DEFINITION_ID, 67547L);
        flownode2_unstable.setId(432950L);
        flownode2_unstable.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        flownode2_unstable.setStable(false);
        flownode3_stable = new SUserTaskInstanceImpl("user3", 532654L, 54336L, 5643456L, 897523454L, STaskPriority.ABOVE_NORMAL, PROCESS_DEFINITION_ID, 67547L);
        flownode3_stable.setId(543522L);
        flownode3_stable.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        flownode3_stable.setStable(true);
        doReturn(Arrays.asList(flownode1_stable, flownode2_unstable, flownode3_stable)).when(flowNodeInstanceService).getFlowNodeInstances(PROCESS_INSTANCE_ID, 0, Integer.MAX_VALUE);
    }

    @Test
    public void should_set_state_category_to_ABORTING_on_all_children() throws Exception {
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING);
        //then
        verify(flowNodeInstanceService).setStateCategory(flownode1_stable, ABORTING);
        verify(flowNodeInstanceService).setStateCategory(flownode2_unstable, ABORTING);
        verify(flowNodeInstanceService).setStateCategory(flownode3_stable, ABORTING);
    }
    @Test
    public void should_set_state_category_to_CANCELLING_on_all_children() throws Exception {
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, CANCELLING);
        //then
        verify(flowNodeInstanceService).setStateCategory(flownode1_stable, CANCELLING);
        verify(flowNodeInstanceService).setStateCategory(flownode2_unstable, CANCELLING);
        verify(flowNodeInstanceService).setStateCategory(flownode3_stable, CANCELLING);
    }

    @Test
    public void should_not_interrupt_excluded_flownode() throws Exception {
        //given
        doReturn(Arrays.asList(flownode1_stable, flownode2_unstable)).when(flowNodeInstanceService).getFlowNodeInstances(PROCESS_INSTANCE_ID, 0, Integer.MAX_VALUE);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING, flownode3_stable.getId());
        //then
        verify(containerRegistry, never()).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode3_stable.getId());
    }

    @Test
    public void should_call_execute_on_stable_elements_only() throws Exception {
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING);
        //then
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode1_stable.getId());
        verify(containerRegistry, never()).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode2_unstable.getId());
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode3_stable.getId());
    }

    @Test
    public void should_not_call_execute_on_terminal_state() throws Exception {
        //given
        flownode1_stable.setTerminal(true);
        flownode3_stable.setTerminal(false);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING);
        //then
        verify(containerRegistry, never()).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode1_stable.getId());
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, flownode3_stable.getId());
    }

    @Test
    public void should_call_execute_on_gateway() throws Exception {
        //given
        // whatever the stable or terminal flag are, gateways are always executed
        SGatewayInstanceImpl sGatewayInstance = new SGatewayInstanceImpl();
        sGatewayInstance.setId(53121234L);
        sGatewayInstance.setLogicalGroup(0, PROCESS_DEFINITION_ID);
        sGatewayInstance.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        sGatewayInstance.setStable(false);
        sGatewayInstance.setTerminal(false);
        doReturn(Collections.singletonList(sGatewayInstance)).when(flowNodeInstanceService).getFlowNodeInstances(PROCESS_INSTANCE_ID,0,Integer.MAX_VALUE);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING);
        //then
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, sGatewayInstance.getId());
    }

    @Test
    public void should_set_process_instance_to_ABORTING() throws Exception {
        //given
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl("proc", PROCESS_INSTANCE_ID);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, ABORTING);
        //then
        verify(processInstanceService).setStateCategory(processInstance, ABORTING);
    }

    @Test
    public void should_set_process_instance_to_CANCELLING() throws Exception {
        //given
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl("proc", PROCESS_INSTANCE_ID);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, CANCELLING);
        //then
        verify(processInstanceService).setStateCategory(processInstance, CANCELLING);
    }

}
