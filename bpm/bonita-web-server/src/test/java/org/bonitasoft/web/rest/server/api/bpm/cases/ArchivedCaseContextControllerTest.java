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
import static org.bonitasoft.web.rest.server.api.RestControllerUtils.initMockMvcWithSessionAttributes;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.session.APISession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArchivedCaseContextControllerTest {

    private static final long ARCHIVED_CASE_ID = 2L;
    private static final String TEST_API_URL = "/" + API_SPRING_INTERNAL + "/bpm/archivedCase/" + ARCHIVED_CASE_ID
            + "/context";

    private MockMvc mockMvc;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession apiSession;

    private final Map<String, Object> sessionAttributes = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        ArchivedCaseContextController controller = spy(new ArchivedCaseContextController());
        mockMvc = initMockMvcWithSessionAttributes(controller, sessionAttributes, apiSession);
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_archived_case_context_with_transformed_values() throws Exception {
        // given
        Map<String, Serializable> context = new HashMap<>();
        context.put("Ticket", "ticketValue");
        context.put("Count", 42);

        doReturn(context).when(processAPI).getArchivedProcessInstanceExecutionContext(ARCHIVED_CASE_ID);

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
                        """));
    }

    @Test
    void should_respond_404_when_archived_case_not_found() throws Exception {
        doThrow(new ProcessInstanceNotFoundException("archived case not found"))
                .when(processAPI).getArchivedProcessInstanceExecutionContext(ARCHIVED_CASE_ID);

        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_expression_evaluation_fails() throws Exception {
        doThrow(new ExpressionEvaluationException(new RuntimeException("evaluation failed")))
                .when(processAPI).getArchivedProcessInstanceExecutionContext(ARCHIVED_CASE_ID);

        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
