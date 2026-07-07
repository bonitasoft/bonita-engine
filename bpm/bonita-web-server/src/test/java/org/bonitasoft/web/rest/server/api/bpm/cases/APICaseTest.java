/**
 * Copyright (C) 2022 Bonitasoft S.A.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

/**
 * @author Celine Souchet
 */
@ExtendWith(MockitoExtension.class)
class APICaseTest {

    @Mock
    private UserDatastore userDatastore;

    @Mock
    private ProcessDatastore processDatastore;

    @Mock
    private FlowNodeDatastore flowNodeDatastore;

    @Mock
    private CaseDatastore caseDatastore;

    private APICase apiCase;

    @BeforeEach
    void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiCase = spy(new APICase());
    }

    @Test
    void defineDefaultSearchOrder_should_be_descending_creation_date() {
        // When
        final String defineDefaultSearchOrder = apiCase.defineDefaultSearchOrder();

        // Then
        assertThat(defineDefaultSearchOrder).isEqualTo(ProcessInstanceCriterion.CREATION_DATE_DESC.name());
    }

    @Test
    void delete_should_delete_case_items_on_CaseDatastore() {
        // Given
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final List<APIID> ids = Collections.singletonList(APIID.makeAPIID(78L));

        // When
        apiCase.delete(ids);

        // Then
        verify(caseDatastore).delete(ids);
    }

    @Test
    void add_should_add_case_item_on_CaseDatastore() {
        // Given
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final CaseItem item = mock(CaseItem.class);
        doReturn(item).when(caseDatastore).add(item);

        // When
        final CaseItem result = apiCase.add(item);

        // Then
        assertThat(result).isEqualTo(item);
        verify(caseDatastore).add(item);
    }

    @Test
    void get_should_get_case_item_on_CaseDatastore() {
        // Given
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(item).when(caseDatastore).get(id);

        // When
        final CaseItem result = apiCase.get(id);

        // Then
        assertThat(result).isEqualTo(item);
        verify(caseDatastore).get(id);
    }

    @Test
    void search_should_search_case_items_on_CaseDatastore() {
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                List.of(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertThat(result).isEqualTo(searchResult);
    }

    @Test
    void search_should_search_case_items_on_CaseDatastore_when_supervisor_filter_is_used_without_team_manager_filter() {
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_SUPERVISOR_ID, "3");

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                List.of(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertThat(result).isEqualTo(searchResult);
    }

    @Test
    void search_should_search_case_items_on_CaseDatastore_when_team_manager_filter_is_used_without_supervisor_filter() {
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_TEAM_MANAGER_ID, "9");

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                List.of(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertThat(result).isEqualTo(searchResult);
    }

    @Test
    void search_should_throw_exception_when_team_manager_and_supervisor_filters_are_used_together() {
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();
        filters.put(CaseItem.FILTER_TEAM_MANAGER_ID, "9");
        filters.put(CaseItem.FILTER_SUPERVISOR_ID, "3");

        // When / Then
        assertThatExceptionOfType(APIException.class)
                .isThrownBy(() -> apiCase.search(page, resultsByPage, search, orders, filters));
    }

    @Test
    void fillDeploys_should_fill_user_who_start_case_when_deploy_of_started_by_is_active() {
        // Given
        doReturn(userDatastore).when(apiCase).getUserDatastore();
        final APIID startedByUserID = APIID.makeAPIID(3L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(startedByUserID).when(item).getStartedByUserId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(startedByUserID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_do_nothing_when_deploy_of_started_by_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    @Test
    void fillDeploys_should_fill_substitute_user_who_start_case_when_deploy_of_started_by_substitute_is_active() {
        // Given
        doReturn(userDatastore).when(apiCase).getUserDatastore();
        final APIID startedBySubstituteUserID = APIID.makeAPIID(6L);
        final CaseItem item = mock(CaseItem.class);
        // lenient: fillDeploys probes started_by and processDefinitionId too, before this stub is matched
        lenient().doReturn(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);
        doReturn(startedBySubstituteUserID).when(item).getStartedBySubstituteUserId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(startedBySubstituteUserID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userItem);
    }

    @Test
    void fillDeploys_should_do_nothing_when_deploy_of_started_by_substitute_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    @Test
    void fillDeploys_should_fill_process_when_deploy_of_process_is_active() {
        // Given
        doReturn(processDatastore).when(apiCase).getProcessDatastore();
        final APIID processId = APIID.makeAPIID(9L);
        final CaseItem item = mock(CaseItem.class);
        // lenient: fillDeploys probes started_by and startedBySubstitute too, before this stub is matched
        lenient().doReturn(CaseItem.ATTRIBUTE_PROCESS_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_PROCESS_ID);
        doReturn(processId).when(item).getProcessId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_PROCESS_ID);

        final ProcessItem processItem = new ProcessItem();
        doReturn(processItem).when(processDatastore).get(processId);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_PROCESS_ID, processItem);
    }

    @Test
    void fillDeploys_should_do_nothing_when_deploy_of_process_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    @Test
    void fillDeploys_should_do_nothing_when_id_is_invalid() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        // lenient: fillDeploys probes started_by and startedBySubstitute too, before this stub is matched
        lenient().doReturn("0").when(item).getAttributeValue(CaseItem.ATTRIBUTE_PROCESS_ID);

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_PROCESS_ID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    @Test
    void fillDeploys_should_skip_started_by_deploy_and_keep_other_deploys_when_user_no_longer_exists() {
        // Given a case started by a user that was deleted from the organization, plus a still-existing process
        doReturn(userDatastore).when(apiCase).getUserDatastore();
        doReturn(processDatastore).when(apiCase).getProcessDatastore();

        final APIID deletedUserId = APIID.makeAPIID(3L);
        final APIID processId = APIID.makeAPIID(9L);
        final CaseItem item = mock(CaseItem.class);
        // lenient: fillDeploys also probes startedBySubstitute (once the fix lets it continue past the
        // unresolvable started_by deploy) while the processDefinitionId stub is still unused
        lenient().doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(deletedUserId).when(item).getStartedByUserId();
        lenient().doReturn(CaseItem.ATTRIBUTE_PROCESS_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_PROCESS_ID);
        doReturn(processId).when(item).getProcessId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                CaseItem.ATTRIBUTE_PROCESS_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);
        final ProcessItem processItem = new ProcessItem();
        doReturn(processItem).when(processDatastore).get(processId);

        // When the unresolvable user deploy must not fail the whole request
        assertThatCode(() -> apiCase.fillDeploys(item, deploys)).doesNotThrowAnyException();

        // Then the started_by deploy is skipped, but the process deploy still succeeds
        verify(item, never()).setDeploy(eq(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID), any());
        verify(item).setDeploy(CaseItem.ATTRIBUTE_PROCESS_ID, processItem);
    }

    @Test
    void fillDeploys_should_skip_started_by_substitute_deploy_when_user_no_longer_exists() {
        // Given a case whose substitute starter was deleted from the organization
        doReturn(userDatastore).when(apiCase).getUserDatastore();

        final APIID deletedUserId = APIID.makeAPIID(6L);
        final CaseItem item = mock(CaseItem.class);
        // lenient: fillDeploys probes started_by first, before this startedBySubstitute stub is matched
        lenient().doReturn(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);
        doReturn(deletedUserId).when(item).getStartedBySubstituteUserId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);

        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        // When / Then the unresolvable substitute deploy is skipped without failing
        assertThatCode(() -> apiCase.fillDeploys(item, deploys)).doesNotThrowAnyException();
        verify(item, never()).setDeploy(eq(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID), any());
    }

    @Test
    void fillDeploys_should_rethrow_when_not_found_cause_is_not_a_deleted_entity() {
        // Given a started_by deploy that fails with an APINotFoundException whose cause is NOT an
        // engine NotFoundException (e.g. some other API-layer failure). Tolerance is scoped to a
        // genuinely removed entity, so this must not be swallowed as a bare id.
        doReturn(userDatastore).when(apiCase).getUserDatastore();

        final APIID userId = APIID.makeAPIID(3L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(userId).when(item).getStartedByUserId();

        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);

        final APINotFoundException unexpected = new APINotFoundException(new RuntimeException("boom"));
        doThrow(unexpected).when(userDatastore).get(userId);

        // When / Then the unexpected not-found propagates unchanged instead of being masked
        assertThatExceptionOfType(APINotFoundException.class)
                .isThrownBy(() -> apiCase.fillDeploys(item, deploys))
                .isSameAs(unexpected);
        verify(item, never()).setDeploy(eq(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID), any());
    }

    @Test
    void fillCounters_should_fill_number_of_failed_flow_nodes_when_counter_of_failed_flow_nodes_is_active() {
        // Given
        doReturn(flowNodeDatastore).when(apiCase).getFlowNodeDatastore();
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(id).when(item).getId();

        final List<String> counters = List.of(CaseItem.COUNTER_FAILED_FLOW_NODES);

        final Map<String, String> filters = new HashMap<>();
        filters.put(FlowNodeItem.ATTRIBUTE_STATE, FlowNodeItem.VALUE_STATE_FAILED);
        filters.put(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, String.valueOf(id.toLong()));
        final long numberOfFailedFlowNodes = 2L;
        doReturn(numberOfFailedFlowNodes).when(flowNodeDatastore).count(null, null, filters);

        // When
        apiCase.fillCounters(item, counters);

        // Then
        verify(item).setAttribute(CaseItem.COUNTER_FAILED_FLOW_NODES, numberOfFailedFlowNodes);
    }

    @Test
    void fillCounters_should_do_nothing_when_counter_of_failed_flow_nodes_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> counters = new ArrayList<>();

        // When
        apiCase.fillCounters(item, counters);

        // Then
        verify(item, never()).setAttribute(anyString(), anyLong());
    }

    @Test
    void fillCounters_should_fill_number_of_active_flow_nodes_when_active_counter_exists() {
        // Given
        doReturn(flowNodeDatastore).when(apiCase).getFlowNodeDatastore();
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(id).when(item).getId();

        final List<String> counters = List.of(CaseItem.COUNTER_ACTIVE_FLOW_NODES);

        final Map<String, String> filters = new HashMap<>();
        filters.put(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, String.valueOf(id.toLong()));
        final long numberOfFailedFlowNodes = 2L;
        doReturn(numberOfFailedFlowNodes).when(flowNodeDatastore).count(null, null, filters);

        // When
        apiCase.fillCounters(item, counters);

        // Then
        verify(item).setAttribute(CaseItem.COUNTER_ACTIVE_FLOW_NODES, numberOfFailedFlowNodes);
    }

    @Test
    void runSearch_should_log_a_single_warning_naming_a_distinct_reference_once_across_rows() {
        // Given a page of two cases both started by the SAME user that was deleted from the
        // organization, and a search that asks to deploy started_by
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        doReturn(userDatastore).when(apiCase).getUserDatastore();

        final APIID deletedUserId = APIID.makeAPIID(5L);
        final CaseItem firstRow = mock(CaseItem.class);
        final CaseItem secondRow = mock(CaseItem.class);
        for (final CaseItem row : List.of(firstRow, secondRow)) {
            doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(row)
                    .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
            doReturn(deletedUserId).when(row).getStartedByUserId();
        }
        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(deletedUserId);

        final int page = 0;
        final int resultsByPage = 10;
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();
        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, 2,
                List.of(firstRow, secondRow));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, null, orders, filters);

        final ListAppender<ILoggingEvent> logAppender = startCapturingConsoleApiLogs();

        // When the page is searched with the dangling deploy
        final ItemSearchResult<CaseItem> result;
        try {
            result = apiCase.runSearch(page, resultsByPage, null, orders, filters, deploys, new ArrayList<>());
        } finally {
            stopCapturingConsoleApiLogs(logAppender);
        }

        // Then both rows are returned (no whole-page failure) ...
        assertThat(result.getResults()).containsExactly(firstRow, secondRow);
        // ... the unresolvable started_by is skipped on every row ...
        verify(firstRow, never()).setDeploy(eq(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID), any());
        verify(secondRow, never()).setDeploy(eq(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID), any());
        // ... and a SINGLE WARN names the missing reference once, even though two rows carried it
        assertThat(logAppender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage())
                    .contains("Could not resolve 1 distinct reference(s)")
                    .contains("'" + CaseItem.ATTRIBUTE_STARTED_BY_USER_ID + "' id " + deletedUserId);
        });
    }

    @Test
    void runSearch_should_log_distinct_references_not_one_per_affected_row() {
        // Given a page of 12 cases split between TWO deleted users (6 rows each) - the WARN must name
        // the two distinct missing references once each, and must NOT grow with the affected-row count
        // (no per-row ids, however many rows carry a dangling reference)
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
        doReturn(userDatastore).when(apiCase).getUserDatastore();

        final APIID firstDeletedUserId = APIID.makeAPIID(5L);
        final APIID secondDeletedUserId = APIID.makeAPIID(6L);
        final int rowCount = 12;
        final List<CaseItem> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            final APIID starterId = i < rowCount / 2 ? firstDeletedUserId : secondDeletedUserId;
            final CaseItem row = mock(CaseItem.class);
            doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(row)
                    .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
            doReturn(starterId).when(row).getStartedByUserId();
            rows.add(row);
        }
        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(firstDeletedUserId);
        doThrow(new APINotFoundException(new UserNotFoundException("user deleted")))
                .when(userDatastore).get(secondDeletedUserId);

        final int page = 0;
        final int resultsByPage = 20;
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();
        final List<String> deploys = List.of(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, rowCount, rows);
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, null, orders, filters);

        final ListAppender<ILoggingEvent> logAppender = startCapturingConsoleApiLogs();

        // When the page is searched with the dangling deploys
        try {
            apiCase.runSearch(page, resultsByPage, null, orders, filters, deploys, new ArrayList<>());
        } finally {
            stopCapturingConsoleApiLogs(logAppender);
        }

        // Then a SINGLE WARN names the two distinct missing references (regardless of the 12 rows)
        assertThat(logAppender.list).singleElement().satisfies(event -> {
            assertThat(event.getLevel()).isEqualTo(Level.WARN);
            assertThat(event.getFormattedMessage())
                    .contains("Could not resolve 2 distinct reference(s)")
                    .contains("'" + CaseItem.ATTRIBUTE_STARTED_BY_USER_ID + "' id " + firstDeletedUserId)
                    .contains("'" + CaseItem.ATTRIBUTE_STARTED_BY_USER_ID + "' id " + secondDeletedUserId);
        });
    }

    private static ListAppender<ILoggingEvent> startCapturingConsoleApiLogs() {
        final Logger consoleApiLogger = (Logger) LoggerFactory.getLogger(ConsoleAPI.class);
        // The summary is logged at WARN and guarded by isWarnEnabled(); pin the level so the message is
        // emitted (and captured) regardless of the ambient test configuration.
        consoleApiLogger.setLevel(Level.WARN);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        consoleApiLogger.addAppender(appender);
        return appender;
    }

    private static void stopCapturingConsoleApiLogs(final ListAppender<ILoggingEvent> appender) {
        final Logger consoleApiLogger = (Logger) LoggerFactory.getLogger(ConsoleAPI.class);
        consoleApiLogger.detachAppender(appender);
        // Restore inheritance from the root logger so the raised level does not leak to other tests.
        consoleApiLogger.setLevel(null);
    }
}
