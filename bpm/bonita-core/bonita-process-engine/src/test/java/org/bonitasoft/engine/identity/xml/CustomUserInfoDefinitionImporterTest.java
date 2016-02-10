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
package org.bonitasoft.engine.identity.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoDefinitionImporterTest {

    private static final String LOCATION_NAME = "Office location";

    private static final String SKILLS_DESCRIPTION = "The user skills";

    private static final String SKILLS_NAME = "Skills";

    @Mock
    private TenantServiceAccessor serviceAccessor;

    @Mock
    private IdentityService identityService;

    @Mock
    private ImportOrganizationStrategy strategy;

    private CustomUserInfoDefinitionImporter importer;

    private final CustomUserInfoDefinitionCreator skillsCreator = new CustomUserInfoDefinitionCreator(SKILLS_NAME, SKILLS_DESCRIPTION);

    private final CustomUserInfoDefinitionCreator locationCreator = new CustomUserInfoDefinitionCreator(LOCATION_NAME);

    @Before
    public void setUp() {
        doReturn(identityService).when(serviceAccessor).getIdentityService();
        importer = new CustomUserInfoDefinitionImporter(serviceAccessor, strategy);
    }

    @Test
    public void importCustomUserInfoDefinitions_call_service_to_create_elements_if_doesnt_exists() throws Exception {
        // when
        importer.importCustomUserInfoDefinitions(Arrays.asList(skillsCreator, locationCreator));

        // then
        ArgumentCaptor<SCustomUserInfoDefinition> userInfoCaptor = ArgumentCaptor.forClass(SCustomUserInfoDefinition.class);
        verify(identityService, times(2)).createCustomUserInfoDefinition(userInfoCaptor.capture());
        SCustomUserInfoDefinition skills = userInfoCaptor.getAllValues().get(0);
        SCustomUserInfoDefinition location = userInfoCaptor.getAllValues().get(1);

        assertThat(skills.getName()).isEqualTo(SKILLS_NAME);
        assertThat(skills.getDescription()).isEqualTo(SKILLS_DESCRIPTION);

        assertThat(location.getName()).isEqualTo(LOCATION_NAME);
        assertThat(location.getDescription()).isNull();
    }

    @Test
    public void importCustomUserInfoDefinitions_dont_call_service_to_create_elements_and_call_import_stragy_if_already_exists() throws Exception {
        // given
        SCustomUserInfoDefinition existingDefinition = mock(SCustomUserInfoDefinition.class);
        given(identityService.hasCustomUserInfoDefinition(SKILLS_NAME)).willReturn(true);
        given(identityService.getCustomUserInfoDefinitionByName(SKILLS_NAME)).willReturn(existingDefinition);

        // when
        importer.importCustomUserInfoDefinitions(Arrays.asList(skillsCreator));

        // then
        verify(identityService, never()).createCustomUserInfoDefinition(any(SCustomUserInfoDefinition.class));
        verify(strategy, times(1)).foundExistingCustomUserInfoDefinition(existingDefinition, skillsCreator);
    }

}
