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
 **/
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CustomUserInfoAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoIT extends CommonAPITest {
    
    private static String DEFAULT_NAME = "Skills";
    private static String DEFAULT_DESCRIPTION = "The user skills.";
    private static String DEFAULT_DISPLAYNAME = "Skills display name";

    @Before
    public void before() throws BonitaException {
        login();
    }

    @After
    public void after() throws BonitaException {
        int pageSize = 20;
        List<CustomUserInfoDefinition> definitions = null;
        do {
            definitions = getIdentityAPI().getCustomUserInfoDefinitions(0, pageSize);
            deleteUserInfo(definitions);
        } while (definitions.size() == pageSize);
        logout();
    }
    
    @Cover(classes = { CustomUserInfoDefinition.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info definition", "Creation" })
    @Test
    public void createCustomUserInfoDefinition_should_return_the_new_created_object() throws Exception {
        //given
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator(DEFAULT_NAME, DEFAULT_DESCRIPTION);
        creator.setDisplayName(DEFAULT_DISPLAYNAME);

        //when
        CustomUserInfoDefinition info = getIdentityAPI().createCustomUserInfoDefinition(creator);

        //then
        assertThat(info.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(info.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(info.getDisplayName()).isEqualTo(DEFAULT_DISPLAYNAME);
    }
    
    @Cover(classes = { CustomUserInfoDefinition.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info definition", "get list" })
    @Test
    public void getCustomUserInfoDefinitions_return_objects_according_to_pagination_size_and_ordered_by_nam_asc() throws Exception {
        //given
        CustomUserInfoDefinition skills = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator(DEFAULT_NAME));
        CustomUserInfoDefinition a = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("A"));
        CustomUserInfoDefinition b = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("B"));
        int pagegSize = 2;
        
        //when
        List<CustomUserInfoDefinition> definitions = getIdentityAPI().getCustomUserInfoDefinitions(0, pagegSize);
        
        //then (first page)
        assertThat(definitions).containsExactly(a, b);
        
        //when
        definitions = getIdentityAPI().getCustomUserInfoDefinitions(2, pagegSize);
        
        //then (second page)
        assertThat(definitions).containsExactly(skills);
    }

    private void deleteUserInfo(List<CustomUserInfoDefinition> definitions) throws DeletionException {
        for (CustomUserInfoDefinition definition : definitions) {
            getIdentityAPI().deleteCustomUserInfoDefinition(definition.getId());
        }
    }
    
    
    @Cover(classes = { CustomUserInfoDefinition.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info definition", "Deletion" })
    @Test
    public void deleteCustomUserInfoDefinition_should_delete_object_from_database() throws Exception {
        //given
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator(DEFAULT_NAME);
        CustomUserInfoDefinition info = getIdentityAPI().createCustomUserInfoDefinition(creator);
        assertThat(getIdentityAPI().getCustomUserInfoDefinitions(0, 10)).containsExactly(info);

        //when
        getIdentityAPI().deleteCustomUserInfoDefinition(info.getId());
        List<CustomUserInfoDefinition> definitions = getIdentityAPI().getCustomUserInfoDefinitions(0, 10);

        //then
        assertThat(definitions).isEmpty();
    }

}
