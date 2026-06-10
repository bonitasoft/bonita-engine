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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;
import org.bonitasoft.engine.bpm.process.impl.internal.ProcessNameInfoImpl;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class ProcessNameControllerTest extends AbstractControllerTest<ProcessNameController> {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected ProcessNameController createController() {
        return spy(new ProcessNameController());
    }

    @Override
    protected void configureMocks(ProcessNameController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_grouped_data_and_content_range() throws Exception {
        // given
        SearchResult<ProcessNameInfo> searchResult = new SearchResultImpl<>(5L,
                List.of(new ProcessNameInfoImpl("invoice", "Invoice", List.of("1.0", "2.0"))));
        doReturn(searchResult).when(processAPI).searchProcessNames(any(SearchOptions.class));

        // when - then
        mockMvc.perform(get("/API/bpm/processName")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-1/5"))
                .andExpect(jsonPath("$[0].name").value("invoice"))
                .andExpect(jsonPath("$[0].displayName").value("Invoice"))
                .andExpect(jsonPath("$[0].versions[0]").value("1.0"))
                .andExpect(jsonPath("$[0].versions[1]").value("2.0"));
    }

    @Test
    void should_default_sort_to_displayName_and_pass_activationState_filter() throws Exception {
        // given
        ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        doReturn(new SearchResultImpl<>(0L, List.of())).when(processAPI).searchProcessNames(captor.capture());

        // when
        mockMvc.perform(get("/API/bpm/processName")
                .sessionAttrs(sessionAttributes)
                .param("f", "activationState=ENABLED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // then
        SearchOptions options = captor.getValue();
        assertThat(options.getSorts()).hasSize(1);
        assertThat(options.getSorts().get(0).getField()).isEqualTo("displayName");
        assertThat(options.getSorts().get(0).getOrder()).isEqualTo(Order.ASC);
        assertThat(options.getFilters())
                .extracting(SearchFilter::getField, SearchFilter::getValue)
                .containsExactly(tuple("activationState", "ENABLED"));
    }

    @Test
    void should_reject_unsupported_sort_field() throws Exception {
        // when - then: ordering on version is not supported for the grouped resource
        mockMvc.perform(get("/API/bpm/processName")
                .sessionAttrs(sessionAttributes)
                .param("o", "version ASC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_reject_compound_sort() throws Exception {
        // when - then: only a single sort clause is applied, so a compound order is rejected rather than half-honored
        mockMvc.perform(get("/API/bpm/processName")
                .sessionAttrs(sessionAttributes)
                .param("o", "displayName ASC, name DESC")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_reject_unknown_activation_state_value() throws Exception {
        // when - then: a value that is not a real ActivationState (incl. the engine sentinel '*') is rejected
        mockMvc.perform(get("/API/bpm/processName")
                .sessionAttrs(sessionAttributes)
                .param("f", "activationState=NOPE")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
