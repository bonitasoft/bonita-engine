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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.CustomUserInfoDefinitionBuilder.aCustomUserInfoDefinition;
import static org.bonitasoft.engine.test.persistence.builder.CustomUserInfoValueBuilder.aCustomUserInfoValue;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.test.persistence.repository.CustomUserInfoRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class CustomUserInfoQueriesTest {

    @Inject
    private CustomUserInfoRepository repository;
    
    @Test
    public void query_should_return_empty_list_if_no_users_has_the_chosen_custom_user_info() throws Exception {
        // when
        List<Long> userIds = repository.getUserIdsWithCustomUserInfo("skills", "Java");
        
        //then
        assertThat(userIds).isEmpty();
    }

    @Test
    public void query_should_return_only_ids_of_users_with_chosen_custom_user_info() throws Exception {
        // given
        // users
        SUser user1 = repository.add(aUser().withUserName("john").build());
        SUser user2 = repository.add(aUser().withUserName("peter").build());
        SUser user3 = repository.add(aUser().withUserName("paul").build());
        SUser user4 = repository.add(aUser().withUserName("mike").build());

        // definitions
        SCustomUserInfoDefinition skills = repository.add(aCustomUserInfoDefinition().withName("skills").build());
        SCustomUserInfoDefinition developer = repository.add(aCustomUserInfoDefinition().withName("developer").build());

        // values
        repository.add(aCustomUserInfoValue().withUserId(user1.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user2.getId()).withCustomUserInfoDefinitionId(developer.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user3.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("C").build());
        repository.add(aCustomUserInfoValue().withUserId(user4.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());

        // when
        List<Long> userIds = repository.getUserIdsWithCustomUserInfo("skills", "Java");

        // then
        assertThat(userIds).hasSize(2);
        assertThat(userIds).contains(user1.getId());
        assertThat(userIds).contains(user4.getId());
    }
}
