/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.xml.ExportedCustomUserInfoValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExportOrganizationTest {

    private static final long USER_ID = 15L;

    private static final long USER_INFO_DEF1_ID = 25L;

    private static final int MAX_RESULTS = 2;

    private static final String SKILSS_NAME = "Skills";

    private static final String SKILLS_VALUE = "Java";

    @Mock
    private IdentityService identityService;

    @Mock
    private SCustomUserInfoDefinition userInfoDef1;

    @Mock
    private SCustomUserInfoDefinition userInfoDef2;

    @Mock
    private SCustomUserInfoDefinition userInfoDef3;

    @Mock
    private SCustomUserInfoValue userInfoVal1;

    @Mock
    private SCustomUserInfoValue userInfoVal2;

    @Mock
    private SCustomUserInfoValue userInfoVal3;

    private ExportOrganization exportOrganization;

    @Before
    public void setUp() {
        exportOrganization = new ExportOrganization(identityService, MAX_RESULTS);
        given(userInfoVal1.getDefinitionId()).willReturn(USER_INFO_DEF1_ID);
        given(userInfoVal1.getValue()).willReturn(SKILLS_VALUE);
    }

    @Test
    public void getAllCustomUserInfoDefinitions_should_return_all_custom_user_info_definition_from_service() throws Exception {
        // given
        given(identityService.getCustomUserInfoDefinitions(0, MAX_RESULTS)).willReturn(Arrays.asList(userInfoDef1, userInfoDef2));
        given(identityService.getCustomUserInfoDefinitions(2, MAX_RESULTS)).willReturn(Arrays.asList(userInfoDef3));

        // when
        List<SCustomUserInfoDefinition> allCustomUserInfoDefinitions = exportOrganization.getAllCustomUserInfoDefinitions();

        // then
        assertThat(allCustomUserInfoDefinitions).isEqualTo(Arrays.asList(userInfoDef1, userInfoDef2, userInfoDef3));
    }

    @Test
    public void getAllCustomUserInfoForUser_should_return_elements_from_service() throws Exception {
        // given
        given(identityService.searchCustomUserInfoValue(any())).willReturn(
                Arrays.asList(userInfoVal1, userInfoVal2)).willReturn(Collections.singletonList(userInfoVal3));

        // when
        List<SCustomUserInfoValue> allCustomUserInfoValues = exportOrganization.getAllCustomUserInfoForUser(USER_ID);

        // then
        assertThat(allCustomUserInfoValues).isEqualTo(Arrays.asList(userInfoVal1, userInfoVal2, userInfoVal3));
    }

    @Test
    public void addCustomUserInfoValues_should_call_addCustomUserInfoValue_on_builder_for_all_elements() throws Exception {
        // given
        given(identityService.searchCustomUserInfoValue(any())).willReturn(
                Arrays.asList(userInfoVal1));
        ExportedUserBuilder clientUserbuilder = mock(ExportedUserBuilder.class);

        // when
        exportOrganization.addCustomUserInfoValues(USER_ID, clientUserbuilder, Collections.singletonMap(USER_INFO_DEF1_ID, SKILSS_NAME));

        // then
        verify(clientUserbuilder, times(1)).addCustomUserInfoValue(new ExportedCustomUserInfoValue(SKILSS_NAME, SKILLS_VALUE));
        verify(clientUserbuilder, times(1)).addCustomUserInfoValue(new ExportedCustomUserInfoValue(SKILSS_NAME, SKILLS_VALUE));
    }

}
