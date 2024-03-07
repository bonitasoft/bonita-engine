/**
 * Copyright (C) 2022 Bonitasoft S.A.
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

import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ProcessContractResourceTest extends RestletTest {

    private static final long PROCESS_DEFINITION_ID = 4L;

    private final String TEST_CONTRACT_API_URL = "/bpm/process/" + PROCESS_DEFINITION_ID + "/contract";

    private ProcessContractResource processContractResource;

    @Mock
    private ProcessAPI processAPI;

    @Before
    public void initializeMocks() {
        processContractResource = spy(new ProcessContractResource(processAPI));
    }

    @Override
    protected ServerResource configureResource() {
        return new ProcessContractResource(processAPI);
    }

    @Test(expected = APIException.class)
    public void should_throw_exception_if_attribute_is_not_found() throws Exception {
        // given:
        doReturn(null).when(processContractResource).getAttribute(anyString());

        // when:
        processContractResource.getContract();
    }

    @Test
    public void should_return_a_contract_for_a_given_process_definition_id() throws Exception {
        //given
        final ContractDefinitionImpl contract = new ContractDefinitionImpl();
        contract.addInput(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));
        final InputDefinitionImpl complexInputDefinitionImpl = new InputDefinitionImpl("complexInput", "description",
                true, null, null);
        complexInputDefinitionImpl.getInputs().add(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));

        contract.addInput(complexInputDefinitionImpl);
        contract.addConstraint(new ConstraintDefinitionImpl("aRule", "an expression", "an explanation"));

        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(contract);

        //when
        final Response response = request(TEST_CONTRACT_API_URL).get();

        //then
        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response).hasJsonEntityEqualTo(readFile("contract.json"));
    }

    @Test
    public void should_respond_404_Not_found_when_process_definition_is_not_found_when_getting_contract()
            throws Exception {
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID))
                .thenThrow(new ProcessDefinitionNotFoundException("process definition not found"));
        final Response response = request(TEST_CONTRACT_API_URL).get();
        assertThat(response).hasStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    @Test
    public void should_respond_204_when_there_is_no_contract_on_the_process() throws Exception {
        when(processAPI.getProcessContract(PROCESS_DEFINITION_ID)).thenReturn(null);

        Response response = request(TEST_CONTRACT_API_URL).get();

        assertThat(response).hasStatus(Status.SUCCESS_NO_CONTENT);
    }

}
