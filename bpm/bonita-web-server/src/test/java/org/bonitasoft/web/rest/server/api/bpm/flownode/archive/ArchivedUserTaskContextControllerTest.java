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

import static org.bonitasoft.web.rest.server.api.AbstractRESTController.API_SPRING_INTERNAL;
import static org.bonitasoft.web.rest.server.api.RestControllerUtils.initMockMvcWithSessionAttributes;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
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
class ArchivedUserTaskContextControllerTest {

    private static final long ARCHIVED_TASK_ID = 2L;
    private static final String TEST_API_URL = "/" + API_SPRING_INTERNAL + "/bpm/archivedUserTask/"
            + ARCHIVED_TASK_ID + "/context";

    private MockMvc mockMvc;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession apiSession;

    private final Map<String, Object> sessionAttributes = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        ArchivedUserTaskContextController controller = spy(new ArchivedUserTaskContextController());
        mockMvc = initMockMvcWithSessionAttributes(controller, sessionAttributes, apiSession);
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_archived_task_context() throws Exception {
        // given
        Map<String, Serializable> context = new HashMap<>();
        context.put("Ticket", "ticketValue");
        context.put("Employee", "employeeValue");

        when(processAPI.getArchivedUserTaskExecutionContext(ARCHIVED_TASK_ID)).thenReturn(context);

        // when/then
        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "Ticket": "ticketValue",
                            "Employee": "employeeValue"
                        }
                        """));
    }

    @Test
    void should_respond_404_when_archived_task_not_found() throws Exception {
        when(processAPI.getArchivedUserTaskExecutionContext(ARCHIVED_TASK_ID))
                .thenThrow(new UserTaskNotFoundException("archived task not found"));

        mockMvc.perform(get(TEST_API_URL)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
