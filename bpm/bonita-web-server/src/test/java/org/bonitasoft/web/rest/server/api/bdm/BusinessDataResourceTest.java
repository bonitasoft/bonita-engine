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
package org.bonitasoft.web.rest.server.api.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class BusinessDataResourceTest extends RestletTest {

    @Mock
    protected CommandAPI commandAPI;

    @Override
    protected ServerResource configureResource() {
        return new BusinessDataResource(commandAPI);
    }

    @Test
    public void should_return_the_business_data_based_on_its_id() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataId", 1983L);
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"name\":\"Matti\"}");

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        assertThat(response.getEntityAsText()).isEqualTo("{\"name\":\"Matti\"}");
    }

    @Test
    public void should_return_an_internal_server_error_status_when_command_is_not_found() throws Exception {
        when(commandAPI.execute(anyString(), anyMap())).thenThrow(new CommandNotFoundException(null));

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983").get();

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_NOT_FOUND);
    }

    @Test
    public void should_return_an_internal_server_error_status_when_command_is_not_well_parameterized()
            throws Exception {
        when(commandAPI.execute(anyString(), anyMap()))
                .thenThrow(new CommandParameterizationException("id is missing"));

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983").get();

        assertThat(response.getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void should_return_a_not_found_status_when_command_fails_business_data_not_found() throws Exception {
        when(commandAPI.execute(anyString(), anyMap()))
                .thenThrow(new CommandExecutionException(new DataNotFoundException(null)));

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983").get();

        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_NOT_FOUND);
    }

    @Test
    public void should_return_an_internal_server_error_status_when_command_fails_during_execution() throws Exception {
        when(commandAPI.execute(anyString(), anyMap())).thenThrow(new CommandExecutionException("server error"));

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983").get();

        assertThat(response.getStatus()).isEqualTo(Status.SERVER_ERROR_INTERNAL);
    }

    @Test
    public void should_fetch_business_data_child_if_it_is_specified() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("entityClassName", "org.bonitasoft.pojo.Employee");
        parameters.put("businessDataId", 1983L);
        parameters.put("businessDataChildName", "child");
        parameters.put("businessDataURIPattern", "/API/bdm/businessData/{className}/{id}/{field}");
        when(commandAPI.execute("getBusinessDataById", parameters)).thenReturn("{\"name\":\"Matti\"}");

        final Response response = request("/bdm/businessData/org.bonitasoft.pojo.Employee/1983/child").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        assertThat(response.getEntityAsText()).isEqualTo("{\"name\":\"Matti\"}");
    }
}
