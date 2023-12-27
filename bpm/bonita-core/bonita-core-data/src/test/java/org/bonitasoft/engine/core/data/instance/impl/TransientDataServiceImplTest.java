/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.data.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.SShortTextDataInstance;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransientDataServiceImplTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private ExpressionResolverService expressionResolverService;

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @InjectMocks
    @Spy
    private TransientDataServiceImpl transientDataServiceImpl;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void before() {
        when(cacheService.getCachesNames()).thenReturn(Arrays.asList("transient_data"));
    }

    @Test
    public void should_createDataInstance_add_in_cache() throws Exception {
        // given
        SShortTextDataInstance data = createData(12, 42, "name", "containerType");

        // when
        transientDataServiceImpl.createDataInstance(data);

        // then
        assertThat(data.getId()).isGreaterThan(0);
        verify(cacheService, times(1)).store("transient_data", "name:42:containerType", data);
    }

    private SShortTextDataInstance createData(final long id, final int containerId, final String name,
            final String containerType) throws SCacheException {
        SShortTextDataInstance data = new SShortTextDataInstance();
        data.setId(id);
        data.setName(name);
        data.setTransientData(true);
        data.setContainerId(containerId);
        data.setContainerType(containerType);
        data.setValue("A value");
        when(cacheService.get("transient_data", name + ":" + containerId + ":" + containerType)).thenReturn(data);
        return data;
    }

    @Test
    public void testUpdateDataInstance() throws Exception {
        // given
        SShortTextDataInstance data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", "newValue");
        transientDataServiceImpl.updateDataInstance(data, entityUpdateDescriptor);

        // then
        assertThat(transientDataServiceImpl.getDataInstance(12).getValue()).isEqualTo("newValue");
        verify(cacheService, times(1)).store("transient_data", "name:42:ctype", data);
    }

    @Test
    public void testDeleteDataInstance() throws Exception {
        SShortTextDataInstance data = createData(12, 42, "name", "ctype");

        transientDataServiceImpl.deleteDataInstance(data);

        verify(cacheService).remove("transient_data", "name:42:ctype");
    }

    @Test
    public void should_getDataInstanceById_return_the_data() throws Exception {
        // given
        SShortTextDataInstance data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        SDataInstance result = transientDataServiceImpl.getDataInstance(12);

        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testGetDataInstanceStringLongString() throws Exception {
        // given
        SShortTextDataInstance data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        SDataInstance result = transientDataServiceImpl.getDataInstance("name", 42, "ctype");

        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void should_get_multiple_data_instances_from_a_container() throws Exception {
        SFlowNodeInstance flowNodeInstance = flowNodeInstance(42, 1);
        SActivityDefinition activityDefinition = flowNodeDefinition(dataWithName("name", null),
                dataWithName("name1", null),
                dataWithName("name2", null));
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        SFlowElementContainerDefinition container = mock(SFlowElementContainerDefinition.class);
        when(processDef.getProcessContainer()).thenReturn(container);
        when(container.getFlowNode(42)).thenReturn(activityDefinition);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(processDef);
        when(flowNodeInstanceService.getFlowNodeInstance(42)).thenReturn(flowNodeInstance);
        SShortTextDataInstance data = createData(12, 42, "name", "ctype");
        SShortTextDataInstance data1 = createData(13, 42, "name1", "ctype");
        SShortTextDataInstance data2 = createData(14, 42, "name2", "ctype");
        when(cacheService.getKeys("transient_data"))
                .thenReturn(Arrays.asList("name:42:ctype", "name:44:ctype", "name:48:ctype"));

        List<SDataInstance> dataInstances = transientDataServiceImpl.getDataInstances(42, "ctype", 0, 10);

        assertThat(dataInstances.size()).isEqualTo(3);
        assertThat(dataInstances).contains(data, data1, data2);
    }

    @Test
    public void should_return_empty_data_list_for_flownodes_not_in_definition() throws Exception {
        //happens when flow node is e.g. a manual task
        SProcessDefinitionImpl processDefinition = new SProcessDefinitionImpl("p", "1");
        SFlowNodeInstance flowNodeInstance = flowNodeInstance(42, 1);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(processDefinition);
        when(flowNodeInstanceService.getFlowNodeInstance(42)).thenReturn(flowNodeInstance);

        List<SDataInstance> dataInstances = transientDataServiceImpl.getDataInstances(42, "ctype", 0, 10);

        assertThat(dataInstances).isEmpty();
    }

    private SDataDefinition dataWithName(String dataName, SExpression defaultValueExpression) {
        SDataDefinition dataDef = mock(SDataDefinition.class);
        when(dataDef.isTransientData()).thenReturn(true);
        when(dataDef.getName()).thenReturn(dataName);
        if (defaultValueExpression != null) {
            when(dataDef.getDefaultValueExpression()).thenReturn(defaultValueExpression);
        }
        return dataDef;
    }

    @Test
    public void should_paginate_result_when_retrieving_multiple_DataInstance() throws Exception {
        SFlowNodeInstance flowNodeInstance = flowNodeInstance(42, 1);
        SDataDefinition dataDef = dataWithName("name", null);
        SActivityDefinition activityDefinition = flowNodeDefinition(dataDef);
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        SFlowElementContainerDefinition container = mock(SFlowElementContainerDefinition.class);
        when(processDef.getProcessContainer()).thenReturn(container);
        when(container.getFlowNode(42)).thenReturn(activityDefinition);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(processDef);
        when(flowNodeInstanceService.getFlowNodeInstance(42)).thenReturn(flowNodeInstance);
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        assertThat(transientDataServiceImpl.getDataInstances(42, "ctype", 0, 10)).hasSize(1);
        assertThat(transientDataServiceImpl.getDataInstances(42, "ctype", 0, 1)).hasSize(1);
        assertThat(transientDataServiceImpl.getDataInstances(42, "ctype", 1, 1)).isEmpty();
    }

    @Test
    public void should_reevaluate_a_transient_data_instance_if_not_found_in_cache_but_data_definition_exists()
            throws Exception {
        // given
        SFlowNodeInstance flowNodeInstance = flowNodeInstance(42, 1);
        SExpression defaultValueExpression = mock(SExpression.class);
        SDataDefinition dataDef = dataWithName("name", defaultValueExpression);
        SActivityDefinition activityDefinition = flowNodeDefinition(dataDef);
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        SFlowElementContainerDefinition container = mock(SFlowElementContainerDefinition.class);
        when(processDef.getProcessContainer()).thenReturn(container);
        when(container.getFlowNode(42)).thenReturn(activityDefinition);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(processDef);
        when(flowNodeInstanceService.getFlowNodeInstance(42)).thenReturn(flowNodeInstance);
        when(expressionResolverService.evaluate(eq(defaultValueExpression), notNull())).thenReturn("new data value");

        // when
        transientDataServiceImpl.getDataInstance("name", 42, "ctype");

        // then
        verify(expressionResolverService).evaluate(eq(defaultValueExpression), notNull());
        ArgumentCaptor<SDataInstance> argumentCaptor = ArgumentCaptor.forClass(SDataInstance.class);
        verify(transientDataServiceImpl).createDataInstance(argumentCaptor.capture());
        SDataInstance dataInstance = argumentCaptor.getValue();
        verify(cacheService).store(TransientDataServiceImpl.TRANSIENT_DATA_CACHE_NAME,
                TransientDataServiceImpl.getKey(dataInstance), dataInstance);
    }

    @Test
    public void should_throw_a_SDataInstanceException_when_trying_reevaluate_a_data_not_defined()
            throws Exception {
        // given
        SFlowNodeInstance flowNodeInstance = flowNodeInstance(42, 1);
        SActivityDefinition activityDefinition = flowNodeDefinition();
        SProcessDefinition processDef = mock(SProcessDefinition.class);
        SFlowElementContainerDefinition container = mock(SFlowElementContainerDefinition.class);
        when(processDef.getProcessContainer()).thenReturn(container);
        when(container.getFlowNode(42)).thenReturn(activityDefinition);
        when(processDefinitionService.getProcessDefinition(1)).thenReturn(processDef);
        when(flowNodeInstanceService.getFlowNodeInstance(42)).thenReturn(flowNodeInstance);

        // then
        expectedException.expect(SDataInstanceException.class);

        // when
        transientDataServiceImpl.getDataInstance("name", 42, "ctype");
    }

    private SFlowNodeInstance flowNodeInstance(long defId, long processDefId) {
        SFlowNodeInstance instance = mock(SFlowNodeInstance.class);
        when(instance.getFlowNodeDefinitionId()).thenReturn(defId);
        when(instance.getProcessDefinitionId()).thenReturn(processDefId);
        return instance;
    }

    private SActivityDefinition flowNodeDefinition(SDataDefinition... dataDefs) {
        SActivityDefinition definition = mock(SActivityDefinition.class);
        if (dataDefs.length > 0) {
            when(definition.getSDataDefinitions()).thenReturn(Arrays.asList(dataDefs));
        } else {
            when(definition.getSDataDefinitions()).thenReturn(Collections.emptyList());
        }
        return definition;
    }
}
