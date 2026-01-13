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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.api.RestControllerUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProcessContractControllerTest {

    private static final long PROCESS_DEFINITION_ID = 4L;
    private static final String TEST_CONTRACT_API_URL = "/APISpringInternal/bpm/process/" + PROCESS_DEFINITION_ID
            + "/contract";

    private MockMvc mockMvc;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession apiSession;

    private final Map<String, Object> sessionAttributes = new HashMap<>();

    @BeforeEach
    public void setUp() throws Exception {
        ProcessContractController processContractController = spy(new ProcessContractController());
        mockMvc = RestControllerUtils.initMockMvcWithSessionAttributes(processContractController, sessionAttributes,
                apiSession);
        doReturn(processAPI).when(processContractController).getProcessAPI(apiSession);
    }

    @Test
    void should_return_a_contract_for_a_given_process_definition_id() throws Exception {
        // given
        final ContractDefinitionImpl contract = new ContractDefinitionImpl();
        contract.addInput(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));
        final InputDefinitionImpl complexInputDefinitionImpl = new InputDefinitionImpl("complexInput",
                "description",
                true, null, null);
        complexInputDefinitionImpl.getInputs()
                .add(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));

        contract.addInput(complexInputDefinitionImpl);
        contract.addConstraint(new ConstraintDefinitionImpl("aRule", "an expression", "an explanation"));

        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contract);

        // when
        mockMvc.perform(get(TEST_CONTRACT_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"constraints\":[{\"name\":\"aRule\",\"expression\":\"an expression\",\"explanation\":\"an explanation\"}],\"inputs\":[{\"type\":\"TEXT\",\"description\":\"aDescription\",\"name\":\"anInput\"},{\"inputs\":[{\"type\":\"TEXT\",\"description\":\"aDescription\",\"name\":\"anInput\"}],\"description\":\"description\",\"name\":\"complexInput\",\"multiple\":true}]}"));
    }

    @Test
    void should_respond_404_Not_found_when_process_definition_is_not_found_when_getting_contract()
                        throws Exception {
                when(processAPI.getProcessContract(PROCESS_DEFINITION_ID))
                                .thenThrow(new ProcessDefinitionNotFoundException("process definition not found"));

                mockMvc.perform(get(TEST_CONTRACT_API_URL)
                                .sessionAttrs(sessionAttributes)
                                .accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());
        }

    @Test
    void should_respond_204_when_there_is_no_contract_on_the_process() throws Exception {
        doReturn(null).when(processAPI).getProcessContract(PROCESS_DEFINITION_ID);

        mockMvc.perform(get(TEST_CONTRACT_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
