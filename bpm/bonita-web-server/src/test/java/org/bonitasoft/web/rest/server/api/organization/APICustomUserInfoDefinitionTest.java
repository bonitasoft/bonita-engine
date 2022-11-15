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
package org.bonitasoft.web.rest.server.api.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoDefinitionItem;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClient;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClientCreator;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class APICustomUserInfoDefinitionTest {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private APIServletCall caller;

    @Mock
    private HttpSession httpSession;

    @Mock
    private APISession apiSession;

    @Mock
    private CustomUserInfoEngineClient engine;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private CustomUserInfoEngineClientCreator engineClientCreator;

    private APICustomUserInfoDefinition api;

    @Before
    public void setUp() throws Exception {
        given(caller.getHttpSession()).willReturn(httpSession);
        given(httpSession.getAttribute("apiSession")).willReturn(apiSession);
        given(engineClientCreator.create(apiSession)).willReturn(engine);
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        api = new APICustomUserInfoDefinition(engineClientCreator);
        api.setCaller(caller);
    }

    @Test
    public void add_should_return_the_added_item() throws Exception {
        given(engine.createDefinition(any(CustomUserInfoDefinitionCreator.class)))
                .willReturn(new EngineCustomUserInfoDefinition(2L));

        CustomUserInfoDefinitionItem added = api.add(new CustomUserInfoDefinitionItem());

        assertThat(added.getId()).isEqualTo(APIID.makeAPIID(2L));
    }

    @Test
    public void add_should_create_an_item_based_on_item_passed_in_parameter() throws Exception {
        given(engine.createDefinition(any(CustomUserInfoDefinitionCreator.class)))
                .willReturn(new EngineCustomUserInfoDefinition(1L));
        ArgumentCaptor<CustomUserInfoDefinitionCreator> argument = ArgumentCaptor
                .forClass(CustomUserInfoDefinitionCreator.class);
        CustomUserInfoDefinitionItem item = new CustomUserInfoDefinitionItem();
        item.setName("foo");
        item.setDescription("bar");

        api.add(item);
        verify(engine).createDefinition(argument.capture());

        assertThat(argument.getValue().getName()).isEqualTo("foo");
        assertThat(argument.getValue().getDescription()).isEqualTo("bar");
    }

    @Test
    public void delete_should_delete_all_items_with_id_passed_through() throws Exception {
        ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);

        api.delete(Arrays.asList(
                APIID.makeAPIID(1L),
                APIID.makeAPIID(2L)));
        verify(engine, times(2)).deleteDefinition(argument.capture());

        assertThat(argument.getAllValues()).containsOnly(1L, 2L);
    }

    @Test
    public void search_should_retrieve_the_list_of_item_from_the_specified_range() throws Exception {
        given(engine.listDefinitions(4, 2)).willReturn(Arrays.<CustomUserInfoDefinition> asList(
                new EngineCustomUserInfoDefinition(3L),
                new EngineCustomUserInfoDefinition(4L)));
        given(engine.countDefinitions()).willReturn(6L);

        List<CustomUserInfoDefinitionItem> result = api.search(2, 2,
                null,
                APICustomUserInfoDefinition.FIX_ORDER,
                null).getResults();

        assertThat(result.get(0).getId()).isEqualTo(APIID.makeAPIID(3L));
        assertThat(result.get(1).getId()).isEqualTo(APIID.makeAPIID(4L));
    }

    @Test
    public void search_should_retrieve_the_total_number_of_definitions() throws Exception {
        given(engine.countDefinitions()).willReturn(42L);

        ItemSearchResult<CustomUserInfoDefinitionItem> result = api.search(0, 2,
                null,
                APICustomUserInfoDefinition.FIX_ORDER,
                null);

        assertThat(result.getTotal()).isEqualTo(42);
    }

    @Test
    public void should_allow_an_empty_filter() throws Exception {
        given(engine.countDefinitions()).willReturn(42L);

        ItemSearchResult<CustomUserInfoDefinitionItem> result = api.search(0, 2,
                null,
                APICustomUserInfoDefinition.FIX_ORDER,
                Collections.<String, String> emptyMap());

        assertThat(result.getTotal()).isEqualTo(42);
    }

    @Test(expected = APIException.class)
    public void search_should_throw_an_exception_when_filters_are_passed_through() throws Exception {
        api.search(0, 1,
                null,
                APICustomUserInfoDefinition.FIX_ORDER,
                Collections.singletonMap("name", "foo"));
    }

    @Test(expected = APIException.class)
    public void search_should_throw_an_exception_when_an_order_is_passed_through() throws Exception {
        api.search(0, 1, null, "NAME ASC", null);
    }

    @Test(expected = APIException.class)
    public void search_should_throw_an_exception_when_a_search_is_passed_through() throws Exception {
        api.search(0, 1, "foo", APICustomUserInfoDefinition.FIX_ORDER, null);
    }
}
