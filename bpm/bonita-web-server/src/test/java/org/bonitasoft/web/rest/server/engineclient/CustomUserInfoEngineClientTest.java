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
package org.bonitasoft.web.rest.server.engineclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.CustomUserInfo;
import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.impl.CustomUserInfoValueImpl;
import org.bonitasoft.web.rest.server.api.organization.EngineCustomUserInfoDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoEngineClientTest {

    @Mock
    private IdentityAPI engine;

    @InjectMocks
    private CustomUserInfoEngineClient client;

    @Test
    public void should_create_a_given_definition() throws Exception {
        given(engine.createCustomUserInfoDefinition(any(CustomUserInfoDefinitionCreator.class)))
                .willReturn(new EngineCustomUserInfoDefinition(1L, "foo", "bar"));

        CustomUserInfoDefinition definition = client.createDefinition(new CustomUserInfoDefinitionCreator("foo"));

        assertThat(definition.getName()).isEqualTo("foo");
    }

    @Test(expected = APIException.class)
    public void should_fail_to_create_a_given_definition_when_engine_throw_an_exception() throws Exception {
        given(engine.createCustomUserInfoDefinition(any(CustomUserInfoDefinitionCreator.class)))
                .willThrow(new CreationException("failure"));

        client.createDefinition(new CustomUserInfoDefinitionCreator("foo"));
    }

    @Test
    public void should_delete_a_given_definition() throws Exception {

        client.deleteDefinition(1L);

        verify(engine).deleteCustomUserInfoDefinition(1L);
    }

    @Test(expected = APIException.class)
    public void should_fail_to_delete_a_given_definition_when_engine_throw_an_exception() throws Exception {
        willThrow(new DeletionException("failure")).given(engine)
                .deleteCustomUserInfoDefinition(1L);

        client.deleteDefinition(1L);
    }

    @Test
    public void should_list_definitions_for_a_given_range() throws Exception {
        given(engine.getCustomUserInfoDefinitions(0, 2)).willReturn(Arrays.<CustomUserInfoDefinition> asList(
                new EngineCustomUserInfoDefinition(1L),
                new EngineCustomUserInfoDefinition(2L)));

        List<CustomUserInfoDefinition> definitions = client.listDefinitions(0, 2);

        assertThat(definitions.get(0).getId()).isEqualTo(1L);
        assertThat(definitions.get(1).getId()).isEqualTo(2L);
    }

    @Test
    public void should_count_definitions() throws Exception {
        given(engine.getNumberOfCustomInfoDefinitions()).willReturn(5L);

        assertThat(client.countDefinitions()).isEqualTo(5);
    }

    @Test
    public void should_list_custom_information_for_a_given_user() {
        given(engine.getCustomUserInfo(1L, 0, 2)).willReturn(Arrays.asList(
                new CustomUserInfo(1L, new EngineCustomUserInfoDefinition(1L), new CustomUserInfoValueImpl()),
                new CustomUserInfo(1L, new EngineCustomUserInfoDefinition(2L), new CustomUserInfoValueImpl())));

        List<CustomUserInfo> information = client.listCustomInformation(1L, 0, 2);

        assertThat(information.get(0).getDefinition().getId()).isEqualTo(1L);
        assertThat(information.get(1).getDefinition().getId()).isEqualTo(2L);
    }

    @Test
    public void should_the_value_of_a_given_custom_user_info() throws UpdateException {
        CustomUserInfoValueImpl value = new CustomUserInfoValueImpl();
        value.setValue("foo");
        given(engine.setCustomUserInfoValue(1L, 2L, "foo")).willReturn(value);

        CustomUserInfoValue foo = client.setCustomUserInfoValue(1L, 2L, "foo");

        assertThat(foo.getValue()).isEqualTo("foo");
    }
}
