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
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.SCustomUserInfoValueAPI;
import org.bonitasoft.engine.identity.ExportedCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomUserInfoValueImporterTest {

    private static final String SKILLS_NAME = "Skills";

    private static final String SKILLS_VALUE = "Java";

    private static final String LOCATION_NAME = "Location";

    private static final String LOCATION_VALUE = "Egineering";

    private static final long SKILLS_ID = 42L;

    private static final long LOCATION_ID = 43L;

    private static final long USER_ID = 50L;

    @Mock
    private SCustomUserInfoValueAPI infoAPI;

    @Mock
    private SCustomUserInfoDefinition skillsDef;

    @Mock
    private SCustomUserInfoDefinition locationDef;

    private CustomUserInfoValueImporter importer;

    @Before
    public void setUp() {
        given(skillsDef.getId()).willReturn(SKILLS_ID);
        given(locationDef.getId()).willReturn(LOCATION_ID);

        Map<String, SCustomUserInfoDefinition> infoDefinitions = new HashMap<String, SCustomUserInfoDefinition>(1);
        infoDefinitions.put(SKILLS_NAME, skillsDef);
        infoDefinitions.put(LOCATION_NAME, locationDef);

        importer = new CustomUserInfoValueImporter(infoAPI, infoDefinitions);
    }

    @Test
    public void importCustomUserInfoValues_should_call_setCustomUserInfoValue() throws Exception {
        // given
        List<ExportedCustomUserInfoValue> customUserInfoValues = new ArrayList<ExportedCustomUserInfoValue>(2);
        customUserInfoValues.add(new ExportedCustomUserInfoValue(SKILLS_NAME, SKILLS_VALUE));
        customUserInfoValues.add(new ExportedCustomUserInfoValue(LOCATION_NAME, LOCATION_VALUE));

        // when
        importer.imporCustomUserInfoValues(customUserInfoValues, USER_ID);

        // then
        verify(infoAPI, times(1)).set(SKILLS_ID, USER_ID, SKILLS_VALUE);
        verify(infoAPI, times(1)).set(LOCATION_ID, USER_ID, LOCATION_VALUE);
    }

    @Test
    public void importUsers_should_throw_OrganizationImportException_if_value_name_isnt_conform_to_definition_name() throws Exception {
        // given
        String notDefinedName = "no exist in definition";
        ExportedCustomUserInfoValue invalidValue = new ExportedCustomUserInfoValue(notDefinedName, "any value");

        try {
            // when
            importer.imporCustomUserInfoValues(Arrays.asList(invalidValue), USER_ID);
            fail("exception expected");
        } catch (SImportOrganizationException e) {
            // then
            String expectedMessage = "The XML file is inconsistent. A custom user info value is refenced with name '" + notDefinedName
                    + "', but no custom user info definition is defined with this name.";
            assertThat(e.getMessage()).isEqualTo(expectedMessage);
        }

    }

}
