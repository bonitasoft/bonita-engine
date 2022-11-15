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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskInstanceImpl;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowNodeDatastoreTest {

    @Mock
    private APISession engineSession;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private FlowNodeConverter converter;

    @InjectMocks
    private FlowNodeDatastore flowNodeDatastore;

    @Before
    public void before() {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        FlowNodeConverter.setFlowNodeConverter(converter);
        flowNodeDatastore = spy(new FlowNodeDatastore(engineSession));
        doReturn(processAPI).when(flowNodeDatastore).getProcessAPI();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.flownode.AbstractFlowNodeDatastore#count(java.lang.String, java.lang.String, java.util.Map)}.
     */
    @Test
    public final void count_should_return_number_of_flow_nodes_on_Engine() throws SearchException {
        final String search = "plop";
        final String orders = FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID;
        final Map<String, String> filters = Collections.emptyMap();
        final List<FlowNodeInstance> flowNodeInstances = Arrays
                .asList((FlowNodeInstance) new UserTaskInstanceImpl("name", 9L, 18L));
        final SearchResult<FlowNodeInstance> searchResult = new SearchResultImpl<>(1L, flowNodeInstances);
        doReturn(searchResult).when(processAPI).searchFlowNodeInstances(any(SearchOptions.class));

        // When
        final long result = flowNodeDatastore.count(search, orders, filters);

        // Then
        assertEquals(searchResult.getCount(), result);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.flownode.AbstractFlowNodeDatastore#search(int, int, java.lang.String, java.lang.String, java.util.Map)}
     * .
     */
    @Test
    public final void search_should_get_list_of_flow_nodes_on_Engine_and_convert_them_to_FlowNodeItem()
            throws SearchException {
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = FlowNodeItem.ATTRIBUTE_DESCRIPTION;
        final Map<String, String> filters = Collections.emptyMap();
        final FlowNodeInstance flowNodeInstance = new UserTaskInstanceImpl("name", 9L, 18L);
        final List<FlowNodeInstance> flowNodeInstances = Arrays.asList(flowNodeInstance);
        final SearchResult<FlowNodeInstance> searchResult = new SearchResultImpl<>(1L, flowNodeInstances);
        doReturn(searchResult).when(processAPI).searchFlowNodeInstances(any(SearchOptions.class));
        final FlowNodeItem flowNodeItem = new FlowNodeItem();
        doReturn(flowNodeItem).when(converter)._convertEngineToConsoleItem(flowNodeInstance);

        // When
        final ItemSearchResult<FlowNodeItem> result = flowNodeDatastore.search(page, resultsByPage, search, orders,
                filters);

        // Then
        final List<FlowNodeItem> flowNodeItems = result.getResults();
        assertEquals(1, flowNodeItems.size());
        assertEquals(flowNodeItem, flowNodeItems.get(0));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.web.rest.server.datastore.bpm.flownode.AbstractFlowNodeDatastore#search(int, int, java.lang.String, java.lang.String, java.util.Map)}
     * .
     */
    @Test(expected = APIException.class)
    public final void search_should_throw_an_exception_when_Engine_failed() throws SearchException {
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "plop";
        final String orders = FlowNodeItem.ATTRIBUTE_DESCRIPTION;
        final Map<String, String> filters = Collections.emptyMap();
        doThrow(new SearchException(new Exception("toto"))).when(processAPI)
                .searchFlowNodeInstances(any(SearchOptions.class));

        // When
        flowNodeDatastore.search(page, resultsByPage, search, orders, filters);
    }
}
