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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.bonitasoft.web.rest.server.utils.ResponseAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.web.rest.server.FinderFactory;
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
public class ArchivedCaseContextResourceTest extends RestletTest {

    @Mock
    private ProcessAPI processAPI;
    @Mock
    private FinderFactory finderFactory;

    ArchivedCaseContextResource archivedCaseContextResource;

    @Override
    protected ServerResource configureResource() {
        return new ArchivedCaseContextResource(processAPI, finderFactory);
    }

    @Before
    public void initializeMocks() {
        archivedCaseContextResource = spy(new ArchivedCaseContextResource(processAPI, finderFactory));
    }

    @Test
    public void should_return_a_context_of_type_SingleBusinessDataRef_for_a_given_archived_case() throws Exception {
        //given
        final Map<String, Serializable> context = new HashMap<>();
        String engineResult = "object returned by engine";

        context.put("Ticket", engineResult);
        when(processAPI.getArchivedProcessInstanceExecutionContext(2L)).thenReturn(context);
        doReturn("clientResult").when(finderFactory).getContextResultElement(engineResult);

        //when
        final Response response = request("/bpm/archivedCase/2/context").get();

        //then
        assertThat(response).hasStatus(Status.SUCCESS_OK);
        assertThat(response).hasJsonEntityEqualTo("{\"Ticket\":\"clientResult\"}");
    }

    @Test
    public void should_getTaskIDParameter_throws_an_exception_when_archived_case_id_parameter_is_null()
            throws Exception {
        //given
        doReturn(null).when(archivedCaseContextResource).getAttribute(ArchivedCaseContextResource.ARCHIVED_CASE_ID);

        try {
            //when
            archivedCaseContextResource.getArchivedCaseIdParameter();
        } catch (final Exception e) {
            //then
            Assertions.assertThat(e).isInstanceOf(APIException.class);
            Assertions.assertThat(e.getMessage()).isEqualTo("Attribute '" + ArchivedCaseContextResource.ARCHIVED_CASE_ID
                    + "' is mandatory in order to get the archived case context");
        }

    }

    @Test
    public void should_respond_404_Not_found_when_archived_case_is_not_found_when_getting_the_context()
            throws Exception {
        when(processAPI.getArchivedProcessInstanceExecutionContext(2))
                .thenThrow(new ProcessInstanceNotFoundException("case 2 not found"));

        final Response response = request("/bpm/archivedCase/2/context").get();

        assertThat(response).hasStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }

}
