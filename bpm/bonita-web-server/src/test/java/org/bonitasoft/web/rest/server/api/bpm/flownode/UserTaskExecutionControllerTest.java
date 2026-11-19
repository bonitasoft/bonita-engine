/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class UserTaskExecutionControllerTest extends AbstractControllerTest<UserTaskExecutionController> {

    private static final long TASK_ID = 2L;
    private static final long USER_ID = 4L;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private ContractDefinition contractDefinition;

    @BeforeAll
    static void initClass() {
        I18n.getInstance();
    }

    @Override
    protected UserTaskExecutionController createController() {
        UserTaskExecutionController controller = spy(new UserTaskExecutionController());
        doReturn(15L).when(controller).getMaxSize();
        return controller;
    }

    @Override
    protected void configureMocks(UserTaskExecutionController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
        when(contractDefinition.getInputs()).thenReturn(Collections.emptyList());
    }

    private String aComplexInputAsJson() {
        return "{\"aBoolean\":true, \"aString\":\"hello world\", \"a_complex_type\":{\"aNumber\":2, \"aBoolean\":false}}";
    }

    private Map<String, Serializable> aComplexInput() {
        final HashMap<String, Serializable> aComplexInput = new HashMap<>();
        aComplexInput.put("aBoolean", true);
        aComplexInput.put("aString", "hello world");

        final HashMap<String, Serializable> childMap = new HashMap<>();
        childMap.put("aNumber", 2);
        childMap.put("aBoolean", false);

        aComplexInput.put("a_complex_type", childMap);

        return aComplexInput;
    }

    @Test
    void should_execute_a_task_with_given_inputs() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        when(apiSession.getUserId()).thenReturn(0L);
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doNothing().when(processAPI).executeUserTask(anyLong(), eq(TASK_ID), any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isNoContent());

        verify(processAPI).executeUserTask(0L, TASK_ID, expectedComplexInput);
        verify(processAPI, never()).assignAndExecuteUserTask(anyLong(), anyLong(), any(Map.class));
    }

    @Test
    void should_assign_and_execute_a_task_with_given_inputs() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        when(apiSession.getUserId()).thenReturn(USER_ID);
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doNothing().when(processAPI).assignAndExecuteUserTask(eq(USER_ID), eq(TASK_ID), any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .param("assign", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isNoContent());

        verify(processAPI).assignAndExecuteUserTask(USER_ID, TASK_ID, expectedComplexInput);
        verify(processAPI, never()).executeUserTask(anyLong(), anyLong(), any(Map.class));
    }

    @Test
    void should_execute_a_task_with_given_inputs_for_a_specific_user() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        final long specificUserId = 1L;
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doNothing().when(processAPI).executeUserTask(eq(specificUserId), eq(TASK_ID), any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .param("user", String.valueOf(specificUserId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isNoContent());

        verify(processAPI).executeUserTask(specificUserId, TASK_ID, expectedComplexInput);
    }

    @Test
    void should_respond_400_Bad_request_when_contract_is_not_valid() throws Exception {
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doThrow(new ContractViolationException("aMessage", "aMessage",
                Arrays.asList("first explanation", "second explanation"), null))
                .when(processAPI).executeUserTask(anyLong(), anyLong(), any(Map.class));

        final String log = tapSystemOut(() -> mockMvc
                .perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                        .sessionAttrs(sessionAttributes)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\": \"value\"}"))
                // Note: Exception response format in MockMvc may differ from runtime,
                // so we only verify the status code as per the conversion guide's recommendation
                .andExpect(status().isBadRequest()));

        assertThat(log).containsPattern("INFO.*\nExplanations:\nfirst explanation\nsecond explanation");
    }

    @Test
    void should_respond_500_Internal_server_error_when_error_occurs_on_task_execution() throws Exception {
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doThrow(new FlowNodeExecutionException("aMessage"))
                .when(processAPI).executeUserTask(anyLong(), anyLong(), any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\": \"value\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_respond_404_Not_found_when_task_is_not_found() throws Exception {
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doThrow(new UserTaskNotFoundException("task not found"))
                .when(processAPI).executeUserTask(anyLong(), anyLong(), any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"key\": \"value\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_execute_task_without_inputs() throws Exception {
        when(apiSession.getUserId()).thenReturn(0L);
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);
        doNothing().when(processAPI).executeUserTask(anyLong(), eq(TASK_ID), any());

        mockMvc.perform(post("/APISpringInternal/bpm/userTask/" + TASK_ID + "/execution")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(processAPI).executeUserTask(eq(0L), eq(TASK_ID), any());
    }

    // Direct method call tests to verify typeConverterUtil.deleteTemporaryFiles behavior
    @Test
    void should_call_deleteFiles_after_executeUserTask() throws Exception {
        // given
        UserTaskExecutionController controller = spy(new UserTaskExecutionController());
        controller.typeConverterUtil = spy(controller.typeConverterUtil);
        doReturn(15L).when(controller).getMaxSize();
        doReturn(apiSession).when(controller).getApiSession(any());
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
        when(apiSession.getUserId()).thenReturn(1L);
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);

        final Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("testKey", "testValue");

        HttpSession mockSession = mock(HttpSession.class);

        // when
        controller.executeTask(TASK_ID, 1L, false, inputs, mockSession);

        // then
        verify(processAPI, never()).assignAndExecuteUserTask(anyLong(), anyLong(), any(Map.class));
        verify(processAPI, times(1)).executeUserTask(1L, TASK_ID, inputs);
        verify(controller.typeConverterUtil, times(1)).deleteTemporaryFiles(anyMap());
    }

    @Test
    void should_call_deleteFiles_after_assignAndExecuteUserTask() throws Exception {
        // given
        UserTaskExecutionController controller = spy(new UserTaskExecutionController());
        controller.typeConverterUtil = spy(controller.typeConverterUtil);
        doReturn(15L).when(controller).getMaxSize();
        doReturn(apiSession).when(controller).getApiSession(any());
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
        when(apiSession.getUserId()).thenReturn(1L);
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contractDefinition);

        final Map<String, Serializable> inputs = new HashMap<>();
        inputs.put("testKey", "testValue");

        HttpSession mockSession = mock(HttpSession.class);

        // when
        controller.executeTask(TASK_ID, 1L, true, inputs, mockSession);

        // then
        verify(processAPI, times(1)).assignAndExecuteUserTask(1L, TASK_ID, inputs);
        verify(processAPI, never()).executeUserTask(anyLong(), anyLong(), any(Map.class));
        verify(controller.typeConverterUtil, times(1)).deleteTemporaryFiles(anyMap());
    }
}
