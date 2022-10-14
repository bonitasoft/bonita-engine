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
package org.bonitasoft.web.rest.server.api.bpm.flownode.archive;

import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.web.rest.server.FinderFactory;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedUserTaskContextResourceTest extends RestletTest {

    @Mock
    private ProcessAPI processAPI;
    @Mock
    private FinderFactory finderFactory;
    ArchivedUserTaskContextResource archviedTaskContextResource;

    @Override
    protected ServerResource configureResource() {
        return new ArchivedUserTaskContextResource(processAPI, finderFactory);
    }

    @Before
    public void initializeMocks() {
        archviedTaskContextResource = spy(new ArchivedUserTaskContextResource(processAPI, finderFactory));
    }

    @Test
    public void should_return_a_context_of_type_SingleBusinessDataRef_for_a_given_archived_task_instance()
            throws Exception {
        //given
        final Map<String, Serializable> context = new HashMap<>();
        String engineResult = "object returned by engine";

        context.put("Ticket", engineResult);
        when(processAPI.getArchivedUserTaskExecutionContext(2L)).thenReturn(context);
        doReturn("clientResult").when(finderFactory).getContextResultElement(engineResult);

        //when
        final Response response = request("/bpm/archivedUserTask/2/context").get();

        //then
        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response).hasJsonEntityEqualTo("{\"Ticket\":\"clientResult\"}");
    }

    @Test
    public void should_getArchivedTaskIDParameter_throws_an_exception_when_archived_task_id_parameter_is_null()
            throws Exception {
        //given
        doReturn(null).when(archviedTaskContextResource).getAttribute(ArchivedUserTaskContextResource.ARCHIVED_TASK_ID);

        try {
            //when
            archviedTaskContextResource.getArchivedTaskIdParameter();
        } catch (final Exception e) {
            //then
            Assertions.assertThat(e).isInstanceOf(APIException.class);
            Assertions.assertThat(e.getMessage())
                    .isEqualTo("Attribute '" + ArchivedUserTaskContextResource.ARCHIVED_TASK_ID
                            + "' is mandatory in order to get the archived task context");
        }

    }

    @Test
    public void should_respond_404_Not_found_when_task_is_not_found_when_getting_context() throws Exception {
        when(processAPI.getArchivedUserTaskExecutionContext(2))
                .thenThrow(new UserTaskNotFoundException("task 2 not found"));

        final Response response = request("/bpm/archivedUserTask/2/context").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }
}
