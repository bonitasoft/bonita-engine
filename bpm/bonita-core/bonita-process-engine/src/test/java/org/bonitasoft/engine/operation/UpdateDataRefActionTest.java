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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.RefBusinessDataRetriever;
import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessMultiRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SProcessSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.business.data.SSimpleRefBusinessDataInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataRefActionTest {

    public static final long FLOW_NODE_INSTANCE_ID = 123L;
    public static final long PROCESS_INSTANCE_ID = 1L;

    @Mock
    private RefBusinessDataService refBusinessDataService;

    @Mock
    private RefBusinessDataRetriever refBusinessDataRetriever;

    @InjectMocks
    private UpdateDataRefAction updateDataRefAction;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static final String PROCESS_INSTANCE = DataInstanceContainer.PROCESS_INSTANCE.name();
    public static final String ACTIVITY_INSTANCE = DataInstanceContainer.ACTIVITY_INSTANCE.name();

    @Test
    public void execute_should_update_simple_business_data_when_without_previous_refDataId() throws Exception {
        //given
        final String dataName = "address";
        final BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(FLOW_NODE_INSTANCE_ID, ACTIVITY_INSTANCE));
        final UpdateDataRefAction updateDataRefAction = new UpdateDataRefAction(refBusinessDataService, refBusinessDataRetriever);
        final SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(null);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //when
        final Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, entity.getPersistenceId());
    }

    @Test
    public void execute_should_not_update_simple_business_data_when_refDataId_is_up_to_date() throws Exception {
        //given
        final long persistenceId = 45L;
        final String dataName = "address";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(persistenceId);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(new Address(persistenceId), businessDataContext);

        //then
        verify(refBusinessDataService, never()).updateRefBusinessDataInstance(any(SSimpleRefBusinessDataInstance.class), anyLong());
    }

    @Test
    public void execute_should_update_simple_business_data_when_with_previous_refDataId() throws Exception {
        //given
        final long persistenceId = 45L;
        final String dataName = "address";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SSimpleRefBusinessDataInstance refBusinessDataInstance = createSimpleRefBusinessDataInstance(30L);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

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

    @Test
    public void execute_should_throw_exception_when_refbusinessDataRetriever_throws_exception() throws Exception {
        //given
        final String dataName = "address";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willThrow(
                new SRefBusinessDataInstanceNotFoundException(PROCESS_INSTANCE_ID, dataName));

        //then
        expectedException.expect(SEntityActionExecutionException.class);

        //when
        final Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);

        //then exception
    }

    @Test
    public void execute_should_throws_exception_when_it_is_simple_entity_and_multi_ref_data() throws Exception {
        //given
        final String dataName = "address";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //then
        expectedException.expect(SEntityActionExecutionException.class);

        //when
        final Address entity = new Address(45L);
        updateDataRefAction.execute(entity, businessDataContext);
    }

    @Test
    public void execute_should_update_multiple_business_data_without_refDataIds_at_process_level() throws Exception {
        //given
        final String dataName = "addresses";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

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
        final String dataName = "addresses";
        final BusinessDataContext businessDataContext = buildContext(dataName);

        final long persistenceId1 = 45L;
        final long persistenceId2 = 46L;
        final SMultiRefBusinessDataInstance refBusinessDataInstance = createMultiRefBusinessDataInstance(persistenceId1, persistenceId2);

        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(persistenceId1), new Address(persistenceId2)), businessDataContext);

        //then
        verify(refBusinessDataService, never()).updateRefBusinessDataInstance(refBusinessDataInstance, Arrays.asList(persistenceId1, persistenceId2));
    }

    @Test
    public void execute_should_update_multiple_business_data_with_previous_refDataIds() throws Exception {
        //given
        final String dataName = "addresses";
        final BusinessDataContext businessDataContext = buildContext(dataName);

        final long persistenceId1 = 45L;
        final long persistenceId2 = 46L;
        final SMultiRefBusinessDataInstance refBusinessDataInstance = createMultiRefBusinessDataInstance(persistenceId1, 34L);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(persistenceId1), new Address(persistenceId2)), businessDataContext);

        //then
        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, Arrays.asList(persistenceId1, persistenceId2));
    }

    @Test
    public void execute_should_throws_exception_when_service_throws_exception() throws Exception {
        //given
        final String dataName = "addresses";
        final SMultiRefBusinessDataInstance refBusinessDataInstance = createMultiRefBusinessDataInstance(34L);

        final BusinessDataContext businessDataContext = buildContext(dataName);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);
        doThrow(new SRefBusinessDataInstanceModificationException(new Exception())).when(refBusinessDataService).updateRefBusinessDataInstance(
                refBusinessDataInstance, Arrays.asList(45L, 46L));

        //then
        expectedException.expect(SEntityActionExecutionException.class);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), new Address(46L)), businessDataContext);

    }

    @Test
    public void execute_should_throw_exception_when_is_a_list_of_entity_and_simple_data_ref() throws Exception {
        //given
        final String dataName = "addresses";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SSimpleRefBusinessDataInstance refBusinessDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //then
        expectedException.expect(SEntityActionExecutionException.class);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), new Address(46L)), businessDataContext);

    }

    @Test
    public void execute_should_throws_exception_when_list_contains_null_entries() throws Exception {
        //given
        final String dataName = "addresses";
        final BusinessDataContext businessDataContext = buildContext(dataName);
        final SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        given(refBusinessDataRetriever.getRefBusinessDataInstance(businessDataContext)).willReturn(refBusinessDataInstance);

        //then
        expectedException.expect(SEntityActionExecutionException.class);

        //when
        updateDataRefAction.execute(Arrays.<Entity> asList(new Address(45L), null), businessDataContext);
    }

    private BusinessDataContext buildContext(final String dataName) {
        final BusinessDataContext businessDataContext = new BusinessDataContext(dataName, new Container(PROCESS_INSTANCE_ID, PROCESS_INSTANCE));
        return businessDataContext;
    }

    @Test
    public void handleNull_delete_the_reference_to_the_business_data() throws Exception {
        final SSimpleRefBusinessDataInstance refBusinessDataInstance = mock(SSimpleRefBusinessDataInstance.class);
        final BusinessDataContext context = buildContext("employee");
        when(refBusinessDataRetriever.getRefBusinessDataInstance(context)).thenReturn(refBusinessDataInstance);

        updateDataRefAction.handleNull(context);

        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, null);
    }

    @Test
    public void handleNull_delete_references_to_the_business_data() throws Exception {
        final SMultiRefBusinessDataInstance refBusinessDataInstance = mock(SMultiRefBusinessDataInstance.class);
        final BusinessDataContext context = buildContext("employee");
        when(refBusinessDataRetriever.getRefBusinessDataInstance(context)).thenReturn(refBusinessDataInstance);

        updateDataRefAction.handleNull(context);

        verify(refBusinessDataService).updateRefBusinessDataInstance(refBusinessDataInstance, new ArrayList<Long>());
    }

}
