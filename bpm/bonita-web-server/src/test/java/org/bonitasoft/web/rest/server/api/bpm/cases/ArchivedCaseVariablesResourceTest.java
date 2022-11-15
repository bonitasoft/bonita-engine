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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedDataInstanceBuilder.anArchivedDataInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseVariable;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedVariable;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedCaseVariablesResourceTest extends RestletTest {

    @Mock
    private ProcessAPI processApi;
    private ArchivedCaseVariablesResource restResource;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected ServerResource configureResource() {
        return new ArchivedCaseVariablesResource(processApi);
    }

    @Before
    public void initializeMocks() {
        restResource = new ArchivedCaseVariablesResource(processApi);
    }

    @Test
    public void should_throw_illegal_argument_exception_when_caseId_not_set() throws Exception {
        Request request = new Request();
        request.setResourceRef(BonitaRestletApplication.BPM_ARCHIVED_CASE_VARIABLE_URL + "?p=0&c=10");
        restResource.setRequest(request);

        assertThrows(APIException.class, () -> restResource.getArchivedCaseVariables());
    }

    @Test
    public void should_throw_illegal_argument_exception_when_caseId_is_not_a_long() throws Exception {
        Request request = new Request();
        request.setAttributes(Map.of("caseId", "hello", "c", "10", "p", "0"));
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedCaseVariables());
    }

    @Test
    public void should_throw_illegal_argument_exception_when_page_is_not_set() throws Exception {
        Request request = new Request();
        request.setResourceRef(BonitaRestletApplication.BPM_ARCHIVED_CASE_VARIABLE_URL + "?f=case_id=12&c=10");
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedCaseVariables());
    }

    @Test
    public void should_throw_illegal_argument_exception_when_page_size_is_not_set() throws Exception {
        Request request = new Request();
        request.setResourceRef(BonitaRestletApplication.BPM_ARCHIVED_CASE_VARIABLE_URL + "?f=case_id=12&p=0");
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedCaseVariables());
    }

    @Test
    public void should_return_archived_variable_with_200_status() throws Exception {
        when(processApi.getArchivedProcessDataInstances(Mockito.eq(12L), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(anArchivedDataInstance("myVar").build(),
                        anArchivedDataInstance("myVar2").build(),
                        anArchivedDataInstance("myVar3").build(),
                        anArchivedDataInstance("myVar4").build()));

        Response response = request(BonitaRestletApplication.BPM_ARCHIVED_CASE_VARIABLE_URL + "?f=case_id=12&p=1&c=2")
                .get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);
        assertThat(response.getHeaders().getFirst("Content-range").getValue()).isEqualTo("1-2/4");

        List<ArchivedCaseVariable> archivedVariables = objectMapper.readValue(response.getEntityAsText(),
                new TypeReference<>() {
                });
        assertThat(archivedVariables).hasSize(2).extracting(ArchivedVariable::getName).containsOnly("myVar3", "myVar4");
    }

}
