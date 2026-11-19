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
package org.bonitasoft.web.rest.server.api.bpm.flownode.archive;

import static org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedDataInstanceBuilder.anArchivedDataInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class ArchivedActivityVariableControllerTest extends AbstractControllerTest<ArchivedActivityVariableController> {

    private static final String API_URL = "/API/bpm/archivedActivityVariable";

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ArchivedActivityVariableController createController() {
        return spy(new ArchivedActivityVariableController());
    }

    @Override
    protected void configureMocks(ArchivedActivityVariableController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_archived_activity_variable() throws Exception {
        when(processAPI.getArchivedActivityDataInstance("myVar", 12L))
                .thenReturn(anArchivedDataInstance("myVar")
                        .withContainerId(12)
                        .withType(String.class.getName())
                        .withValue("Hello World")
                        .withArchivedDate(new Date(1700000000000L))
                        .build());

        mockMvc.perform(get(API_URL + "/12/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("myVar"))
                .andExpect(jsonPath("$.containerId").value("12"))
                .andExpect(jsonPath("$.type").value(String.class.getName()))
                .andExpect(jsonPath("$.value").value("Hello World"));
    }

    @Test
    void should_respond_404_when_archived_data_not_found() throws Exception {
        when(processAPI.getArchivedActivityDataInstance("unknownVar", 12L))
                .thenThrow(new ArchivedDataNotFoundException(new Exception("not found")));

        mockMvc.perform(get(API_URL + "/12/unknownVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_404_when_archived_flownode_not_found() throws Exception {
        when(processAPI.getArchivedActivityDataInstance("unknownFlownode", 12L))
                .thenThrow(new ArchivedDataNotFoundException(new ArchivedActivityInstanceNotFoundException(12L)));

        mockMvc.perform(get(API_URL + "/12/unknownFlownode")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_400_when_activity_id_is_not_a_number() throws Exception {
        mockMvc.perform(get(API_URL + "/notANumber/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
