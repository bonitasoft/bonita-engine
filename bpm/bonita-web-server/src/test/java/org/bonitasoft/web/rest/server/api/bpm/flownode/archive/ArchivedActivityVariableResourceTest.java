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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedDataInstanceBuilder.anArchivedDataInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedActivityVariable;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.utils.RestletTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class ArchivedActivityVariableResourceTest extends RestletTest {

    @Mock
    private ProcessAPI processApi;
    private ArchivedActivityVariableResource restResource;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected ServerResource configureResource() {
        return new ArchivedActivityVariableResource(processApi);
    }

    @Before
    public void initializeMocks() {
        restResource = new ArchivedActivityVariableResource(processApi);
    }

    @Test
    public void should_throw_illegal_argument_exception_when_caseId_not_set() throws Exception {
        Request request = new Request();
        request.setAttributes(Map.of("activityId", "myVar"));
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedActivityVariable());
    }

    @Test
    public void should_throw_illegal_argument_exception_when_variableName_not_set() throws Exception {
        Request request = new Request();
        request.setAttributes(Map.of("activityId", "12"));
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedActivityVariable());
    }

    @Test
    public void should_throw_illegal_argument_exception_when_caseId_is_not_a_long() throws Exception {
        Request request = new Request();
        request.setAttributes(Map.of("activityId", "hello"));
        restResource.setRequest(request);

        assertThrows(IllegalArgumentException.class, () -> restResource.getArchivedActivityVariable());
    }

    @Test
    public void should_retrieve_archived_variable_from_processApi() throws Exception {
        Request request = new Request();
        request.setAttributes(Map.of("activityId", "12", "variableName", "myVar"));
        restResource.setRequest(request);

        Date archivedDate = new Date();
        when(processApi.getArchivedActivityDataInstance("myVar", 12L))
                .thenReturn(anArchivedDataInstance("myVar")
                        .withContainerId(12)
                        .withType(String.class.getName())
                        .withValue("Hello World")
                        .withArchivedDate(archivedDate).build());

        ArchivedActivityVariable archivedVariable = restResource.getArchivedActivityVariable();

        assertThat(archivedVariable.getContainerId()).isEqualTo("12");
        assertThat(archivedVariable.getName()).isEqualTo("myVar");
        assertThat(archivedVariable.getType()).isEqualTo(String.class.getName());
        assertThat(archivedVariable.getValue()).isEqualTo("Hello World");
        assertThat(archivedVariable.getArchivedDate()).isEqualTo(archivedDate);
    }

    @Test
    public void should_return_archived_variable_with_200_status() throws Exception {
        Date archivedDate = new Date();
        when(processApi.getArchivedActivityDataInstance("myVar", 12L))
                .thenReturn(anArchivedDataInstance("myVar").withContainerId(12).withType(String.class.getName())
                        .withValue("Hello World").withArchivedDate(archivedDate).build());

        Response response = request(BonitaRestletApplication.BPM_ARCHIVED_ACTIVITY_VARIABLE_URL + "/12/myVar").get();

        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS_OK);

        ArchivedActivityVariable archivedVariable = objectMapper.readValue(response.getEntityAsText(),
                ArchivedActivityVariable.class);
        assertThat(archivedVariable.getName()).isEqualTo("myVar");
        assertThat(archivedVariable.getContainerId()).isEqualTo("12");
        assertThat(archivedVariable.getType()).isEqualTo(String.class.getName());
        assertThat(archivedVariable.getValue()).isEqualTo("Hello World");
        assertThat(archivedVariable.getArchivedDate()).isEqualTo(archivedDate);
    }

}
