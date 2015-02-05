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
import org.junit.Before;
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

    private static final String DEVELOPER_NAME = "developer";

    private static final String SKILLS_NAME = "skills";

    @Inject
    private CustomUserInfoRepository repository;

    private SUser user1;

    private SUser user2;

    private SUser user3;

    private SUser user4;

    private SCustomUserInfoDefinition skills;

    private SCustomUserInfoDefinition developer;

    @Before
    public void setUp() {
        user1 = repository.add(aUser().withUserName("john").build());
        user2 = repository.add(aUser().withUserName("peter").build());
        user3 = repository.add(aUser().withUserName("paul").build());
        user4 = repository.add(aUser().withUserName("mike").build());

        skills = repository.add(aCustomUserInfoDefinition().withName(SKILLS_NAME).build());
        developer = repository.add(aCustomUserInfoDefinition().withName(DEVELOPER_NAME).build());
    }

    @Test
    public void query_getUserIdsWithCustomUserInfo_should_return_empty_list_if_no_users_has_the_chosen_custom_user_info() {
        // when
        final List<Long> userIds = repository.getUserIdsWithCustomUserInfo(SKILLS_NAME, "Java", false);

        // then
        assertThat(userIds).isEmpty();
    }

    @Test
    public void query_getUserIdsWithCustomUserInfo_should_return_only_ids_of_users_with_chosen_custom_user_info() {
        // given
        repository.add(aCustomUserInfoValue().withUserId(user1.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user2.getId()).withCustomUserInfoDefinitionId(developer.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user3.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("C").build());
        repository.add(aCustomUserInfoValue().withUserId(user4.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());

        // when
        final List<Long> userIds = repository.getUserIdsWithCustomUserInfo(SKILLS_NAME, "Java", false);

        // then
        assertThat(userIds).hasSize(2);
        assertThat(userIds).contains(user1.getId());
        assertThat(userIds).contains(user4.getId());
    }

    @Test
    public void query_getUserIdsWithCustomUserInfoContains_should_return_ids_of_users_with_chosen_custom_user_info_partial_match() {
        // given
        repository.add(aCustomUserInfoValue().withUserId(user1.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user2.getId()).withCustomUserInfoDefinitionId(developer.getId()).withValue("Java").build());
        repository.add(aCustomUserInfoValue().withUserId(user3.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("C").build());
        repository.add(aCustomUserInfoValue().withUserId(user4.getId()).withCustomUserInfoDefinitionId(skills.getId()).withValue("Java").build());

        // when
        final List<Long> userIdsWholeValue = repository.getUserIdsWithCustomUserInfo(SKILLS_NAME, "Java", true);
        final List<Long> userIdsPartialValue = repository.getUserIdsWithCustomUserInfo(SKILLS_NAME, "av", true);

        // then
        assertThat(userIdsWholeValue).hasSize(2);
        assertThat(userIdsWholeValue).contains(user1.getId());
        assertThat(userIdsWholeValue).contains(user4.getId());

        assertThat(userIdsPartialValue).hasSize(2);
        assertThat(userIdsPartialValue).contains(user1.getId());
        assertThat(userIdsPartialValue).contains(user4.getId());
    }
}
