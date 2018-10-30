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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

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
    TechnicalLoggerService loggerService;

    @Captor
    private ArgumentCaptor<SelectListDescriptor<SParameter>> getSelectDescriptor;

    @InjectMocks
    @Spy
    private ParameterServiceImpl parameterService;

    @Test
    public void update_should_call_internal_update_with_retrieved_existing_value() throws Exception {
        final String aParam = "aParam";
        final long processDefinitionId = 1544878L;
        final SParameterImpl sParameter = new SParameterImpl(aParam, "value", processDefinitionId);
        doReturn(sParameter).when(parameterService).get(processDefinitionId, aParam);

        final String newValue = "newValue";
        parameterService.update(processDefinitionId, aParam, newValue);

        verify(parameterService).update(sParameter, newValue);
    }

    @Test
    public void update_should_convert_parameter_value() throws Exception {
        final String newValue = "newValue";

        parameterService.update(new SParameterImpl("parameter", "defaultValue", 9874654654L), newValue);

        verify(parameterService).interpretParameterValue(newValue);
    }

    @Test
    public void add_should_convert_parameter_value() throws Exception {
        parameterService.add(9874654654L,"parameter", "defaultValue");

        verify(parameterService).interpretParameterValue("defaultValue");
    }

    @Test
    public void interpretParameterValue_should_convert_NULL_VALUES() {
        assertThat(parameterService.interpretParameterValue("")).isEqualTo("");
        assertThat(parameterService.interpretParameterValue(null)).isEqualTo(null);
        assertThat(parameterService.interpretParameterValue("someValue")).isEqualTo("someValue");
        assertThat(parameterService.interpretParameterValue("-==NULLL==-")).isEqualTo(null);
    }

    @Test
    public void update_should_call_recordUpdate_on_recorder() throws Exception {
        parameterService.update(mock(SParameter.class), "value");
        verify(recorder).recordUpdate(any(UpdateRecord.class), nullable(String.class));
    }

    @Test(expected = SParameterNameNotFoundException.class)
    public void updateNonExistingParameter() throws Exception {
        parameterService.update(123L, "aParam", "newValue");
    }

    @Test
    public void addAll_should_call_record_for_all_parameters() throws Exception {
        HashMap<String, String> parameters = new HashMap<>(3);
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        parameters.put("param3", "value3");
        parameterService.addAll(123L, parameters);
        verify(recorder, times(3)).recordInsert(any(InsertRecord.class), nullable(String.class));
    }

    @Test
    public void deleteAll_should_call_recordDelete() throws Exception {
        final long processDefinitionId = 123L;
        doReturn(Collections.singletonList(new SParameterImpl())).when(parameterService).get(eq(processDefinitionId),
                anyInt(), anyInt(), nullable(OrderBy.class));
        parameterService.deleteAll(processDefinitionId);
        verify(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
    }

    @Test
    public void containsNullValues_should_return_true_for_non_empty_persistenceService_result() throws Exception {
        final ArrayList<SParameter> toBeReturned = new ArrayList<>(1);
        toBeReturned.add(new SParameterImpl());
        doReturn(toBeReturned).when(persistenceService).selectList(any());
        assertThat(parameterService.containsNullValues(123L)).isTrue();
    }

    @Test
    public void containsNullValues_should_return_false_for_empty_persistenceService_result() throws Exception {
        doReturn(Collections.emptyList()).when(persistenceService).selectList(any());
        assertThat(parameterService.containsNullValues(123L)).isFalse();
    }

    @Test
    public void getParameter_should_call_persistenceService() throws Exception {
        parameterService.get(123L, "aParam");
        verify(persistenceService).selectOne(any(SelectOneDescriptor.class));
    }

    @Test
    public void getNonExistingParameter_should_return_null() throws Exception {
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
        final SParameter sParameter = mock(SParameter.class);
        doReturn(sParameter).when(parameterService).get(processDefinitionId, paramName);
        parameterService.addOrUpdate(processDefinitionId, paramName, paramValue);
        verify(parameterService).update(sParameter, paramValue);
    }

    @Test
    public void getAll_should_get_paginated_method() throws Exception {
        final long processDefinitionId = 4656556L;
        parameterService.getAll(processDefinitionId);
        verify(parameterService).get(eq(processDefinitionId), eq(0), anyInt(), nullable(OrderBy.class));
    }

    @Test
    public void should_merge_parameters_with_existing_parameters() throws Exception {
        final long processDefinitionId = 4656556L;
        SParameter hostParameter = sParameter("host", "localhost", processDefinitionId);
        doReturn(hostParameter).when(parameterService).get(processDefinitionId, "host");
        when(loggerService.isLoggable(any(), any())).thenReturn(true);
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("host", "192.168.0.1");
        parameters.put("password", "kittycat");
        
        parameterService.merge(processDefinitionId, parameters);

        verify(parameterService).update(hostParameter, "192.168.0.1");
        verify(parameterService, never()).add(processDefinitionId, "password", "kittycat");
        verify(loggerService, times(1)).log(eq(ParameterServiceImpl.class), any(TechnicalLogSeverity.class),
                eq("Parameter <password> doesn't exist in process definition <4656556> and has not been merged."));
    }

    private SParameter sParameter(String name, String value, long processDefinitionId) {
        return new SParameterImpl(name, value, processDefinitionId);
    }
}
