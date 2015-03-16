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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataLeftOperandHandlerTest {

    private static final long CONTAINER_ID = 125l;
    private static final String CONTAINER_TYPE = "CONTAINER_TYPE";
    private static final long PROCESS_DEFINITION_ID = 123456789l;
    @Mock
    private DataInstanceService dataInstanceService;
    @Mock
    private ParentContainerResolver parentContainerResolver;
    @InjectMocks
    private DataLeftOperandHandler handler;

    private SLeftOperandImpl createLeftOperand(final String name) {
        final SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        handler.delete(createLeftOperand("myData"), 45l, "container");
    }


    @Test
    public void loadLeftOperandInContext_should_do_one_request() throws Exception {
        //given
        SExpressionContext sExpressionContext = createContext();
        HashMap<String, Object> context = new HashMap<String, Object>();
        SDataInstance data1 = data("data1", "value1");
        SDataInstance data2 = data("data2", "value2");
        doReturn(Arrays.asList(data1, data2)).when(dataInstanceService).getDataInstances(Arrays.asList("data2", "data1"), CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        //when
        handler.loadLeftOperandInContext(Arrays.asList(leftOperand("data2"), leftOperand("data1")), sExpressionContext, context);
        verify(dataInstanceService).getDataInstances(Arrays.asList("data2", "data1"), CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        verifyNoMoreInteractions(dataInstanceService);
        //then
        assertThat(context).containsOnly(entry("data1", "value1"), entry("%DATA_INSTANCE%_data1", data1), entry("data2", "value2"), entry("%DATA_INSTANCE%_data2", data2));
    }
    @Test
    public void loadLeftOperandInContext_on_single_left_operand() throws Exception {
        //given
        SExpressionContext sExpressionContext = createContext();
        HashMap<String, Object> context = new HashMap<String, Object>();
        SDataInstance data1 = data("data1", "value1");
        doReturn(data1).when(dataInstanceService).getDataInstance("data1", CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        //when
        handler.loadLeftOperandInContext(leftOperand("data1"), sExpressionContext, context);
        verify(dataInstanceService).getDataInstance("data1", CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        verifyNoMoreInteractions(dataInstanceService);
        //then
        assertThat(context).containsOnly(entry("data1", "value1"), entry("%DATA_INSTANCE%_data1", data1));
    }


    @Test
    public void update_should_get_from_context_data_instances() throws Exception {
        //given
        HashMap<String, Object> context = new HashMap<String, Object>();
        SDataInstance data2 = data("data2", "value2");
        context.put("%DATA_INSTANCE%_data2", data2);
        //when
        handler.update(leftOperand("data2"), context, "newValue", CONTAINER_ID, CONTAINER_TYPE);

        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(dataInstanceService).updateDataInstance(eq(data2), captor.capture());
        verifyNoMoreInteractions(dataInstanceService);
        //then
        assertThat(captor.getValue().getFields().get("value")).isEqualTo("newValue");
    }

    @Test
    public void update_should_get_from_service_data_instances_when_not_in_context() throws Exception {
        //given
        HashMap<String, Object> context = new HashMap<String, Object>();
        SDataInstance data2 = data("data2", "value2");
        doReturn(data2).when(dataInstanceService).getDataInstance("data2", CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        //when
        handler.update(leftOperand("data2"), context, "newValue", CONTAINER_ID, CONTAINER_TYPE);

        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(dataInstanceService).updateDataInstance(eq(data2), captor.capture());
        verify(dataInstanceService).getDataInstance("data2", CONTAINER_ID, CONTAINER_TYPE, parentContainerResolver);
        verifyNoMoreInteractions(dataInstanceService);
        //then
        assertThat(captor.getValue().getFields().get("value")).isEqualTo("newValue");
    }

    private SDataInstance data(String data1, String value1) {
        SShortTextDataInstanceImpl sShortTextDataInstance = new SShortTextDataInstanceImpl();
        sShortTextDataInstance.setName(data1);
        sShortTextDataInstance.setValue(value1);
        return sShortTextDataInstance;
    }

    private SExpressionContext createContext() {
        return new SExpressionContext(CONTAINER_ID, CONTAINER_TYPE, PROCESS_DEFINITION_ID);
    }

    private SLeftOperand leftOperand(String name) {
        SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(name);
        return sLeftOperand;
    }
}
