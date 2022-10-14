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
package org.bonitasoft.web.rest.server.datastore.applicationpage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageDataStoreTest extends APITestWithMock {

    @Mock
    private ApplicationAPI applicationAPI;

    @Mock
    private ApplicationPageItemConverter converter;

    @Spy
    @InjectMocks
    private ApplicationPageDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ItemDefinitionFactory() {

            @Override
            public ItemDefinition<?> defineItemDefinitions(final String token) {
                return new ApplicationPageDefinition();
            }
        });
    }

    @Test
    public void should_return_result_of_engine_call_converted_to_item_on_add() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = new ApplicationPageItem();
        itemToCreate.setToken("firstPage");
        itemToCreate.setApplicationId(14L);
        itemToCreate.setPageId(28L);
        final ApplicationPageImpl applicationPage = new ApplicationPageImpl(14L, 28L, "firstPage");

        given(applicationAPI.createApplicationPage(14L, 28L, "firstPage")).willReturn(applicationPage);

        //when
        final ApplicationPageItem createdItem = dataStore.add(itemToCreate);

        //then
        assertThat(createdItem).isEqualTo(createdItem);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_CreationException_on_add() throws Exception {
        //given
        given(applicationAPI.createApplicationPage(anyLong(), anyLong(), anyString()))
                .willThrow(new CreationException(""));

        //when
        dataStore.add(new ApplicationPageItem());

        //then exception
    }

    @Test
    public void should_return_the_applicationPage_supplied_by_the_engine_converted_to_item_on_get() throws Exception {
        //given
        final ApplicationPage applicationPage = mock(ApplicationPage.class);
        final ApplicationPageItem item = mock(ApplicationPageItem.class);
        given(applicationAPI.getApplicationPage(1)).willReturn(applicationPage);
        given(converter.toApplicationPageItem(applicationPage)).willReturn(item);

        //when
        final ApplicationPageItem retrievedItem = dataStore.get(APIID.makeAPIID("1"));

        //then
        assertThat(retrievedItem).isNotNull();
        assertThat(retrievedItem).isEqualTo(item);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_the_engine_throw_NotFoundException_on_get() throws Exception {
        //given
        given(applicationAPI.getApplicationPage(1)).willThrow(new ApplicationPageNotFoundException(""));

        //when
        dataStore.get(APIID.makeAPIID("1"));

        //then exception
    }

    @Test
    public void should_delete_the_good_Application_Page_on_delete() throws Exception {
        //given

        //when
        dataStore.delete(Arrays.asList(APIID.makeAPIID("1"), APIID.makeAPIID("2")));

        //then
        verify(applicationAPI, times(1)).deleteApplicationPage(1);
        verify(applicationAPI, times(1)).deleteApplicationPage(2);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_on_delete_when_engine_throws_exception() throws Exception {
        doThrow(new DeletionException("")).when(applicationAPI).deleteApplicationPage(1);

        //when
        dataStore.delete(List.of(APIID.makeAPIID("1")));

        //then exception
    }

    @Test
    public void should_return_a_valid_ItemSearchResult_on_search() throws Exception {
        //given
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        final ApplicationPageImpl appPage = new ApplicationPageImpl(1, 11, "MyAppPage");
        appPage.setId(1);
        final ApplicationPageItem item = new ApplicationPageItem();
        given(converter.toApplicationPageItem(appPage)).willReturn(item);

        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(2, List.of(appPage)));

        //when
        final ItemSearchResult<ApplicationPageItem> retrievedItems = dataStore.search(0, 1, null, orders,
                Collections.emptyMap());

        //then
        assertThat(retrievedItems).isNotNull();
        assertThat(retrievedItems.getLength()).isEqualTo(1);
        assertThat(retrievedItems.getPage()).isEqualTo(0);
        assertThat(retrievedItems.getTotal()).isEqualTo(2);
        assertThat(retrievedItems.getResults().get(0).getToken()).isEqualTo("MyAppPage");

    }

    @Test
    public void should_call_engine_with_good_parameters_on_search() throws Exception {
        //given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "string to Match";
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID, "1");
        final ApplicationPageImpl appPage = new ApplicationPageImpl(1, 11, "MyAppPage");
        appPage.setId(1);
        final ApplicationPageItem item = new ApplicationPageItem();
        given(converter.toApplicationPageItem(appPage)).willReturn(item);

        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(2, List.of(appPage)));

        //when
        dataStore.search(page, resultsByPage, search, orders, filters);

        //then
        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationAPI, times(1)).searchApplicationPages(captor.capture());

        final SearchOptions searchOption = captor.getValue();
        assertThat(searchOption.getFilters()).hasSize(1);
        assertThat(searchOption.getFilters().get(0).getField()).isEqualTo(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID);
        assertThat(searchOption.getFilters().get(0).getValue()).isEqualTo("1");
        assertThat(searchOption.getSearchTerm()).isEqualTo(search);
        assertThat(searchOption.getMaxResults()).isEqualTo(1);
        assertThat(searchOption.getStartIndex()).isEqualTo(0);
        assertThat(searchOption.getSorts()).hasSize(1);
        assertThat(searchOption.getSorts().get(0).getField()).isEqualTo(ApplicationPageItem.ATTRIBUTE_TOKEN);
        assertThat(searchOption.getSorts().get(0).getOrder()).isEqualTo(Order.DESC);

    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_SearchException_on_search() throws Exception {
        //given
        final String orders = ApplicationPageItem.ATTRIBUTE_TOKEN + " DESC";
        given(applicationAPI.searchApplicationPages(any(SearchOptions.class))).willThrow(new SearchException(null));

        //when
        dataStore.search(0, 1, null, orders, Collections.emptyMap());

        //then exception

    }

}
