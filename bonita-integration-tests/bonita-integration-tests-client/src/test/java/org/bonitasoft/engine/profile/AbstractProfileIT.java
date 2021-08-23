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
package org.bonitasoft.engine.profile;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.junit.After;
import org.junit.Before;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractProfileIT extends TestWithTechnicalUser {

    protected Long adminProfileId;

    protected Long userProfileId;

    protected User user1;

    protected User user2;

    protected User user3;

    protected User user4;

    protected User user5;

    protected Group group1;

    protected Group group2;

    protected Group group3;

    protected Role role1;

    protected Role role2;

    protected Role role3;

    @Override
    @Before
    public void before() throws Exception {
        super.before();

        user1 = createUser("userName1", "User1Pwd", "User1FirstName", "User1LastName");
        user2 = createUser("userName2", "User2Pwd", "User2FirstName", "User2LastName");
        user3 = createUser("userName3", "User3Pwd", "User3FirstName", "User3LastName");
        user4 = createUser("userName4", "User4Pwd", "User4FirstName", "User4LastName");
        user5 = createUser("userName5", "User5Pwd", "User5FirstName", "User5LastName");

        group1 = createGroup("group1");
        group2 = createGroup("group2");
        group3 = createGroup("group3");
        role1 = createRole("role1");
        role2 = createRole("role2");
        role3 = createRole("role3");

        // search for the newly created profile IDs:
        adminProfileId = getProfileByName("Administrator").getId();
        userProfileId = getProfileByName("User").getId();

        getProfileAPI().createProfileMember(userProfileId, user1.getId(), null, null);
        getProfileAPI().createProfileMember(userProfileId, null, group1.getId(), null);
        getProfileAPI().createProfileMember(adminProfileId, user1.getId(), null, null);
        getProfileAPI().createProfileMember(adminProfileId, null, group1.getId(), role2.getId());
        getProfileAPI().createProfileMember(adminProfileId, null, group2.getId(), role2.getId());
        getProfileAPI().createProfileMember(adminProfileId, null, null, role1.getId());
        getProfileAPI().createProfileMember(adminProfileId, null, null, role2.getId());

    }

    private Profile getProfileByName(String name) throws SearchException {
        return getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 1).filter("name", name).done()).getResult()
                .get(0);
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUsers(user1, user2, user3, user4, user5);
        deleteGroups(group1, group2, group3);
        deleteRoles(role1, role2, role3);
        super.after();
    }

}
