/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder.DEFAULT_TENANT_ID;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;

import javax.inject.Inject;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserLogin;
import org.bonitasoft.engine.test.persistence.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Danila Mazour
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class UserTest {

    @Inject
    private UserRepository repository;

    //Those tests currently verify that the queries returning UserMemberships correctly retrieve the groupParentPath when building the Usermembership objects

    @Test
    public void should_retrieve_user_login() {
        SUser user = aUser().withId(124L).withUserName("walter.bates").build();
        repository.add(user);
        repository.flush();
        SUserLogin userLogin = SUserLogin.builder().id(124L).tenantId(DEFAULT_TENANT_ID).lastConnection(1234567L)
                .sUser(user).build();
        user.setSUserLogin(userLogin);
        repository.add(userLogin);
        repository.flush();

        assertThat(repository.getUserByName("walter.bates").getSUserLogin().getLastConnection()).isEqualTo(1234567L);

    }

}
