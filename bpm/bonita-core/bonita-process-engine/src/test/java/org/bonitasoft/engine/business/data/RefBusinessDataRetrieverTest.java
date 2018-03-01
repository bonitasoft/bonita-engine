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

package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.business.data.DummyBusinessDataRefBuilder.buildArchivedSimpleRefBusinessData;
import static org.bonitasoft.engine.business.data.DummyBusinessDataRefBuilder.buildSimpleRefBusinessData;
import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.business.data.SASimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAProcessInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SAutomaticTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SFlowNodeInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SProcessInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.operation.BusinessDataContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RefBusinessDataRetrieverTest {

    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private RefBusinessDataService refBusinessDataService;
    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @InjectMocks
    private RefBusinessDataRetriever retriever;
    private static long PROCESS_INSTANCE_ID = 50L;
    private static long EVENT_SUBPROCESS_ID = 5452220L;
    private static long FLOW_NODE_INSTANCE_OF_EVENT_SUBPROCESS = 213215L;
    private static long FLOW_NODE_INSTANCE_ID = 100L;
    private SAFlowNodeInstanceImpl archivedFlowNodeInstance;
    private SFlowNodeInstanceImpl flowNodeInstance;
    private SProcessInstanceImpl processInstance;
    private SAProcessInstanceImpl archivedProcessInstance;
    private SProcessInstanceImpl eventSubProcessInstance;
    private SFlowNodeInstanceImpl eventSubProcessFlowNode;

    @Before
    public void before() throws Exception {
        flowNodeInstance = new SAutomaticTaskInstanceImpl("auto1", 312490L, PROCESS_INSTANCE_ID, PROCESS_INSTANCE_ID, 0L, 0L);
        flowNodeInstance.setLogicalGroup(1, PROCESS_INSTANCE_ID);
        flowNodeInstance.setLogicalGroup(3, PROCESS_INSTANCE_ID);
        archivedFlowNodeInstance = new SAAutomaticTaskInstanceImpl((SAutomaticTaskInstance) flowNodeInstance);
        processInstance = new SProcessInstanceImpl("MyProcess", 809234L);
        processInstance.setId(PROCESS_INSTANCE_ID);
        archivedProcessInstance = new SAProcessInstanceImpl(processInstance);
        eventSubProcessInstance = new SProcessInstanceImpl();
        eventSubProcessInstance.setId(EVENT_SUBPROCESS_ID);
        eventSubProcessInstance.setCallerType(SFlowNodeType.SUB_PROCESS);
        eventSubProcessInstance.setCallerId(FLOW_NODE_INSTANCE_ID);
        eventSubProcessFlowNode = new SAutomaticTaskInstanceImpl("flownodeInEventSubProcess", 352523L, PROCESS_INSTANCE_ID, EVENT_SUBPROCESS_ID,
                0L, PROCESS_INSTANCE_ID);
        eventSubProcessFlowNode.setLogicalGroup(1, PROCESS_INSTANCE_ID);
        eventSubProcessFlowNode.setLogicalGroup(3, EVENT_SUBPROCESS_ID);

        given(flowNodeInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_OF_EVENT_SUBPROCESS)).willReturn(eventSubProcessFlowNode);
        given(flowNodeInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willReturn(flowNodeInstance);
        given(processInstanceService.getProcessInstance(PROCESS_INSTANCE_ID)).willReturn(processInstance);
        given(processInstanceService.getProcessInstance(EVENT_SUBPROCESS_ID)).willReturn(eventSubProcessInstance);
        given(flowNodeInstanceService.getLastArchivedFlowNodeInstance(SAFlowNodeInstance.class, FLOW_NODE_INSTANCE_ID)).willReturn(archivedFlowNodeInstance);
    }

    @Test
    public void getRefBusinessData_should_retrieve_data_using_process_context_when_container_is_a_process() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = buildSimpleRefBusinessData(4L);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever
                .getRefBusinessDataInstance(new BusinessDataContext("data", new Container(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retrieve_archived_data_when_container_is_a_process_and_data_not_found_in_journal() throws Exception {
        //given
        final SASimpleRefBusinessDataInstance refBusinessDataInstance = buildArchivedSimpleRefBusinessData(30L);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID))
                .willThrow(new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data"));
        given(refBusinessDataService.getSARefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever
                .getRefBusinessDataInstance(new BusinessDataContext("data", new Container(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retrieve_data_using_flow_node_context_when_container_is_a_flow_node() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = buildSimpleRefBusinessData(4L);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retry_to_retrieve_data_using_process_context_when_retrieving_data_using_flow_node_fails() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = buildSimpleRefBusinessData(4L);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID))
                .willThrow(new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data"));
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retry_when_not_found_in_journal_on_flow_node() throws Exception {
        //given
        final SASimpleRefBusinessDataInstance archRefBusinessDataInstance = buildArchivedSimpleRefBusinessData(531L);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID))
                .willThrow(new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data"));
        given(refBusinessDataService.getSAFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willReturn(archRefBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(archRefBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retry_up_to_archived_process_scope_when_not_found_in_journal_on_flow_node() throws Exception {
        //given
        final SASimpleRefBusinessDataInstance archRefBusinessDataInstance = buildArchivedSimpleRefBusinessData(531L);
        final SRefBusinessDataInstanceNotFoundException notFoundException = new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data");
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willThrow(notFoundException);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willThrow(notFoundException);
        given(refBusinessDataService.getSAFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willThrow(notFoundException);
        given(refBusinessDataService.getSARefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(archRefBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(archRefBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_look_up_to_archived_process_scope_when_not_found_in_journal_on_flow_node_when_flow_node_and_process_is_archived()
            throws Exception {
        //given
        final SASimpleRefBusinessDataInstance archRefBusinessDataInstance = buildArchivedSimpleRefBusinessData(531L);
        final SRefBusinessDataInstanceNotFoundException notFoundException = new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data");
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willThrow(notFoundException);
        given(flowNodeInstanceService.getFlowNodeInstance(FLOW_NODE_INSTANCE_ID)).willThrow(new SFlowNodeNotFoundException(FLOW_NODE_INSTANCE_ID));
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willThrow(notFoundException);
        given(refBusinessDataService.getSAFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willThrow(notFoundException);
        given(refBusinessDataService.getSARefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(archRefBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(archRefBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_get_from_root_process_for_event_subprocess() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = buildSimpleRefBusinessData(4L);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever
                .getRefBusinessDataInstance(new BusinessDataContext("data", new Container(EVENT_SUBPROCESS_ID, DataInstanceContainer.PROCESS_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_get_from_root_process_for_flow_node_in_event_subprocess() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = buildSimpleRefBusinessData(4L);

        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_OF_EVENT_SUBPROCESS))
                .willThrow(new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data"));
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(
                new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_OF_EVENT_SUBPROCESS, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);

    }

}
