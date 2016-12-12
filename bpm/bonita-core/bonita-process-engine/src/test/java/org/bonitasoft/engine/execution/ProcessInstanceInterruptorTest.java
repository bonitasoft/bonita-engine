/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    @Captor
    private ArgumentCaptor<QueryOptions> queryOptionsArgumentCaptor;

    @InjectMocks
    private ProcessInstanceInterruptor processInstanceInterruptor;
    private long PROCESS_INSTANCE_ID = 5348927512390L;
    private SUserTaskInstanceImpl user1;
    private SUserTaskInstanceImpl user2;

    @Before
    public void before() throws Exception {
        user1 = new SUserTaskInstanceImpl("user1", 532654L, 54336L, 5643456L, 897523454L, STaskPriority.ABOVE_NORMAL, PROCESS_DEFINITION_ID, 67547L);
        user1.setId(9846769L);
        user1.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        user2 = new SUserTaskInstanceImpl("user2", 532654L, 54336L, 5643456L, 897523454L, STaskPriority.ABOVE_NORMAL, PROCESS_DEFINITION_ID, 67547L);
        user2.setId(432950L);
        user2.setLogicalGroup(3, PROCESS_INSTANCE_ID);
    }

    @Test
    public void should_set_children_of_process_as_ABORTING() throws Exception {
        //given
        doReturn(Arrays.asList(user1, user2)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), any(QueryOptions.class));
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(flowNodeInstanceService).setStateCategory(user1, SStateCategory.ABORTING);
        verify(flowNodeInstanceService).setStateCategory(user2, SStateCategory.ABORTING);
    }

    @Test
    public void should_search_all_elements_of_the_process() throws Exception {
        //given
        doReturn(Arrays.asList(user1, user2)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), any(QueryOptions.class));
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), queryOptionsArgumentCaptor.capture());
        assertThat(queryOptionsArgumentCaptor.getValue().getFilters())
                .containsOnly(new FilterOption(SFlowNodeInstance.class, "stateCategory", "NORMAL"),
                        new FilterOption(SFlowNodeInstance.class, "logicalGroup4", PROCESS_INSTANCE_ID));
    }

    @Test
    public void should_search_all_elements_of_the_process_except_the_one_defined() throws Exception {
        //given
        doReturn(Arrays.asList(user1, user2)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), any(QueryOptions.class));
        //when
        long exceptionChildId = 67543543L;
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING, exceptionChildId);
        //then
        verify(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), queryOptionsArgumentCaptor.capture());
        assertThat(queryOptionsArgumentCaptor.getValue().getFilters())
                .containsOnly(new FilterOption(SFlowNodeInstance.class, "stateCategory", "NORMAL"),
                        new FilterOption(SFlowNodeInstance.class, "logicalGroup4", PROCESS_INSTANCE_ID),
                        new FilterOption(SFlowNodeInstance.class, "id", exceptionChildId, FilterOperationType.DIFFERENT));
    }

    @Test
    public void should_call_execute_on_stable_elements_only() throws Exception {
        //given
        doReturn(Arrays.asList(user1, user2)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), any(QueryOptions.class));
        user1.setStable(true);
        user2.setStable(false);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, user1.getId());
        verify(containerRegistry, never()).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, user2.getId());
    }

    @Test
    public void should_not_call_execute_on_terminal_state() throws Exception {
        //given
        doReturn(Arrays.asList(user1, user2)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class), any(QueryOptions.class));
        user1.setStable(true);
        user1.setTerminal(true);
        user2.setStable(true);
        user2.setTerminal(false);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(containerRegistry, never()).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, user1.getId());
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, user2.getId());
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
        doReturn(Collections.singletonList(sGatewayInstance)).when(flowNodeInstanceService).searchFlowNodeInstances(eq(SFlowNodeInstance.class),
                any(QueryOptions.class));
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(containerRegistry).executeFlowNode(PROCESS_DEFINITION_ID, PROCESS_INSTANCE_ID, sGatewayInstance.getId());
    }

    @Test
    public void should_set_process_instance_to_aborting() throws Exception {
        //given
        SProcessInstanceImpl processInstance = new SProcessInstanceImpl("proc", PROCESS_INSTANCE_ID);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        //when
        processInstanceInterruptor.interruptProcessInstance(PROCESS_INSTANCE_ID, SStateCategory.ABORTING);
        //then
        verify(processInstanceService).setStateCategory(processInstance, SStateCategory.ABORTING);
    }

}
