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

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.business.data.impl.MultipleBusinessDataReferenceImpl;
import org.bonitasoft.engine.business.data.impl.SimpleBusinessDataReferenceImpl;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

class CaseContextControllerTest extends AbstractControllerTest<CaseContextController> {

    private static final long CASE_ID = 2L;
    private static final String TEST_API_URL = "/" + API_SPRING_INTERNAL + "/bpm/case/" + CASE_ID + "/context";

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected CaseContextController createController() {
        return spy(new CaseContextController());
    }

    @Override
    protected void configureMocks(CaseContextController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_case_context_with_transformed_values() throws Exception {
        // given
        Map<String, Serializable> context = new HashMap<>();
        context.put("Ticket", "ticketValue");
        context.put("Count", 42);

        doReturn(context).when(processAPI).getProcessInstanceExecutionContext(CASE_ID);

        // when/then
        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "Ticket": "ticketValue",
                            "Count": 42
                        }
                        """, true));
    }

    @Test
    void should_respond_404_when_case_not_found() throws Exception {
        doThrow(new ProcessInstanceNotFoundException("case not found"))
                .when(processAPI).getProcessInstanceExecutionContext(CASE_ID);

        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_expression_evaluation_fails() throws Exception {
        doThrow(new ExpressionEvaluationException(new RuntimeException("evaluation failed")))
                .when(processAPI).getProcessInstanceExecutionContext(CASE_ID);

        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_convert_simple_business_data_reference_to_client_representation() throws Exception {
        // given
        Map<String, Serializable> context = new HashMap<>();
        context.put("myEmployee_ref",
                new SimpleBusinessDataReferenceImpl("myEmployee", "com.bonitasoft.pojo.Employee", 487467354L));

        doReturn(context).when(processAPI).getProcessInstanceExecutionContext(CASE_ID);

        // when/then
        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "myEmployee_ref": {
                                "name": "myEmployee",
                                "type": "com.bonitasoft.pojo.Employee",
                                "storageId": 487467354,
                                "storageId_string": "487467354",
                                "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/487467354"
                            }
                        }
                        """, true));
    }

    @Test
    void should_convert_multiple_business_data_reference_to_client_representation() throws Exception {
        // given
        Map<String, Serializable> context = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        ids.add(687646784L);
        ids.add(2313213874354L);
        context.put("myTeam_ref",
                new MultipleBusinessDataReferenceImpl("myTeam", "com.bonitasoft.pojo.Employee", ids));

        doReturn(context).when(processAPI).getProcessInstanceExecutionContext(CASE_ID);

        // when/then
        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("""
                                {
                                    "myTeam_ref": {
                                        "name": "myTeam",
                                        "type": "com.bonitasoft.pojo.Employee",
                                        "storageIds": [687646784, 2313213874354],
                                        "storageIds_string": ["687646784", "2313213874354"],
                                        "link": "API/bdm/businessData/com.bonitasoft.pojo.Employee/findByIds?ids=687646784,2313213874354"
                                    }
                                }
                                """,
                                true));
    }
}
