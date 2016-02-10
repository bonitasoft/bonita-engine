/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.argThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoAPIDelegateTest {

    private CustomUserInfoAPIDelegate api;

    @Mock
    private IdentityService service;

    @Before
    public void setUp() {
        api = new CustomUserInfoAPIDelegate(service);
    }

    @Test
    public void list_should_retrieve_CustomUserItems_for_a_given_user() throws Exception {
        given(service.getCustomUserInfoDefinitions(0, 2)).willReturn(
                Arrays.<SCustomUserInfoDefinition> asList(
                        new DummySCustomUserInfoDefinition(1L),
                        new DummySCustomUserInfoDefinition(2L)));

        List<CustomUserInfo> result = api.list(1L, 0, 2);

        assertThat(result.get(0).getDefinition().getId()).isEqualTo(1L);
        assertThat(result.get(1).getDefinition().getId()).isEqualTo(2L);
    }

    @Test
    public void list_should_return_an_empty_when_there_is_no_definitions() throws Exception {
        given(service.getCustomUserInfoDefinitions(0, 2)).willReturn(Collections.<SCustomUserInfoDefinition> emptyList());

        List<CustomUserInfo> result = api.list(1L, 0, 2);

        assertThat(result).isEmpty();
    }

    @Test
    public void list_should_retrieve_values_associated_to_definitions_for_a_given_user() throws Exception {
        given(service.getCustomUserInfoDefinitions(0, 2)).willReturn(Arrays.<SCustomUserInfoDefinition> asList(
                new DummySCustomUserInfoDefinition(1L, "definition 1", ""),
                new DummySCustomUserInfoDefinition(2L, "definition 2", "")));
        given(service.searchCustomUserInfoValue(argThat(new QueryOptionsMatcher(1L, Arrays.asList(1L, 2L)))))
                .willReturn(Arrays.<SCustomUserInfoValue> asList(
                        new DummySCustomUserInfoValue(1L, 1L, 1L, "value 1"),
                        new DummySCustomUserInfoValue(2L, 2L, 1L, "value 2")));

        List<CustomUserInfo> result = api.list(1L, 0, 2);

        assertThat(result.get(0).getValue()).isEqualTo("value 1");
        assertThat(result.get(1).getValue()).isEqualTo("value 2");
    }

    @Test
    public void list_should_return_a_null_value_for_a_not_found_definition_matching_value() throws Exception {
        given(service.getCustomUserInfoDefinitions(0, 2)).willReturn(Arrays.<SCustomUserInfoDefinition> asList(
                new DummySCustomUserInfoDefinition(1L, "definition", "")));
        given(service.searchCustomUserInfoValue(argThat(new QueryOptionsMatcher(2L, Arrays.asList(1L)))))
                .willReturn(Collections.<SCustomUserInfoValue> emptyList());

        List<CustomUserInfo> result = api.list(2L, 0, 2);

        assertThat(result.get(0).getValue()).isEqualTo(null);
    }

    private class QueryOptionsMatcher extends ArgumentMatcher<QueryOptions> {

        private final long userId;

        private final List<Long> definitions;

        private QueryOptionsMatcher(long userId, List<Long> definitions) {
            this.userId = userId;
            this.definitions = definitions;
        }

        @Override
        public boolean matches(Object object) {
            QueryOptions options = (QueryOptions) object;
            return options.getFilters().get(0).getValue().equals(userId)
                    && options.getFilters().get(1).getIn().containsAll(definitions);
        }
    }
}
