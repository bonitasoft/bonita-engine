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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.identity.impl.CustomUserInfoValueImpl;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.impl.SearchResultImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.CustomUserInfoItem;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClient;
import org.bonitasoft.web.rest.server.engineclient.CustomUserInfoEngineClientCreator;
import org.bonitasoft.web.rest.server.framework.APIServletCall;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class APICustomUserInfoValueTest {

    static {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
    }

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

    @InjectMocks
    private APICustomUserInfoValue api;

    @Before
    public void setUp() throws Exception {
        api.setCaller(caller);
        given(caller.getHttpSession()).willReturn(httpSession);
        given(httpSession.getAttribute("apiSession")).willReturn(apiSession);
        given(engineClientCreator.create(apiSession)).willReturn(engine);
    }

    @Test
    public void should_retrieve_custom_user_info() {
        given(engine.searchCustomUserInfoValues(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(3, Arrays.asList(
                        createValue("foo"),
                        createValue("bar"))));

        ItemSearchResult<CustomUserInfoItem> result = api.search(0, 2, null, null, Collections.emptyMap());

        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getLength()).isEqualTo(2);
        assertThat(result.getResults().get(0).getValue()).isEqualTo("foo");
        assertThat(result.getResults().get(1).getValue()).isEqualTo("bar");
    }

    private CustomUserInfoValueImpl createValue(String value) {
        CustomUserInfoValueImpl information = new CustomUserInfoValueImpl();
        information.setValue(value);
        return information;
    }

    @Test
    public void should_retrieve_custom_user_info_sorted() {
        given(engine.searchCustomUserInfoValues(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(0, emptyList()));
        ArgumentCaptor<SearchOptions> argument = ArgumentCaptor.forClass(SearchOptions.class);

        api.search(0, 2, null, "userId ASC", Collections.emptyMap());

        verify(engine).searchCustomUserInfoValues(argument.capture());
        assertThat(argument.getValue().getSorts().get(0).getField()).isEqualTo("userId");
        assertThat(argument.getValue().getSorts().get(0).getOrder()).isEqualTo(Order.ASC);
    }

    @Test
    public void should_retrieve_custom_user_info_filtered() {
        given(engine.searchCustomUserInfoValues(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(0, emptyList()));
        ArgumentCaptor<SearchOptions> argument = ArgumentCaptor.forClass(SearchOptions.class);

        api.search(0, 2, null, null, Collections.singletonMap(CustomUserInfoItem.ATTRIBUTE_VALUE, "bar"));

        verify(engine).searchCustomUserInfoValues(argument.capture());
        assertThat(argument.getValue().getFilters().get(0).getField()).isEqualTo(CustomUserInfoItem.ATTRIBUTE_VALUE);
        assertThat(argument.getValue().getFilters().get(0).getValue()).isEqualTo("bar");
    }

    @Test
    public void should_retrieve_custom_user_info_term_filtered() {
        given(engine.searchCustomUserInfoValues(any(SearchOptions.class))).willReturn(
                new SearchResultImpl<>(0, emptyList()));
        ArgumentCaptor<SearchOptions> argument = ArgumentCaptor.forClass(SearchOptions.class);

        api.search(0, 2, "foo", null, Collections.emptyMap());

        verify(engine).searchCustomUserInfoValues(argument.capture());
        assertThat(argument.getValue().getSearchTerm()).isEqualTo("foo");
    }

    @Test
    public void should_update_a_given_custom_item_value() {
        CustomUserInfoValueImpl update = new CustomUserInfoValueImpl();
        update.setValue("foo");
        given(engine.setCustomUserInfoValue(1L, 2L, "foo")).willReturn(update);

        CustomUserInfoItem value = api.update(APIID.makeAPIID(2L, 1L), Collections.singletonMap("value", "foo"));

        assertThat(value.getValue()).isEqualTo("foo");
    }
}
