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
package org.bonitasoft.engine.search.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameGroupQuery;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameKey;
import org.bonitasoft.engine.core.process.definition.model.ProcessNameVersion;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchProcessNamesTest {

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Test
    public void should_group_versions_by_name_and_displayName() throws Exception {
        // given
        doReturn(2L).when(processDefinitionService).getNumberOfProcessNameGroups(nullable(String.class), anyString());
        doReturn(List.of(new ProcessNameKey("invoice", "Invoice"), new ProcessNameKey("report", "Report")))
                .when(processDefinitionService).searchProcessNameGroups(any());
        doReturn(List.of(new ProcessNameVersion("invoice", "Invoice", "2.0"),
                new ProcessNameVersion("invoice", "Invoice", "1.0"),
                new ProcessNameVersion("report", "Report", "1.0")))
                .when(processDefinitionService).getVersionsForProcessNames(any(), nullable(String.class));

        // when
        final SearchResult<ProcessNameInfo> result = search(new SearchOptionsBuilder(0, 10).done());

        // then
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getResult())
                .extracting(ProcessNameInfo::getName, ProcessNameInfo::getDisplayName, ProcessNameInfo::getVersions)
                .containsExactly(
                        tuple("invoice", "Invoice", List.of("2.0", "1.0")),
                        tuple("report", "Report", List.of("1.0")));
    }

    @Test
    public void should_extract_activationState_filter_default_sort_and_match_all_term() throws Exception {
        // given - no search term, no sort, only an activationState filter
        givenOneGroupWithOneVersion();
        final SearchOptions options = new SearchOptionsBuilder(0, 10)
                .filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, "ENABLED").done();

        // when
        search(options);

        // then - activationState forwarded, term defaults to "%", sort defaults to displayName ascending, paging kept
        final ProcessNameGroupQuery query = capturePageQuery();
        assertThat(query.activationState()).isEqualTo("ENABLED");
        assertThat(query.searchTerm()).isEqualTo("%");
        assertThat(query.sortByName()).isFalse();
        assertThat(query.ascending()).isTrue();
        assertThat(query.startIndex()).isZero();
        assertThat(query.maxResults()).isEqualTo(10);
        // versions query receives the same activationState filter
        verify(processDefinitionService).getVersionsForProcessNames(any(), eq("ENABLED"));
    }

    @Test
    public void should_translate_name_descending_sort_and_wrap_search_term() throws Exception {
        // given
        givenOneGroupWithOneVersion();
        final SearchOptions options = new SearchOptionsBuilder(0, 10)
                .searchTerm("inv")
                .sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.DESC).done();

        // when
        search(options);

        // then
        final ProcessNameGroupQuery query = capturePageQuery();
        assertThat(query.searchTerm()).isEqualTo("%inv%");
        assertThat(query.sortByName()).isTrue();
        assertThat(query.ascending()).isFalse();
    }

    @Test
    public void should_escape_like_wildcards_in_search_term() throws Exception {
        // given - a term containing the escape char and both LIKE wildcards
        givenOneGroupWithOneVersion();
        final SearchOptions options = new SearchOptionsBuilder(0, 10).searchTerm("a%b_c#").done();

        // when
        search(options);

        // then - '#','%','_' are escaped (escape char first) and the term is wrapped
        assertThat(capturePageQuery().searchTerm()).isEqualTo("%a#%b#_c##%");
    }

    @Test
    public void should_return_empty_result_without_querying_groups_when_count_is_zero() throws Exception {
        // given
        doReturn(0L).when(processDefinitionService).getNumberOfProcessNameGroups(nullable(String.class), anyString());

        // when
        final SearchResult<ProcessNameInfo> result = search(new SearchOptionsBuilder(0, 10).done());

        // then
        assertThat(result.getCount()).isZero();
        assertThat(result.getResult()).isEmpty();
        verify(processDefinitionService, never()).searchProcessNameGroups(any());
        verify(processDefinitionService, never()).getVersionsForProcessNames(any(), nullable(String.class));
    }

    private void givenOneGroupWithOneVersion() throws Exception {
        doReturn(1L).when(processDefinitionService).getNumberOfProcessNameGroups(nullable(String.class), anyString());
        doReturn(List.of(new ProcessNameKey("invoice", "Invoice"))).when(processDefinitionService)
                .searchProcessNameGroups(any());
        doReturn(List.of(new ProcessNameVersion("invoice", "Invoice", "1.0"))).when(processDefinitionService)
                .getVersionsForProcessNames(any(), nullable(String.class));
    }

    private ProcessNameGroupQuery capturePageQuery() throws Exception {
        final ArgumentCaptor<ProcessNameGroupQuery> captor = ArgumentCaptor.forClass(ProcessNameGroupQuery.class);
        verify(processDefinitionService).searchProcessNameGroups(captor.capture());
        return captor.getValue();
    }

    private SearchResult<ProcessNameInfo> search(final SearchOptions options) throws Exception {
        return new SearchProcessNames(processDefinitionService, options).search();
    }
}
