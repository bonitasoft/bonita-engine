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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedDataInstanceBuilder.anArchivedDataInstance;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class ArchivedCaseVariableControllerTest extends AbstractControllerTest<ArchivedCaseVariableController> {

    private static final String API_URL = "/API/bpm/archivedCaseVariable";

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ArchivedCaseVariableController createController() {
        return spy(new ArchivedCaseVariableController());
    }

    @Override
    protected void configureMocks(ArchivedCaseVariableController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void getArchivedCaseVariable_should_return_variable_with_all_fields() throws Exception {
        var archivedDate = new Date();
        doReturn(anArchivedDataInstance("myVar")
                .withContainerId(12)
                .withType(String.class.getName())
                .withValue("Hello World")
                .withArchivedDate(archivedDate)
                .build())
                .when(processAPI).getArchivedProcessDataInstance("myVar", 12L);

        mockMvc.perform(get(API_URL + "/12/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("myVar"))
                .andExpect(jsonPath("$.case_id").value("12"))
                .andExpect(jsonPath("$.type").value(String.class.getName()))
                .andExpect(jsonPath("$.value").value("Hello World"));
    }

    @Test
    void getArchivedCaseVariable_should_return_404_when_variable_not_found() throws Exception {
        doThrow(new ArchivedDataNotFoundException(new SDataInstanceException("No archived data instance found")))
                .when(processAPI).getArchivedProcessDataInstance("unknownVar", 12L);

        mockMvc.perform(get(API_URL + "/12/unknownVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getArchivedCaseVariable_should_return_404_when_case_not_found() throws Exception {
        doThrow(new ArchivedDataNotFoundException("Archived process instance not found: " + 12L))
                .when(processAPI).getArchivedProcessDataInstance("unknownCase", 12L);

        mockMvc.perform(get(API_URL + "/12/unknownCase")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getArchivedCaseVariable_should_return_400_when_case_id_is_not_a_number() throws Exception {
        var caseId = "notANumber";
        mockMvc.perform(get(API_URL + "/" + caseId + "/myVar")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("[ %s ] must be a number".formatted(caseId)));
    }

    @Test
    void getArchivedCaseVariables_should_return_paginated_results_with_content_range() throws Exception {
        doReturn(List.of(
                anArchivedDataInstance("myVar1").withContainerId(12).build(),
                anArchivedDataInstance("myVar2").withContainerId(12).build(),
                anArchivedDataInstance("myVar3").withContainerId(12).build(),
                anArchivedDataInstance("myVar4").withContainerId(12).build()))
                .when(processAPI).getArchivedProcessDataInstances(12L, 0, Integer.MAX_VALUE);

        mockMvc.perform(get(API_URL)
                .param("f", "case_id=12")
                .param("p", "1")
                .param("c", "2")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Range", "1-2/4"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("myVar3"))
                .andExpect(jsonPath("$[1].name").value("myVar4"));
    }

    @Test
    void getArchivedCaseVariables_should_return_first_page() throws Exception {
        doReturn(List.of(
                anArchivedDataInstance("myVar1").withContainerId(12).build(),
                anArchivedDataInstance("myVar2").withContainerId(12).build()))
                .when(processAPI).getArchivedProcessDataInstances(12L, 0, Integer.MAX_VALUE);

        mockMvc.perform(get(API_URL)
                .param("f", "case_id=12")
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Range", "0-10/2"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getArchivedCaseVariables_should_return_400_when_case_id_filter_is_missing() throws Exception {
        mockMvc.perform(get(API_URL)
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter case_id is mandatory"));
    }

    @Test
    void getArchivedCaseVariables_should_return_empty_list_when_no_variables_exist() throws Exception {
        doReturn(List.of())
                .when(processAPI).getArchivedProcessDataInstances(12L, 0, Integer.MAX_VALUE);

        mockMvc.perform(get(API_URL)
                .param("f", "case_id=12")
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Range", "0-10/0"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getArchivedCaseVariables_should_return_empty_list_when_page_exceeds_available_data() throws Exception {
        doReturn(List.of(
                anArchivedDataInstance("myVar1").withContainerId(12).build(),
                anArchivedDataInstance("myVar2").withContainerId(12).build()))
                .when(processAPI).getArchivedProcessDataInstances(12L, 0, Integer.MAX_VALUE);

        mockMvc.perform(get(API_URL)
                .param("f", "case_id=12")
                .param("p", "100")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Range", "100-10/2"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getArchivedCaseVariables_should_return_400_when_case_id_is_not_a_number() throws Exception {
        mockMvc.perform(get(API_URL)
                .param("f", "case_id=notANumber")
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter case_id must be a number"));
    }

    @Test
    void getArchivedCaseVariables_should_extract_case_id_when_multiple_filters_provided() throws Exception {
        doReturn(List.of(
                anArchivedDataInstance("myVar1").withContainerId(42).build()))
                .when(processAPI).getArchivedProcessDataInstances(42L, 0, Integer.MAX_VALUE);

        mockMvc.perform(get(API_URL)
                .param("f", "other_filter=ignored")
                .param("f", "case_id=42")
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("myVar1"));
    }
}
