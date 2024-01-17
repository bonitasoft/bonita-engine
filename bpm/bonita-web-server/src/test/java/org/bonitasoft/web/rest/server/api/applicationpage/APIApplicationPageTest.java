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
package org.bonitasoft.web.rest.server.api.applicationpage;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStore;
import org.bonitasoft.web.rest.server.datastore.applicationpage.ApplicationPageDataStore;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastore;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class APIApplicationPageTest {

    static {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        I18n.getInstance();
    }

    @Mock
    private APIApplicationDataStoreFactory dataStoreFactory;

    @Mock
    private ApplicationPageDataStore applicationPageDataStore;

    @Mock
    private PageDatastore pageDataStore;

    @Mock
    private ApplicationDataStore applicationDataStore;

    @InjectMocks
    private APIApplicationPage apiApplicationPage;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private APIServletCall caller;

    @Mock
    private HttpSession httpSession;

    @Mock
    private APISession apiSession;

    @Before
    public void setUp() throws Exception {
        apiApplicationPage.setCaller(caller);
        given(caller.getHttpSession()).willReturn(httpSession);
        given(httpSession.getAttribute("apiSession")).willReturn(apiSession);
        given(dataStoreFactory.createApplicationPageDataStore(apiSession)).willReturn(applicationPageDataStore);
        given(dataStoreFactory.createApplicationDataStore(apiSession)).willReturn(applicationDataStore);
        given(dataStoreFactory.createPageDataStore(apiSession)).willReturn(pageDataStore);
    }

    @Test
    public void add_should_return_the_result_of_dataStore_add() throws Exception {
        //given
        final ApplicationPageItem itemToCreate = mock(ApplicationPageItem.class);
        final ApplicationPageItem createdItem = mock(ApplicationPageItem.class);
        given(applicationPageDataStore.add(itemToCreate)).willReturn(createdItem);

        //when
        final ApplicationPageItem retrievedItem = apiApplicationPage.add(itemToCreate);

        //then
        assertThat(retrievedItem).isEqualTo(createdItem);
    }

    @Test
    public void search_should_return_the_result_of_dataStore_search() throws Exception {
        //given
        @SuppressWarnings("unchecked")
        final ItemSearchResult<ApplicationPageItem> result = mock(ItemSearchResult.class);
        given(applicationPageDataStore.search(0, 10, "request", "default", Collections.singletonMap("token", "page")))
                .willReturn(result);

        //when
        final ItemSearchResult<ApplicationPageItem> retrievedResult = apiApplicationPage.search(0, 10, "request",
                "default",
                Collections.singletonMap("token", "page"));

        //then
        assertThat(retrievedResult).isEqualTo(result);
    }

    @Test
    public void defineDefaultSearchOrder_should_return_attribute_token() throws Exception {
        //when
        final String defaultSearchOrder = apiApplicationPage.defineDefaultSearchOrder();

        //then
        assertThat(defaultSearchOrder).isEqualTo("token");
    }

    @Test
    public void defineItemDefinition_return_an_instance_of_ApplicationDefinition() throws Exception {
        //when
        final ItemDefinition<ApplicationPageItem> itemDefinition = apiApplicationPage.defineItemDefinition();

        //then
        assertThat(itemDefinition).isExactlyInstanceOf(ApplicationPageDefinition.class);
    }

    @Test
    public void should_fill_application_deploy_when_requested() {
        final ApplicationPageItem applicationPage = new ApplicationPageItem();
        applicationPage.setApplicationId(1L);
        final ApplicationItem application = new ApplicationItem();
        application.setDisplayName("foo");
        given(applicationDataStore.get(APIID.makeAPIID(1L))).willReturn(application);

        apiApplicationPage.fillDeploys(applicationPage, singletonList(ApplicationPageItem.ATTRIBUTE_APPLICATION_ID));

        assertThat(applicationPage.getApplication()).isEqualTo(application);
    }

    @Test
    public void should_fill_page_deploy_when_requested() {
        final ApplicationPageItem applicationPage = new ApplicationPageItem();
        applicationPage.setPageId(2L);
        final PageItem customPage = new PageItem();
        customPage.setContentName("bar");
        given(pageDataStore.get(APIID.makeAPIID(2L))).willReturn(customPage);

        apiApplicationPage.fillDeploys(applicationPage, singletonList(ApplicationPageItem.ATTRIBUTE_PAGE_ID));

        assertThat(applicationPage.getPage()).isEqualTo(customPage);
    }

}
