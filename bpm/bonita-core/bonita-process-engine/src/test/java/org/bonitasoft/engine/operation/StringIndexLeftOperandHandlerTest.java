/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.impl.SLeftOperandImpl;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringIndexLeftOperandHandlerTest {

    private static final long FLOWNODE_ID = 3248907432L;
    private static final long CALLED_PROCESS_FLOW_NODE_ID = 654907432L;
    private static final long SUB_PROCESS_FLOW_NODE_ID = 123327432L;
    private static final long PROCESS_INSTANCE_ID = 9019873L;
    private static final long SUB_PROCESS_INSTANCE_ID = 906234273L;
    private static final long CALLED_PROCESS_INSTANCE_ID = 43273L;
    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @InjectMocks
    private StringIndexLeftOperandHandler handler;
    @Captor
    private ArgumentCaptor<EntityUpdateDescriptor> entityUpdateDescriptorArgumentCaptor;
    private SUserTaskInstance userTaskInstance;
    private SProcessInstance processInstance;
    private SProcessInstance subProcessInstance;
    private SProcessInstance calledProcessInstance;
    private SUserTaskInstance taskOfCalledProcess;
    private SUserTaskInstance taskOfSubProcess;

    @Before
    public void before() throws Exception {

        userTaskInstance = aTaskInstance(FLOWNODE_ID);
        taskOfCalledProcess = aTaskInstance(CALLED_PROCESS_FLOW_NODE_ID);
        taskOfSubProcess = aTaskInstance(SUB_PROCESS_FLOW_NODE_ID);

        processInstance = aProcessInstance(PROCESS_INSTANCE_ID);
        calledProcessInstance = aProcessInstance(CALLED_PROCESS_INSTANCE_ID);
        calledProcessInstance.setCallerType(SFlowNodeType.CALL_ACTIVITY);
        calledProcessInstance.setCallerId(userTaskInstance.getId());
        subProcessInstance = aProcessInstance(SUB_PROCESS_INSTANCE_ID);
        subProcessInstance.setCallerType(SFlowNodeType.SUB_PROCESS);
        subProcessInstance.setCallerId(taskOfCalledProcess.getId());

        userTaskInstance.setLogicalGroup(3, processInstance.getId());
        taskOfCalledProcess.setLogicalGroup(3, calledProcessInstance.getId());
        taskOfSubProcess.setLogicalGroup(3, subProcessInstance.getId());
    }

    private SProcessInstance aProcessInstance(long id)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        SProcessInstance processInstance = new SProcessInstance();
        processInstance.setId(id);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(id);
        return processInstance;
    }

    private SUserTaskInstance aTaskInstance(long id) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        SUserTaskInstance userTaskInstance = new SUserTaskInstance();
        userTaskInstance.setId(id);
        doReturn(userTaskInstance).when(activityInstanceService).getFlowNodeInstance(id);
        return userTaskInstance;
    }

    @Test(expected = SOperationExecutionException.class)
    public void deleteThrowsAnExceptionNotYetSupported() throws Exception {
        handler.delete(stringIndex("2"), 45l, "container");
    }

    @Test
    public void should_update_string_index_of_process_when_on_flownode() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("1"), objectObjectMap, "newValue", FLOWNODE_ID,
                DataInstanceContainer.ACTIVITY_INSTANCE.name());

        verify(processInstanceService).updateProcess(eq(processInstance),
                entityUpdateDescriptorArgumentCaptor.capture());
        assertThat(entityUpdateDescriptorArgumentCaptor.getValue().getFields())
                .containsOnly(entry("stringIndex1", "newValue"));
    }

    @Test
    public void should_update_string_index_of_process_when_on_flownode_in_called_process() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("2"), objectObjectMap, "newValue", CALLED_PROCESS_FLOW_NODE_ID,
                DataInstanceContainer.ACTIVITY_INSTANCE.name());

        verify(processInstanceService).updateProcess(eq(calledProcessInstance),
                entityUpdateDescriptorArgumentCaptor.capture());
        assertThat(entityUpdateDescriptorArgumentCaptor.getValue().getFields())
                .containsOnly(entry("stringIndex2", "newValue"));
    }

    @Test
    public void should_update_string_index_of_parent_process_when_on_flownode_in_subProcess() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("3"), objectObjectMap, "newValue", SUB_PROCESS_FLOW_NODE_ID,
                DataInstanceContainer.ACTIVITY_INSTANCE.name());

        verify(processInstanceService).updateProcess(eq(calledProcessInstance),
                entityUpdateDescriptorArgumentCaptor.capture());
        assertThat(entityUpdateDescriptorArgumentCaptor.getValue().getFields())
                .containsOnly(entry("stringIndex3", "newValue"));
    }

    @Test
    public void should_update_string_index_of_given_process() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("4"), objectObjectMap, "newValue", PROCESS_INSTANCE_ID,
                DataInstanceContainer.PROCESS_INSTANCE.name());

        verify(processInstanceService).updateProcess(eq(processInstance),
                entityUpdateDescriptorArgumentCaptor.capture());
        assertThat(entityUpdateDescriptorArgumentCaptor.getValue().getFields())
                .containsOnly(entry("stringIndex4", "newValue"));
    }

    @Test(expected = SOperationExecutionException.class)
    public void should_fail_when_updating_string_index_with_wrong_index() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("0"), objectObjectMap, "newValue", PROCESS_INSTANCE_ID,
                DataInstanceContainer.PROCESS_INSTANCE.name());
    }

    @Test(expected = SOperationExecutionException.class)
    public void should_fail_when_updating_string_index_with_not_a_number_index() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("toto"), objectObjectMap, "newValue", PROCESS_INSTANCE_ID,
                DataInstanceContainer.PROCESS_INSTANCE.name());
    }

    @Test(expected = SOperationExecutionException.class)
    public void should_fail_when_updating_string_index_with_value_that_is_not_string() throws Exception {
        Map<String, Object> objectObjectMap = Collections.emptyMap();

        handler.update(stringIndex("1"), objectObjectMap, 1231, PROCESS_INSTANCE_ID,
                DataInstanceContainer.PROCESS_INSTANCE.name());
    }

    private SLeftOperandImpl stringIndex(String index) {
        SLeftOperandImpl sLeftOperand = new SLeftOperandImpl();
        sLeftOperand.setName(index);
        return sLeftOperand;
    }
}
