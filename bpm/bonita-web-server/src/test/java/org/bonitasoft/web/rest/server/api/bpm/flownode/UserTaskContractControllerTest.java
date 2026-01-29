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

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class UserTaskContractControllerTest extends AbstractControllerTest<UserTaskContractController> {

    private static final long TASK_ID = 2L;
    private static final String TEST_CONTRACT_API_URL = "/" + API_SPRING_INTERNAL + "/bpm/userTask/" + TASK_ID
            + "/contract";

    @Mock
    private ProcessAPI processAPI;

    @Override
    protected UserTaskContractController createController() {
        return spy(new UserTaskContractController());
    }

    @Override
    protected void configureMocks(UserTaskContractController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_a_contract_for_a_given_task_instance() throws Exception {
        // given
        final ContractDefinitionImpl contract = new ContractDefinitionImpl();
        contract.addInput(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));
        final InputDefinitionImpl complexInputDefinitionImpl = new InputDefinitionImpl("complexInput", "description",
                true, null, null);
        complexInputDefinitionImpl.getInputs().add(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));

        contract.addInput(complexInputDefinitionImpl);
        contract.addConstraint(new ConstraintDefinitionImpl("aRule", "an expression", "an explanation"));

        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(contract);

        // when/then
        mockMvc.perform(get(TEST_CONTRACT_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("""
                                {
                                    "constraints":[
                                        {"name":"aRule","expression":"an expression","explanation":"an explanation","inputNames":[]}
                                    ],
                                    "inputs":[
                                        {"description":"aDescription","name":"anInput","multiple":false,"type":"TEXT","inputs":[]},
                                        {"description":"description","name":"complexInput","multiple":true,"type":null,"inputs":[
                                            {"description":"aDescription","name":"anInput","multiple":false,"type":"TEXT","inputs":[]}
                                        ]}
                                    ]
                                }
                                """));
    }

    @Test
    void should_respond_404_Not_found_when_task_is_not_found_when_getting_contract() throws Exception {
        when(processAPI.getUserTaskContract(TASK_ID))
                .thenThrow(new UserTaskNotFoundException("task not found"));

        mockMvc.perform(get(TEST_CONTRACT_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_204_when_there_is_no_contract_on_the_task() throws Exception {
        when(processAPI.getUserTaskContract(TASK_ID)).thenReturn(null);

        mockMvc.perform(get(TEST_CONTRACT_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
