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

package org.bonitasoft.engine.operation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.Entity;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataRefActionTest {

    public static final long FLOW_NODE_INSTANCE_ID = 123L;
    public static final long PROCESS_INSTANCE_ID = 1L;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @InjectMocks
    private UpdateDataRefAction updateDataRefAction;

    public static final String PROCESS_INSTANCE = DataInstanceContainer.PROCESS_INSTANCE.name();
    public static final String ACTIVITY_INSTANCE = DataInstanceContainer.ACTIVITY_INSTANCE.name();

    @Test
    public void execute_should_update_simple_business_data_at_flow_node_level() throws Exception {
        //given
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(FLOW_NODE_INSTANCE_ID, ACTIVITY_INSTANCE));
        UpdateDataRefAction updateDataRefAction = new UpdateDataRefAction(refBusinessDataService, flowNodeInstanceService);
        SSimpleRefBusinessDataInstance refBusinessDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance(dataName, FLOW_NODE_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, entity.getPersistenceId());
    }

    @Test
    public void execute_should_update_simple_business_data_without_refDataId_at_process_level() throws Exception {
        //given
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(null);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, entity.getPersistenceId());
    }

    @Test
    public void execute_should_not_update_simple_business_data_when_refDataId_is_up_to_date() throws Exception {
        //given
        long persistenceId = 45L;
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(persistenceId);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(new Address(persistenceId), businessDataContext);

        //then
        verify(refBusinessDataService, never()).updateRefBusinessDataInstance(any(SSimpleRefBusinessDataInstance.class), anyLong());
    }

    @Test
    public void execute_should_update_simple_business_data_when_with_previous_refDataId() throws Exception {
        //given
        long persistenceId = 45L;
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(30L);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(new Address(persistenceId), businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, persistenceId);
    }

    private SSimpleRefBusinessDataInstance createSimpleRefBusinessDataInstance(final Long dataId) {
        final SSimpleRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessSimpleRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataId(dataId);
        return sRefBusinessDataInstanceImpl;
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throw_exception_when_refbusinessDataService_throws_exception() throws Exception {
        //given
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willThrow(
                new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, dataName));

        //when
        Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then exception
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_when_it_is_simple_entity_and_multi_ref_data() throws Exception {
        //given
        String dataName = "address";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then exception
    }

    @Test
    public void execute_should_retry_update_simple_data_using_process_instance_when_flown_node_instance_fails() throws Exception {
        //given
        String dataName = "address";
        Container container = new Container(FLOW_NODE_INSTANCE_ID, ACTIVITY_INSTANCE);
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, container);

        SSimpleRefBusinessDataInstance refBusinessDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        given(refBusinessDataService.getFlowNodeRefBusinessDataInstance(dataName, FLOW_NODE_INSTANCE_ID)).willThrow(
                new SRefBusinessDataInstanceNotFoundException(FLOW_NODE_INSTANCE_ID, dataName));

        given(flowNodeInstanceService.getProcessInstanceId(container.getId(), container.getType())).willReturn(PROCESS_INSTANCE_ID);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, entity.getPersistenceId());
    }

    @Test
    public void execute_should_update_multiple_business_data_without_refDataIds_at_process_level() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), new Address(46L)), businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, Arrays.asList(45L, 46L));
    }

    private SMultiRefBusinessDataInstance createMultiRefBusinessDataInstance(final Long... dataIds) {
        final SProcessMultiRefBusinessDataInstanceImpl sRefBusinessDataInstanceImpl = new SProcessMultiRefBusinessDataInstanceImpl();
        sRefBusinessDataInstanceImpl.setDataIds(Arrays.asList(dataIds));
        return sRefBusinessDataInstanceImpl;
    }

    @Test
    public void execute_should_not_update_multiple_business_data_when_ref_dataIds_are_up_to_date() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));

        long persistenceId1 = 45L;
        long persistenceId2 = 46L;
        SMultiRefBusinessDataInstance refBusinessDataInstance = createMultiRefBusinessDataInstance(persistenceId1, persistenceId2);

        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(persistenceId1), new Address(persistenceId2)), businessDataContext);

        //then
        verify(refBusinessDataService, never()).updateRefBusinessDataInstance(refBusinessDataInstance, Arrays.asList(persistenceId1, persistenceId2));
    }

    @Test
    public void execute_should_update_multiple_business_data_with_previous_refDataIds() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));

        long persistenceId1 = 45L;
        long persistenceId2 = 46L;
        SMultiRefBusinessDataInstance refBusinessDataInstance = createMultiRefBusinessDataInstance(persistenceId1, 34L);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(persistenceId1), new Address(persistenceId2)), businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, Arrays.asList(persistenceId1, persistenceId2));
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_when_service_throws_exception() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willThrow(
                new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, dataName));

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), new Address(46L)), businessDataContext);

        //then exception
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throw_exception_when_is_a_list_of_entity_and_simple_data_ref() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SSimpleRefBusinessDataInstance refBusinessDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), new Address(46L)), businessDataContext);

        //then exception
    }

    @Test(expected = SEntityActionExecutionException.class)
    public void execute_should_throws_exception_when_list_contains_null_entries() throws Exception {
        //given
        String dataName = "addresses";
        BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataService.getRefBusinessDataInstance(dataName, PROCESS_INSTANCE_ID)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), null), businessDataContext);

        //then exception
    }

}
