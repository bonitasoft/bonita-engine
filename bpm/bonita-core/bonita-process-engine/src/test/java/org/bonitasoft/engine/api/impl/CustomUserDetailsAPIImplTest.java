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

import org.bonitasoft.engine.identity.CustomUserInfoDefinition;
import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SProfileMetadataDefinition;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/**
 * @author Vincent Elcrin
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomUserDetailsAPIImplTest {

    @Mock
    private IdentityService service;

    @Mock
    private SProfileMetadataDefinitionBuilderFactory factory;

    @Mock
    private SProfileMetadataDefinitionBuilder builder;

    @Mock
    private SProfileMetadataDefinition sDefinition;

    private CustomUserInfoAPIImpl api;

    private CustomUserInfoDefinitionCreator creator;

    @Before
    public void setUp() throws Exception {
        given(factory.createNewInstance()).willReturn(builder);
        api = new CustomUserInfoAPIImpl(service, factory);
        creator = new CustomUserInfoDefinitionCreator();
    }

    @Test
    public void should_return_definition_matching_server_definition() throws Exception {
        given(sDefinition.getName()).willReturn("name");
        given(sDefinition.getDisplayName()).willReturn("display name");
        given(sDefinition.getDescription()).willReturn("description");
        given(service.createProfileMetadataDefinition(any(SProfileMetadataDefinition.class))).willReturn(sDefinition);

        CustomUserInfoDefinition definition = api.createCustomUserInfoDefinition(creator);

        assertThat(definition.getName()).isEqualTo(sDefinition.getName());
        assertThat(definition.getDisplayName()).isEqualTo(sDefinition.getDisplayName());
        assertThat(definition.getDescription()).isEqualTo(sDefinition.getDescription());
    }
}
