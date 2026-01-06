/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.bonitasoft.web.rest.server.api.RestControllerUtils.initMockMvcWithSessionAttributes;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessInstanceImpl;
import org.bonitasoft.engine.session.APISession;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@MockitoSettings(strictness = LENIENT)
public class ProcessInstantiationControllerTest {

    private static final long PROCESS_DEFINITION_ID = 2L;

    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private MockMvc mockMvc;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession apiSession;

    @Mock
    private ContractDefinition contractDefinition;

    @BeforeAll
    public static void initClass() {
        I18n.getInstance();
    }

    @BeforeEach
    public void setUp() throws Exception {
        final ProcessInstantiationController controller = spy(new ProcessInstantiationController());
        doReturn(3L).when(controller).getMaxFileSize();
        mockMvc = initMockMvcWithSessionAttributes(controller, sessionAttributes, apiSession);
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
    public void should_instantiate_a_process_with_given_inputs() throws Exception {
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        final ProcessInstanceImpl processInstance = new ProcessInstanceImpl("complexProcessInstance");
        processInstance.setId(12L);
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);
        when(processAPI.startProcessWithInputs(anyLong(), any(Map.class)))
                .thenReturn(processInstance);

        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"caseId\":12}"));

        verify(processAPI).startProcessWithInputs(eq(PROCESS_DEFINITION_ID), eq(expectedComplexInput));
    }

    @Test
    public void should_instantiate_process_with_specific_user_id() throws Exception {
        final long userId = 42L;
        final Map<String, Serializable> expectedComplexInput = aComplexInput();
        final ProcessInstanceImpl processInstance = new ProcessInstanceImpl("processForUser");
        processInstance.setId(99L);
        doReturn(contractDefinition).when(processAPI).getProcessContract(PROCESS_DEFINITION_ID);
        doReturn(processInstance).when(processAPI).startProcessWithInputs(eq(userId), eq(PROCESS_DEFINITION_ID),
                any(Map.class));

        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .param("user", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"caseId\":99}"));

        verify(processAPI).startProcessWithInputs(eq(userId), eq(PROCESS_DEFINITION_ID),
                eq(expectedComplexInput));
    }

    @Test
    public void should_return_429_with_retry_after_header_when_rate_limit_exceeded() throws Exception {
        final long retryAfterTimestamp = System.currentTimeMillis() + 60000; // 1 minute from now
        final ProcessExecutionException exception = new ProcessExecutionException(
                new RuntimeException("Rate limit exceeded"), retryAfterTimestamp);

        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);
        when(processAPI.startProcessWithInputs(anyLong(), any(Map.class)))
                .thenThrow(exception);

        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Case creation limit reached."));
    }

    @Test
    public void should_throw_process_execution_exception_with_enhanced_message() throws Exception {
        final ProcessExecutionException exception = new ProcessExecutionException("Process execution failed");

        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);
        when(processAPI.startProcessWithInputs(anyLong(), any(Map.class)))
                .thenThrow(exception);

        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_handle_contract_violation_exception() throws Exception {
        final ContractViolationException exception = new ContractViolationException(
                "Contract validation failed",
                "Detailed message about contract violations",
                Collections.emptyList(),
                null);

        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);
        when(processAPI.startProcessWithInputs(anyLong(), any(Map.class)))
                .thenThrow(exception);

        // ContractViolationException is wrapped in BonitaException and handled by the
        // global exception handler as HTTP 500
        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_instantiate_process_without_inputs() throws Exception {
        final ProcessInstanceImpl processInstance = new ProcessInstanceImpl("processWithoutInputs");
        processInstance.setId(77L);
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contractDefinition);
        when(processAPI.startProcessWithInputs(eq(PROCESS_DEFINITION_ID), any()))
                .thenReturn(processInstance);

        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"caseId\":77}"));

        verify(processAPI).startProcessWithInputs(eq(PROCESS_DEFINITION_ID), any());
    }

    @Test
    public void should_return_400_when_user_parameter_is_not_a_valid_number() throws Exception {
        mockMvc.perform(post("/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID + "/instantiation")
                .sessionAttrs(sessionAttributes)
                .param("user", "invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(aComplexInputAsJson()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("'user' URL query parameter should be Integer")));
    }
}
