/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */

package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.GroupBuilder.aGroup;
import static org.bonitasoft.engine.test.persistence.builder.RoleBuilder.aRole;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;

import javax.inject.Inject;
import java.util.List;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;
import org.bonitasoft.engine.test.persistence.repository.UserMembershipRepository;
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
public class UserMembershipTest {

    @Inject
    private UserMembershipRepository repository;

    //Those tests currently verify that the queries returning UserMemberships correctly retrieve the groupParentPath when building the Usermembership objects

    @Test
    public void getUserMembershipsByGroup_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getUserMembershipsByGroup(group);
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void getUserMembershipsByRole_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getUserMembershipsByRole(role);
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void getUserMembershipsOfUser_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getUserMembershipsOfUser(user.getId());
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void getUserMembershipWithIds_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getUserMembershipWithIds(role.getId(), group.getId(), user.getId());
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void getUserMemberships_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getUserMemberships();
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void getSUserMembershipById_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.getSUserMembershipById(sUserMembership.getId());
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }

    @Test
    public void searchUserMembership_should_fill_in_groupParentPath() {

        SUser user = repository.add(aUser().withId(1L).withUserName("dummy username").build());
        SGroup group = repository.add(aGroup().forGroupId(258L).forGroupName("dummy groupName").forParentPath("bonita/devList").build());
        SRole role = repository.add(aRole().forRoleId(259L).forRoleName("dummy roleName").build());
        SUserMembershipImpl sUserMembership = aUserMembership().forUser(user).memberOf(group.getId(), role.getId()).build();
        repository.add(sUserMembership);
        List userMemberships = repository.searchUserMembership();
        assertThat(userMemberships).hasSize(1);
        SUserMembershipImpl userMembership = (SUserMembershipImpl) userMemberships.get(0);
        assertThat(userMembership.getGroupParentPath()).isEqualTo("bonita/devList");

    }
}
