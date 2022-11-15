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
package org.bonitasoft.web.rest.server.datastore.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.business.application.*;
import org.bonitasoft.engine.business.application.impl.ApplicationImpl;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.web.rest.model.application.ApplicationDefinition;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDataStoreTest extends APITestWithMock {

    @Mock
    private ApplicationAPI applicationAPI;

    @Mock
    private PageAPI pageAPI;

    @Mock
    private ApplicationItemConverter converter;

    @InjectMocks
    private ApplicationDataStore dataStore;

    @Mock
    private ApplicationPage applicationPage;

    @Mock
    private Page homePage;

    @Before
    public void setUp() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ItemDefinitionFactory() {

            @Override
            public ItemDefinition<?> defineItemDefinitions(final String token) {
                return new ApplicationDefinition();
            }
        });
    }

    @Test
    public void should_return_application_created_by_ApplicationAPI_converted_to_ApplicationItem_on_add()
            throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("app", "My application", "1.0");
        ApplicationItem app = new ApplicationItem();
        given(converter.toApplicationCreator(app)).willReturn(creator);
        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desc", 2L, 3L);
        given(applicationAPI.createApplication(creator)).willReturn(application);
        final ApplicationItem item = new ApplicationItem();
        given(converter.toApplicationItem(application)).willReturn(item);

        given(pageAPI.getPageByName("custompage_home")).willReturn(homePage);
        given(homePage.getId()).willReturn(1L);

        given(applicationAPI.createApplicationPage(application.getId(), 1, "home")).willReturn(applicationPage);

        //when
        final ApplicationItem createdItem = dataStore.add(new ApplicationItem());

        //then
        assertThat(createdItem).isEqualTo(item);
    }

    @Test
    public void should_create_default_home_page_on_add() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("app", "My application", "1.0");
        ApplicationItem item = new ApplicationItem();
        given(converter.toApplicationCreator(item)).willReturn(creator);
        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desc", 2L, 3L);
        application.setId(1);
        given(applicationAPI.createApplication(creator)).willReturn(application);
        final ApplicationItem convertedAppItem = new ApplicationItem();
        given(converter.toApplicationItem(application)).willReturn(convertedAppItem);
        given(pageAPI.getPageByName("custompage_home")).willReturn(homePage);
        given(homePage.getId()).willReturn(1L);
        given(applicationAPI.createApplicationPage(application.getId(), 1L, "home")).willReturn(applicationPage);
        given(applicationPage.getId()).willReturn(3L);

        //when
        dataStore.add(new ApplicationItem());

        //then
        verify(applicationAPI, times(1)).setApplicationHomePage(application.getId(), 3);
    }

    @Test
    public void should_return_application_updated_by_ApplicationAPI_converted_to_ApplicationItem_on_update()
            throws Exception {
        //given
        final HashMap<String, String> attributesToUpDate = new HashMap<>();
        attributesToUpDate.put(ApplicationItem.ATTRIBUTE_TOKEN, "app_name");
        attributesToUpDate.put(ApplicationItem.ATTRIBUTE_DISPLAY_NAME, "App display name");
        final ApplicationUpdater applicationUpdater = new ApplicationUpdater();
        given(converter.toApplicationUpdater(attributesToUpDate)).willReturn(applicationUpdater);

        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desc", 2L, 3L);
        given(applicationAPI.updateApplication(1, applicationUpdater)).willReturn(application);
        final ApplicationItem item = new ApplicationItem();
        given(converter.toApplicationItem(application)).willReturn(item);

        //when
        final ApplicationItem createdItem = dataStore.update(APIID.makeAPIID(1L), attributesToUpDate);

        //then
        verify(converter, times(1)).toApplicationUpdater(attributesToUpDate);
        verify(applicationAPI, times(1)).updateApplication(1, applicationUpdater);
        verify(converter, times(1)).toApplicationItem(application);
        assertThat(createdItem).isEqualTo(new ApplicationItem());
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_ApplicationAPI_throws_an_exception_on_add() throws Exception {
        ApplicationItem app = new ApplicationItem();
        final ApplicationCreator creator = new ApplicationCreator("app", "My application", "1.0");

        given(pageAPI.getPageByName("custompage_home")).willReturn(homePage);
        given(converter.toApplicationCreator(app)).willReturn(creator);

        //given
        when(applicationAPI.createApplication(any())).thenThrow(new CreationException(""));

        //when
        dataStore.add(app);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_when_ApplicationAPI_throws_an_exception_on_UpDate() throws Exception {
        //given
        when(applicationAPI.updateApplication(eq(1L), any())).thenThrow(new UpdateException(""));

        //when
        dataStore.update(APIID.makeAPIID(1L), new HashMap<>());
    }

    @Test
    public void should_return_the_good_application_on_get() throws Exception {
        //given
        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desc");
        final ApplicationItem item = new ApplicationItem();
        given(converter.toApplicationItem(application)).willReturn(item);
        application.setId(1);
        given(applicationAPI.getApplication(1)).willReturn(application);

        //when
        final ApplicationItem retrivedItem = dataStore.get(APIID.makeAPIID("1"));

        //then
        assertThat(retrivedItem).isEqualTo(item);
    }

    @Test(expected = APIException.class)
    public void should_return_throw_APIException_on_get_when_engine_throws_exception() throws Exception {
        //given
        given(applicationAPI.getApplication(1)).willThrow(new ApplicationNotFoundException(1));

        //when
        dataStore.get(APIID.makeAPIID("1"));

        //then exception
    }

    @Test
    public void should_delete_the_good_Application_on_delete() throws Exception {
        //given

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1"), APIID.makeAPIID("2")));

        //then
        verify(applicationAPI, times(1)).deleteApplication(1);
        verify(applicationAPI, times(1)).deleteApplication(2);
    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_on_delete_when_engine_throws_exception() throws Exception {
        doThrow(new DeletionException("")).when(applicationAPI).deleteApplication(1);

        //when
        dataStore.delete(Arrays.<APIID> asList(APIID.makeAPIID("1")));

        //then exception
    }

    @Test
    public void should_call_engine_with_good_parameters_on_search() throws Exception {
        //given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "string to Match";
        final String orders = ApplicationItem.ATTRIBUTE_TOKEN + " DESC";
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ApplicationItem.ATTRIBUTE_CREATED_BY, "1");
        filters.put(ApplicationItem.ATTRIBUTE_VERSION, "1.0");
        filters.put(ApplicationItem.FILTER_USER_ID, "4");
        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desccription");
        application.setId(1);
        application.setCreationDate(new Date());
        application.setLastUpdateDate(new Date());
        application.setVisibility(ApplicationVisibility.ALL);

        given(applicationAPI.searchApplications(any(SearchOptions.class)))
                .willReturn(new SearchResultImpl<>(2, Arrays.<Application> asList(application)));

        //when
        dataStore.search(page, resultsByPage, search, orders, filters);

        //then
        final ArgumentCaptor<SearchOptions> searchOptionCaptor = ArgumentCaptor.forClass(SearchOptions.class);
        verify(applicationAPI, times(1)).searchApplications(searchOptionCaptor.capture());

        final SearchOptions searchOption = searchOptionCaptor.getValue();
        assertThat(searchOption.getFilters().get(0).getField()).isEqualTo(ApplicationItem.ATTRIBUTE_CREATED_BY);
        assertThat(searchOption.getFilters().get(0).getValue()).isEqualTo("1");
        assertThat(searchOption.getFilters().get(1).getField()).isEqualTo(ApplicationItem.ATTRIBUTE_VERSION);
        assertThat(searchOption.getFilters().get(1).getValue()).isEqualTo("1.0");
        assertThat(searchOption.getFilters().get(2).getField()).isEqualTo(ApplicationItem.FILTER_USER_ID);
        assertThat(searchOption.getFilters().get(2).getValue()).isEqualTo("4");
        assertThat(searchOption.getSearchTerm()).isEqualTo(search);
        assertThat(searchOption.getMaxResults()).isEqualTo(1);
        assertThat(searchOption.getStartIndex()).isEqualTo(0);
        assertThat(searchOption.getSorts().get(0).getField()).isEqualTo(ApplicationItem.ATTRIBUTE_TOKEN);
        assertThat(searchOption.getSorts().get(0).getOrder()).isEqualTo(Order.DESC);
    }

    @Test
    public void should_return_a_valid_ItemSearchResult_on_search() throws Exception {
        //given
        final int page = 0;
        final int resultsByPage = 1;
        final String search = "string to Match";
        final String orders = ApplicationItem.ATTRIBUTE_TOKEN + " DESC";
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ApplicationItem.ATTRIBUTE_CREATED_BY, "1");
        filters.put(ApplicationItem.ATTRIBUTE_VERSION, "1.0");
        final ApplicationImpl application = new ApplicationImpl("app", "1.0", "app desccription");
        application.setId(1);
        application.setCreationDate(new Date());
        application.setLastUpdateDate(new Date());
        application.setVisibility(ApplicationVisibility.ALL);

        given(applicationAPI.searchApplications(any(SearchOptions.class)))
                .willReturn(new SearchResultImpl<>(2, Arrays.<Application> asList(application)));

        //when
        final ItemSearchResult<ApplicationItem> retrievedItems = dataStore.search(page, resultsByPage, search, orders,
                filters);

        //then
        assertThat(retrievedItems.getLength()).isEqualTo(1);
        assertThat(retrievedItems.getPage()).isEqualTo(0);
        assertThat(retrievedItems.getTotal()).isEqualTo(2);
        assertThat(retrievedItems.getResults().get(0).getToken()).isEqualTo("app");

    }

    @Test(expected = APIException.class)
    public void should_throw_APIException_on_search_when_engine_throws_exception() throws Exception {
        //given
        given(applicationAPI.searchApplications(any(SearchOptions.class)))
                .willThrow(new SearchException(new Exception()));

        //when
        final String orders = ApplicationItem.ATTRIBUTE_TOKEN + " DESC";
        dataStore.search(1, 2, "search", orders, Collections.<String, String> emptyMap());

        //then exception
    }

}
