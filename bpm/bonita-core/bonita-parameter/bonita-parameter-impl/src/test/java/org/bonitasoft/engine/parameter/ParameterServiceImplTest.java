/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.parameter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterServiceImplTest {

    @Mock
    private Recorder recorder;
    @Mock
    ReadPersistenceService persistenceService;
    @Mock
    EventService eventService;

    @Captor
    private ArgumentCaptor<SelectListDescriptor<SParameter>> getSelectDescriptor;

    @InjectMocks
    private ParameterServiceImpl parameterService;

    public ParameterServiceImplTest() {
    }

    @Test
    public void update_should_call_internal_update_with_retrieved_existing_value() throws Exception {
        final ParameterServiceImpl spy = spy(parameterService);
        final String aParam = "aParam";
        final long processDefinitionId = 1544878L;
        final SParameterImpl sParameter = new SParameterImpl(aParam, "value", processDefinitionId);
        doReturn(sParameter).when(spy).get(processDefinitionId, aParam);

        final String newValue = "newValue";
        spy.update(processDefinitionId, aParam, newValue);

        verify(spy).update(sParameter, newValue);
    }

    @Test
    public void update_shoudl_call_recordUpdate_on_recorder() throws Exception {
        parameterService.update(mock(SParameter.class), "value");
        verify(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));
    }

    @Test(expected = SParameterNameNotFoundException.class)
    public void updateUnexistingParameter() throws Exception {
        parameterService.update(123L, "aParam", "newValue");
    }

    @Test
    public void addAll_should_call_record_for_all_parameters() throws Exception {
        HashMap<String, String> parameters = new HashMap<>(3);
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        parameters.put("param3", "value3");
        parameterService.addAll(123L, parameters);
        verify(recorder, times(3)).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test
    public void deleteAll_should_call_recordDelete() throws Exception {
        final long processDefinitionId = 123L;
        final ParameterServiceImpl spy = spy(parameterService);
        List<SParameter> parameters = new ArrayList<>();
        parameters.add(new SParameterImpl());
        doReturn(parameters).when(spy).get(eq(processDefinitionId), anyInt(), anyInt(), any(OrderBy.class));
        spy.deleteAll(processDefinitionId);
        verify(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    }

    @Test
    public void containsNullValues_should_return_true_for_non_empty_persistenceService_result() throws Exception {
        final ArrayList<SParameter> toBeReturned = new ArrayList<>(1);
        toBeReturned.add(new SParameterImpl());
        doReturn(toBeReturned).when(persistenceService).selectList(any(SelectListDescriptor.class));
        assertThat(parameterService.containsNullValues(123L)).isTrue();
    }

    @Test
    public void containsNullValues_should_return_false_for_empty_persistenceService_result() throws Exception {
        doReturn(Collections.emptyList()).when(persistenceService).selectList(any(SelectListDescriptor.class));
        assertThat(parameterService.containsNullValues(123L)).isFalse();
    }

    @Test
    public void getParameter_should_call_persistenceService() throws Exception {
        parameterService.get(123L, "aParam");
        verify(persistenceService).selectOne(any(SelectOneDescriptor.class));
    }

    @Test
    public void getUnexistingParameter_should_return_null() throws Exception {
        assertThat(parameterService.get(123L, "aParam")).isNull();
    }

    @Test
    public void getParameters_should_call_persistenceService_respecting_given_order() throws Exception {
        parameterService.get(123L, 0, 10, OrderBy.NAME_DESC);
        verify(persistenceService).selectList(getSelectDescriptor.capture());
        assertThat(getSelectDescriptor.getValue().getQueryOptions().getOrderByOptions().get(0))
                .isEqualTo(new OrderByOption(SParameter.class, "name", OrderByType.DESC));
    }

    @Test
    public void getParameters_should_use_pagination() throws Exception {
        parameterService.get(16546235L, 2, 7, OrderBy.NAME_ASC);
        verify(persistenceService).selectList(getSelectDescriptor.capture());
        assertThat(getSelectDescriptor.getValue().getQueryOptions().getFromIndex()).isEqualTo(2);
        assertThat(getSelectDescriptor.getValue().getQueryOptions().getNumberOfResults()).isEqualTo(7);
    }

    @Test
    public void getNullValues_should_call_persistenceService_query_getParametersWithNullValues() throws Exception {
        final long processDefinitionId = 1561654L;
        parameterService.getNullValues(processDefinitionId, 0, 12, OrderBy.NAME_ASC);
        verify(persistenceService).selectList(getSelectDescriptor.capture());
        assertThat(getSelectDescriptor.getValue().getQueryName()).isEqualTo("getParametersWithNullValues");
    }

    @Test
    public void addOrUpdate_should_update_if_parameter_already_exists() throws Exception {
        final String paramName = "paramName";
        final long processDefinitionId = 5457878L;
        final String paramValue = "paramValue";
        final ParameterServiceImpl spy = spy(parameterService);
        final SParameter sParameter = mock(SParameter.class);
        doReturn(sParameter).when(spy).get(processDefinitionId, paramName);
        spy.addOrUpdate(processDefinitionId, paramName, paramValue);
        verify(spy).update(sParameter, paramValue);
    }

    @Test
    public void getAll_shoul_get_paginated_method() throws Exception {
        final ParameterServiceImpl spy = spy(parameterService);
        final long processDefinitionId = 4656556L;
        spy.getAll(processDefinitionId);
        verify(spy).get(eq(processDefinitionId), eq(0), anyInt(), any(OrderBy.class));
    }
}
