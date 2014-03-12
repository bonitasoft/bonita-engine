/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 */
package org.bonitasoft.engine.api.impl;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoAPIImplTest {

    @Mock
    private IdentityService service;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SCustomUserInfoDefinitionBuilderFactory factory;

    private CustomUserInfoAPIImpl api;

    @Before
    public void setUp() throws Exception {
        api = new CustomUserInfoAPIImpl(service);
    }

    @Test
    public void createCustomUserInfoDefinition_should_return_definition_matching_server_definition_returned_by_the_identity_service() throws Exception {
        given(service.createCustomUserInfoDefinition(any(SCustomUserInfoDefinition.class)))
                .willReturn(createDummyServerDefinition("name", "display name", "description"));

        CustomUserInfoDefinition definition = api.createCustomUserInfoDefinition(factory, new CustomUserInfoDefinitionCreator());

        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDisplayName()).isEqualTo("display name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void getCustomUserDefinitions_should_return_the_list_of_CustomUserDefinition_fetch_from_the_identity_service() throws Exception {
        given(service.getCustomUserInfoDefinition(0, 3)).willReturn(
                Arrays.asList(
                        createDummyServerDefinition("first", "", ""),
                        createDummyServerDefinition("second", "", ""),
                        createDummyServerDefinition("last", "", "")));

        List<CustomUserInfoDefinition> definitions = api.getCustomUserInfoDefinitions(0, 3);

        assertThat(definitions.get(0).getName()).isEqualTo("first");
        assertThat(definitions.get(1).getName()).isEqualTo("second");
        assertThat(definitions.get(2).getName()).isEqualTo("last");
    }

    private SCustomUserInfoDefinition createDummyServerDefinition(final String name, final String displayName, final String description) {
        return new SCustomUserInfoDefinition() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public String getDiscriminator() {
                return null;
            }

            @Override
            public void setId(long id) {

            }

            @Override
            public void setTenantId(long id) {

            }
        };
    }
}
