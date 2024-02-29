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
package org.bonitasoft.web.rest.server.datastore.bpm.connector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorInstanceImpl;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.bpm.connector.ConnectorInstanceItem;
import org.bonitasoft.web.rest.server.BonitaRestAPIServlet;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * @author Vincent Elcrin
 */
public class ConnectorInstanceDatastoreTest {

    @Spy
    private final ConnectorInstanceDatastore spiedDatastore = new ConnectorInstanceDatastore(null);

    @Mock
    private ProcessAPI mockedProcessAPI;

    @BeforeClass
    public static void initEnvironnement() {
        I18n.getInstance();
        new BonitaRestAPIServlet();
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(this.mockedProcessAPI).when(this.spiedDatastore).getProcessAPI();
    }

    // //////////////////////////////////////////////////////////////////////////
    // / Test search
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Test right parameters go through engine API call method
     *
     * @throws Exception
     */
    @Test
    public void searchBuildRightParameters() throws Exception {
        final Map<String, String> filters = new HashMap<>();
        filters.put(ConnectorInstanceItem.ATTRIBUTE_CONTAINER_ID, "1");
        filters.put(ConnectorInstanceItem.ATTRIBUTE_STATE, "2");

        final SearchOptions searchOptions = this.spiedDatastore.buildSearchOptions(0, 123, "searchTerm",
                "order " + Order.ASC, filters);

        Assert.assertEquals(0, searchOptions.getStartIndex());
        Assert.assertEquals(123, searchOptions.getMaxResults());
        Assert.assertEquals("searchTerm", searchOptions.getSearchTerm());
        Assert.assertEquals("order", searchOptions.getSorts().get(0).getField());
        Assert.assertEquals(Order.ASC, searchOptions.getSorts().get(0).getOrder());
        Assert.assertEquals(filters.size(), searchOptions.getFilters().size());
    }

    /**
     * Test search result conversion
     *
     * @throws Exception
     */
    @Test
    public void searchReturnAllItems() throws Exception {
        final ConnectorInstance connectorInstance1 = createConnectorInstanceImpl(1L, "instance 1");
        final ConnectorInstance connectorInstance2 = createConnectorInstanceImpl(1L, "instance 2");
        final SearchResult<ConnectorInstance> expected = new SearchResultImpl<>(2,
                Arrays.asList(connectorInstance1, connectorInstance2));

        Mockito.when(this.mockedProcessAPI.searchConnectorInstances(Mockito.any(SearchOptions.class)))
                .thenReturn(expected);

        final ItemSearchResult<ConnectorInstanceItem> searchResult = this.spiedDatastore.search(0, 10, null, null,
                new HashMap<>());

        Mockito.verify(this.mockedProcessAPI).searchConnectorInstances(Mockito.any(SearchOptions.class));
        Assert.assertTrue(areEquals(new ConnectorInstanceItemWrapper(expected.getResult().get(0)),
                searchResult.getResults().get(0)));
        Assert.assertTrue(areEquals(new ConnectorInstanceItemWrapper(expected.getResult().get(1)),
                searchResult.getResults().get(1)));
    }

    // //////////////////////////////////////////////////////////////////////////
    // / Convenient tools
    // //////////////////////////////////////////////////////////////////////////

    private boolean areEquals(final Item expected, final Item actual) {
        return expected.getAttributes().equals(actual.getAttributes());
    }

    private ConnectorInstance createConnectorInstanceImpl(final long id, final String name) {
        final ConnectorInstanceImpl connectorInstance = new ConnectorInstanceImpl(name, 2L, "containerType",
                String.valueOf(id), "version",
                ConnectorState.DONE,
                ConnectorEvent.ON_ENTER);
        connectorInstance.setId(1L);
        return connectorInstance;
    }

}
