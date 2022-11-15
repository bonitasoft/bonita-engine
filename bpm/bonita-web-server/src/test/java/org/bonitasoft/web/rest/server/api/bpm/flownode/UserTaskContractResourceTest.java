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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ContractDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.InputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
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
public class UserTaskContractResourceTest extends RestletTest {

    @Mock
    private ProcessAPI processAPI;

    UserTaskContractResource taskContractResource;

    @Override
    protected ServerResource configureResource() {
        return new UserTaskContractResource(processAPI);
    }

    @Before
    public void initializeMocks() {
        taskContractResource = spy(new UserTaskContractResource(processAPI));
    }

    @Test
    public void should_return_a_contract_for_a_given_task_instance() throws Exception {
        //given
        final ContractDefinitionImpl contract = new ContractDefinitionImpl();
        contract.addInput(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));
        final InputDefinitionImpl complexInputDefinitionImpl = new InputDefinitionImpl("complexInput", "description",
                true, null, null);
        complexInputDefinitionImpl.getInputs().add(new InputDefinitionImpl("anInput", Type.TEXT, "aDescription"));

        contract.addInput(complexInputDefinitionImpl);
        contract.addConstraint(new ConstraintDefinitionImpl("aRule", "an expression", "an explanation"));

        when(processAPI.getUserTaskContract(2L)).thenReturn(contract);

        //when
        final Response response = request("/bpm/userTask/2/contract").get();

        //then
        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response).hasJsonEntityEqualTo(readFile("contract.json"));
    }

    @Test
    public void should_respond_404_Not_found_when_task_is_not_found_when_getting_contract() throws Exception {
        when(processAPI.getUserTaskContract(2)).thenThrow(new UserTaskNotFoundException("task 2 not found"));

        final Response response = request("/bpm/userTask/2/contract").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

    @Test
    public void should_respond_204_when_there_is_no_contract_on_the_task() throws Exception {
        when(processAPI.getUserTaskContract(2)).thenReturn(null);

        Response response = request("/bpm/userTask/2/contract").get();

        assertThat(response).hasStatus(Status.SUCCESS_NO_CONTENT);
    }

    @Test
    public void should_getTaskIDParameter_throws_an_exception_when_task_id_parameter_is_null() {
        //given
        doReturn(null).when(taskContractResource).getAttribute(UserTaskContractResource.TASK_ID);

        try {
            //when
            taskContractResource.getTaskIdParameter();
        } catch (final Exception e) {
            //then
            assertThat(e).isInstanceOf(APIException.class);
            assertThat(e.getMessage()).isEqualTo("Attribute '" + UserTaskContractResource.TASK_ID + "' is mandatory");
        }

    }

}
