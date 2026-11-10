/**
 * Copyright (C) 2025 Bonitasoft S.A.
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.BusinessDataAPI;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.business.data.BusinessDataReference;
import org.bonitasoft.engine.business.data.MultipleBusinessDataReference;
import org.bonitasoft.engine.business.data.SimpleBusinessDataReference;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class BusinessDataReferenceControllerTest extends AbstractControllerTest<BusinessDataReferenceController> {

    private static final long FAKE_CASE_ID = 486L;
    private static final String FAKE_DATA_NAME = "myEmployee";
    private static final String FAKE_EXCEPTION_MESSAGE = "fake exception message";

    @Mock
    protected BusinessDataAPI businessDataAPI;

    @Override
    protected BusinessDataReferenceController createController() {
        return spy(new BusinessDataReferenceController());
    }

    @Override
    protected void configureMocks(BusinessDataReferenceController controller) throws Exception {
        doReturn(businessDataAPI).when(controller).getBusinessDataAPI(apiSession);
    }

    private SimpleBusinessDataReference buildSimpleEmployeeReference(String name, long businessDataId) {
        return new SimpleBusinessDataReferenceImpl(name, "com.bonitasoft.pojo.Employee", businessDataId);
    }

    private MultipleBusinessDataReference buildMultipleEmployeeReference(String name, long... businessDataIds) {
        List<Long> ids = new ArrayList<>();
        for (long businessDataId : businessDataIds) {
            ids.add(businessDataId);
        }
        return new MultipleBusinessDataReferenceImpl(name, "com.bonitasoft.pojo.Employee", ids);
    }

    // =================================================================================================================
    // GET single reference: /API/bdm/businessDataReference/{caseId}/{dataName}
    // =================================================================================================================

    @Test
    void should_return_the_simple_reference_of_the_business_data_of_the_process_instance() throws Exception {
        // given
        SimpleBusinessDataReference reference = buildSimpleEmployeeReference(FAKE_DATA_NAME, 487467354L);
        when(businessDataAPI.getProcessBusinessDataReference(FAKE_DATA_NAME, FAKE_CASE_ID)).thenReturn(reference);

        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference/{caseId}/{dataName}", FAKE_CASE_ID, FAKE_DATA_NAME)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "name": "myEmployee",
                          "type": "com.bonitasoft.pojo.Employee",
                          "storageId": 487467354,
                          "storageId_string": "487467354",
                          "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/487467354"
                        }
                        """, true));
    }

    @Test
    void should_return_the_multi_reference_of_the_business_data_of_the_process_instance() throws Exception {
        // given
        MultipleBusinessDataReference reference = buildMultipleEmployeeReference(FAKE_DATA_NAME, 487467354L,
                48674634L);
        when(businessDataAPI.getProcessBusinessDataReference(FAKE_DATA_NAME, FAKE_CASE_ID)).thenReturn(reference);

        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference/{caseId}/{dataName}", FAKE_CASE_ID, FAKE_DATA_NAME)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "name": "myEmployee",
                          "type": "com.bonitasoft.pojo.Employee",
                          "storageIds": [487467354, 48674634],
                          "storageIds_string": ["487467354", "48674634"],
                          "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/findByIds?ids=487467354,48674634"
                        }
                        """, true));
    }

    @Test
    void should_respond_bad_request_when_caseId_pathparam_is_not_a_number() throws Exception {
        // when/then
        var caseId = "foo";
        mockMvc.perform(
                get("/API/bdm/businessDataReference/{caseId}/{dataName}", caseId, FAKE_DATA_NAME)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("[ %s ] must be a number".formatted(caseId)));
    }

    @Test
    void should_return_a_not_found_status_when_business_data_is_not_found() throws Exception {
        // given
        when(businessDataAPI.getProcessBusinessDataReference(FAKE_DATA_NAME, FAKE_CASE_ID))
                .thenThrow(new DataNotFoundException(new Exception(FAKE_EXCEPTION_MESSAGE)));

        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference/{caseId}/{dataName}", FAKE_CASE_ID, FAKE_DATA_NAME)
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(DataNotFoundException.class.toString()))
                .andExpect(jsonPath("$.message").value(FAKE_EXCEPTION_MESSAGE));

    }

    // =================================================================================================================
    // GET list of references: /API/bdm/businessDataReference?f=caseId=X&p=X&c=X
    // =================================================================================================================

    @Test
    void should_return_the_references_of_the_business_data_of_the_process_instance() throws Exception {
        // given
        List<BusinessDataReference> references = new ArrayList<>();
        references.add(buildSimpleEmployeeReference("john", 487467354L));
        references.add(buildMultipleEmployeeReference("Ateam", 687646784L, 2313213874354L));
        when(businessDataAPI.getProcessBusinessDataReferences(FAKE_CASE_ID, 10, 10)).thenReturn(references);

        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "caseId=" + FAKE_CASE_ID)
                        .param("p", "1")
                        .param("c", "10")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        """
                                [
                                  {
                                    "name": "john",
                                    "type": "com.bonitasoft.pojo.Employee",
                                    "storageId": 487467354,
                                    "storageId_string": "487467354",
                                    "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/487467354"
                                  },
                                  {
                                    "name": "Ateam",
                                    "type": "com.bonitasoft.pojo.Employee",
                                    "storageIds": [687646784, 2313213874354],
                                    "storageIds_string": ["687646784", "2313213874354"],
                                    "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/findByIds?ids=687646784,2313213874354"
                                  }
                                ]
                                """,
                        true));
    }

    @Test
    void should_respond_bad_request_when_caseId_filter_not_specified() throws Exception {
        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "unknownfilter=123")
                        .param("p", "0")
                        .param("c", "10")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter caseId is mandatory"));
    }

    @Test
    void should_be_valid_when_extra_unwanted_filter_is_provided() throws Exception {
        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "unknownfilter=123")
                        .param("f", "caseId=" + FAKE_CASE_ID)
                        .param("p", "0")
                        .param("c", "10")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    void should_respond_bad_request_when_caseId_filter_is_not_a_number() throws Exception {
        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "caseId=toto")
                        .param("p", "0")
                        .param("c", "10")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("filter caseId must be a number"));
    }

    @Test
    void should_respond_bad_request_when_page_parameter_is_missing() throws Exception {
        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "caseId=" + FAKE_CASE_ID)
                        .param("c", "10")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter p (page) is mandatory"));
    }

    @Test
    void should_respond_bad_request_when_count_parameter_is_missing() throws Exception {
        // when/then
        mockMvc.perform(
                get("/API/bdm/businessDataReference")
                        .param("f", "caseId=" + FAKE_CASE_ID)
                        .param("p", "0")
                        .sessionAttrs(sessionAttributes)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.exception").value(IllegalArgumentException.class.toString()))
                .andExpect(jsonPath("$.message").value("query parameter c (count) is mandatory"));
    }

}
