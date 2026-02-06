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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class TimerEventTriggerControllerTest extends AbstractControllerTest<TimerEventTriggerController> {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected TimerEventTriggerController createController() {
        return spy(new TimerEventTriggerController());
    }

    @Override
    protected void configureMocks(TimerEventTriggerController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_search_timer_event_triggers_for_given_case() throws Exception {
        // Given
        final long caseId = 123L;
        final long triggerId = 42L;
        final long eventInstanceId = 100L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(1,
                        List.of(new TimerEventTriggerInstanceImpl(triggerId, eventInstanceId, "カキクケコ", new Date()))));

        // When & Then
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-1/1"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                // Verify _string fields are present (backward compatibility with Restlet)
                .andExpect(jsonPath("$[0].id").value(triggerId))
                .andExpect(jsonPath("$[0].id_string").value(String.valueOf(triggerId)))
                .andExpect(jsonPath("$[0].eventInstanceId").value(eventInstanceId))
                .andExpect(jsonPath("$[0].eventInstanceId_string").value(String.valueOf(eventInstanceId)))
                .andExpect(jsonPath("$[0].eventInstanceName").value("カキクケコ"));

        verify(processAPI).searchTimerEventTriggerInstances(eq(caseId), any(SearchOptions.class));
    }

    @Test
    void should_return_string_fields_for_numeric_ids() throws Exception {
        // Given - test with large IDs that could lose precision in JavaScript
        final long caseId = 123L;
        final long triggerId = 9007199254740993L; // > Number.MAX_SAFE_INTEGER
        final long eventInstanceId = 9007199254740994L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(1,
                        List.of(new TimerEventTriggerInstanceImpl(triggerId, eventInstanceId, "bigTimer",
                                new Date()))));

        // When & Then - _string fields preserve precision for JavaScript clients
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(triggerId))
                .andExpect(jsonPath("$[0].id_string").value("9007199254740993"))
                .andExpect(jsonPath("$[0].eventInstanceId").value(eventInstanceId))
                .andExpect(jsonPath("$[0].eventInstanceId_string").value("9007199254740994"));
    }

    @Test
    void should_return_bad_request_when_caseId_is_missing() throws Exception {
        // When & Then
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_update_timer_event_trigger_execution_date() throws Exception {
        // Given
        final long triggerId = 456L;
        final long newExecutionTime = System.currentTimeMillis() + 3600000; // +1 hour
        final Date updatedDate = new Date(newExecutionTime);

        when(processAPI.updateExecutionDateOfTimerEventTriggerInstance(anyLong(), any(Date.class)))
                .thenReturn(updatedDate);

        final String requestBody = """
                {
                    "executionDate": %d
                }
                """.formatted(newExecutionTime);

        // When & Then
        mockMvc.perform(put("/API/bpm/timerEventTrigger/{id}", triggerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionDate").value(newExecutionTime));

        verify(processAPI).updateExecutionDateOfTimerEventTriggerInstance(eq(triggerId), any(Date.class));
    }

    @Test
    void should_return_not_found_when_timer_event_trigger_does_not_exist() throws Exception {
        // Given
        final long triggerId = 999L;
        final long newExecutionTime = System.currentTimeMillis() + 3600000;

        when(processAPI.updateExecutionDateOfTimerEventTriggerInstance(anyLong(), any(Date.class)))
                .thenThrow(new TimerEventTriggerInstanceNotFoundException(triggerId));

        final String requestBody = """
                {
                    "executionDate": %d
                }
                """.formatted(newExecutionTime);

        // When & Then
        mockMvc.perform(put("/API/bpm/timerEventTrigger/{id}", triggerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_bad_request_when_execution_date_is_missing() throws Exception {
        // Given
        final long triggerId = 456L;
        final String requestBody = "{}";

        // When & Then
        mockMvc.perform(put("/API/bpm/timerEventTrigger/{id}", triggerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_return_no_content_when_no_timer_triggers_found() throws Exception {
        // Given
        final long caseId = 123L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(0, List.of()));

        // When & Then - 204 No Content has no body
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .param("p", "0")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-0/0"));
    }

    @Test
    void should_return_no_content_when_page_out_of_bounds() throws Exception {
        // Given
        final long caseId = 123L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(2, List.of())); // Total 2, but empty results for this page

        // When & Then - 204 No Content has no body
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .param("p", "10") // Page 10 when only 2 results exist
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-0/0"));
    }

    @Test
    void should_handle_different_pagination_parameters() throws Exception {
        // Given
        final long caseId = 123L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(50,
                        List.of(new TimerEventTriggerInstanceImpl(1L, 1L, "timer1", new Date()),
                                new TimerEventTriggerInstanceImpl(2L, 1L, "timer2", new Date()),
                                new TimerEventTriggerInstanceImpl(3L, 1L, "timer3", new Date()),
                                new TimerEventTriggerInstanceImpl(4L, 1L, "timer4", new Date()),
                                new TimerEventTriggerInstanceImpl(5L, 1L, "timer5", new Date()))));

        // When & Then - page 2 with 5 items per page
        // Content-Range format: page-countOnCurrentPage/total
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .param("p", "2")
                .param("c", "5")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "2-5/50"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));

        // Verify correct search options were used (page 2, count 5 -> startIndex 10, maxResults 5)
        var searchOptionsCaptor = org.mockito.ArgumentCaptor.forClass(SearchOptions.class);
        verify(processAPI).searchTimerEventTriggerInstances(eq(caseId), searchOptionsCaptor.capture());
        var searchOptions = searchOptionsCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(searchOptions.getStartIndex()).isEqualTo(10);
        org.assertj.core.api.Assertions.assertThat(searchOptions.getMaxResults()).isEqualTo(5);
    }

    @Test
    void should_use_default_pagination_values_when_not_provided() throws Exception {
        // Given
        final long caseId = 123L;

        when(processAPI.searchTimerEventTriggerInstances(anyLong(), any(SearchOptions.class)))
                .thenReturn(new SearchResultImpl<>(5,
                        List.of(new TimerEventTriggerInstanceImpl(1L, 1L, "timer1", new Date()))));

        // When & Then - no p and c parameters, should use defaults (p=0, c=10)
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", String.valueOf(caseId))
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify defaults were used (p=0, c=10 -> startIndex 0, maxResults 10)
        var searchOptionsCaptor = org.mockito.ArgumentCaptor.forClass(SearchOptions.class);
        verify(processAPI).searchTimerEventTriggerInstances(eq(caseId), searchOptionsCaptor.capture());
        var searchOptions = searchOptionsCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(searchOptions.getStartIndex()).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(searchOptions.getMaxResults()).isEqualTo(10);
    }

    @Test
    void should_return_bad_request_when_p_parameter_is_invalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", "123")
                .param("p", "invalid")
                .param("c", "10")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_when_c_parameter_is_invalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/API/bpm/timerEventTrigger")
                .param("caseId", "123")
                .param("p", "0")
                .param("c", "invalid")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
