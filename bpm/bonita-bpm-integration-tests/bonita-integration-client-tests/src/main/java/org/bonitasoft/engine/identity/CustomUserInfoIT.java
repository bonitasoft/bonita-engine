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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CustomUserInfoAPI;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class CustomUserInfoIT extends CommonAPITest {
    
    private static String DEFAULT_NAME = "Skills";

    private User user;

    @Before
    public void before() throws BonitaException {
        login();
        user = getIdentityAPI().createUser("john", "doe");
    }

    @After
    public void after() throws BonitaException {
        int pageSize = 20;
        List<CustomUserInfoDefinition> definitions;
        do {
            definitions = getIdentityAPI().getCustomUserInfoDefinitions(0, pageSize);
            deleteUserInfo(definitions);
        } while (definitions.size() == pageSize);
        getIdentityAPI().deleteUser(user.getId());
        logout();
    }
    
    @Cover(classes = { CustomUserInfoDefinition.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info definition", "Creation" })
    @Test
    public void createCustomUserInfoDefinition_should_return_the_new_created_object() throws Exception {
        //given
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator(DEFAULT_NAME, "The user skills.");

        //when
        CustomUserInfoDefinition info = getIdentityAPI().createCustomUserInfoDefinition(creator);

        //then
        assertThat(info.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(info.getDescription()).isEqualTo("The user skills.");
    }
    
    @Cover(classes = { CustomUserInfoDefinition.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info definition", "get list" })
    @Test
    public void getCustomUserInfoDefinitions_return_objects_according_to_pagination_size_and_ordered_by_name_asc() throws Exception {
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

    @Cover(classes = { CustomUserInfo.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info", "get list" })
    @Test
    public void getCustomUserInfo_should_return_all_info_even_when_value_is_null() throws Exception {
        //given
        CustomUserInfoDefinition job = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("job"));
        CustomUserInfoDefinition skill = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("skill"));
        getIdentityAPI().setCustomUserInfoValue(skill.getId(), user.getId(), "java");

        //when
        List<CustomUserInfo> infoPage1 = getIdentityAPI().getCustomUserInfo(user.getId(), 0, 1);
        List<CustomUserInfo> infoPage2 = getIdentityAPI().getCustomUserInfo(user.getId(), 1, 1);

        //then
        assertThat(infoPage1.get(0).getDefinition()).isEqualTo(job);
        assertThat(infoPage1.get(0).getValue()).isEqualTo(null);
        assertThat(infoPage2.get(0).getDefinition()).isEqualTo(skill);
        assertThat(infoPage2.get(0).getValue()).isEqualTo("java");
    }

    @Cover(classes = { CustomUserInfo.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info", "set value" })
    @Test
    public void setCustomUserInfoValue_should_delete_CustomUserInfoValue_when_set_to_null() throws Exception {
        //given
        CustomUserInfoDefinition job = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("job"));
        getIdentityAPI().setCustomUserInfoValue(job.getId(), user.getId(), "code slayer");
        CustomUserInfoDefinition skill = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("skill"));
        getIdentityAPI().setCustomUserInfoValue(skill.getId(), user.getId(), "java");

        //when
        getIdentityAPI().setCustomUserInfoValue(skill.getId(), user.getId(), null);

        //then
        List<CustomUserInfoValue> values = getIdentityAPI().searchCustomUserInfoValues(new SearchOptionsBuilder(0, 10).done()).getResult();
        assertThat(values.get(0).getDefinitionId()).isEqualTo(job.getId());
        assertThat(values.get(0).getUserId()).isEqualTo(user.getId());
        assertThat(values.get(0).getValue()).isEqualTo("code slayer");
    }

    @Cover(classes = { CustomUserInfo.class, CustomUserInfoAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7150", keywords = { "Custom user info", "set value" })
    @Test
    public void setCustomUserInfoValue_should_update_CustomUserInfoValue_when_one_already_exist() throws Exception {
        //given
        CustomUserInfoDefinition job = getIdentityAPI().createCustomUserInfoDefinition(new CustomUserInfoDefinitionCreator("job"));
        getIdentityAPI().setCustomUserInfoValue(job.getId(), user.getId(), "code slayer");

        //when
        getIdentityAPI().setCustomUserInfoValue(job.getId(), user.getId(), "or not");

        //then
        List<CustomUserInfoValue> values = getIdentityAPI().searchCustomUserInfoValues(new SearchOptionsBuilder(0, 10).done()).getResult();
        assertThat(values.get(0).getDefinitionId()).isEqualTo(job.getId());
        assertThat(values.get(0).getUserId()).isEqualTo(user.getId());
        assertThat(values.get(0).getValue()).isEqualTo("or not");
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
        CustomUserInfoDefinition info = createDefinition(DEFAULT_NAME);
        assertThat(getIdentityAPI().getCustomUserInfoDefinitions(0, 10)).containsExactly(info);

        //when
        getIdentityAPI().deleteCustomUserInfoDefinition(info.getId());
        List<CustomUserInfoDefinition> definitions = getIdentityAPI().getCustomUserInfoDefinitions(0, 10);

        //then
        assertThat(definitions).isEmpty();
    }

    @Ignore
    @Test
    public void deleteCustomUserInfoDefinition_should_delete_object_from_database_loading_scalability() throws Exception {
        // given
        List<User> users = createUsers(10);
        CustomUserInfoDefinition info = createDefinition("Skill");
        setValues(users, info, "code slayer");

        // when
        getIdentityAPI().deleteCustomUserInfoDefinition(info.getId());

        // then
        assertThat(getIdentityAPI().getCustomUserInfoDefinitions(0, 1)).isEmpty();
        assertThat(getIdentityAPI().searchCustomUserInfoValues(new SearchOptionsBuilder(0, 1).done()).getCount()).isEqualTo(0);

        // clean
        deleteUsers(users);
    }

    private void setValues(List<User> users, CustomUserInfoDefinition definition, String value) throws UpdateException {
        for (User user : users) {
            getIdentityAPI().setCustomUserInfoValue(definition.getId(), user.getId(), value);
        }
    }

    private CustomUserInfoDefinition createDefinition(String name) throws CreationException {
        CustomUserInfoDefinitionCreator creator = new CustomUserInfoDefinitionCreator(name);
        return getIdentityAPI().createCustomUserInfoDefinition(creator);
    }

    private List<User> createUsers(int number) throws BonitaException, InterruptedException {
        List<User> users = new ArrayList<User>(number);
        for (int i = 0; i < number; i++) {
            users.add(createUser("User " + i, "password"));
        }
        return users;
    }

}
