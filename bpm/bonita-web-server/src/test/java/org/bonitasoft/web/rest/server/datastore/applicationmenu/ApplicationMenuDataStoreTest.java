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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.impl.ApplicationMenuImpl;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuDefinition;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuDataStoreTest extends APITestWithMock {

    @Mock
    private ApplicationAPI applicationAPI;

    @Mock
    private ApplicationMenuItemConverter converter;

    @Mock
    private ApplicationMenuUpdater applicationMenuUpdater;

    @InjectMocks
    private ApplicationMenuDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ItemDefinitionFactory() {

            @Override
            public ItemDefinition<?> defineItemDefinitions(final String token) {
                return new ApplicationMenuDefinition();
            }
        });

    }

    @Test
    public void should_return_result_of_engine_call_converted_to_item_on_add() throws Exception {
        //given
        final ApplicationMenuItem itemToCreate = new ApplicationMenuItem();
        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl("firstMenu", 11L, 14L, 1);
        given(applicationAPI.createApplicationMenu(any(ApplicationMenuCreator.class))).willReturn(applicationMenu);

        given(converter.toApplicationMenuItem(applicationMenu)).willReturn(new ApplicationMenuItem());
        //when
        final ApplicationMenuItem createdItem = dataStore.add(itemToCreate);

        //then
        assertThat(createdItem).isEqualTo(itemToCreate);

    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_CreationException_on_add() throws Exception {
        //given
        given(applicationAPI.createApplicationMenu(any(ApplicationMenuCreator.class)))
                .willThrow(new CreationException(""));

        //when
        dataStore.add(new ApplicationMenuItem());

        //then exception
    }

    @Test
    public void should_return_application_menu_updated_by_ApplicationAPI_and_converted_to_ApplicationItem_on_update()
            throws Exception {
        //given
        final HashMap<String, String> attributesToUpDate = new HashMap<>();
        given(converter.toApplicationMenuUpdater(any(Map.class))).willReturn(applicationMenuUpdater);

        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl("menu name", 1L, 1L, 2);
        given(applicationAPI.updateApplicationMenu(1, applicationMenuUpdater)).willReturn(applicationMenu);
        final ApplicationMenuItem item = new ApplicationMenuItem();
        given(converter.toApplicationMenuItem(applicationMenu)).willReturn(item);

        //when
        final ApplicationMenuItem createdItem = dataStore.update(APIID.makeAPIID(1L), attributesToUpDate);

        //then
        verify(converter, times(1)).toApplicationMenuUpdater(attributesToUpDate);
        verify(applicationAPI, times(1)).updateApplicationMenu(1, applicationMenuUpdater);
        verify(converter, times(1)).toApplicationMenuItem(applicationMenu);
        assertThat(createdItem).isEqualTo(new ApplicationMenuItem());
    }

    @Test
    public void should_return_the_ApplicationMenu_supplied_by_the_engine_converted_to_item_on_get() throws Exception {
        //given
        final ApplicationMenuItem itemToCreate = new ApplicationMenuItem();
        final ApplicationMenuImpl applicationMenu = new ApplicationMenuImpl("firstMenu", 11L, 14L, 1);
        given(applicationAPI.getApplicationMenu(1L)).willReturn(applicationMenu);
        given(converter.toApplicationMenuItem(applicationMenu)).willReturn(new ApplicationMenuItem());

        //when
        final ApplicationMenuItem createdItem = dataStore.get(APIID.makeAPIID(1L));

        //then
        assertThat(createdItem).isEqualTo(itemToCreate);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_the_engine_throw_NotFoundException_on_get() throws Exception {
        //given
        given(applicationAPI.getApplicationMenu(1)).willThrow(new ApplicationMenuNotFoundException(""));

        //when
        dataStore.get(APIID.makeAPIID("1"));

        //then exception
    }

    @Test
    public void should_delete_the_good_Application_Page_on_delete() throws Exception {
        //given

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1"), APIID.makeAPIID("2")));

        //then
        verify(applicationAPI, times(1)).deleteApplicationMenu(1);
        verify(applicationAPI, times(1)).deleteApplicationMenu(2);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_on_delete_when_engine_throws_exception() throws Exception {
        doThrow(new DeletionException("")).when(applicationAPI).deleteApplicationMenu(1);

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1")));

        //then exception
    }

    @Test
    public void should_return_a_valid_ItemSearchResult_on_search() throws Exception {
        //given
        final String orders = ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME + " DESC";
        final ApplicationMenuImpl appMenu = new ApplicationMenuImpl("MyMenu", 11L, 2L, 1);
        appMenu.setParentId(-1L);

        given(applicationAPI.searchApplicationMenus(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(2, Arrays.<ApplicationMenu> asList(appMenu)));

        //when
        final ItemSearchResult<ApplicationMenuItem> retrievedItems = dataStore.search(0, 1, null, orders,
                Collections.<String, String> emptyMap());

        //then
        assertThat(retrievedItems).isNotNull();
        assertThat(retrievedItems.getLength()).isEqualTo(1);
        assertThat(retrievedItems.getPage()).isEqualTo(0);
        assertThat(retrievedItems.getTotal()).isEqualTo(2);
        assertThat(retrievedItems.getResults().get(0).getDisplayName()).isEqualTo("MyMenu");

    }

    @Test
    public void should_call_engine_with_good_parameters_on_search() throws Exception {
        //given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "string to Match";
        final String orders = ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME + " DESC";
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID, "1");
        final ApplicationMenuImpl appPage = new ApplicationMenuImpl("MyMenu", 11L, 1L, 2);
        appPage.setParentId(-1L);

        given(applicationAPI.searchApplicationMenus(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(2, Arrays.<ApplicationMenu> asList(appPage)));

        //when
        dataStore.search(page, resultsByPage, search, orders, filters);

        //then
        final ArgumentCaptor<SearchOptions> captor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationAPI, times(1)).searchApplicationMenus(captor.capture());

        final SearchOptions searchOption = captor.getValue();
        assertThat(searchOption.getFilters()).hasSize(1);
        assertThat(searchOption.getFilters().get(0).getField())
                .isEqualTo(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID);
        assertThat(searchOption.getFilters().get(0).getValue()).isEqualTo("1");
        assertThat(searchOption.getSearchTerm()).isEqualTo(search);
        assertThat(searchOption.getMaxResults()).isEqualTo(1);
        assertThat(searchOption.getStartIndex()).isEqualTo(0);
        assertThat(searchOption.getSorts()).hasSize(1);
        assertThat(searchOption.getSorts().get(0).getField()).isEqualTo(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME);
        assertThat(searchOption.getSorts().get(0).getOrder()).isEqualTo(Order.DESC);

    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_engine_throws_SearchException_on_search() throws Exception {
        //given
        final String orders = ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME + " DESC";
        given(applicationAPI.searchApplicationMenus(any(SearchOptions.class))).willThrow(new SearchException(null));

        //when
        dataStore.search(0, 1, null, orders, Collections.<String, String> emptyMap());

        //then exception

    }

}
