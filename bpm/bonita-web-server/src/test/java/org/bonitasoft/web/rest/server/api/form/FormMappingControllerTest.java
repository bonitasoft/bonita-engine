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
package org.bonitasoft.web.rest.server.api.form;

import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.web.rest.server.api.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class FormMappingControllerTest extends AbstractControllerTest<FormMappingController> {

    @Mock
    protected ProcessAPI processAPI;

    @Override
    protected FormMappingController createController() {
        return spy(new FormMappingController());
    }

    @Override
    protected void configureMocks(FormMappingController controller) throws Exception {
        doReturn(processAPI).when(controller).getProcessAPI(apiSession);
    }

    @Test
    void should_return_content_range_header() throws Exception {
        SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        FormMapping formMapping = mock(FormMapping.class);
        List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(21L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        mockMvc.perform(get("/API/form/mapping")
                .sessionAttrs(sessionAttributes)
                .param("p", "2")
                .param("c", "10")
                .param("f", "type=TASK")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "2-1/21"));
    }

    @Test
    void should_return_form_mapping_data() throws Exception {
        SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        FormMapping formMapping = new FormMapping();
        formMapping.setTask("myTask");
        List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        mockMvc.perform(get("/API/form/mapping")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].task").value("myTask"));
    }

    @Test
    void should_return_long_ids_as_strings() throws Exception {
        SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        FormMapping formMapping = new FormMapping();
        formMapping.setId(11125555888888L);
        formMapping.setTask("myTask");
        formMapping.setProcessDefinitionId(4871148324840256385L);
        formMapping.setPageId(1L);
        formMapping.setLastUpdatedBy(1L);
        List<FormMapping> formMappings = new ArrayList<>();
        formMappings.add(formMapping);
        doReturn(formMappings).when(searchResult).getResult();
        doReturn(1L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        mockMvc.perform(get("/API/form/mapping")
                .sessionAttrs(sessionAttributes)
                .param("p", "0")
                .param("c", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("11125555888888"))
                .andExpect(jsonPath("$[0].processDefinitionId").value("4871148324840256385"))
                .andExpect(jsonPath("$[0].pageId").value("1"))
                .andExpect(jsonPath("$[0].lastUpdatedBy").value("1"));
    }

    @Test
    void should_use_default_pagination_parameters() throws Exception {
        SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        doReturn(nCopies(10, mock(FormMapping.class))).when(searchResult).getResult();
        doReturn(11L).when(searchResult).getCount();
        doReturn(searchResult).when(processAPI).searchFormMappings(any(SearchOptions.class));

        mockMvc.perform(get("/API/form/mapping")
                .sessionAttrs(sessionAttributes)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "0-10/11"));
    }

    @Test
    void should_pass_correct_search_options() throws Exception {
        SearchResult<FormMapping> searchResult = mock(SearchResult.class);
        doReturn(List.of()).when(searchResult).getResult();
        doReturn(0L).when(searchResult).getCount();
        ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        doReturn(searchResult).when(processAPI).searchFormMappings(captor.capture());

        mockMvc.perform(get("/API/form/mapping")
                .sessionAttrs(sessionAttributes)
                .param("p", "2")
                .param("c", "10")
                .param("s", "mySearch")
                .param("o", "id ASC")
                .param("f", "type=TASK")
                .param("f", "processDefinitionId=12345")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        SearchOptions options = captor.getValue();
        assertThat(options.getStartIndex()).isEqualTo(20); // page 2 * count 10
        assertThat(options.getMaxResults()).isEqualTo(10);
        assertThat(options.getSearchTerm()).isEqualTo("mySearch");
        assertThat(options.getSorts()).hasSize(1);
        assertThat(options.getSorts().get(0).getField()).isEqualTo("id");
        assertThat(options.getSorts().get(0).getOrder()).isEqualTo(Order.ASC);
        assertThat(options.getFilters())
                .hasSize(2)
                .extracting(SearchFilter::getField, SearchFilter::getValue)
                .containsExactlyInAnyOrder(
                        tuple("type", FormMappingType.TASK),
                        tuple("processDefinitionId", "12345"));
    }

}
