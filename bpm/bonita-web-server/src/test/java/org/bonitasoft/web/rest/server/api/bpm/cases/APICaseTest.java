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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class APICaseTest {

    @Mock
    private UserDatastore userDatastore;

    @Mock
    private ProcessDatastore processDatastore;

    @Mock
    private FlowNodeDatastore flowNodeDatastore;

    @Mock
    private CaseDatastore caseDatastore;

    private APICase apiCase;

    @Before
    public void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        apiCase = spy(new APICase());
        doReturn(userDatastore).when(apiCase).getUserDatastore();
        doReturn(processDatastore).when(apiCase).getProcessDatastore();
        doReturn(flowNodeDatastore).when(apiCase).getFlowNodeDatastore();
        doReturn(caseDatastore).when(apiCase).getCaseDatastore();
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#defineDefaultSearchOrder()}.
     */
    @Test
    public final void defineDefaultSearchOrder_should_be_descending_creation_date() {
        // When
        final String defineDefaultSearchOrder = apiCase.defineDefaultSearchOrder();

        // Then
        assertEquals(ProcessInstanceCriterion.CREATION_DATE_DESC.name(), defineDefaultSearchOrder);
    }

    /**
     * Test method for {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#delete(java.util.List)}.
     */
    @Test
    public final void delete_should_delete_case_items_on_CaseDatastore() {
        // Given
        final List<APIID> ids = Arrays.asList(APIID.makeAPIID(78L));

        // When
        apiCase.delete(ids);

        // Then
        verify(caseDatastore).delete(ids);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#add(org.bonitasoft.web.rest.model.bpm.cases.CaseItem)}.
     */
    @Test
    public final void add_should_add_case_item_on_CaseDatastore() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        doReturn(item).when(caseDatastore).add(item);

        // When
        final CaseItem result = apiCase.add(item);

        // Then
        assertEquals(item, result);
        verify(caseDatastore).add(item);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#get(org.bonitasoft.web.toolkit.client.data.APIID)}.
     */
    @Test
    public final void get_should_get_case_item_on_CaseDatastore() {
        // Given
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(item).when(caseDatastore).get(id);

        // When
        final CaseItem result = apiCase.get(id);

        // Then
        assertEquals(item, result);
        verify(caseDatastore).get(id);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#search(int, int, java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test
    public final void search_should_search_case_items_on_CaseDatastore() {
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                Arrays.asList(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertEquals(searchResult, result);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#search(int, int, java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test
    public final void search_should_search_case_items_on_CaseDatastore_when_supervisor_filter_is_used_without_team_manager_filter() {
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_SUPERVISOR_ID, "3");

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                Arrays.asList(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertEquals(searchResult, result);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#search(int, int, java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test
    public final void search_should_search_case_items_on_CaseDatastore_when_team_manager_filter_is_used_without_supervisor_filter() {
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = Collections.singletonMap(CaseItem.FILTER_TEAM_MANAGER_ID, "9");

        final ItemSearchResult<CaseItem> searchResult = new ItemSearchResult<>(page, resultsByPage, resultsByPage,
                Arrays.asList(new CaseItem()));
        doReturn(searchResult).when(caseDatastore).search(page, resultsByPage, search, orders, filters);

        // When
        final ItemSearchResult<CaseItem> result = apiCase.search(page, resultsByPage, search, orders, filters);

        // Then
        verify(caseDatastore).search(page, resultsByPage, search, orders, filters);
        assertEquals(searchResult, result);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#search(int, int, java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test(expected = APIException.class)
    public final void search_should_throw_exception_when_team_manager_and_supervisor_filters_are_used_together() {
        final int page = 6;
        final int resultsByPage = 10;
        final String search = "plop";
        final String orders = CaseItem.ATTRIBUTE_END_DATE;
        final Map<String, String> filters = new HashMap<>();
        filters.put(CaseItem.FILTER_TEAM_MANAGER_ID, "9");
        filters.put(CaseItem.FILTER_SUPERVISOR_ID, "3");

        // When
        apiCase.search(page, resultsByPage, search, orders, filters);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_fill_user_who_start_case_when_deploy_of_started_by_is_active() {
        // Given
        final APIID startedByUserID = APIID.makeAPIID(3L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);
        doReturn(startedByUserID).when(item).getStartedByUserId();

        final List<String> deploys = Arrays.asList(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(startedByUserID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID, userItem);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_do_nothing_when_deploy_of_started_by_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_fill_substitute_user_who_start_case_when_deploy_of_started_by_substitute_is_active() {
        // Given
        final APIID startedBySubstituteUserID = APIID.makeAPIID(6L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID).when(item)
                .getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);
        doReturn(startedBySubstituteUserID).when(item).getStartedBySubstituteUserId();

        final List<String> deploys = Arrays.asList(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);

        final UserItem userItem = new UserItem();
        doReturn(userItem).when(userDatastore).get(startedBySubstituteUserID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userItem);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_do_nothing_when_deploy_of_started_by_substitute_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_fill_process_when_deploy_of_process_is_active() {
        // Given
        final APIID processId = APIID.makeAPIID(9L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(CaseItem.ATTRIBUTE_PROCESS_ID).when(item).getAttributeValue(CaseItem.ATTRIBUTE_PROCESS_ID);
        doReturn(processId).when(item).getProcessId();

        final List<String> deploys = Arrays.asList(CaseItem.ATTRIBUTE_PROCESS_ID);

        final ProcessItem processItem = new ProcessItem();
        doReturn(processItem).when(processDatastore).get(processId);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item).setDeploy(CaseItem.ATTRIBUTE_PROCESS_ID, processItem);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_do_nothing_when_deploy_of_process_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> deploys = new ArrayList<>();

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillDeploys(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillDeploys_should_do_nothing_when_id_is_invalid() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        doReturn("0").when(item).getAttributeValue(CaseItem.ATTRIBUTE_PROCESS_ID);
        doReturn("-1").when(item).getAttributeValue(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID);

        final List<String> deploys = Arrays.asList(CaseItem.ATTRIBUTE_PROCESS_ID);

        // When
        apiCase.fillDeploys(item, deploys);

        // Then
        verify(item, never()).setDeploy(anyString(), any(Item.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillCounters(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillCounters_should_fill_number_of_failed_flow_nodes_when_counter_of_failed_flow_nodes_is_active() {
        // Given
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(id).when(item).getId();

        final List<String> counters = Arrays.asList(CaseItem.COUNTER_FAILED_FLOW_NODES);

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

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillCounters(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public final void fillCounters_should_do_nothing_when_counter_of_failed_flow_nodes_is_not_active() {
        // Given
        final CaseItem item = mock(CaseItem.class);
        final List<String> counters = new ArrayList<>();

        // When
        apiCase.fillCounters(item, counters);

        // Then
        verify(item, never()).setAttribute(anyString(), anyLong());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.api.bpm.cases.APICase#fillCounters(org.bonitasoft.web.rest.model.bpm.cases.CaseItem, java.util.List)}.
     */
    @Test
    public void fillNumberOfActiveFlowNodesIfActiveCounterExists() {
        // Given
        final APIID id = APIID.makeAPIID(78L);
        final CaseItem item = mock(CaseItem.class);
        doReturn(id).when(item).getId();

        final List<String> counters = Arrays.asList(CaseItem.COUNTER_ACTIVE_FLOW_NODES);

        final Map<String, String> filters = new HashMap<>();
        filters.put(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, String.valueOf(id.toLong()));
        final long numberOfFailedFlowNodes = 2L;
        doReturn(numberOfFailedFlowNodes).when(flowNodeDatastore).count(null, null, filters);

        // When
        apiCase.fillCounters(item, counters);

        // Then
        verify(item).setAttribute(CaseItem.COUNTER_ACTIVE_FLOW_NODES, numberOfFailedFlowNodes);
    }
}
