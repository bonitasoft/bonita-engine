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
import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.operation.BusinessDataContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RefBusinessDataRetrieverTest {

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @InjectMocks
    private RefBusinessDataRetriever retriever;

    private static long PROCESS_INSTANCE_ID = 50L;

    private static long FLOW_NODE_INSTANCE_ID = 100L;


    @Test
    public void getRefBusinessData_should_retrieve_data_using_process_context_when_container_is_a_process() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(4L);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(new BusinessDataContext("data", new Container(PROCESS_INSTANCE_ID, DataInstanceContainer.PROCESS_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retrieve_data_using_flow_node_context_when_container_is_a_flown_ode() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(4L);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    @Test
    public void getRefBusinessData_should_retry_to_retrieve_data_using_process_context_when_retrieving_data_using_flow_node_fails() throws Exception {
        //given
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(4L);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance("data", FLOW_NODE_INSTANCE_ID)).willThrow(new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, "data"));
        given(flowNodeInstanceService.getProcessInstanceId(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())).willReturn(PROCESS_INSTANCE_ID);
        given(refBusinessDataService.getRefBusinessDataInstance("data", PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        SRefBusinessDataInstance retrievedData = retriever.getRefBusinessDataInstance(new BusinessDataContext("data", new Container(FLOW_NODE_INSTANCE_ID, DataInstanceContainer.ACTIVITY_INSTANCE.name())));

        //then
        assertThat(retrievedData).isEqualTo(refBusinessDataInstance);
    }

    private SSimpleRefBusinessDataInstance createSimpleRefBusinessDataInstance(final Long dataId) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataId(dataId);
        return sRefBusinessDataInstanceImpl;
    }

}